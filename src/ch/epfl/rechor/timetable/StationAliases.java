package ch.epfl.rechor.timetable;

/**
 * Gère les noms alternatifs des gares.
 * <p>
 * Certaines gares peuvent être connues sous différents noms. Par exemple,
 * "Genève-Aéroport" peut aussi être appelée "Geneva Airport" ou "GVA".
 * Cette interface permet d'accéder à ces alias et de retrouver le nom
 * officiel de la gare correspondante.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public interface StationAliases extends Indexed {

    /**
     * Retourne le nom alternatif à l'index donné.
     *
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String alias(int id);

    /**
     * Retourne le nom officiel de la gare correspondant à l'alias.
     *
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String stationName(int id);
}
