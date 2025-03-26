package ch.epfl.rechor.journey;

import static ch.epfl.rechor.Bits32_24_8.*;
import static ch.epfl.rechor.journey.PackedCriteria.*;
import ch.epfl.rechor.timetable.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JourneyExtractor {

    private JourneyExtractor(){}


    public  static List<Journey> journeys(Profile profile, int depStationId ){
        List<Journey> journeys = new ArrayList<>();

        int arrStationId = profile.arrStationId();
        ParetoFront initialPf= profile.forStation(depStationId);
        TimeTable tt = profile.timeTable();
        LocalDate date = profile.date();

        // Récupération des données nécessaires
        Connections connections = tt.connectionsFor(date);
        Trips trips = tt.tripsFor(date);
        Routes routes = tt.routes();
        Stations stations = tt.stations();
        Platforms platforms = tt.platforms();
        Transfers transfers = tt.transfers();


        initialPf.forEach((long criteria) -> {
            // pour chaque critère on va créer un voyage (commençant par une liste de legs)
            List<Journey.Leg> legs = new ArrayList<>();


            int depTime = depMins(criteria);
            int targetArrTime = arrMins(criteria);
            int remainingChanges = changes(criteria);

            int connectionID = unpack24(payload(criteria));
            int nbOfIntermediateStops = unpack8(payload(criteria));

            int firstStopId = connections.depStopId(connectionID);
            int currentStationId = depStationId;
            int firstStationID = platforms.stationId(firstStopId);

            // Etape à pied initiale si nécessaire
            if(currentStationId!=firstStationID){
                legs.add(createFootLeg(profile, currentStationId, firstStationID, createTime(depTime, date), transfers));
                currentStationId = firstStationID;
            }

            // Crétaion des legs pour chaque changement dans le critère
            while(remainingChanges>=0){
                int depStopId = connections.depStopId(connectionID);
                int arrStopId = connections.arrStopId(connectionID);
                int tripId = connections.tripId(connectionID);
                int routeId = trips.routeId(tripId);

                String route = routes.name(routeId);
                Vehicle vehicle = routes.vehicle(routeId);
                String destination = trips.destination(tripId);

                List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>(nbOfIntermediateStops);
                int nextConnectionId = connectionID;
                for (int j = 0; j < nbOfIntermediateStops; j++) {
                    int interStopId = connections.arrStopId(nextConnectionId);
                    LocalDateTime interArrTime = createTime(connections.arrMins(nextConnectionId),date);
                    nextConnectionId = connections.nextConnectionId(nextConnectionId);
                    LocalDateTime interDepTime = createTime(connections.depMins(nextConnectionId),date);

                    intermediateStops.add(new Journey.Leg.IntermediateStop(createStop(stations, platforms, interStopId), interArrTime, interDepTime));
                    arrStopId = connections.arrStopId(nextConnectionId);
                }

                LocalDateTime tripDepTime = createTime(connections.depMins(connectionID), date);
                LocalDateTime tripArrTime = createTime(connections.arrMins(nextConnectionId), date);

                Stop depStop = createStop(stations, platforms, depStopId);
                Stop arrStop = createStop(stations, platforms, arrStopId);

                Journey.Leg leg = new Journey.Leg.Transport(depStop, tripDepTime, arrStop, tripArrTime, intermediateStops, vehicle, route, destination);
                legs.add(leg);

                currentStationId = platforms.stationId(arrStopId);


                if (currentStationId != arrStationId) {
                    legs.add(createFootLeg(profile, currentStationId, arrStationId, tripArrTime,transfers));
                    // Accès aux données concernant la prochaine station / la prochaine étape du voyage
                    ParetoFront nextStationFront = profile.forStation(currentStationId);
                    remainingChanges--;
                    long nextCriteria = nextStationFront.get(targetArrTime, remainingChanges);
                    // actualisation des données nécessaires
                    depTime = depMins(nextCriteria);
                    connectionID = unpack24(payload(nextCriteria));
                    nbOfIntermediateStops = unpack8(payload(nextCriteria));
                    firstStopId = connections.depStopId(connectionID);
                    currentStationId = platforms.stationId(firstStopId);


                } else {

                    journeys.add(new Journey(legs));
                    break;

                }
            }

            // Etape à pied finale si nécessaire
            if(currentStationId!=arrStationId){
                legs.add(createFootLeg(profile, currentStationId, arrStationId, createTime(depTime, date), transfers));
            }



        });

        //Tri des voyages
        journeys.sort(Comparator.comparing(Journey :: depTime).thenComparing(Journey::arrTime));


        return journeys;
    }


    private static Journey.Leg.Foot createFootLeg(Profile profile, int fromStationId, int toStationId, LocalDateTime depTime, Transfers transfers) {
        Stations stations = profile.timeTable().stations();
        Platforms platforms = profile.timeTable().platforms();

        Stop depStop = createStop(stations, platforms, fromStationId);
        Stop arrStop = createStop(stations, platforms, toStationId);

        LocalDateTime arrTime = depTime.plusMinutes(transfers.minutesBetween(fromStationId, toStationId));

        return new Journey.Leg.Foot(depStop, depTime, arrStop, arrTime);
    }

    private static Stop createStop(Stations stations, Platforms platforms, int stopId) {
        return new Stop(stations.name(platforms.stationId(stopId)), platforms.name(stopId), stations.longitude(platforms.stationId(stopId)), stations.latitude(platforms.stationId(stopId)));
    }

    private static LocalDateTime createTime(int timeAfterMidnight, LocalDate date) {
        return date.atStartOfDay().plusMinutes(timeAfterMidnight);
    }
}
