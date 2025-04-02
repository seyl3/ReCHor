package ch.epfl.rechor.timetable;

import java.time.LocalDate;

public final class CachedTimeTable implements TimeTable {

    private final TimeTable underlying;
    private LocalDate cachedDate;
    private Connections cachedConnections;
    private Trips cachedTrips;

    public CachedTimeTable(TimeTable underlying) {
        this.underlying = underlying;
    }

    @Override
    public Connections connectionsFor(LocalDate date) {
        if (!date.equals(cachedDate)) {
            cachedConnections = underlying.connectionsFor(date);
            cachedTrips = underlying.tripsFor(date);
            cachedDate = date;
        }
        return cachedConnections;
    }

    @Override
    public Trips tripsFor(LocalDate date) {
        if (!date.equals(cachedDate)) {
            cachedConnections = underlying.connectionsFor(date);
            cachedTrips = underlying.tripsFor(date);
            cachedDate = date;
        }
        return cachedTrips;
    }

    @Override public Stations stations() {
        return underlying.stations();
    }

    @Override public Platforms platforms() {
        return underlying.platforms();
    }

    @Override public Routes routes() {
        return underlying.routes();
    }

    @Override public Transfers transfers() {
        return underlying.transfers();
    }

    @Override public StationAliases stationAliases() {
        return underlying.stationAliases();
    }

    @Override public int stationId(int stopId) {
        return underlying.stationId(stopId);
    }

    @Override public String platformName(int stopId) {
        return underlying.platformName(stopId);
    }

    @Override public boolean isPlatformId(int stopId) {
        return underlying.isPlatformId(stopId);
    }
}
