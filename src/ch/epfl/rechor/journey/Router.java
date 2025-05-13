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

        for (int i = 0; i < connections.size(); ++i) {

            ParetoFront.Builder f = new ParetoFront.Builder();

            int liaisonId = i;
            int arrStationId = timeTable().stationId(connections.arrStopId(liaisonId));
            int depStationId = timeTable().stationId(connections.depStopId(liaisonId));
            int tripId = connections.tripId(liaisonId);
            int arrMins = connections.arrMins(liaisonId);
            int depMins = connections.depMins(liaisonId);


            // OPTION 1:
            int walkMin = walkTab[arrStationId];

            if (walkMin != -1) {
                f.add((arrMins + walkMin), 0, liaisonId);
            }

            // OPTION 2:
            if (profile.forTrip(tripId) != null) {
                    f.addAll(profile.forTrip(tripId));
            }
            // OPTION 3:
            if (profile.forStation(arrStationId) != null) {
                profile.forStation(arrStationId).forEach((long t) -> {
                    if (PackedCriteria.depMins(t) >= arrMins) {
                        f.add(PackedCriteria.withAdditionalChange(
                                PackedCriteria.withoutDepMins(
                                        PackedCriteria.withPayload(t, liaisonId))));
                    }
                });
            }
            // only update if necessary
            if (!f.isEmpty()) {
                //update trip pareto front
                if (profile.forTrip(tripId) == null) {
                    profile.setForTrip(tripId, new ParetoFront.Builder(f));
                } else { profile.forTrip(tripId).addAll(f); }

                int start = PackedRange.startInclusive(transfers.arrivingAt(depStationId));
                int end = PackedRange.endExclusive(transfers.arrivingAt(depStationId));

                for (int j = start; j < end; ++j) {

                    int newDepMins = depMins - transfers.minutes(j);

                    int depTransferStationId = transfers.depStationId(j);


                    if (profile.forStation(depTransferStationId) == null) {
                        profile.setForStation(depTransferStationId, new ParetoFront.Builder());
                    }

                    if (profile.forStation(depStationId) != null && !profile.forStation(depStationId).fullyDominates(f, depMins)) {

                        f.forEach((long t) -> {

                            int tLiaisonId = PackedCriteria.payload(t);


                            int nbInterStops = connections.tripPos(tLiaisonId) - connections.tripPos(liaisonId);

                            profile.forStation(depTransferStationId)
                                    .add(PackedCriteria.withDepMins(
                                    withPayload(t, liaisonId, nbInterStops),
                                            newDepMins));
                        });

                    }
                }
            }
        }
        return profile.build();
    }

    /**
     * Méthode auxiliaire, ajoute a un critère de Pareto existant une nouvelle charge utile
     * combinant l'identifiant de la liaison et le nombre d'arrêts intermédiaires.
     *
     * @param criteria  le critère Pareto initial empaqueté
     * @param liaisonId l'identifiant de la liaison source
     * @param nbStops   le nombre d'arrêts intermédiaires dans la course
     * @return un nouveau critère Pareto incluant la charge utile mise à jour
     */
    private long withPayload (long criteria, int liaisonId, int nbStops){
        int payload = Bits32_24_8.pack(liaisonId, nbStops);
        return PackedCriteria.withPayload(criteria, payload);
    }
}