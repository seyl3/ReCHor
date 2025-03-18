package ch.epfl.rechor.timetable;


import java.util.NoSuchElementException;

/**
 * Interface représentant des changements indexés dans un horaire de transport public.
 * <p>
 * Chaque changement est associé à une gare de départ, une durée et une gare d'arrivée.
 * Les changements ne sont possibles qu'entre des gares et non entre des voies ou quais.
 * </p>
 *
 * @see Indexed
 */
public interface Transfers extends Indexed {
    /**
     * Retourne l'index de la gare de départ du changement d'index donné.
     *
     * @param id L'index du changement.
     * @return L'index de la gare de départ pour ce changement.
     * @throws IndexOutOfBoundsException Si l'index {@code id} est invalide.
     */
    int depStationId(int id);

    /**
     * Retourne la durée du changement d'index donné, en minutes.
     *
     * @param id L'index du changement.
     * @return La durée du changement en minutes.
     * @throws IndexOutOfBoundsException Si l'index {@code id} est invalide.
     */
    int minutes(int id);

    /**
     * Retourne l'intervalle empaqueté des index des changements dont la gare d'arrivée
     * est celle d'index donné. L'intervalle est exprimé selon la convention utilisée
     * par PackedRange.
     *
     * @param stationId L'index de la gare d'arrivée.
     * @return L'intervalle empaqueté des index des changements pour cette gare d'arrivée.
     * @throws IndexOutOfBoundsException Si l'index {@code stationId} est invalide.
     */
    int arrivingAt(int stationId);

    /**
     * Retourne la durée, en minutes, du changement entre les deux gares d'index donnés.
     * Si aucun changement n'est possible entre ces deux gares, une {@link NoSuchElementException}
     * est levée.
     *
     * @param depStationId L'index de la gare de départ.
     * @param arrStationId L'index de la gare d'arrivée.
     * @return La durée du changement entre les deux gares en minutes.
     * @throws NoSuchElementException    Si aucun changement n'est possible entre ces deux gares.
     * @throws IndexOutOfBoundsException Si l'un des index est invalide.
     */
    int minutesBetween(int depStationId, int arrStationId);
}
