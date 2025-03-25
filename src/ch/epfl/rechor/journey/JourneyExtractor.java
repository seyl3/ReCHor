package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ch.epfl.rechor.Bits32_24_8.*;
import static ch.epfl.rechor.journey.PackedCriteria.*;

public final class JourneyExtractor {

    private JourneyExtractor(){}

    public  List<Journey> journeys(Profile profile, int depStationId) {
        return buildJourneys(profile, depStationId, profile.arrStationId(), profile.forStation(depStationId));
    }

    private  List<Journey> buildJourneys(Profile profile, int currentDepStationId, int arrStationId, ParetoFront pf) {
        List<Journey> journeys = new ArrayList<>();

        TimeTable fileTimeTable = profile.timeTable();
        LocalDate date = profile.date();

        Connections connections = fileTimeTable.connectionsFor(date);
        Trips trips = fileTimeTable.tripsFor(date);
        Routes routes = fileTimeTable.routes();
        Stations stations = fileTimeTable.stations();
        Platforms platforms = fileTimeTable.platforms();

        pf.forEach((long criteria) -> {
            List<Journey.Leg> legs = new ArrayList<>();

            int depTime = depMins(criteria);
            int connectionID = unpack24(payload(criteria));
            int nbOfIntermediateStops = unpack8(payload(criteria));

            int firstStopId = connections.depStopId(connectionID);
            int currentArrStationId = platforms.stationId(firstStopId);

            if(currentDepStationId!=currentArrStationId){
                legs.add(createFootLeg(profile, currentDepStationId, currentArrStationId, date.atStartOfDay().plusMinutes(depTime)));
            }


            for (int i = 0; i <= changes(criteria); i++) {
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

                currentArrStationId = platforms.stationId(arrStopId);


                if (currentArrStationId != arrStationId) {
                    ParetoFront nextParetoFront = profile.forStation(currentArrStationId);
                    legs.add(createFootLeg(profile, currentArrStationId, arrStationId, tripArrTime));

                    List<Journey> nextJourneys = buildJourneys(profile, currentArrStationId, arrStationId, nextParetoFront);
                    for (Journey nextJourney : nextJourneys) {
                        List<Journey.Leg> combinedLegs = new ArrayList<>(legs);
                        combinedLegs.addAll(nextJourney.legs());
                        journeys.add(new Journey(combinedLegs));
                    }
                    return ;

                } else {

                    journeys.add(new Journey(legs));
                    break;

                }
            }

        });

        //Tri des voyages
        journeys.sort(Comparator.comparing(Journey :: depTime).thenComparing(Journey::arrTime));


        return journeys;
    }

    private Journey.Leg.Foot createFootLeg(Profile profile, int fromStationId, int toStationId, LocalDateTime depTime) {
        Stations stations = profile.timeTable().stations();
        Platforms platforms = profile.timeTable().platforms();

        Stop depStop = createStop(stations, platforms, fromStationId);
        Stop arrStop = createStop(stations, platforms, toStationId);

        return new Journey.Leg.Foot(depStop, depTime, arrStop, depTime.plusMinutes(5)); // Comment connaitre la durée de la marche???
    }

    private Stop createStop(Stations stations, Platforms platforms, int stopId) {
        return new Stop(stations.name(platforms.stationId(stopId)), platforms.name(stopId), stations.longitude(platforms.stationId(stopId)), stations.latitude(platforms.stationId(stopId)));
    }

    private LocalDateTime createTime(int timeAfterMidnight, LocalDate date) {
        return date.atStartOfDay().plusMinutes(timeAfterMidnight);
    }
}