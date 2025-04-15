package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MyStopIndexTest {

    private final List<String> stops = List.of(
            "Lausanne Gare",
            "Mézières VD",
            "Charleville-Mézières",
            "Villeneuve",
            "Morges"
    );

    private final Map<String, String> alternatives = Map.of(
            "Losanna", "Lausanne Gare",
            "Charleville", "Charleville-Mézières"
    );

    private final StopIndex index = new StopIndex(stops, alternatives);

    @Test
    void testExactMatch() {
        List<String> result = index.stopsMatching("Morges", 5);
        assertEquals(List.of("Morges"), result);
    }

    @Test
    void testMatchWithAccents() {
        List<String> result = index.stopsMatching("losa", 5);
        assertEquals(List.of("Lausanne Gare"), result);
    }

    @Test
    void testAccentInsensitive() {
        List<String> result = index.stopsMatching("Mezieres", 5);
        assertTrue(result.contains("Mézières VD"));
    }

    @Test
    void testAlternativeName() {
        List<String> result = index.stopsMatching("charleville", 5);
        assertEquals(List.of("Charleville-Mézières"), result);
    }

    @Test
    void testCaseInsensitive() {
        List<String> result = index.stopsMatching("lausanne", 5);
        assertEquals(List.of("Lausanne Gare"), result);
    }

    @Test
    void testMultipleSubwords() {
        List<String> result = index.stopsMatching("mez vil", 5);
        List<String> expected = List.of("Charleville-Mézières", "Villeneuve", "Mézières VD");
        assertTrue(result.containsAll(expected));
    }

    @Test
    void testLimit() {
        List<String> result = index.stopsMatching("e", 2);
        System.out.println(result);
        assertEquals(2, result.size());
    }

    @Test
    void testEmptyQuery() {
        List<String> result = index.stopsMatching("", 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testNullQuery() {
        List<String> result = index.stopsMatching(null, 5);
        assertTrue(result.isEmpty());
    }
}