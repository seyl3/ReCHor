package ch.epfl.rechor.journey;

/*
 * @author : Sarra Zghal, Elyes Ben Abid
 */

import ch.epfl.rechor.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JourneyGeoJsonConverter {

    public static Json.JObject toGeoJson(Journey journey) {
        List<Double> unsortedCoordinates = new ArrayList<>();
        List<Json> coordinatesList = new ArrayList<>();
        List<Json.JNumber> coordinates;
        Json.JArray coordinatesArray;


        for (Journey.Leg leg : journey.legs()) {
            unsortedCoordinates.add(leg.depStop().longitude());
            unsortedCoordinates.add(leg.depStop().latitude());
            for (Journey.Leg.IntermediateStop stops : leg.intermediateStops()) {
                unsortedCoordinates.add(stops.stop().longitude());
                unsortedCoordinates.add(stops.stop().latitude());
            }
            unsortedCoordinates.add(leg.arrStop().longitude());
            unsortedCoordinates.add(leg.arrStop().latitude());
        }

        coordinates = unsortedCoordinates.stream()
                .distinct()
                .map( s -> s * 1e5)
                .map(Math::round)
                .map(s -> s / 1e5)
                .map(Json.JNumber::new)
                .toList();

        for (int i = 0; i < coordinates.size() - 1; i += 2) {
            coordinatesList.add(new Json.JArray(List.of(coordinates.get(i), coordinates.get(i + 1))));
        }

        coordinatesArray = new Json.JArray(coordinatesList);

        //linkedHashMap permet de garantir que la map a bien le bon ordre qu'est l'ordre d'ajout
        Map<String, Json> orderedMap = new LinkedHashMap<>();
        orderedMap.put("type", new Json.JString("LineString"));
        orderedMap.put("coordinates", coordinatesArray);

        return new Json.JObject(orderedMap);
    }
}
