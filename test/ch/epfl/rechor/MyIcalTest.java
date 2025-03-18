package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MyIcalTest {


    @Test
    void testBuildWithoutEndThrowsException() {
        IcalBuilder ical = new IcalBuilder();
        ical.begin(IcalBuilder.Component.VCALENDAR);
        assertThrows(IllegalArgumentException.class, ical::build);
    }

    @Test
    void testEndWithoutBeginThrowsException() {
        IcalBuilder ical = new IcalBuilder();
        assertThrows(IllegalArgumentException.class, ical::end);
    }

    @Test
    void testNestedComponents() {
        IcalBuilder ical = new IcalBuilder();
        ical.begin(IcalBuilder.Component.VCALENDAR);
        ical.end();
        ical.begin(IcalBuilder.Component.VEVENT);
        ical.end();
        assertDoesNotThrow(ical::build);
    }


    @Test
    void testNullValues() {
        IcalBuilder ical = new IcalBuilder();
        String nullString = null;
        LocalDateTime nullDateTime = null;

        assertThrows(IllegalArgumentException.class, () -> ical.add(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> ical.add(IcalBuilder.Name.DESCRIPTION, nullString));
        assertThrows(IllegalArgumentException.class, () -> ical.add(null, nullDateTime));
    }
}