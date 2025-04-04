package ch.epfl.rechor.timetable;

/**
 * Interface représentant un ensemble de quais ou de voies, indexés.
 * <p>
 * Chaque quai ou voie est identifié par un index unique et possède un nom ainsi
 * qu'un identifiant correspondant à la gare ou station à laquelle il appartient.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public interface Platforms extends Indexed {
    /**
     * Retourne le nom du quai ou de la voie correspondant à l'index donné.
     * <p>
     *
     * @param id l'index du quai ou de la voie
     * @return le nom du quai ou de la voie, ou une chaîne vide si non défini
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    String name(int id);

    /**
     * Retourne l'index de la gare à laquelle appartient le quai ou la voie donné.
     *
     * @param id l'index du quai ou de la voie
     * @return l'index de la gare associée
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    int stationId(int id);


}
