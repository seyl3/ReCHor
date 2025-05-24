package ch.epfl.rechor.gui.map;

import ch.epfl.rechor.journey.Stop;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Contrôleur du fond de carte OSM : télécharge les tuiles, les dessine sur
 * un {@link Canvas} et gère les interactions (zoom + pan).
 *
 * @author Pessi
 */
public final class BaseMapController {

    // -------------------------------------------------------------------------
    // Champs principaux
    // -------------------------------------------------------------------------
    private final TileManager   tileManager;
    private final MapParameters mapParams;

    private final Canvas canvas = new Canvas();
    private final Pane   pane   = new Pane(canvas);

    // « redessin nécessaire ? » + throttle
    private boolean redrawNeeded = true;

    /** Accumulateur pour un zoom plus fluide (≈ 160 px de molette → 1 niveau). */
    private double wheelAccumulator = 0;

    private static final int TILE_SIZE = 256;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------
    /**
     * @param tm  gestionnaire de tuiles (cache + téléchargement)
     * @param mp  paramètres de la portion de carte visible
     */
    public BaseMapController(TileManager tm, MapParameters mp) {
        this.tileManager = tm;
        this.mapParams   = mp;

        installBindings();
        installEventHandlers();
        requestRedraw();
    }

    // -------------------------------------------------------------------------
    // Accès public
    // -------------------------------------------------------------------------
    /** @return le panneau JavaFX contenant la carte */
    public Pane pane() { return pane; }

    /** Centre la carte sur la position géographique donnée. */
    public void centerOn(Stop pos) {
        double x = WebMercator.lonToX(pos.longitude(), mapParams.zoom());
        double y = WebMercator.latToY(pos.latitude(), mapParams.zoom());
        mapParams.scroll(x - (pane.getWidth() / 2) - mapParams.minX(),
                         y - (pane.getHeight() / 2) - mapParams.minY());
        requestRedraw();
    }

    // -------------------------------------------------------------------------
    // Implémentation interne
    // -------------------------------------------------------------------------
    private void installBindings() {
        // le canevas suit la taille du panneau
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        // redessin lorsqu’on redimensionne le canevas ou change les paramètres
        canvas.widthProperty().addListener((p,o,n) -> requestRedraw());
        canvas.heightProperty().addListener((p,o,n) -> requestRedraw());
        mapParams.zoomProperty().addListener((p,o,n) -> requestRedraw());
        mapParams.minXProperty().addListener((p,o,n) -> requestRedraw());
        mapParams.minYProperty().addListener((p,o,n) -> requestRedraw());

        // branche le redraw « au prochain pulse JavaFX »
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            if (newS != null)
                newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    private void installEventHandlers() {
        // Pan (drag)
        pane.addEventHandler(MouseEvent.MOUSE_PRESSED,  e -> lastMouse = new Point2D(e.getX(), e.getY()));
        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED,  e -> {
            if (Double.isNaN(lastMouse.getX())) return;
            double dx = lastMouse.getX() - e.getX();
            double dy = lastMouse.getY() - e.getY();
            mapParams.scroll(dx, dy);           // déplace la carte
            lastMouse = new Point2D(e.getX(), e.getY());
        });
        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> lastMouse = new Point2D(Double.NaN, Double.NaN));

        // Zoom (scroll)
        pane.addEventHandler(ScrollEvent.SCROLL, e -> {
            wheelAccumulator += e.getDeltaY();

            final double STEP = 55;               // valeur empirique ~1 cran
            int zoomSteps = (int) (wheelAccumulator / STEP);
            if (zoomSteps == 0) return;

            wheelAccumulator -= zoomSteps * STEP;  // conserve le reliquat
            mapParams.changeZoomLevel(
                    zoomSteps,
                    new Point2D(e.getX(), e.getY()),
                    pane.getWidth(), pane.getHeight()
            );
        });
    }

    // ------------------- redessin différé -------------------
    private void requestRedraw() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        drawMap();
    }

    // ------------------- dessin proprement dit ---------------
    private void drawMap() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int z = mapParams.zoom();
        double minX = mapParams.minX();
        double minY = mapParams.minY();

        double paneW = canvas.getWidth();
        double paneH = canvas.getHeight();

        int firstTileX = (int) Math.floor(minX / TILE_SIZE);
        int firstTileY = (int) Math.floor(minY / TILE_SIZE);
        int lastTileX  = (int) Math.floor((minX + paneW) / TILE_SIZE);
        int lastTileY  = (int) Math.floor((minY + paneH) / TILE_SIZE);

        for (int ty = firstTileY; ty <= lastTileY; ty++) {
            for (int tx = firstTileX; tx <= lastTileX; tx++) {
                TileManager.TileId id = new TileManager.TileId(z, tx, ty);
                try {
                    Image img = tileManager.imageForTileAt(id);
                    double dx = tx * TILE_SIZE - minX;
                    double dy = ty * TILE_SIZE - minY;
                    gc.drawImage(img, dx, dy);
                } catch (IOException ignored) {
                    // si la tuile manque: on ne dessine rien
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Petits champs/méthodes utilitaires
    // -------------------------------------------------------------------------
    private Point2D lastMouse = new Point2D(Double.NaN, Double.NaN);
}