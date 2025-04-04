package ch.epfl.rechor.timetable;

/**
 * Interface représentant des gares indexées.
 * <p>
 * Chaque gare est identifiée par un index unique et possède un nom, ainsi que des informations
 * géographiques telles que la latitude et la longitude en degrés.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public interface Stations extends Indexed {
    /**
     * Retourne le nom de la gare correspondant à l'index donné.
     *
     * @param id L'index de la gare à récupérer.
     * @return Le nom de la gare correspondant à l'index spécifié.
     * @throws IndexOutOfBoundsException Si l'index donné est inférieur à 0 ou supérieur ou égal
     *                                   à size().
     */
    String name(int id);

    /**
     * Retourne la longitude de la gare correspondant à l'index donné.
     * La longitude est retournée en degrés.
     *
     * @param id L'index de la gare à récupérer.
     * @return La longitude de la gare en degrés.
     * @throws IndexOutOfBoundsException Si l'index donné est inférieur à 0 ou supérieur ou égal
     *                                   à size().
     */
    double longitude(int id);

    /**
     * Retourne la latitude de la gare correspondant à l'index donné.
     * La latitude est retournée en degrés.
     *
     * @param id L'index de la gare à récupérer.
     * @return La latitude de la gare en degrés.
     * @throws IndexOutOfBoundsException Si l'index donné est inférieur à 0 ou supérieur ou égal
     *                                   à size().
     */
    double latitude(int id);

}
