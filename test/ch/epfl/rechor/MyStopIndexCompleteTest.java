package ch.epfl.rechor;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class MyStopIndexCompleteTest {
    private static StopIndex stopIndex;

    @BeforeAll
    static void setUp() throws IOException {

        Path timetablePath = Path.of("timetable_20");
        TimeTable tt = FileTimeTable.in(timetablePath);

        List<String> stopNames = IntStream.range(0, tt.stations().size())
                .mapToObj(i -> tt.stations().name(i))
                .toList();

        Map<String, String> alternativeNames = new HashMap<>();
        for (int i = 0; i < tt.stationAliases().size(); i++) {
            String alias = tt.stationAliases().alias(i);
            String stationName = tt.stationAliases().stationName(i);
            alternativeNames.put(alias, stationName);
        }
        stopIndex = new StopIndex(stopNames, alternativeNames);
    }

    @Test
    void testExactMatch() {
        List<String> result = stopIndex.stopsMatching("Lausanne", 1);
        assertTrue(result.contains("Lausanne"));
    }

    @Test
    void testAlternativeNameLosannaReturnsLausanne() {
        List<String> results = stopIndex.stopsMatching("Losanna", 5);
        assertTrue(results.contains("Lausanne"));
    }

    @Test
    void testPartialMatch() {
        List<String> result = stopIndex.stopsMatching("Morg", 5);
        assertTrue(result.stream().anyMatch(name -> name.contains("Morges")));
    }

    @Test
    void testCaseInsensitive() {
        List<String> result = stopIndex.stopsMatching("villeneuve", 5);
        assertTrue(result.contains("Villeneuve VD"));
    }

    @Test
    void testAccentInsensitive() {
        List<String> result = stopIndex.stopsMatching("Mezieres", 5);
        assertTrue(result.stream().anyMatch(name -> name.contains("Mézières")));
    }

    @Test
    void testEmptyQuery() {
        List<String> result = stopIndex.stopsMatching("", 5);
        assertFalse(result.isEmpty());
    }

    @Test
    void testNullQuery() {
        assertThrows(NullPointerException.class, () -> stopIndex.stopsMatching(null, 5));
    }
}
