package ch.epfl.rechor.gui.map;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.geometry.Point2D;

/**
 * Paramètres de la portion de carte actuellement affichée :
 * <ul>
 *   <li>niveau de zoom (6 .. 19) ;</li>
 *   <li>coordonnée {@code minX} du coin haut-gauche dans le système WebMercator ;</li>
 *   <li>coordonnée {@code minY} du coin haut-gauche dans le système WebMercator.</li>
 * </ul>
 *
 * @author Pessi
 */
public final class MapParameters {

    // -------------------------------------------------------------------------
    // Constantes
    // -------------------------------------------------------------------------
    public static final int MIN_ZOOM = 6;
    public static final int MAX_ZOOM = 19;

    // -------------------------------------------------------------------------
    // Propriétés (read-only)
    // -------------------------------------------------------------------------
    private final ReadOnlyIntegerWrapper zoom;
    private final ReadOnlyDoubleWrapper  minX;
    private final ReadOnlyDoubleWrapper  minY;

    // -------------------------------------------------------------------------
    // Constructeur
    // -------------------------------------------------------------------------
    /**
     * @param zoom niveau de zoom initial (doit être entre {@link #MIN_ZOOM}
     *             et {@link #MAX_ZOOM} inclus)
     * @param minX coordonnée x du coin haut-gauche
     * @param minY coordonnée y du coin haut-gauche
     * @throws IllegalArgumentException si {@code zoom} hors limites
     */
    public MapParameters(int zoom, double minX, double minY) {
        if (zoom < MIN_ZOOM || zoom > MAX_ZOOM)
            throw new IllegalArgumentException("Zoom hors limites : " + zoom);

        this.zoom = new ReadOnlyIntegerWrapper(zoom);
        this.minX = new ReadOnlyDoubleWrapper(minX);
        this.minY = new ReadOnlyDoubleWrapper(minY);
    }

    // -------------------------------------------------------------------------
    // Accès aux propriétés
    // -------------------------------------------------------------------------
    public ReadOnlyIntegerProperty zoomProperty() { return zoom.getReadOnlyProperty(); }
    public int zoom()                             { return zoom.get(); }

    public ReadOnlyDoubleProperty minXProperty()  { return minX.getReadOnlyProperty(); }
    public double minX()                          { return minX.get(); }

    public ReadOnlyDoubleProperty minYProperty()  { return minY.getReadOnlyProperty(); }
    public double minY()                          { return minY.get(); }

    // -------------------------------------------------------------------------
    // Méthodes de modification
    // -------------------------------------------------------------------------
    /** Translate le coin haut-gauche de la carte du vecteur (dx, dy). */
    public void scroll(double dx, double dy) {
        this.minX.set(this.minX.get() + dx);
        this.minY.set(this.minY.get() + dy);
    }

    /**
     * Change le niveau de zoom de {@code deltaZoom} (par ex. +1 ou -1) en conservant
     * le point {@code mousePos} (coordonnées écran) immobile à l’écran.
     */
    public void changeZoomLevel(int deltaZoom, Point2D mousePos, double paneWidth, double paneHeight) {
        if (deltaZoom == 0) return;

        int oldZ = zoom.get();
        int newZ = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, oldZ + deltaZoom));
        if (newZ == oldZ) return; // aucune modification possible

        double scale = Math.pow(2, newZ - oldZ);

        // Coordonnées monde du point sous la souris avant zoom
        double worldX = minX.get() + mousePos.getX();
        double worldY = minY.get() + mousePos.getY();

        // Coordonnées monde après changement d'échelle
        double worldXNew = worldX * scale;
        double worldYNew = worldY * scale;

        // Nouveau coin haut‑gauche afin de laisser le pointeur immobile
        minX.set(worldXNew - mousePos.getX());
        minY.set(worldYNew - mousePos.getY());
        zoom.set(newZ);
    }
}