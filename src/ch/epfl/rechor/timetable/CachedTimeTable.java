package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Version optimisée de TimeTable qui met en cache les données dépendantes de la date.
 * <p>
 * Cette classe enveloppe un autre TimeTable et garde en mémoire les connexions et 
 * trajets de la dernière date consultée. Cela évite de recharger ces données à 
 * chaque appel si la date n'a pas changé.
 * </p>
 * <p>
 * Les autres données (stations, quais, etc.) sont directement transmises au TimeTable
 * sous-jacent car elles ne dépendent pas de la date.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public final class CachedTimeTable implements TimeTable {

    private final TimeTable underlying;
    private LocalDate cachedDate;
    private Connections cachedConnections;
    private Trips cachedTrips;

    /**
     * Crée une nouvelle TimeTable avec cache.
     *
     * @param underlying la TimeTable à envelopper
     */
    public CachedTimeTable(TimeTable underlying) {
        this.underlying = underlying;
    }

    /**
     * Retourne les connexions pour une date donnée, en utilisant le cache si possible.
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        if (!date.equals(cachedDate)) {
            cachedConnections = underlying.connectionsFor(date);
            cachedTrips = underlying.tripsFor(date);
            cachedDate = date;
        }
        return cachedConnections;
    }

    /**
     * Retourne les trajets pour une date donnée, en utilisant le cache si possible.
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        if (!date.equals(cachedDate)) {
            cachedConnections = underlying.connectionsFor(date);
            cachedTrips = underlying.tripsFor(date);
            cachedDate = date;
        }
        return cachedTrips;
    }

    @Override
    public Stations stations() {
        return underlying.stations();
    }

    @Override
    public Platforms platforms() {
        return underlying.platforms();
    }

    @Override
    public Routes routes() {
        return underlying.routes();
    }

    @Override
    public Transfers transfers() {
        return underlying.transfers();
    }

    @Override
    public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    @Override
    public int stationId(int stopId) {
        return underlying.stationId(stopId);
    }

    @Override
    public String platformName(int stopId) {
        return underlying.platformName(stopId);
    }

    @Override
    public boolean isPlatformId(int stopId) {
        return underlying.isPlatformId(stopId);
    }
}
