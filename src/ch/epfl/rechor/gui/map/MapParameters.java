package ch.epfl.rechor.gui.map;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.geometry.Point2D;

public final class MapParameters {

    public static final int MIN_ZOOM = 6;
    public static final int MAX_ZOOM = 19;

    private final ReadOnlyIntegerWrapper zoom;
    private final ReadOnlyDoubleWrapper minX;
    private final ReadOnlyDoubleWrapper minY;

    public MapParameters(int zoom, double minX, double minY) {
        if (zoom < MIN_ZOOM || zoom > MAX_ZOOM)
            throw new IllegalArgumentException("Zoom hors limites : " + zoom);

        this.zoom = new ReadOnlyIntegerWrapper(zoom);
        this.minX = new ReadOnlyDoubleWrapper(minX);
        this.minY = new ReadOnlyDoubleWrapper(minY);
    }

    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom.getReadOnlyProperty();
    }

    public int zoom() {
        return zoom.get();
    }

    public ReadOnlyDoubleProperty minXProperty() {
        return minX.getReadOnlyProperty();
    }

    public double minX() {
        return minX.get();
    }

    public ReadOnlyDoubleProperty minYProperty() {
        return minY.getReadOnlyProperty();
    }

    public double minY() {
        return minY.get();
    }

    public void scroll(double dx, double dy) {
        this.minX.set(this.minX.get() + dx);
        this.minY.set(this.minY.get() + dy);
    }

    public void changeZoomLevel(int deltaZoom, Point2D mousePos, double paneWidth,
                                double paneHeight) {
        if (deltaZoom == 0) return;

        int oldZ = zoom.get();
        int newZ = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, oldZ + deltaZoom));
        if (newZ == oldZ) return;

        double scale = Math.pow(2, newZ - oldZ);

        double worldX = minX.get() + mousePos.getX();
        double worldY = minY.get() + mousePos.getY();

        double worldXNew = worldX * scale;
        double worldYNew = worldY * scale;

        minX.set(worldXNew - mousePos.getX());
        minY.set(worldYNew - mousePos.getY());
        zoom.set(newZ);
    }
}