package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.HashMap;
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

        List<Json> coordinatePairs = journey.legs().stream()
                .<Stop>mapMulti((leg, sink) -> {
                    sink.accept(leg.depStop());
                    leg.intermediateStops().forEach(s -> sink.accept(s.stop()));
                    sink.accept(leg.arrStop());
                })
                .map(JourneyGeoJsonConverter::createJArray)
                .distinct()
                .toList();

        Map<String, Json> orderedMap = new HashMap<>();
        orderedMap.put("type", new Json.JString("LineString"));
        orderedMap.put("coordinates", new Json.JArray(coordinatePairs));

        return new Json.JObject(orderedMap);
    }

    // Ajouter commentaire qui dit "méthode intermédiaire ggnngng"
    private static Json.JNumber roundToJnumber(double number) {
        return new Json.JNumber((Math.round(number * 1e5) / 1e5));
    }

    // Ajouter commentaire qui dit "méthode intermédiaire ggnngng"
    private static Json createJArray(Stop stop) {
        Json.JNumber lon = roundToJnumber(stop.longitude());
        Json.JNumber lat = roundToJnumber(stop.latitude());
        return new Json.JArray(List.of(lon, lat));
    }
}
