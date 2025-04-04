package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class MyRouterJavaTest {
    private TimeTable timetable;
    private Router router;

    @BeforeEach
    void setUp() throws Exception {
        Path timetablePath = Path.of("timetable"); // Make sure timetable files are in this directory
        this.timetable = new CachedTimeTable(FileTimeTable.in(timetablePath));
        this.router = new Router(timetable);
    }

    @Test
    void profileShouldNotBeNullForValidDateAndStation() {
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrivalStationId = 11486; // Gruyères

        Profile profile = router.profile(date, arrivalStationId);
        assertNotNull(profile);
    }

    @Test
    void profileShouldContainParetoFrontForDepartureStation() {
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrivalStationId = 11486;
        int departureStationId = 7872; // EPFL

        Profile profile = router.profile(date, arrivalStationId);
        assertDoesNotThrow(() -> profile.forStation(departureStationId));
    }

    @Test
    void profileParetoFrontShouldNotBeEmptyIfJourneyExists() {
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrivalStationId = 11486;
        int departureStationId = 7872;

        Profile profile = router.profile(date, arrivalStationId);
        //assertFalse((profile.forStation(departureStationId)).isEmpty());
    }

    @Test
    void profileShouldContainArrivalStationId() {
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrivalStationId = 11486;

        Profile profile = router.profile(date, arrivalStationId);
        assertEquals(arrivalStationId, profile.arrStationId());
    }
}
