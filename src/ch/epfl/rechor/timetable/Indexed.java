package ch.epfl.rechor.timetable;


/**
 * Interface représentant une collection de données indexées.
 * <p>
 * Toute donnée conforme à cette interface est stockée sous forme de tableau et
 * identifiée par un index allant de 0 (inclus) à la taille du tableau (exclue).
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public interface Indexed {

    /**
     * Retourne la taille de la collection, c'est-à-dire le nombre d'éléments stockés.
     *
     * @return la taille de la collection, toujours positive ou nulle.
     */
    int size();
}