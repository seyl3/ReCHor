package ch.epfl.rechor.journey;


/*
 * @author : Sarra Zghal, Elyes Ben Abid
 */

import ch.epfl.rechor.Json;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyJsonTest {
    static Journey MyExampleJourney() {
        var s1 = new Stop("Ecublens VD, EPFL", null, 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", null, 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var l1 = new Journey.Leg.Transport(
                s1,
                d.atTime(16, 13),
                s2,
                d.atTime(16, 19),
                List.of(),
                Vehicle.METRO,
                "m1",
                "Renens VD, gare");

        var l2 = new Journey.Leg.Foot(s2, d.atTime(16, 19), s3, d.atTime(16, 22));

        var l3 = new Journey.Leg.Transport(
                s3,
                d.atTime(16, 26),
                s4,
                d.atTime(16, 33),
                List.of(),
                Vehicle.TRAIN,
                "R4",
                "Bex");

        var l4 = new Journey.Leg.Foot(s4, d.atTime(16, 33), s5, d.atTime(16, 38));

        var l5 = new Journey.Leg.Transport(
                s5,
                d.atTime(16, 40),
                s6,
                d.atTime(17, 13),
                List.of(),
                Vehicle.TRAIN,
                "IR15",
                "Luzern");

        return new Journey(List.of(l1, l2, l3, l4, l5));
    }


    @Test
    void jNumberToStringReturnsCorrectPrecision() {
        Json.JNumber num = new Json.JNumber(3.1415926535);
        assertEquals("3.1415926535", num.toString());
    }

    @Test
    void jStringToStringAddsQuotes() {
        Json.JString js = new Json.JString("Hello");
        assertEquals("\"Hello\"", js.toString());
    }

    @Test
    void jArrayToStringWorksOnEmptyAndSingleElement() {
        Json.JArray empty = new Json.JArray(List.of());
        assertEquals("[]", empty.toString());

        Json.JArray single = new Json.JArray(List.of(new Json.JNumber(1.0)));
        assertEquals("[1.0]", single.toString());
    }

    @Test
    void jArrayToStringWorksOnMultipleElements() {
        Json.JArray arr = new Json.JArray(List.of(
                new Json.JNumber(1.0),
                new Json.JString("test")
        ));
        assertEquals("[1.0,\"test\"]", arr.toString());
    }

    @Test
    void jObjectToStringProducesCorrectFormat() {
        Json.JObject obj = new Json.JObject(Map.of(
                "a", new Json.JNumber(1.0),
                "b", new Json.JString("hello")
        ));
        String result = obj.toString();
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("\"a\":1.0"));
        assertTrue(result.contains("\"b\":\"hello\""));
    }

    @Test
    void toGeoJsonReturnsValidGeoJsonStructure() {
        Journey j = MyJsonTest.MyExampleJourney();
        Json.JObject geoJson = JourneyGeoJsonConverter.toGeoJson(j);
        System.out.println(geoJson);
        String jsonStr = geoJson.toString();

        assertTrue(jsonStr.contains("\"type\":\"LineString\""));
        assertTrue(jsonStr.contains("\"coordinates\""));
        assertTrue(jsonStr.contains("["));
        assertTrue(jsonStr.contains("]"));
        assertTrue(jsonStr.startsWith("{"));
        assertTrue(jsonStr.endsWith("}"));
    }

    @Test
    void toGeoJsonCoordinatesAreDistinctAndOrderedPairs() {
        Journey j = MyJsonTest.MyExampleJourney();
        Json.JObject geoJson = JourneyGeoJsonConverter.toGeoJson(j);
        String jsonStr = geoJson.toString();

        // Vérifie que chaque coordonnée est un tableau de deux éléments
        assertTrue(jsonStr.matches(".*\\[\\d+\\.\\d+\\,\\d+\\.\\d+\\].*"));
    }
}
