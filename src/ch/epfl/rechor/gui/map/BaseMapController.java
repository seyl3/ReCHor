package ch.epfl.rechor.gui.map;

import ch.epfl.rechor.journey.Stop;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;

public final class BaseMapController {

    private static final int TILE_SIZE = 256;

    private final TileManager tileManager;
    private final MapParameters mapParams;
    private final Canvas canvas = new Canvas();
    private final Pane pane = new Pane(canvas);

    private boolean redrawNeeded = true;

    private double wheelAccumulator = 0;
    private final BooleanProperty online = new SimpleBooleanProperty(true);


    private Point2D lastMouse = new Point2D(Double.NaN, Double.NaN);


    public BaseMapController(TileManager tm, MapParameters mp) {
        this.tileManager = tm;
        this.mapParams = mp;

        installBindings();
        installEventHandlers();
        requestRedraw();
        Timeline onlineChecker = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> checkConnectivity())
        );
        onlineChecker.setCycleCount(Timeline.INDEFINITE);
        onlineChecker.play();
    }


    public Pane pane() {
        return pane;
    }

    public ReadOnlyBooleanProperty onlineProperty() {
        return online;
    }


    public void centerOn(Stop pos) {
        double x = WebMercator.lonToX(pos.longitude(), mapParams.zoom());
        double y = WebMercator.latToY(pos.latitude(), mapParams.zoom());
        mapParams.scroll(x - (pane.getWidth() / 2) - mapParams.minX(),
                y - (pane.getHeight() / 2) - mapParams.minY());
        requestRedraw();
    }


    private void installBindings() {

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());


        canvas.widthProperty().addListener((p, o, n) -> requestRedraw());
        canvas.heightProperty().addListener((p, o, n) -> requestRedraw());
        mapParams.zoomProperty().addListener((p, o, n) -> requestRedraw());
        mapParams.minXProperty().addListener((p, o, n) -> requestRedraw());
        mapParams.minYProperty().addListener((p, o, n) -> requestRedraw());


        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            if (newS != null)
                newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    private void installEventHandlers() {

        pane.addEventHandler(MouseEvent.MOUSE_PRESSED,
                e -> lastMouse = new Point2D(e.getX(), e.getY()));
        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (Double.isNaN(lastMouse.getX())) return;
            double dx = lastMouse.getX() - e.getX();
            double dy = lastMouse.getY() - e.getY();
            mapParams.scroll(dx, dy);
            lastMouse = new Point2D(e.getX(), e.getY());
        });
        pane.addEventHandler(MouseEvent.MOUSE_RELEASED,
                e -> lastMouse = new Point2D(Double.NaN, Double.NaN));


        pane.addEventHandler(ScrollEvent.SCROLL, e -> {
            wheelAccumulator += e.getDeltaY();

            final double STEP = 55;
            int zoomSteps = (int) (wheelAccumulator / STEP);
            if (zoomSteps == 0) return;

            wheelAccumulator -= zoomSteps * STEP;
            mapParams.changeZoomLevel(
                    zoomSteps,
                    new Point2D(e.getX(), e.getY()),
                    pane.getWidth(), pane.getHeight()
            );
        });
    }


    private void requestRedraw() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        drawMap();
    }


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
        int lastTileX = (int) Math.floor((minX + paneW) / TILE_SIZE);
        int lastTileY = (int) Math.floor((minY + paneH) / TILE_SIZE);

        for (int ty = firstTileY; ty <= lastTileY; ty++) {
            for (int tx = firstTileX; tx <= lastTileX; tx++) {
                TileManager.TileId id = new TileManager.TileId(z, tx, ty);
                try {
                    Image img = tileManager.imageForTileAt(id);
                    double dx = tx * TILE_SIZE - minX;
                    double dy = ty * TILE_SIZE - minY;
                    gc.drawImage(img, dx, dy);
                } catch (IOException e) {
                    online.set(false);
                }
            }
        }
    }

    private void checkConnectivity() {
        if (online.get()) return;
        int z = mapParams.zoom();
        int tx = (int) Math.floor(mapParams.minX() / TILE_SIZE);
        int ty = (int) Math.floor(mapParams.minY() / TILE_SIZE);
        TileManager.TileId id = new TileManager.TileId(z, tx, ty);
        try {
            tileManager.imageForTileAt(id);
            online.set(true);
            requestRedraw();
        } catch (IOException ignored) {
        }
    }
}