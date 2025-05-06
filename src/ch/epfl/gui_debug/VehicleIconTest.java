package ch.epfl.gui_debug;

import ch.epfl.rechor.journey.Vehicle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

import static ch.epfl.rechor.gui.VehicleIcons.iconFor;

public class VehicleIconTest extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        HBox root = new HBox(10);
        List<Vehicle> vehicles = Vehicle.ALL;

        for (Vehicle vehicle : vehicles) {
            Image img = iconFor(vehicle);
            ImageView imgView = new ImageView(img);
            imgView.setFitHeight(64);
            imgView.setFitWidth(64);
            root.getChildren().add(imgView);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Vehicle Icons Viewer");
        stage.show();
    }
}