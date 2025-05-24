package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;

public class VehicleFilterMenu extends MenuButton {
    private final ObservableSet<Vehicle> excluded = FXCollections.observableSet();

    public VehicleFilterMenu() {
        super("Modes de transport");
        for (Vehicle v : Vehicle.values()) {
            CheckBox checkBox = new CheckBox(v.getDisplayName());
            checkBox.setSelected(true);
            checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    excluded.remove(v);
                } else {
                    excluded.add(v);
                }
            });
            CustomMenuItem item = new CustomMenuItem(checkBox, false);
            getItems().add(item);
        }
    }

    public ObservableSet<Vehicle> excludedVehicles() {
        return excluded;
    }
}
