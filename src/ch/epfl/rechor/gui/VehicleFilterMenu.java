package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.MenuButton;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.CheckBox;

/**
 * Menu déroulant permettant d’exclure des modes de transport, hérite du bouton MenuButton.
 *
 * <p>Chaque case est cochée par défaut (aucun mode n’est exclu initialement).</p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public class VehicleFilterMenu extends MenuButton {
    /**
     * Ensemble observable des modes de transport actuellement exclus par l’utilisateur.
     */
    private final ObservableSet<Vehicle> excluded = FXCollections.observableSet();

    /**
     * Construit le menu de sélection des modes de transport.
     *
     * <p>Pour chaque type de véhicule, crée une case à cocher associée
     * et met à jour l’ensemble exclu lors de la sélection.</p>
     */
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
    /**
     * Renvoie l’ensemble des véhicules actuellement exclus.
     *
     * @return ObservableSet&lt;Vehicle&gt; des modes exclus
     */
    public ObservableSet<Vehicle> excludedVehicles() {
        return excluded;
    }
}
