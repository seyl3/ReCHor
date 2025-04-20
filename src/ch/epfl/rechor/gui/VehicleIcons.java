package ch.epfl.rechor.gui;


import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Gestionnaire d'icônes pour les différents types de véhicules.
 * <p>
 * Cette classe non instanciable gère le chargement et la mise en cache des icônes
 * des véhicules pour optimiser l'utilisation de la mémoire.
 *
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public final class VehicleIcons {

    private static final Map<Vehicle, Image> cache = new EnumMap<>(Vehicle.class);

    private VehicleIcons() {
    }

    /**
     * Retourne l'icône correspondant au type de véhicule donné.
     * <p>
     * L'icône est chargée lors de sa première utilisation puis mise en cache.
     * Les appels suivants pour le même type de véhicule retournent l'instance
     * mise en cache.
     *
     * @param vehicle le type de véhicule
     * @return l'image correspondant au véhicule
     */
    public static Image iconFor(Vehicle vehicle) {
        return cache.computeIfAbsent(vehicle,
                newVehicle -> new Image(newVehicle.toString() + ".png"));
    }
}
