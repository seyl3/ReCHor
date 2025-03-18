package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * Interface représentant des lignes de transport public indexées.
 * <p>
 * Chaque ligne de transport est identifiée par un index unique et possède un type de véhicule
 * ainsi qu'un nom.
 */
public interface Routes extends Indexed {

    /**
     * Retourne le type de véhicule desservant la ligne correspondant à l'index donné.
     *
     * @param id l'index de la ligne de transport
     * @return le type de véhicule de la ligne
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    Vehicle vehicle(int id);

    /**
     * Retourne le nom de la ligne de transport correspondant à l'index donné.
     *
     * @param id l'index de la ligne de transport
     * @return le nom de la ligne
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name(int id);


}
