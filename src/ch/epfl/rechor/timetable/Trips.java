package ch.epfl.rechor.timetable;

/**
 * Interface représentant des courses de transport public indexées.
 * <p>
 * Chaque course est associée à une ligne de transport (identifiée par un index) et à une destination finale.
 * Les méthodes de cette interface permettent d'accéder à ces informations en fonction de l'index de la course.
 * </p>
 */
public interface Trips extends Indexed {
    /**
     * Retourne l'index de la ligne à laquelle la course d'index donné appartient.
     *
     * @param id L'index de la course.
     * @return L'index de la ligne associée à la course.
     * @throws IndexOutOfBoundsException Si l'index {@code id} est invalide.
     */
    int routeId(int id);

    /**
     * Retourne le nom de la destination finale de la course d'index donné.
     *
     * @param id L'index de la course.
     * @return Le nom de la destination finale de la course.
     * @throws IndexOutOfBoundsException Si l'index {@code id} est invalide.
     */
    String destination(int id);


}
