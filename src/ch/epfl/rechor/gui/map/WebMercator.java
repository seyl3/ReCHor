package ch.epfl.rechor.gui.map;

public final class WebMercator {
    private static final double TILE_SIZE = 256.0;

    private WebMercator() {
    }

    public static double lonToX(double lonDeg, int z) {
        double x = (lonDeg + 180.0) / 360.0;
        return x * (TILE_SIZE * (1 << z));
    }

    public static double latToY(double latDeg, int z) {
        double latRad = Math.toRadians(latDeg);
        double y = (1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2;
        return y * (TILE_SIZE * (1 << z));
    }
}