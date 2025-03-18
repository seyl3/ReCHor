package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Interface représentant un horaire de transport public.
 * <p>
 * Un horaire contient des informations sur les gares, les noms alternatifs des gares, les voies/quais,
 * les lignes de transport, les changements, les courses et les liaisons, toutes indexées.
 * </p>
 */
public interface TimeTable {
    /**
     * Retourne les gares indexées de l'horaire.
     *
     * @return Les gares indexées.
     */
    Stations stations();

    /**
     * Retourne les noms alternatifs indexés des gares de l'horaire.
     *
     * @return Les noms alternatifs des gares.
     */
    StationAliases stationAliases();

    /**
     * Retourne les voies/quais indexées de l'horaire.
     *
     * @return Les voies/quais indexées.
     */
    Platforms platforms();

    /**
     * Retourne les lignes indexées de l'horaire.
     *
     * @return Les lignes indexées.
     */
    Routes routes();

    /**
     * Retourne les changements indexés de l'horaire.
     *
     * @return Les changements indexés.
     */
    Transfers transfers();

    /**
     * Retourne les courses indexées de l'horaire actives le jour donné.
     *
     * @param date La date pour laquelle récupérer les courses.
     * @return Les courses actives à la date spécifiée.
     */
    Trips tripsFor(LocalDate date);

    /**
     * Retourne les liaisons indexées de l'horaire actives le jour donné.
     *
     * @param date La date pour laquelle récupérer les liaisons.
     * @return Les liaisons actives à la date spécifiée.
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Détermine si l'index d'arrêt donné correspond à une gare.
     *
     * @param stopId L'index d'arrêt à vérifier.
     * @return {@code true} si l'index correspond à une gare, {@code false} sinon.
     */
    default boolean isStationId(int stopId) {
        return (stopId < stations().size());
    }

    /**
     * Détermine si l'index d'arrêt donné correspond à une voie ou un quai.
     *
     * @param platformId L'index d'arrêt à vérifier.
     * @return {@code true} si l'index correspond à une voie/quai, {@code false} sinon.
     */
    default boolean isPlatformId(int platformId) {
        return (platformId - stations().size() >= 0 && (platformId - stations().size()) < platforms().size());
    }

    /**
     * Retourne l'index de la gare de l'arrêt d'index donné.
     * Si l'arrêt est déjà une gare, l'index retourné peut être le même.
     *
     * @param stopId L'index d'arrêt à vérifier.
     * @return L'index de la gare associée à l'arrêt.
     * @throws IllegalArgumentException Si l'index d'arrêt ne correspond ni à une gare ni à une voie/quai.
     */
    default int stationId(int stopId) {
        if (isPlatformId(stopId)) {
            return platforms().stationId(stopId - stations().size());
        }
        return stopId;


    }

    /**
     * Retourne le nom de la voie ou du quai de l'arrêt d'index donné.
     * Si l'arrêt est une gare, {@code null} est retourné.
     *
     * @param stopId L'index d'arrêt à vérifier.
     * @return Le nom de la voie/quai ou {@code null} si l'arrêt est une gare.
     * @throws IllegalArgumentException Si l'index d'arrêt ne correspond ni à une gare ni à une voie/quai.
     */
    default String platformName(int stopId) {
        if (isPlatformId(stopId)) {
            return platforms().name(stopId - stations().size());
        } else {
            return null;
        }

    }
}
