package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.StationAliases;
import ch.epfl.rechor.timetable.Platforms;
import ch.epfl.rechor.timetable.Routes;
import ch.epfl.rechor.timetable.Transfers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Profile and Profile.Builder classes
 */
class MyProfileAndProfileBuilderTest {
    
    private static final LocalDate TEST_DATE = LocalDate.of(2025, 3, 18);
    private static final int TEST_ARRIVAL_STATION = 11486; // Gruyères
    private static final int TEST_DEPARTURE_STATION = 7872; // EPFL
    
    private TimeTable mockTimeTable;
    private Profile.Builder profileBuilder;
    private Profile profile;
    
    /**
     * Setup method to create mock objects for each test
     */
    @BeforeEach
    void setup() {
        // Create a mock TimeTable
        mockTimeTable = new MockTimeTable();
        
        // Create a profile builder with the mock timetable
        profileBuilder = new Profile.Builder(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION);
        
        // Create some ParetoFront builders
        ParetoFront.Builder station0Builder = new ParetoFront.Builder();
        station0Builder.add(480, 1, 123); // 8:00, 1 change, payload 123
        station0Builder.add(400, 2, 456); // 9:00, 2 changes, payload 456
        
        ParetoFront.Builder station1Builder = new ParetoFront.Builder();
        station1Builder.add(510, 0, 789); // 8:30, 0 changes, payload 789
        
        // Set the ParetoFront builders in the profile builder
        profileBuilder.setForStation(0, station0Builder);
        profileBuilder.setForStation(1, station1Builder);
        
        // Build the profile
        profile = profileBuilder.build();
    }
    
    /**
     * Test the profile builder construction
     */
    @Test
    void profileBuilderConstructionTest() {
        assertNotNull(profileBuilder);
        assertEquals(TEST_DATE, profileBuilder.date);
        assertEquals(TEST_ARRIVAL_STATION, profileBuilder.arrStationId);
        assertEquals(mockTimeTable, profileBuilder.timeTable);
        assertNotNull(profileBuilder.stationFrontBuilders);
        assertNotNull(profileBuilder.tripsFrontBuilders);
    }
    
    /**
     * Test that forStation gets the correct builder
     */
    @Test
    void forStationReturnsCorrectBuilderTest() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(600, 3, 999);
        profileBuilder.setForStation(2, builder);
        
        assertSame(builder, profileBuilder.forStation(2));
    }
    
    /**
     * Test that forStation returns null for a station without a builder
     */
    @Test
    void forStationReturnsNullForUnsetStationTest() {
        assertNull(profileBuilder.forStation(3));
    }
    
    /**
     * Test that forStation throws IndexOutOfBoundsException for invalid index
     */
    @Test
    void forStationThrowsExceptionForInvalidIndexTest() {
        assertThrows(IndexOutOfBoundsException.class, () -> profileBuilder.forStation(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> profileBuilder.forStation(10));
    }
    
    /**
     * Test that setForStation correctly sets a builder
     */
    @Test
    void setForStationWorksCorrectlyTest() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(600, 3, 999);
        
        profileBuilder.setForStation(3, builder);
        assertSame(builder, profileBuilder.forStation(3));
    }
    
    /**
     * Test that setForStation throws IndexOutOfBoundsException for invalid index
     */
    @Test
    void setForStationThrowsExceptionForInvalidIndexTest() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        
        assertThrows(IndexOutOfBoundsException.class, () -> profileBuilder.setForStation(-1, builder));
        assertThrows(IndexOutOfBoundsException.class, () -> profileBuilder.setForStation(10, builder));
    }
    
    /**
     * Test that forTrip gets the correct builder
     */
    @Test
    void forTripReturnsCorrectBuilderTest() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        builder.add(600, 3, 999);
        profileBuilder.setForTrip(1, builder);
        
        // Note: The current implementation has a bug in forTrip - it accesses stationFrontBuilders instead of tripsFrontBuilders
        // This test will fail until that bug is fixed
        assertSame(builder, profileBuilder.forTrip(1));
    }
    
    /**
     * Test that setForTrip correctly sets a builder
     */
    @Test
    void setForTripWorksCorrectlyTest() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        profileBuilder.setForTrip(1, builder);
        
        // This requires accessing the tripsFrontBuilders array directly for verification
        assertSame(builder, profileBuilder.tripsFrontBuilders[1]);
    }
    
    /**
     * Test that build() correctly constructs a profile
     */
    @Test
    void buildCreatesCorrectProfileTest() {
        Profile builtProfile = profileBuilder.build();
        
        assertNotNull(builtProfile);
        assertEquals(TEST_DATE, builtProfile.date());
        assertEquals(TEST_ARRIVAL_STATION, builtProfile.arrStationId());
        assertEquals(mockTimeTable, builtProfile.timeTable());
        
        // Check that the ParetoFronts were correctly built
        assertNotEquals(0, builtProfile.forStation(0).size());
        assertNotEquals(0, builtProfile.forStation(1).size());
        assertEquals(0, builtProfile.forStation(2).size()); // Should be empty
    }
    
    /**
     * Test that build() uses ParetoFront.EMPTY for null builders
     */
    @Test
    void buildUsesEmptyFrontForNullBuildersTest() {
        Profile builtProfile = profileBuilder.build();
        
        // Station 2 has no builder set, so should use EMPTY
        assertSame(ParetoFront.EMPTY, builtProfile.forStation(2));
    }
    
    /**
     * Test Profile's connections() method
     */
    @Test
    void connectionsReturnsConnectionsForDateTest() {
        Connections connections = profile.connections();
        assertNotNull(connections);
        // The mock TimeTable returns a non-null value
    }
    
    /**
     * Test Profile's trips() method
     */
    @Test
    void tripsReturnsTripsForDateTest() {
        Trips trips = profile.trips();
        assertNotNull(trips);
        // The mock TimeTable returns a non-null value
    }
    
    /**
     * Test Profile's forStation() method
     */
    @Test
    void forStationReturnsCorrectParetoFrontTest() {
        ParetoFront front0 = profile.forStation(0);
        assertNotNull(front0);
        assertEquals(2, front0.size());
        
        ParetoFront front1 = profile.forStation(1);
        assertNotNull(front1);
        assertEquals(1, front1.size());
    }
    
    /**
     * Test that forStation throws IndexOutOfBoundsException for invalid index
     */
    @Test
    void profileForStationThrowsExceptionForInvalidIndexTest() {
        assertThrows(IndexOutOfBoundsException.class, () -> profile.forStation(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> profile.forStation(10));
    }

    /**
     * Test that Profile constructor makes a defensive copy of the station fronts list
     */
    @Test
    void profileConstructorMakesDefensiveCopyTest() {
        List<ParetoFront> fronts = new ArrayList<>();
        fronts.add(ParetoFront.EMPTY);
        fronts.add(ParetoFront.EMPTY);
        
        Profile defensiveProfile = new Profile(mockTimeTable, TEST_DATE, TEST_ARRIVAL_STATION, fronts);
        
        // Modify the original list
        fronts.add(ParetoFront.EMPTY);
        
        // The profile's list should not be affected
        assertEquals(2, defensiveProfile.stationFront().size());
    }
    
    /**
     * Test with edge cases - empty ParetoFront builders
     */
    @Test
    void emptyParetoFrontBuildersTest() {
        ParetoFront.Builder emptyBuilder = new ParetoFront.Builder();
        profileBuilder.setForStation(2, emptyBuilder);
        
        Profile builtProfile = profileBuilder.build();
        ParetoFront front = builtProfile.forStation(2);
        
        assertNotNull(front);
        assertEquals(0, front.size());
    }
    
    /**
     * Test that you can update a builder after getting it with forStation
     */
    @Test
    void updateBuilderAfterGetTest() {
        ParetoFront.Builder builder = new ParetoFront.Builder();
        profileBuilder.setForStation(2, builder);
        
        // Get the builder and update it
        ParetoFront.Builder retrievedBuilder = profileBuilder.forStation(2);
        retrievedBuilder.add(630, 2, 888);
        
        // Build the profile and verify
        Profile builtProfile = profileBuilder.build();
        ParetoFront front = builtProfile.forStation(2);
        
        assertEquals(1, front.size());
        // The builder should have been updated
    }

    /**
     * Mock implementation of TimeTable for testing
     */
    private static class MockTimeTable implements TimeTable {
        @Override
        public Stations stations() {
            return new MockStations();
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
            return new MockTrips();
        }

        @Override
        public Connections connectionsFor(LocalDate date) {
            return new MockConnections();
        }
    }
    
    /**
     * Mock implementation of Stations for testing
     */
    private static class MockStations implements Stations {
        @Override
        public int size() {
            return 5;
        }

        @Override
        public double latitude(int index) {
            return 0;
        }

        @Override
        public double longitude(int index) {
            return 0;
        }

        @Override
        public String name(int index) {
            return "Mock Station " + index;
        }
    }
    
    /**
     * Mock implementation of StationAliases for testing
     */
    private static class MockStationAliases implements StationAliases {
        @Override
        public int size() {
            return 3;
        }
        
        @Override
        public String alias(int id) {
            return "Alias " + id;
        }
        
        @Override
        public String stationName(int id) {
            return "Station for alias " + id;
        }
    }
    
    /**
     * Mock implementation of Platforms for testing
     */
    private static class MockPlatforms implements Platforms {
        @Override
        public int size() {
            return 8;
        }
        
        @Override
        public String name(int id) {
            return "Platform " + id;
        }
        
        @Override
        public int stationId(int id) {
            return id % 5; // Maps platform to one of 5 stations
        }
    }
    
    /**
     * Mock implementation of Routes for testing
     */
    private static class MockRoutes implements Routes {
        @Override
        public int size() {
            return 2;
        }
        
        @Override
        public Vehicle vehicle(int id) {
            return Vehicle.TRAIN;
        }
        
        @Override
        public String name(int id) {
            return "Route " + id;
        }
    }
    
    /**
     * Mock implementation of Transfers for testing
     */
    private static class MockTransfers implements Transfers {
        @Override
        public int size() {
            return 10;
        }
        
        @Override
        public int depStationId(int id) {
            return id % 5;
        }
        
        @Override
        public int minutes(int id) {
            return 5 + id;
        }
        
        @Override
        public int arrivingAt(int stationId) {
            return stationId; // Simplified for testing
        }
        
        @Override
        public int minutesBetween(int depStationId, int arrStationId) {
            if (depStationId == arrStationId) {
                return 0;
            } else if (depStationId < 0 || arrStationId < 0 || depStationId >= 5 || arrStationId >= 5) {
                throw new IndexOutOfBoundsException();
            } else if (depStationId > arrStationId) {
                throw new NoSuchElementException();
            }
            return 5 + Math.abs(depStationId - arrStationId);
        }
    }
    
    /**
     * Mock implementation of Trips for testing
     */
    private static class MockTrips implements Trips {
        @Override
        public int size() {
            return 3;
        }

        @Override
        public int routeId(int index) {
            return index % 2;
        }
        
        @Override
        public String destination(int id) {
            return "Destination " + id;
        }
    }
    
    /**
     * Mock implementation of Connections for testing
     */
    private static class MockConnections implements Connections {
        @Override
        public int size() {
            return 10;
        }

        @Override
        public int depStopId(int id) {
            return id % 5;
        }
        
        @Override
        public int depMins(int id) {
            return 480 + id * 10;
        }

        @Override
        public int arrStopId(int id) {
            return (id + 1) % 5;
        }
        
        @Override
        public int arrMins(int id) {
            return 490 + id * 10;
        }

        @Override
        public int tripId(int id) {
            return id % 3;
        }
        
        @Override
        public int tripPos(int id) {
            return id % 4;
        }
        
        @Override
        public int nextConnectionId(int id) {
            return (id + 1) % 10;
        }
    }
} 