package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Transfers;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Représente un routeur capable de calculer, pour une date et une station d'arrivée données,
 * le profil de tous les voyages optimaux utilisant l'algorithme CSA.
 * <p>
 * Le calcul construit, pour chaque liaison de l'horaire du jour, la frontière de Pareto des
 * critères (temps d'arrivée, nombre de changements, informations de premier arrêt et nombre
 * d'arrêts intermédiaires), puis met à jour les frontières des courses et des gares
 * atteignables à pied.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public record Router(TimeTable timeTable) {

    /**
     * Pré‑alloue un {@link ParetoFront.Builder} vide pour chaque station et
     * chaque course.
     *
     * @param p            le builder de profil à initialiser
     * @param stationCount nombre total de gares
     * @param tripCount    nombre total de courses actives pour la date
     */
    private static void preallocateBuilders(Profile.Builder p,
                                            int stationCount,
                                            int tripCount) {
        for (int s = 0; s < stationCount; ++s) {
            p.setForStation(s, new ParetoFront.Builder());
        }
        for (int t = 0; t < tripCount; ++t) {
            p.setForTrip(t, new ParetoFront.Builder());
        }
    }

    /**
     * Calcule le profil de voyages optimaux permettant de rejoindre la gare d'arrivée spécifiée.
     * <p>
     * L'algorithme parcourt toutes les liaisons actives du jour en ordre croissant d'indice
     * (qui correspond à un ordre décroissant d'heure de départ), et pour chaque liaison
     * construit une frontière de Pareto temporaire à partir de trois options :
     * <ul>
     *   <li>Option 1 : descendre et marcher jusqu'à la destination si possible</li>
     *   <li>Option 2 : rester dans le même véhicule et poursuivre la course</li>
     *   <li>Option 3 : changer de véhicule à la fin de la liaison</li>
     * </ul>
     * Puis met à jour les frontières associées aux courses et aux gares accessibles à pied
     * pour propager ces solutions.
     * </p>
     *
     * @param date          la date pour laquelle les voyages sont calculés
     * @param destinationId l'identifiant de la gare d'arrivée
     * @return un {@link Profile} immuable contenant les frontières de Pareto pour toutes les gares
     */
    public Profile profile(LocalDate date, int destinationId) {

        Stations stations = timeTable.stations();
        Transfers transfers = timeTable.transfers();
        Connections connections = timeTable.connectionsFor(date);

        int[] walkTab = new int[stations.size()];
        Arrays.fill(walkTab, -1);
        int destTransfersRange = transfers.arrivingAt(destinationId);
        int destStart = PackedRange.startInclusive(destTransfersRange);
        int destEnd = PackedRange.endExclusive(destTransfersRange);
        for (int i = destStart; i < destEnd; ++i) {
            walkTab[transfers.depStationId(i)] = transfers.minutes(i);
        }

        Profile.Builder profile = new Profile.Builder(timeTable, date, destinationId);

        // Pré‑alloue des builders vides pour toutes les gares et toutes les courses
        preallocateBuilders(profile, timeTable.stations().size(), timeTable.tripsFor(date).size());

        for (int i = 0; i < connections.size(); ++i) {

            ParetoFront.Builder f = new ParetoFront.Builder();

            int liaisonId = i;
            int arrStationId = timeTable().stationId(connections.arrStopId(liaisonId));
            int depStationId = timeTable().stationId(connections.depStopId(liaisonId));
            int tripId = connections.tripId(liaisonId);
            int arrMins = connections.arrMins(liaisonId);
            int depMins = connections.depMins(liaisonId);

            // option 1 : marcher a pied
            int walkMin = walkTab[arrStationId];
            if (walkMin != -1) {
                f.add((arrMins + walkMin), 0, liaisonId);
            }

            // option 2 : continuer avec la course courante
            f.addAll(profile.forTrip(tripId));

            // option 3 : changer de véhicule à l’arrivée de la liaison
            profile.forStation(arrStationId).forEach((long t) -> {
                if (PackedCriteria.depMins(t) >= arrMins) {
                    f.add(PackedCriteria.withAdditionalChange(
                            PackedCriteria.withoutDepMins(
                                    PackedCriteria.withPayload(t, liaisonId))));
                }
            });

            if (!f.isEmpty()) {
                profile.forTrip(tripId).addAll(f);

                int start = PackedRange.startInclusive(transfers.arrivingAt(depStationId));
                int end = PackedRange.endExclusive(transfers.arrivingAt(depStationId));

                for (int j = start; j < end; ++j) {
                    int newDepMins = depMins - transfers.minutes(j);
                    int depTransferStationId = transfers.depStationId(j);

                    if (!profile.forStation(depStationId).fullyDominates(f, depMins)) {

                        f.forEach((long t) -> {
                            int tLiaisonId = PackedCriteria.payload(t);
                            int nbInterStops = connections.tripPos(tLiaisonId) -
                                    connections.tripPos(liaisonId);

                            profile.forStation(depTransferStationId)
                                    .add(PackedCriteria.withDepMins(
                                            PackedCriteria.withPayload(t,
                                                    Bits32_24_8.pack(liaisonId, nbInterStops)),
                                            newDepMins
                                    ));
                        });
                    }
                }
            }
        }
        return profile.build();
    }

}