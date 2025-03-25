package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.FileTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the JourneyExtractor class using a real-world profile
 */
public class JourneyExtractorTest2 {
    
    /**
     * Test loading a profile from file and extracting journeys
     */
    @Test
    public void testExtractJourneysFromFile() throws IOException {
        // Load timetable from file
        TimeTable timeTable = FileTimeTable.in(Path.of("timetable"));
        
        // Set date and destination station
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int destinationStationId = 11486; // Gruyères
        
        // Read profile from file
        Profile profile = readProfile(timeTable, date, destinationStationId);
        
        // Extract journeys
        int departureStationId = 7872; // Ecublens VD, EPFL
        List<Journey> journeys = JourneyExtractor.journeys(profile, departureStationId);
        
        // Verify we have journeys
        assertFalse(journeys.isEmpty(), "Should have extracted journeys");
        
        // Check if we have enough journeys to access index 32
        if (journeys.size() > 32) {
            // Convert journey to iCalendar
            Journey journey = journeys.get(32);
            assertNotNull(journey, "Journey at index 32 should not be null");
            
            String icalEvent = JourneyIcalConverter.toIcalendar(journey);
            
            // Verify iCalendar event structure
            assertNotNull(icalEvent, "iCalendar event should not be null");
            
            // Verify key elements are present
            assertTrue(icalEvent.contains("BEGIN:VCALENDAR"), "Should have calendar begin tag");
            assertTrue(icalEvent.contains("VERSION:2.0"), "Should have version");
            assertTrue(icalEvent.contains("BEGIN:VEVENT"), "Should have event begin tag");
            assertTrue(icalEvent.contains("SUMMARY:"), "Should have summary");
            assertTrue(icalEvent.contains("Ecublens VD, EPFL"), "Should mention departure station");
            assertTrue(icalEvent.contains("Gruyères"), "Should mention arrival station");
            
            // Print the iCalendar event for manual verification
            System.out.println("iCalendar event for journey 32:");
            System.out.println(icalEvent);
        }
    }
    
    /**
     * Helper method to read a profile from a file
     */
    private Profile readProfile(TimeTable timeTable, 
                                LocalDate date, 
                                int arrStationId) throws IOException {
        Path path = Path.of("profile_" + date + "_" + arrStationId + ".txt");
        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB = new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(","))
                    frontB.add(Long.parseLong(t, 16));
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }
} 