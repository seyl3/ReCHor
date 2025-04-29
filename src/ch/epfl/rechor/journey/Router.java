package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Calcule le profil des trajets optimaux pour une date et une station de destination données
 * en utilisant l'algorithme Connection Scan Algorithm (CSA).
 *
 * @param tt L'horaire complet contenant toutes les données nécessaires (stations, connexions,
 *           etc.).
 * @author Sarra Zghal, Elyes Ben Abid
 */
public record Router(TimeTable tt) {

    /**
     * Calcule le profil des trajets optimaux pour une date et une station de destination données.
     *
     * @param date                 La date pour laquelle calculer le profil.
     * @param destinationStationId L'identifiant de la station de destination.
     * @return Le profil contenant les frontières de Pareto pour chaque station.
     */
    public Profile profile(LocalDate date, int destinationStationId) {
        // Étape 1 : Initialisation des structures de données 
        Stations stations = tt.stations();
        Connections connections = tt.connectionsFor(date);
        Trips trips = tt.tripsFor(date);
        Transfers transfers = tt.transfers();

        int stationCount = stations.size();
        int tripCount = trips.size();

        // Bâtisseurs pour les frontières de Pareto des stations et trajets
        ParetoFront.Builder[] stationFronts = new ParetoFront.Builder[stationCount];
        for (int i = 0; i < stationCount; i++) {
            stationFronts[i] = new ParetoFront.Builder();
        }
        ParetoFront.Builder[] tripFronts = new ParetoFront.Builder[tripCount];
        for (int i = 0; i < tripCount; i++) {
            tripFronts[i] = new ParetoFront.Builder();
        }

        // Tableau des temps de marche vers la destination
        int[] walkTimeToDest = new int[stationCount];
        Arrays.fill(walkTimeToDest, -1);
        int destTransfersRange = transfers.arrivingAt(destinationStationId);
        int destStart = PackedRange.startInclusive(destTransfersRange);
        int destEnd = PackedRange.endExclusive(destTransfersRange);
        for (int i = destStart; i < destEnd; ++i) {
            walkTimeToDest[transfers.depStationId(i)] = transfers.minutes(i);

        }
        // Le temps de marche depuis la destination vers elle-même est de 0.
        if (destinationStationId >= 0 && destinationStationId < stationCount) {
            walkTimeToDest[destinationStationId] = 0;
        }

        // Étape 2 : Algorithme Connection Scan (CSA) 
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

            // Frontière temporaire 'f' pour cette connexion
            ParetoFront.Builder f = new ParetoFront.Builder();

            // Étape 2a : Construction de la frontière temporaire 'f' (3 options) ---
            // Option 1 : Marche vers destination
            int walkTime = walkTimeToDest[arrStationId];
            if (walkTime >= 0) {
                int finalArrTime = arrTime + walkTime;
                int payload = Bits32_24_8.pack(connId, 0);
                long packedCriteria = PackedCriteria.pack(finalArrTime, 0, payload);
                packedCriteria = PackedCriteria.withDepMins(packedCriteria, depTime);
                f.add(packedCriteria);
            }

            // Option 2 : Continuer même trajet
            if (tripFronts[tripId] != null && !tripFronts[tripId].isEmpty()) {
                tripFronts[tripId].build().forEach(tripCrit -> {
                    int existingPayload = PackedCriteria.payload(tripCrit);
                    int arrMins = PackedCriteria.arrMins(tripCrit);
                    int changes = PackedCriteria.changes(tripCrit);
                    long packedCriteria = PackedCriteria.pack(arrMins, changes, existingPayload);
                    packedCriteria = PackedCriteria.withDepMins(packedCriteria, depTime);
                    f.add(packedCriteria);
                });
            }

            // Option 3 : Changer de véhicule
            if (stationFronts[arrStationId] != null && !stationFronts[arrStationId].isEmpty()) {
                stationFronts[arrStationId].forEach(stationCrit -> {
                    //System.out.println("Station: " + Long.toUnsignedString(stationCrit));
                    int stationDepTime = PackedCriteria.depMins(stationCrit);
                    if (stationDepTime >= arrTime) {
                        int arrMins = PackedCriteria.arrMins(stationCrit);
                        int changes = PackedCriteria.changes(stationCrit) + 1;
                        int payload = Bits32_24_8.pack(connId, 0);
                        long packedCriteria = PackedCriteria.pack(arrMins, changes, payload);
                        packedCriteria = PackedCriteria.withDepMins(packedCriteria, depTime);
                        f.add(packedCriteria);
                    }
                });
            }

            //  Étape 2b : Optimisation et mise à jour des frontières 
            if (f.isEmpty()) continue;

            // Ignorer si 'f' est dominée
            if (stationFronts[depStationId] != null) {
                ParetoFront.Builder stationFrontWithDepTime = new ParetoFront.Builder();
                stationFronts[depStationId].build().forEach(stationCrit -> {
                    if (!PackedCriteria.hasDepMins(stationCrit)) {
                        stationFrontWithDepTime.add(
                                PackedCriteria.withDepMins(stationCrit, depTime));
                    } else {
                        stationFrontWithDepTime.add(stationCrit);
                    }
                });

                ParetoFront.Builder fWithDepTime = new ParetoFront.Builder();
                f.build().forEach(fCrit -> {
                    if (!PackedCriteria.hasDepMins(fCrit)) {
                        fWithDepTime.add(PackedCriteria.withDepMins(fCrit, depTime));
                    } else {
                        fWithDepTime.add(fCrit);
                    }
                });

                if (stationFrontWithDepTime.fullyDominates(fWithDepTime, depTime)) {
                    continue;
                }
            }

            // Mise à jour frontière du trajet
            tripFronts[tripId].addAll(f);

            // Mise à jour frontières des stations via transferts à pied
            int transferRange = transfers.arrivingAt(depStationId);
            int start = PackedRange.startInclusive(transferRange);
            int end = PackedRange.endExclusive(transferRange);

            for (int transferId = start; transferId < end; transferId++) {
                int walkStartStationId = transfers.depStationId(transferId);
                int walkDuration;
                walkDuration = transfers.minutes(transferId);
                final int effectiveDepTime = depTime - walkDuration;

                ParetoFront.Builder stationUpdateBuilder = new ParetoFront.Builder();
                f.build().forEach(fCrit -> {
                    int arrMins = PackedCriteria.arrMins(fCrit);
                    int changes = PackedCriteria.changes(fCrit);
                    int tripPayload = PackedCriteria.payload(fCrit);
                    int posToLeave = Bits32_24_8.unpack8(tripPayload);
                    int stopsToSkip = Math.max(0, posToLeave - tripPos);
                    int finalPayload = Bits32_24_8.pack(connId, stopsToSkip);
                    long packedStationCrit = PackedCriteria.pack(arrMins, changes, finalPayload);
                    packedStationCrit =
                            PackedCriteria.withDepMins(packedStationCrit, effectiveDepTime);
                    stationUpdateBuilder.add(packedStationCrit);
                });

                if (!stationUpdateBuilder.isEmpty()) {
                    stationFronts[walkStartStationId].addAll(stationUpdateBuilder);
                }
            }

            // Mise à jour frontière de la station de départ elle-même (transfert sur place)
            int selfWalkDuration = 0;
            selfWalkDuration = transfers.minutesBetween(depStationId, depStationId);


            final int selfEffectiveDepTime = depTime - selfWalkDuration;
            ParetoFront.Builder selfUpdateBuilder = new ParetoFront.Builder();
            f.build().forEach(fCrit -> {
                int arrMins = PackedCriteria.arrMins(fCrit);
                int changes = PackedCriteria.changes(fCrit);
                int tripPayload = PackedCriteria.payload(fCrit);
                int posToLeave = Bits32_24_8.unpack8(tripPayload);
                int stopsToSkip = Math.max(0, posToLeave - tripPos);
                int finalPayload = Bits32_24_8.pack(connId, stopsToSkip);
                long packedStationCrit = PackedCriteria.pack(arrMins, changes, finalPayload);
                packedStationCrit =
                        PackedCriteria.withDepMins(packedStationCrit, selfEffectiveDepTime);
                selfUpdateBuilder.add(packedStationCrit);
            });
            if (!selfUpdateBuilder.isEmpty()) {
                stationFronts[depStationId].addAll(selfUpdateBuilder);
            }
        }

        // Étape 3 : Construction du Profil Final
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
