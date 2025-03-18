package ch.epfl.rechor.timetable;

/**
 * Interface représentant un ensemble de liaisons indexées.
 * <p>
 * Les liaisons sont ordonnées par heure de départ décroissante.
 * Chaque liaison est associée à un arrêt de départ, un arrêt d'arrivée,
 * une heure de départ et une heure d'arrivée. De plus, chaque liaison
 * appartient à une course et a une position définie dans celle-ci.
 * </p>
 */
public interface Connections extends Indexed {
    /**
     * Retourne l'index de l'arrêt de départ de la liaison spécifiée.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt de départ
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depStopId(int id);

    /**
     * Retourne l'heure de départ de la liaison spécifiée,
     * exprimée en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure de départ en minutes après minuit
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int depMins(int id);

    /**
     * Retourne l'index de l'arrêt d'arrivée de la liaison spécifiée.
     *
     * @param id l'index de la liaison
     * @return l'index de l'arrêt d'arrivée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrStopId(int id);

    /**
     * Retourne l'heure d'arrivée de la liaison spécifiée,
     * exprimée en minutes après minuit.
     *
     * @param id l'index de la liaison
     * @return l'heure d'arrivée en minutes après minuit
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int arrMins(int id);

    /**
     * Retourne l'index de la course à laquelle appartient la liaison spécifiée.
     *
     * @param id l'index de la liaison
     * @return l'index de la course correspondante
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int tripId(int id);

    /**
     * Retourne la position de la liaison dans la course à laquelle elle appartient.
     * La première liaison d'une course a la position 0.
     *
     * @param id l'index de la liaison
     * @return la position de la liaison dans sa course
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int tripPos(int id);

    /**
     * Retourne l'index de la liaison suivante dans la même course.
     * Si la liaison spécifiée est la dernière de sa course, retourne
     * l'index de la première liaison de la course.
     *
     * @param id l'index de la liaison
     * @return l'index de la liaison suivante dans la course
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int nextConnectionId(int id);


}
