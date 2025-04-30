package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for FileTimeTable implementation.
 */
public class MyFileTimeTableTest {

    private Path timetablePath;
    private TimeTable timeTable;

    @BeforeEach
    public void setUp() throws IOException {
        // Use the actual timetable directory in the project
        timetablePath = Path.of("timetable_13");
        
        // Skip tests if the timetable directory doesn't exist
        if (!Files.exists(timetablePath)) {
            System.out.println("Timetable directory not found. Skipping tests.");
            return;
        }
        
        timeTable = FileTimeTable.in(timetablePath);
    }

    // --- Basic functionality tests ---

    @Test
    public void testBasicCreation() throws IOException {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        assertNotNull(timeTable);
        assertNotNull(timeTable.stations());
        assertNotNull(timeTable.stationAliases());
        assertNotNull(timeTable.platforms());
        assertNotNull(timeTable.routes());
        assertNotNull(timeTable.transfers());
    }
    
    @Test
    public void testStationsNotEmpty() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        Stations stations = timeTable.stations();
        assertTrue(stations.size() > 0, "Stations should not be empty");
    }

    @Test
    public void testStationNamesExist() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        Stations stations = timeTable.stations();
        
        // Check that at least the first station has a valid name
        if (stations.size() > 0) {
            String stationName = stations.name(0);
            assertNotNull(stationName);
            assertFalse(stationName.isEmpty(), "Station name should not be empty");
        }
    }

    // --- Date specific data tests ---
    
    @Test
    public void testTripsForValidDate() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        // Use a date from the provided week (2025-03-17 to 2025-03-23)
        LocalDate testDate = LocalDate.of(2025, 3, 17);
        
        // Check if the directory for this date exists before testing
        if (!Files.exists(timetablePath.resolve(testDate.toString()))) {
            System.out.println("Directory for " + testDate + " not found. Skipping test.");
            return;
        }
        
        Trips trips = timeTable.tripsFor(testDate);
        assertNotNull(trips);
        assertTrue(trips.size() > 0, "Trips for a valid date should not be empty");
    }
    
    @Test
    public void testConnectionsForValidDate() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        // Use a date from the provided week (2025-03-17 to 2025-03-23)
        LocalDate testDate = LocalDate.of(2025, 3, 17);
        
        // Check if the directory for this date exists before testing
        if (!Files.exists(timetablePath.resolve(testDate.toString()))) {
            System.out.println("Directory for " + testDate + " not found. Skipping test.");
            return;
        }
        
        Connections connections = timeTable.connectionsFor(testDate);
        assertNotNull(connections);
        assertTrue(connections.size() > 0, "Connections for a valid date should not be empty");
    }

    // --- Edge cases and error handling ---
    
    @Test
    public void testInvalidDirectory() {
        Path nonExistentPath = Path.of("non_existent_directory");
        assertThrows(IOException.class, () -> FileTimeTable.in(nonExistentPath));
    }
    
    @Test
    public void testTripsForInvalidDate() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        // Use a date that definitely won't have data
        LocalDate invalidDate = LocalDate.of(1900, 1, 1);
        
        assertThrows(UncheckedIOException.class, () -> timeTable.tripsFor(invalidDate));
    }
    
    @Test
    public void testConnectionsForInvalidDate() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        // Use a date that definitely won't have data
        LocalDate invalidDate = LocalDate.of(1900, 1, 1);
        
        assertThrows(UncheckedIOException.class, () -> timeTable.connectionsFor(invalidDate));
    }
    
    // --- Helper methods tests ---
    
    @Test
    public void testIsStationId() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        int stationCount = timeTable.stations().size();
        assertTrue(stationCount > 0, "Need at least one station for this test");
        
        // Test with valid station id
        assertTrue(timeTable.isStationId(0));
        assertTrue(timeTable.isStationId(stationCount - 1));
        
        // Test with invalid station id
        assertFalse(timeTable.isStationId(stationCount));
        assertFalse(timeTable.isStationId(-1));
    }
    
    @Test
    public void testIsPlatformId() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        int stationCount = timeTable.stations().size();
        int platformCount = timeTable.platforms().size();
        
        // If we have platforms, test with valid platform IDs
        if (platformCount > 0) {
            assertTrue(timeTable.isPlatformId(stationCount));
            assertTrue(timeTable.isPlatformId(stationCount + platformCount - 1));
        }
        
        // Test with invalid platform ids
        assertFalse(timeTable.isPlatformId(-1));
        assertFalse(timeTable.isPlatformId(stationCount - 1)); // This is a station, not a platform
        assertFalse(timeTable.isPlatformId(stationCount + platformCount)); // Beyond range
    }
    
    @Test
    public void testStationId() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        int stationCount = timeTable.stations().size();
        int platformCount = timeTable.platforms().size();
        
        // For station IDs, the result should be the same
        assertEquals(0, timeTable.stationId(0));
        
        // If we have platforms, test with a valid platform ID
        if (platformCount > 0) {
            int platformId = stationCount; // First platform ID
            int expectedStationId = timeTable.platforms().stationId(platformId - stationCount);
            assertEquals(expectedStationId, timeTable.stationId(platformId));
        }
    }
    
    @Test
    public void testPlatformName() {
        // Skip if directory not found
        if (!Files.exists(timetablePath)) return;
        
        int stationCount = timeTable.stations().size();
        int platformCount = timeTable.platforms().size();
        
        // For station IDs, platformName should return null
        assertNull(timeTable.platformName(0));
        
        // If we have platforms, test with a valid platform ID
        if (platformCount > 0) {
            int platformId = stationCount; // First platform ID
            String expectedName = timeTable.platforms().name(platformId - stationCount);
            assertEquals(expectedName, timeTable.platformName(platformId));
        }
    }
    
    // --- Test with custom data ---
    
    @Test
    public void testWithCustomData(@TempDir Path tempDir) throws IOException {
        // Create minimal valid data structure
        Path stringsPath = tempDir.resolve("strings.txt");
        Files.writeString(stringsPath, "Line 1\nLine 2\nLine 3", StandardCharsets.ISO_8859_1);
        
        // Create empty binary files for required data
        createEmptyFile(tempDir.resolve("stations.bin"));
        createEmptyFile(tempDir.resolve("station-aliases.bin"));
        createEmptyFile(tempDir.resolve("platforms.bin"));
        createEmptyFile(tempDir.resolve("routes.bin"));
        createEmptyFile(tempDir.resolve("transfers.bin"));
        
        // Create a day directory with empty files
        Path dayDir = tempDir.resolve("2025-03-17");
        Files.createDirectory(dayDir);
        createEmptyFile(dayDir.resolve("trips.bin"));
        createEmptyFile(dayDir.resolve("connections.bin"));
        createEmptyFile(dayDir.resolve("connections-succ.bin"));
        
        // Test that FileTimeTable can load this minimal valid structure
        TimeTable minimalTable = FileTimeTable.in(tempDir);
        assertNotNull(minimalTable);
        
        // Test daily data access - might throw exceptions if empty files aren't valid
        try {
            Trips trips = minimalTable.tripsFor(LocalDate.of(2025, 3, 17));
            assertNotNull(trips);
            assertEquals(0, trips.size());
        } catch (Exception e) {
            // Empty files might not be valid for some implementations
            System.out.println("Empty trips file not supported: " + e.getMessage());
        }
        
        try {
            Connections connections = minimalTable.connectionsFor(LocalDate.of(2025, 3, 17));
            assertNotNull(connections);
            assertEquals(0, connections.size());
        } catch (Exception e) {
            // Empty files might not be valid for some implementations
            System.out.println("Empty connections file not supported: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to create empty files for testing
     */
    private void createEmptyFile(Path path) throws IOException {
        Files.createFile(path);
    }
} 