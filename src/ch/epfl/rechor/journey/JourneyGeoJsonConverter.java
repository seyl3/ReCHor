package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convertisseur de voyages au format GeoJSON pour visualisation sur carte.
 * <p>
 * Cette classe non instanciable permet de convertir un voyage en une représentation
 * GeoJSON de son tracé, optimisée pour la transmission web.
 *
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public final class JourneyGeoJsonConverter {

    private JourneyGeoJsonConverter() {
    }

    /**
     * Convertit un voyage en document GeoJSON représentant son tracé.
     * <p>
     * Le document généré contient une unique ligne brisée (LineString) passant par tous
     * les arrêts du voyage. Les coordonnées sont arrondies à 5 décimales (~1m de précision)
     * et les points successifs sont garantis différents. Le document est minifié (sans
     * espaces ni retours à la ligne) pour optimiser sa taille.
     *
     * @param journey le voyage à convertir
     * @return le document GeoJSON représentant le tracé du voyage
     */
    public static Json.JObject toGeoJson(Journey journey) {
        List<Double> unsortedCoordinates = new ArrayList<>();
        List<Json.JNumber> sortedCoordinates;
        List<Json> coordinatesList = new ArrayList<>();
        Json.JArray coordinatesJArray;




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

        sortedCoordinates = unsortedCoordinates.stream()
                .distinct()
                .map(s -> s * 1e5)
                .map(Math::round)
                .map(s -> s / 1e5)
                .map(Json.JNumber::new)
                .toList();

        for (int i = 0; i < sortedCoordinates.size() - 1; i += 2) {
            coordinatesList.add(
                    new Json.JArray(List.of(sortedCoordinates.get(i), sortedCoordinates.get(i + 1))));
        }

        coordinatesJArray = new Json.JArray(coordinatesList);

        //linkedHashMap permet de garantir que la map a bien le bon ordre qu'est l'ordre d'ajout
        Map<String, Json> orderedMap = new LinkedHashMap<>();
        orderedMap.put("type", new Json.JString("LineString"));
        orderedMap.put("coordinates", coordinatesJArray);

        return new Json.JObject(orderedMap);
    }
}
