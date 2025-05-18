package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;

/**
 * Menu allowing users to exclude transport modes.
 */
public class VehicleFilterMenu extends MenuButton {
    private final ObservableSet<Vehicle> excluded = FXCollections.observableSet();

    public VehicleFilterMenu() {
        super("Modes de transport");
        for (Vehicle v : Vehicle.values()) {
            CheckMenuItem item = new CheckMenuItem(v.getDisplayName());
            item.setSelected(true);
            item.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    excluded.remove(v);
                } else {
                    excluded.add(v);
                }
            });
            getItems().add(item);
        }
    }

    /** @return the set of vehicles currently excluded */
    public ObservableSet<Vehicle> excludedVehicles() {
        return excluded;
    }
}
