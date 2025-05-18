package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Enumération représentant les différents types de véhicules utilisés dans ReCHor.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public enum Vehicle {
    TRAM("Tramway"),
    METRO("Métro"),
    TRAIN("Train"),
    BUS("Bus"),
    FERRY("Bateau"),
    AERIAL_LIFT("Téléphérique"),
    FUNICULAR("Funiculaire");

    private final String displayName;

    Vehicle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Liste contenant tous les types de véhicules.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());
}