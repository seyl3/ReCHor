package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.rechor.Bits32_24_8.pack;
import static ch.epfl.rechor.journey.PackedCriteria.pack;
import static ch.epfl.rechor.journey.PackedCriteria.withDepMins;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the JourneyExtractor class
 */
class JourneyExtractorTest {

    // Sample data for tests
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 3, 18);
    private static final int TEST_ARRIVAL_STATION = 1; // Station index 1 (destination)
    private static final int TEST_DEPARTURE_STATION = 0; // Station index 0 (origin)

    // Mock objects
    private MockTimeTable mockTimeTable;
    private Profile mockProfile;

    @BeforeEach
    void setUp() {
        // Create mock objects for testing
        mockTimeTable = new MockTimeTable();

        // Create a profile with the mock timetable
        mockProfile = createMockProfile(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
    }

    /**
     * Helper method to call the journeys method through reflection
     * since JourneyExtractor has a private constructor
     */
    private List<Journey> extractJourneys(Profile profile, int depStationId) throws Exception {
        // Get the journeys method
        Method journeysMethod = JourneyExtractor.class.getDeclaredMethod("journeys", Profile.class, int.class);
        journeysMethod.setAccessible(true);

        // Invoke the static method (null for static methods)
        return (List<Journey>) journeysMethod.invoke(null, profile, depStationId);
    }

    /**
     * Test extraction of journeys from a profile for a valid departure station
     */
    @Test
    void testJourneyExtractionForValidStation() throws Exception {
        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify we have some journeys
        assertFalse(journeys.isEmpty());

        // Verify journey properties
        Journey journey = journeys.get(0);
        assertEquals(TEST_DEPARTURE_STATION, mockTimeTable.stationId(journey.depStop().name()));
        assertEquals(TEST_ARRIVAL_STATION, mockTimeTable.stationId(journey.arrStop().name()));
    }

    /**
     * Test that journeys are sorted first by departure time, then by arrival time
     */
    @Test
    void testJourneysAreSortedCorrectly() throws Exception {
        // Create a profile with multiple journeys
        mockProfile = createProfileWithMultipleJourneys();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify sorting
        assertTrue(journeys.size() >= 2);

        for (int i = 0; i < journeys.size() - 1; i++) {
            Journey current = journeys.get(i);
            Journey next = journeys.get(i + 1);

            // First sorted by departure time
            if (current.depTime().equals(next.depTime())) {
                // Then by arrival time if departure times are equal
                assertTrue(current.arrTime().compareTo(next.arrTime()) <= 0);
            } else {
                // Departure times should be in ascending order
                assertTrue(current.depTime().compareTo(next.depTime()) < 0);
            }
        }
    }

    /**
     * Test that payload is correctly used to determine the first connection and stop count
     */
    @Test
    void testCorrectUseOfPayload() throws Exception {
        // Extract a journey
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Get the first journey
        assertFalse(journeys.isEmpty());
        Journey journey = journeys.get(0);

        // Get the first leg, which should be a transport
        assertTrue(journey.legs().get(0) instanceof Journey.Leg.Transport);
        Journey.Leg.Transport transportLeg = (Journey.Leg.Transport) journey.legs().get(0);

        // Verify connection and intermediate stops match what we expect
        // based on the payload in the mock ParetoFront
        assertEquals("Mock Station 0", transportLeg.depStop().name());
        assertEquals("Mock Station 1", transportLeg.arrStop().name());

        // The intermediate stops should match the number specified in the payload (1)
        assertEquals(1, transportLeg.intermediateStops().size());
    }

    /**
     * Test that journeys alternate correctly between vehicle connections and foot transfers
     */
    @Test
    void testJourneysAlternateCorrectly() throws Exception {
        // Create a profile with a multi-leg journey
        mockProfile = createProfileWithMultiLegJourney();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify alternating legs
        assertFalse(journeys.isEmpty());
        Journey journey = journeys.get(0);

        List<Journey.Leg> legs = journey.legs();
        assertTrue(legs.size() >= 2, "Journey should have multiple legs");

        for (int i = 0; i < legs.size() - 1; i++) {
            if (legs.get(i) instanceof Journey.Leg.Transport) {
                assertTrue(legs.get(i + 1) instanceof Journey.Leg.Foot,
                        "Transport should be followed by Foot");
            } else {
                assertTrue(legs.get(i + 1) instanceof Journey.Leg.Transport,
                        "Foot should be followed by Transport");
            }
        }
    }

    /**
     * Test that an empty Pareto frontier results in an empty list of journeys
     */
    @Test
    void testEmptyParetoFrontier() throws Exception {
        // Create a profile with an empty Pareto frontier
        Profile emptyProfile = createProfileWithEmptyParetoFront();

        // Extract journeys
        List<Journey> journeys = extractJourneys(emptyProfile, TEST_DEPARTURE_STATION);

        // Should be empty
        assertTrue(journeys.isEmpty());
    }

    /**
     * Test that an invalid departure station ID throws IndexOutOfBoundsException
     */
    @Test
    void testInvalidDepartureStationId() {
        // Try to extract journeys with invalid station ID
        assertThrows(Exception.class, () -> {
            extractJourneys(mockProfile, 999); // Station ID that doesn't exist
        });
    }

    /**
     * Test that journey properly handles when the first leg needs a foot transfer
     * because the departure platform doesn't match the first connection platform
     */
    @Test
    void testJourneyStartsWithFootTransfer() throws Exception {
        // Create a profile where dep platform != first connection platform
        mockProfile = createProfileWithDifferentDepartureAndConnectionPlatforms();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify first leg is a foot transfer
        assertFalse(journeys.isEmpty());
        Journey journey = journeys.get(0);

        // First leg should be a foot transfer
        assertTrue(journey.legs().get(0) instanceof Journey.Leg.Foot,
                "First leg should be a foot transfer");
    }

    /**
     * Test that journey properly handles when the last leg needs a foot transfer
     * because the arrival platform doesn't match the final destination station
     */
    @Test
    void testJourneyEndsWithFootTransfer() throws Exception {
        // Create a profile where final arrival platform != destination
        mockProfile = createProfileWithDifferentArrivalAndDestinationPlatforms();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify last leg is a foot transfer
        assertFalse(journeys.isEmpty());
        Journey journey = journeys.get(0);

        // Last leg should be a foot transfer
        assertTrue(journey.legs().get(journey.legs().size() - 1) instanceof Journey.Leg.Foot,
                "Last leg should be a foot transfer");
    }

    /**
     * Test a journey with only one connection and no transfers
     */
    @Test
    void testJourneyWithOneConnection() throws Exception {
        // Create a profile with a single connection journey
        mockProfile = createProfileWithSingleConnection();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify we have a journey with a single leg
        assertFalse(journeys.isEmpty());
        Journey journey = journeys.get(0);

        // Should have exactly one leg (Transport) if departure platform matches
        assertEquals(1, journey.legs().size());
        assertTrue(journey.legs().get(0) instanceof Journey.Leg.Transport);
    }

    /**
     * Test that the extractor correctly handles intermediate stops
     */
    @Test
    void testMaxIntermediateStops() throws Exception {
        // Create a profile with multiple intermediate stops
        mockProfile = createProfileWithMultipleIntermediateStops();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify intermediate stops
        assertFalse(journeys.isEmpty());
        Journey journey = journeys.get(0);

        // Get the transport leg
        Journey.Leg.Transport transportLeg = null;
        for (Journey.Leg leg : journey.legs()) {
            if (leg instanceof Journey.Leg.Transport) {
                transportLeg = (Journey.Leg.Transport) leg;
                break;
            }
        }

        assertNotNull(transportLeg);
        assertEquals(3, transportLeg.intermediateStops().size(),
                "Transport leg should have 3 intermediate stops");
    }

    /**
     * Test that Pareto entries with overlapping times but different number of changes
     * are all extracted correctly
     */
    @Test
    void testMultipleParetoEntriesWithOverlappingTimes() throws Exception {
        // Create a profile with multiple criteria entries (same dep/arr times but different changes)
        mockProfile = createProfileWithOverlappingTimesParetoFront();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify we have multiple journeys
        assertTrue(journeys.size() >= 2);

        // Check they have different number of changes (legs)
        Journey journey1 = journeys.get(0);
        Journey journey2 = journeys.get(1);

        // Verify journeys have same departure time but different number of legs
        assertEquals(journey1.depTime(), journey2.depTime());
        assertNotEquals(journey1.legs().size(), journey2.legs().size());
    }

    /**
     * Test that payloads referring to different trips from the same station
     * are handled independently and properly
     */
    @Test
    void testPayloadsDifferentTrips() throws Exception {
        // Create a profile with payloads referring to different trips
        mockProfile = createProfileWithDifferentTripPayloads();

        // Extract journeys
        List<Journey> journeys = extractJourneys(mockProfile, TEST_DEPARTURE_STATION);

        // Verify we have multiple journeys
        assertTrue(journeys.size() >= 2);

        // Get transport legs from each journey
        Journey.Leg.Transport transport1 = null;
        Journey.Leg.Transport transport2 = null;

        for (Journey.Leg leg : journeys.get(0).legs()) {
            if (leg instanceof Journey.Leg.Transport) {
                transport1 = (Journey.Leg.Transport) leg;
                break;
            }
        }

        for (Journey.Leg leg : journeys.get(1).legs()) {
            if (leg instanceof Journey.Leg.Transport) {
                transport2 = (Journey.Leg.Transport) leg;
                break;
            }
        }

        assertNotNull(transport1);
        assertNotNull(transport2);

        // Verify different trips (different routes or destinations)
        assertNotEquals(transport1.route(), transport2.route());
    }

    // Helper methods to create mock profiles for different test scenarios

    private Profile createMockProfile(TimeTable timeTable, LocalDate date, int arrStationId) {
        Profile.Builder builder = new Profile.Builder(timeTable, date, arrStationId);
        
        // For departure station, create a Pareto front with one entry
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        // Pack connection ID (0) and intermediate stops (1) into payload
        int payload = pack(0, 1);
        // Add with departure minutes (480 = 8:00 AM)
        long criteria = pack(540, 0, payload); // Arr 9:00, 0 changes, payload
        criteria = withDepMins(criteria, 480); // Add departure minutes (8:00 AM)
        frontBuilder.add(criteria);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithMultipleJourneys() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create multiple journey entries in the Pareto front for the departure station
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        
        // First journey: dep 8:00, arr 9:00
        long criteria1 = pack(540, 0, pack(0, 0));
        criteria1 = withDepMins(criteria1, 480);
        frontBuilder.add(criteria1);
        
        // Second journey: dep 8:00, arr 8:45 (same dep time, earlier arr)
        long criteria2 = pack(525, 1, pack(1, 0));
        criteria2 = withDepMins(criteria2, 480);
        frontBuilder.add(criteria2);
        
        // Third journey: dep 8:30, arr 9:30 (later dep time)
        long criteria3 = pack(570, 0, pack(2, 0));
        criteria3 = withDepMins(criteria3, 510);
        frontBuilder.add(criteria3);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithMultiLegJourney() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // For departure station, create a journey with multiple legs
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        long criteria1 = pack(540, 1, pack(0, 0)); // 1 change
        criteria1 = withDepMins(criteria1, 480);
        frontBuilder.add(criteria1);
        
        // For the intermediate station
        ParetoFront.Builder intermediateFrontBuilder = new ParetoFront.Builder();
        long criteria2 = pack(540, 0, pack(1, 0)); // 0 changes
        criteria2 = withDepMins(criteria2, 510); // Departure from intermediate at 8:30
        intermediateFrontBuilder.add(criteria2);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        builder.setForStation(1, intermediateFrontBuilder); // Station 1 as intermediate
        
        return builder.build();
    }

    private Profile createProfileWithEmptyParetoFront() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);

        // Leave the Pareto front empty for the departure station
        builder.setForStation(TEST_DEPARTURE_STATION, new ParetoFront.Builder());

        return builder.build();
    }

    private Profile createProfileWithDifferentDepartureAndConnectionPlatforms() {
        // For this test, we need to modify the mock TimeTable to return different platform IDs
        MockTimeTable timeTable = new MockTimeTable() {
            @Override
            public int stationId(int stopId) {
                // For this test, pretend station 0 has platform ID 5
                if (stopId == 5) return 0;
                return super.stationId(stopId);
            }
        };
        
        Profile.Builder builder = new Profile.Builder(timeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create a Pareto front where the first connection uses a different platform
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        
        // Connection uses platform 0 while departure is from platform 5
        long criteria = pack(540, 0, pack(0, 0));
        criteria = withDepMins(criteria, 480);
        frontBuilder.add(criteria);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithDifferentArrivalAndDestinationPlatforms() {
        // For this test, we need to modify the mock TimeTable to return different platform IDs
        MockTimeTable timeTable = new MockTimeTable() {
            @Override
            public int stationId(int stopId) {
                // For this test, pretend station 1 has platform ID 6
                if (stopId == 6) return 1;
                return super.stationId(stopId);
            }
        };
        
        Profile.Builder builder = new Profile.Builder(timeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create a Pareto front where the last connection arrives at a different platform
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        
        // Connection arrives at platform 6, destination is station 1
        long criteria = pack(540, 0, pack(0, 0));
        criteria = withDepMins(criteria, 480);
        frontBuilder.add(criteria);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithSingleConnection() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create a Pareto front with a single journey (no changes)
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        long criteria = pack(540, 0, pack(0, 0)); // No changes
        criteria = withDepMins(criteria, 480);
        frontBuilder.add(criteria);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithMultipleIntermediateStops() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create a journey with 3 intermediate stops
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        long criteria = pack(600, 0, pack(0, 3)); // 3 intermediate stops
        criteria = withDepMins(criteria, 540); // Depart at 9:00
        frontBuilder.add(criteria);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithOverlappingTimesParetoFront() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create Pareto entries with same times but different changes
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        
        // Journey 1: dep 8:00, arr 9:00, 0 changes
        long criteria1 = pack(540, 0, pack(0, 0));
        criteria1 = withDepMins(criteria1, 480);
        frontBuilder.add(criteria1);
        
        // Journey 2: dep 8:00, arr 9:00, 1 change (but perhaps more reliable or preferred)
        long criteria2 = pack(540, 1, pack(1, 0));
        criteria2 = withDepMins(criteria2, 480);
        frontBuilder.add(criteria2);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        // For the intermediate station (for the journey with 1 change)
        ParetoFront.Builder intermediateFrontBuilder = new ParetoFront.Builder();
        long criteria3 = pack(540, 0, pack(2, 0));
        criteria3 = withDepMins(criteria3, 510); // Departure from intermediate at 8:30
        intermediateFrontBuilder.add(criteria3);
        
        builder.setForStation(1, intermediateFrontBuilder);
        
        return builder.build();
    }

    private Profile createProfileWithDifferentTripPayloads() {
        Profile.Builder builder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create Pareto entries with different trip IDs in payload
        ParetoFront.Builder frontBuilder = new ParetoFront.Builder();
        
        // Journey 1: Uses trip ID 0
        long criteria1 = pack(540, 0, pack(0, 0));
        criteria1 = withDepMins(criteria1, 480);
        frontBuilder.add(criteria1);
        
        // Journey 2: Uses trip ID 1
        long criteria2 = pack(545, 0, pack(1, 0));
        criteria2 = withDepMins(criteria2, 485);
        frontBuilder.add(criteria2);
        
        builder.setForStation(TEST_DEPARTURE_STATION, frontBuilder);
        
        return builder.build();
    }

    // Mock classes for testing

    static class MockTimeTable implements TimeTable {
        private final MockStations stations = new MockStations();
        private final MockConnections connections = new MockConnections();
        private final MockTrips trips = new MockTrips();

        @Override
        public Stations stations() {
            return stations;
        }

        @Override
        public StationAliases stationAliases() {
            return new MockStationAliases();
        }

        @Override
        public Platforms platforms() {
            return new MockPlatforms();
        }

        @Override
        public Routes routes() {
            return new MockRoutes();
        }

        @Override
        public Transfers transfers() {
            return new MockTransfers();
        }

        @Override
        public Trips tripsFor(LocalDate date) {
            return trips;
        }

        @Override
        public Connections connectionsFor(LocalDate date) {
            return connections;
        }

        public int stationId(String name) {
            // Convert station name to ID (for test verification)
            if (name.equals("Mock Station 0")) return 0;
            if (name.equals("Mock Station 1")) return 1;
            if (name.equals("Mock Station 2")) return 2;
            return -1;
        }

        public int stationId(int stopId) {
            return stopId % 3;
        }
    }

    static class MockStations implements Stations {
        @Override
        public int size() {
            return 3; // 3 stations
        }

        @Override
        public double latitude(int index) {
            return 46.5 + index * 0.1;
        }

        @Override
        public double longitude(int index) {
            return 6.5 + index * 0.1;
        }

        @Override
        public String name(int index) {
            return "Mock Station " + index;
        }
    }

    static class MockStationAliases implements StationAliases {
        @Override
        public String alias(int id) {
            return "Alias " + id;
        }

        @Override
        public String stationName(int id) {
            return "Mock Station " + id;
        }

        @Override
        public int size() {
            return 3;
        }
    }

    static class MockPlatforms implements Platforms {
        @Override
        public String name(int id) {
            return "Platform " + id;
        }

        @Override
        public int stationId(int id) {
            return id % 3; // Map platform to one of 3 stations
        }

        @Override
        public int size() {
            return 6; // 6 platforms
        }
    }

    static class MockRoutes implements Routes {
        @Override
        public Vehicle vehicle(int id) {
            return Vehicle.TRAIN;
        }

        @Override
        public String name(int id) {
            return "Route " + id;
        }

        @Override
        public int size() {
            return 3;
        }
    }

    static class MockTransfers implements Transfers {
        @Override
        public int depStationId(int id) {
            return id % 3;
        }

        @Override
        public int minutes(int id) {
            return 5;
        }

        @Override
        public int arrivingAt(int stationId) {
            return stationId;
        }

        @Override
        public int minutesBetween(int depStationId, int arrStationId) {
            if (depStationId == arrStationId) {
                return 0;
            } else if (depStationId < 0 || arrStationId < 0 || depStationId >= 3 || arrStationId >= 3) {
                throw new IndexOutOfBoundsException();
            }
            return 5;
        }

        @Override
        public int size() {
            return 9; // 3*3 station combinations
        }
    }

    static class MockTrips implements Trips {
        @Override
        public int routeId(int id) {
            return id % 3;
        }

        @Override
        public String destination(int id) {
            return "Destination " + id;
        }

        @Override
        public int size() {
            return 3;
        }
    }

    static class MockConnections implements Connections {
        @Override
        public int depStopId(int id) {
            return id % 3;
        }

        @Override
        public int depMins(int id) {
            return 480 + id * 10; // Starting at 8:00 AM
        }

        @Override
        public int arrStopId(int id) {
            // Instead of cycling connections, ensure they move forward only
            // This prevents cycles by ensuring connections always go from lower to higher station IDs
            int startStation = depStopId(id);
            // Always move forward to prevent cycles (0->1, 1->2, 2->final)
            if (startStation == 0) return 1;
            if (startStation == 1) return 2;
            return 0; // Default case, should rarely be used in well-structured tests
        }

        @Override
        public int arrMins(int id) {
            // Ensure arrival is always after departure 
            return depMins(id) + 15 + (id % 5); // Add variable offset to make times unique
        }

        @Override
        public int tripId(int id) {
            return id % 3;
        }

        @Override
        public int tripPos(int id) {
            return id % 5;
        }

        @Override
        public int nextConnectionId(int id) {
            // To prevent infinite sequences of connections, we limit the recursion depth
            // Only return a next connection if the ID is small enough
            if (id < 3) { // Limit recursion depth
                return id + 1;
            }
            // No more connections
            return id; // Return the same ID to indicate we're at the end
        }

        @Override
        public int size() {
            return 10;
        }
    }
} 