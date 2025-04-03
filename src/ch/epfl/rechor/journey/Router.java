package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;
import ch.epfl.rechor.timetable.Trips;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public record Router(TimeTable tt) {

    public Profile profile(LocalDate date, int destinationStationId) {
        Stations stations = tt.stations();
        Connections connections = tt.connectionsFor(date);
        Trips trips = tt.tripsFor(date);
        Transfers transfers = tt.transfers();

        int stationCount = stations.size();
        int tripCount = trips.size();

        ParetoFront.Builder[] stationFronts = new ParetoFront.Builder[stationCount];
        for (int i = 0; i < stationCount; i++) {
            stationFronts[i] = new ParetoFront.Builder();
        }

        ParetoFront.Builder[] tripFronts = new ParetoFront.Builder[tripCount];
        for (int i = 0; i < tripCount; i++) {
            tripFronts[i] = new ParetoFront.Builder();
        }

        int[] walkTimeToDest = new int[stationCount];
        Arrays.fill(walkTimeToDest, -1);
        int destTransfersRange = transfers.arrivingAt(destinationStationId);
        int destStart = PackedRange.startInclusive(destTransfersRange);
        int destEnd = PackedRange.endExclusive(destTransfersRange);
        for (int i = destStart; i < destEnd; ++i) {
            try {
                walkTimeToDest[transfers.depStationId(i)] = transfers.minutes(i);
            } catch (NoSuchElementException e) {
                // Skip this transfer if it doesn't exist
                continue;
            }
        }
        if (destinationStationId >= 0 && destinationStationId < stationCount) {
             walkTimeToDest[destinationStationId] = 0;
        }

        for (int currentConnId = 0; currentConnId < connections.size(); currentConnId++) {
            final int connId = currentConnId;
            int depStopId = connections.depStopId(connId);
            int arrStopId = connections.arrStopId(connId);
            int depStationId = tt.stationId(depStopId);
            int arrStationId = tt.stationId(arrStopId);
            int depTime = connections.depMins(connId);
            int arrTime = connections.arrMins(connId);
            int tripId = connections.tripId(connId);
            final int tripPos = connections.tripPos(connId);

            ParetoFront.Builder f = new ParetoFront.Builder();

            int walkTime = walkTimeToDest[arrStationId];
            if (walkTime >= 0) {
                int finalArrTime = arrTime + walkTime;
                int payload = Bits32_24_8.pack(connId, tripPos);
                f.add(finalArrTime, 0, payload);
            }

            if (tripFronts[tripId] != null && !tripFronts[tripId].isEmpty()) {
                 tripFronts[tripId].forEach(tripCrit -> {
                    int existingPayload = PackedCriteria.payload(tripCrit);
                    int arrMins = PackedCriteria.arrMins(tripCrit);
                    int changes = PackedCriteria.changes(tripCrit);
                    f.add(arrMins, changes, existingPayload);
                 });
            }

            if (stationFronts[arrStationId] != null && !stationFronts[arrStationId].isEmpty()) {
                stationFronts[arrStationId].forEach(stationCrit -> {
                    try {
                        int stationDepTime = PackedCriteria.depMins(stationCrit);
                         if (stationDepTime >= arrTime) {
                            int arrMins = PackedCriteria.arrMins(stationCrit);
                            int changes = PackedCriteria.changes(stationCrit) + 1;
                            int payload = Bits32_24_8.pack(connId, tripPos);
                            f.add(arrMins, changes, payload);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }

            if (f.isEmpty()) continue;

            if (stationFronts[depStationId] != null && stationFronts[depStationId].fullyDominates(f, depTime)) {
                 continue;
            }

            tripFronts[tripId].addAll(f);

            int transferRange = transfers.arrivingAt(depStationId);
            int start = PackedRange.startInclusive(transferRange);
            int end = PackedRange.endExclusive(transferRange);

            for (int transferId = start; transferId < end; transferId++) {
                int walkStartStationId = transfers.depStationId(transferId);
                int walkDuration;
                try {
                    walkDuration = transfers.minutes(transferId);
                } catch (NoSuchElementException e) {
                    continue; // Skip this transfer if it doesn't exist
                }
                final int effectiveDepTime = depTime - walkDuration;

                ParetoFront.Builder stationUpdateBuilder = new ParetoFront.Builder();

                 f.forEach(fCrit -> {
                    int arrMins = PackedCriteria.arrMins(fCrit);
                    int changes = PackedCriteria.changes(fCrit);
                    int tripPayload = PackedCriteria.payload(fCrit);

                    int connToLeave = Bits32_24_8.unpack24(tripPayload);
                    int posToLeave = Bits32_24_8.unpack8(tripPayload);

                    int stopsToSkip = Math.max(0, posToLeave - tripPos);

                    int finalPayload = Bits32_24_8.pack(connId, stopsToSkip);

                    long packedStationCrit = PackedCriteria.pack(arrMins, changes, finalPayload);
                    packedStationCrit = PackedCriteria.withDepMins(packedStationCrit, effectiveDepTime);

                    stationUpdateBuilder.add(packedStationCrit);
                });

                 if (!stationUpdateBuilder.isEmpty()) {
                    stationFronts[walkStartStationId].addAll(stationUpdateBuilder);
                 }
            }
             int selfWalkDuration = 0;
            try {
                selfWalkDuration = transfers.minutesBetween(depStationId, depStationId);
            } catch (NoSuchElementException ignored) { /* Use 0 */ }

            final int selfEffectiveDepTime = depTime - selfWalkDuration;
            ParetoFront.Builder selfUpdateBuilder = new ParetoFront.Builder();
            f.forEach(fCrit -> {
                int arrMins = PackedCriteria.arrMins(fCrit);
                int changes = PackedCriteria.changes(fCrit);
                int tripPayload = PackedCriteria.payload(fCrit);
                 int connToLeave = Bits32_24_8.unpack24(tripPayload);
                int posToLeave = Bits32_24_8.unpack8(tripPayload);
                int stopsToSkip = Math.max(0, posToLeave - tripPos);
                 int finalPayload = Bits32_24_8.pack(connId, stopsToSkip);
                long packedStationCrit = PackedCriteria.pack(arrMins, changes, finalPayload);
                packedStationCrit = PackedCriteria.withDepMins(packedStationCrit, selfEffectiveDepTime);
                selfUpdateBuilder.add(packedStationCrit);
            });
            if (!selfUpdateBuilder.isEmpty()) {
                stationFronts[depStationId].addAll(selfUpdateBuilder);
            }
        }

        List<ParetoFront> finalStationFronts = new ArrayList<>(stationCount);
        for (int i = 0; i < stationCount; i++) {
            ParetoFront front = (stationFronts[i] != null && !stationFronts[i].isEmpty())
                    ? stationFronts[i].build()
                    : ParetoFront.EMPTY;
            finalStationFronts.add(front);
        }

        return new Profile(tt, date, destinationStationId, finalStationFronts);
    }
}
