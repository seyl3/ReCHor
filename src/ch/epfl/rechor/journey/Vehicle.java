package ch.epfl.rechor.journey;

import java.util.List;

/**
 * Enumération représentant les différents types de véhicules utilisés dans ReCHor.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public enum Vehicle {
    TRAM, METRO, TRAIN, BUS, FERRY, AERIAL_LIFT, FUNICULAR;

    /**
     * Liste contenant tous les types de véhicules.
     */
    public static final List<Vehicle> ALL = List.of(Vehicle.values());
}