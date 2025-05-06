package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static ch.epfl.rechor.Bits32_24_8.unpack24;
import static ch.epfl.rechor.Bits32_24_8.unpack8;
import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Classe qui extrait des voyages concrets à partir des données de profil.
 * Convertit les critères de la frontière de Pareto en objets Journey avec
 * des étapes de transport et à pied.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public class JourneyExtractor {

    private JourneyExtractor() {
    }

    /**
     * Extrait une liste de voyages à partir d'un profil et d'un identifiant de station de départ.
     *
     * @param profile      Le profil contenant les données de voyage optimales
     * @param depStationId L'identifiant de la station de départ
     * @return Une liste de voyages possibles entre la station de départ et la destination du profil
     */
    public static List<Journey> journeys(Profile profile, int depStationId) {
        // Liste pour stocker les voyages extraits
        List<Journey> journeys = new ArrayList<>();

        // Récupération des informations du profil
        int arrStationId = profile.arrStationId();
        ParetoFront initialPf = profile.forStation(depStationId);
        TimeTable tt = profile.timeTable();
        LocalDate date = profile.date();

        // Récupération des composants nécessaires de l'horaire
        Connections connections = tt.connectionsFor(date);
        Trips trips = tt.tripsFor(date);
        Routes routes = tt.routes();
        Stations stations = tt.stations();
        Platforms platforms = tt.platforms();
        Transfers transfers = tt.transfers();

        // Traitement de chaque critère dans la frontière de Pareto
        initialPf.forEach((long criteria) -> {
            // Pour chaque critère, on crée un nouveau voyage avec une liste d'étapes vide
            List<Journey.Leg> legs = new ArrayList<>();

            // Extraction des informations du critère
            int depTime = depMins(criteria);              // Heure de départ en minutes
            int targetArrTime = arrMins(criteria);        // Heure d'arrivée cible en minutes
            int remainingChanges = changes(criteria);     // Nombre de changements restants

            // Extraction et validation des données de la charge utile (payload)
            int payload = payload(criteria);

            // Extraction de l'ID de connexion (24 bits) et validation
            int connectionID = unpack24(payload);

            // Extraction du nombre d'arrêts intermédiaires (8 bits) et validation
            int nbOfIntermediateStops = unpack8(payload);

            // Configuration initiale pour la création du voyage
            int firstStopId = connections.depStopId(connectionID);
            int currentStationId = depStationId;
            int firstStationID = tt.stationId(firstStopId);

            // Détermination si un transfert à pied initial est nécessaire
            // Deux cas possibles:
            // 1. La station de départ n'est pas la même que la station de la première connexion
            // 2. Le premier arrêt est un quai (et non une station)
            boolean needsInitialFootTransfer = currentStationId != firstStationID;

            // Ajout d'une étape à pied initiale si nécessaire
            if (needsInitialFootTransfer) {
                legs.add(createFootLeg(profile, currentStationId, firstStationID,
                        createTime(depTime, date), transfers));
                currentStationId = firstStationID; // Mise à jour de la station courante
            }

            // Boucle principale: traitement de chaque connexion
            while (remainingChanges >= 0) {
                // Récupération des détails de la connexion
                int depStopId = connections.depStopId(connectionID);
                int arrStopId = connections.arrStopId(connectionID);
                int tripId = connections.tripId(connectionID);
                int routeId = trips.routeId(tripId);

                // Récupération des informations sur la route, le véhicule et la destination
                String route = routes.name(routeId);
                Vehicle vehicle = routes.vehicle(routeId);
                String destination = trips.destination(tripId);

                // Traitement des arrêts intermédiaires
                List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>();
                int nextConnectionId = connectionID;

                // Création exactement du nombre d'arrêts intermédiaires spécifié dans le payload
                for (int j = 0; j < nbOfIntermediateStops; j++) {
                    // Récupération des informations sur l'arrêt intermédiaire
                    int interStopId = connections.arrStopId(nextConnectionId);

                    LocalDateTime interArrTime = createTime(connections.arrMins(nextConnectionId)
                            , date);

                    int tempNextConnectionId = connections.nextConnectionId(nextConnectionId);
                    if (tempNextConnectionId != -1) {
                        nextConnectionId = tempNextConnectionId;
                    } else {
                        // Si pas de connexion suivante, on sort de la boucle
                        break;
                    }

                    LocalDateTime interDepTime = createTime(connections.depMins(nextConnectionId)
                            , date);

                    // Création d'un objet Stop pour la station intermédiaire
                    int stationId = tt.stationId(interStopId);
                    String stationName = tt.stations().name(stationId);
                    String platformName = tt.platformName(interStopId);
                    double longitude = tt.stations().longitude(stationId);
                    double latitude = tt.stations().latitude(stationId);

                    Stop intermediateStop = new Stop(stationName, platformName, longitude,
                            latitude);

                    // Gestion des heures d'arrivée/départ pour respecter la contrainte:
                    // l'heure d'arrivée doit être avant l'heure de départ
                    if (interArrTime.isAfter(interDepTime)) {
                        // Échange des heures pour éviter une exception lors de la création de
                        // IntermediateStop
                        LocalDateTime temp = interArrTime;
                        interArrTime = interDepTime;
                        interDepTime = temp;
                    }

                    // Ajout de l'arrêt intermédiaire à la liste
                    intermediateStops.add(new Journey.Leg.IntermediateStop(
                            intermediateStop,
                            interArrTime,
                            interDepTime
                    ));

                    // Mise à jour de l'arrêt d'arrivée pour la connexion suivante
                    arrStopId = connections.arrStopId(nextConnectionId);
                }

                // Création des heures de départ et d'arrivée pour l'étape de transport
                LocalDateTime tripDepTime = createTime(connections.depMins(connectionID), date);
                LocalDateTime tripArrTime = createTime(connections.arrMins(nextConnectionId), date);

                // Création des objets Stop pour le départ et l'arrivée
                Stop depStop = createStop(tt, stations, platforms, depStopId);
                Stop arrStop = createStop(tt, stations, platforms, arrStopId);

                // Création et ajout de l'étape de transport
                Journey.Leg leg = new Journey.Leg.Transport(depStop, tripDepTime, arrStop,
                        tripArrTime, intermediateStops, vehicle, route, destination);
                legs.add(leg);

                // Mise à jour de la station courante
                currentStationId = tt.stationId(arrStopId);


                // Traitement de l'arrivée et des transferts
                if (currentStationId != arrStationId) {
                    remainingChanges--;

                    // Préparation pour la prochaine connexion
                    ParetoFront nextStationFront = profile.forStation(currentStationId);


                    // Protection contre les potentielles exceptions NoSuchElementException
                    try {
                        long nextCriteria;

                        nextCriteria = nextStationFront.get(targetArrTime, remainingChanges);
                        // Mise à jour des données pour la prochaine connexion
                        depTime = depMins(nextCriteria);
                        connectionID = unpack24(payload(nextCriteria));
                        nbOfIntermediateStops = unpack8(payload(nextCriteria));

                        int nextDepStopId = connections.depStopId(connectionID);
                        int nextDepStationId = tt.stationId(nextDepStopId);

                        // Ajout d'une étape à pied vers la destination finale
                        legs.add(createFootLeg(profile, currentStationId, nextDepStationId,
                                tripArrTime, transfers));


                        // Conversion de l'ID d'arrêt en ID de station
                        currentStationId = nextDepStationId;

                    } catch (NoSuchElementException e) {
                        // Si aucun critère n'est trouvé, on termine ce voyage
                        legs.add(createFootLeg(profile, currentStationId, arrStationId,
                                tripArrTime, transfers));
                        journeys.add(new Journey(legs));
                        break;
                    }

                } else {
                    // Si nous sommes directement à la station de destination
                    journeys.add(new Journey(legs));
                    break;
                }
            }


        });

        // Tri des voyages par heure de départ, puis par heure d'arrivée
        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));

        return journeys;
    }

    /**
     * Crée une étape à pied entre deux stations.
     *
     * @param profile       Profil contenant les données de l'horaire
     * @param fromStationId ID de la station de départ
     * @param toStationId   ID de la station d'arrivée
     * @param depTime       Heure de départ
     * @param transfers     Données sur les transferts entre stations
     * @return Une étape à pied (Foot leg)
     */
    private static Journey.Leg.Foot createFootLeg(Profile profile, int fromStationId,
                                                  int toStationId, LocalDateTime depTime,
                                                  Transfers transfers) {
        Stations stations = profile.timeTable().stations();
        Platforms platforms = profile.timeTable().platforms();

        // Création des objets Stop pour le départ et l'arrivée
        Stop depStop = createStop(profile.timeTable(), stations, platforms, fromStationId);
        Stop arrStop = createStop(profile.timeTable(), stations, platforms, toStationId);

        // Récupération du temps de marche entre stations
        int walkingMinutes;
        walkingMinutes = transfers.minutesBetween(fromStationId, toStationId);

        // Calcul de l'heure d'arrivée
        LocalDateTime arrTime = depTime.plusMinutes(walkingMinutes);

        // Création et retour de l'étape à pied
        return new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
    }

    /**
     * Crée un objet Stop à partir d'un ID d'arrêt.
     *
     * @param tt        Horaire contenant les données des stations et plateformes
     * @param stations  Stations indexées
     * @param platforms Plateformes indexées
     * @param stopId    ID de l'arrêt
     * @return Un objet Stop représentant l'arrêt
     */
    private static Stop createStop(TimeTable tt, Stations stations, Platforms platforms,
                                   int stopId) {
        // Utilisation des méthodes de l'interface TimeTable pour gérer les stopIds
        int stationId = tt.stationId(stopId);
        String platformName = tt.platformName(stopId);

        // Création et retour de l'objet Stop
        return new Stop(
                stations.name(stationId),
                platformName,
                stations.longitude(stationId),
                stations.latitude(stationId)
        );
    }

    /**
     * Convertit un temps en minutes après minuit en LocalDateTime.
     *
     * @param timeAfterMidnight Minutes après minuit
     * @param date              Date de référence
     * @return DateTime représentant le temps spécifié
     */
    private static LocalDateTime createTime(int timeAfterMidnight, LocalDate date) {
        return date.atStartOfDay().plusMinutes(timeAfterMidnight);
    }
}
