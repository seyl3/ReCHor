package ch.epfl.rechor.gui.map;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;

public final class JourneyLayer {

    private final Canvas canvas = new Canvas();

    public Canvas canvas() {
        return canvas;
    }

    public void draw(Journey j, MapParameters mp) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int z = mp.zoom();
        double offsetX = mp.minX();
        double offsetY = mp.minY();

        gc.setLineWidth(3);
        gc.setStroke(Color.DARKBLUE);

        for (Journey.Leg leg : j.legs()) {
            if (leg instanceof Journey.Leg.Transport t) {
                List<Stop> stops = new java.util.ArrayList<>();
                stops.add(t.depStop());
                t.intermediateStops().forEach(s -> stops.add(s.stop()));
                stops.add(t.arrStop());

                double[] xs = new double[stops.size()];
                double[] ys = new double[stops.size()];
                for (int i = 0; i < stops.size(); i++) {
                    xs[i] = WebMercator.lonToX(stops.get(i).longitude(), z) - offsetX;
                    ys[i] = WebMercator.latToY(stops.get(i).latitude(), z) - offsetY;
                }
                gc.strokePolyline(xs, ys, xs.length);
            }
        }
        Stop first = j.legs().getFirst().depStop();
        Stop last = j.legs().getLast().arrStop();
        double sx = WebMercator.lonToX(first.longitude(), z) - offsetX;
        double sy = WebMercator.latToY(first.latitude(), z) - offsetY;
        double ex = WebMercator.lonToX(last.longitude(), z) - offsetX;
        double ey = WebMercator.latToY(last.latitude(), z) - offsetY;

        gc.setFill(Color.GREEN);
        gc.fillOval(sx - 4, sy - 4, 8, 8);
        gc.setFill(Color.RED);
        gc.fillOval(ex - 4, ey - 4, 8, 8);
    }

    public void bindTo(Pane mapPane) {
        canvas.widthProperty().bind(mapPane.widthProperty());
        canvas.heightProperty().bind(mapPane.heightProperty());
    }
}