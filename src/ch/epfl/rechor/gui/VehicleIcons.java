package ch.epfl.rechor.gui;


/*
 * @author : Sarra Zghal, Elyes Ben Abid
 */

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

public final class VehicleIcons {

    private static Map<Vehicle, Image> cache = new EnumMap<>(Vehicle.class);

    private VehicleIcons() {}

    public static Image iconFor(Vehicle vehicle) {
        return cache.computeIfAbsent(vehicle, newVehicle -> new Image(newVehicle.toString() + ".png"));
    }
}
