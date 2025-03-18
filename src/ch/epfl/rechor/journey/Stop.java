package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.util.Objects;

/**
 * Représente un arrêt dans un voyage.
 * <p>
 * Une instance de {@code Stop} doit respecter les contraintes suivantes :
 * <ul>
 *   <li>Le nom ne peut être {@code null}.</li>
 *   <li>La longitude doit être comprise entre -180 et 180.</li>
 *   <li>La latitude doit être comprise entre -90 et 90.</li>
 * </ul>
 */
public record Stop(String name, String platformName, double longitude, double latitude) {

    /**
     * Construit un arrêt avec les paramètres spécifiés.
     *
     * @throws NullPointerException     si {@code name} est {@code null}
     * @throws IllegalArgumentException si la longitude n'est pas dans [-180, 180]
     *                                  ou si la latitude n'est pas dans [-90, 90]
     */
    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(longitude >= -180 && longitude <= 180);
        Preconditions.checkArgument(latitude >= -90 && latitude <= 90);

    }
}
