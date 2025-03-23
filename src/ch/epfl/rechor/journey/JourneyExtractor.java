package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;
import ch.epfl.rechor.timetable.mapped.BufferedConnections;
import ch.epfl.rechor.timetable.mapped.BufferedStations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.rechor.Bits32_24_8.*;
import static ch.epfl.rechor.journey.PackedCriteria.*;

public class JourneyExtractor {

    private JourneyExtractor(){}

    public List<Journey> journeys(Profile profile, int depStationId){
        ParetoFront pf = profile.forStation(depStationId);
        List<Journey> journeys = new ArrayList<>();

        TimeTable fileTimeTable = profile.timeTable();
        LocalDate date = profile.date();
        int arrStationId = profile.arrStationId();

        Connections connections = fileTimeTable.connectionsFor(date);
        Trips trips = fileTimeTable.tripsFor(date);
        Routes routes = fileTimeTable.routes();
        //Transfers transfers = fileTimeTable.transfers();
        Stations stations = fileTimeTable.stations();
        Platforms platforms = fileTimeTable.platforms();

        pf.forEach((long criteria) -> {
            List<Journey.Leg> legs = new ArrayList<>();

            int depTime = depMins(criteria);
            int arrTime = arrMins(criteria);
            int changes = changes(criteria);
            int connectionID = unpack24(payload(criteria));
            int nbOfIntermediateStops = unpack8(payload(criteria));






            while (changes >= 0) {
                int depStopId = connections.depStopId(connectionID);
                int arrStopId = connections.arrStopId(connectionID);
                int tripId = connections.tripId(connectionID);
                int routeId = trips.routeId(tripId);

                String route = routes.name(routeId);
                Vehicle vehicle = routes.vehicle(routeId);
                String destination = trips.destination(tripId);


                List<Journey.Leg.IntermediateStop> intermediateStops = new ArrayList<>(nbOfIntermediateStops);
                int nextConnectionId = connectionID;
                for (int i = 0; i < nbOfIntermediateStops; i++) {
                    int interStopId = connections.arrStopId(nextConnectionId);
                    LocalDateTime interArrTime = date.atStartOfDay().plusMinutes(connections.arrMins(nextConnectionId));
                    nextConnectionId = connections.nextConnectionId(nextConnectionId);
                    LocalDateTime interDepTime = date.atStartOfDay().plusMinutes(connections.depMins(nextConnectionId));


                    String stopName = stations.name(interStopId);
                    String platformName = fileTimeTable.platformName(interStopId);
                    double longitude = stations.longitude(interStopId);
                    double latitude = stations.latitude(interStopId);
                    intermediateStops.add(new Journey.Leg.IntermediateStop(new Stop(stopName, platformName, longitude, latitude), interArrTime, interDepTime));
                    arrStopId = connections.arrStopId(nextConnectionId);
                }

                LocalDateTime tripDepTime = date.atStartOfDay().plusMinutes(connections.depMins(connectionID));
                LocalDateTime tripArrTime = date.atStartOfDay().plusMinutes(connections.arrMins(nextConnectionId));

                Stop depStop = new Stop(stations.name(platforms.stationId((depStopId))), platforms.name(depStopId), stations.longitude(platforms.stationId((depStopId))),stations.latitude(platforms.stationId((depStopId))) );
                Stop arrStop = new Stop(stations.name(platforms.stationId((arrStopId))), platforms.name(arrStopId), stations.longitude(platforms.stationId((arrStopId))),stations.latitude(platforms.stationId((arrStopId))) );






                Journey.Leg leg = new Journey.Leg.Transport(depStop,tripDepTime,arrStop,tripArrTime,intermediateStops,vehicle, route, destination);
                legs.add(leg);


                changes--;


            }



            
            journeys.add(new Journey(legs));
        });


        return journeys;
    }
}
