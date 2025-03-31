package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

import java.util.List;



/**
 * Tests for the JourneyExtractor class using a real-world profile
 */
public class MyJourneyExtractorTest2 {
    
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
        
        // Verify the timetable structure
        System.out.println("Stations: " + timeTable.stations().size());
        System.out.println("Platforms: " + timeTable.platforms().size());
        
        // Read profile from file
        Profile profile = readProfile(timeTable, date, destinationStationId);
        
        // Verify profile structure
        System.out.println("Profile station fronts: " + profile.stationFront().size());
        
        // Extract journeys
        int departureStationId = 7872; // Ecublens VD, EPFL
        List<Journey> journeys = JourneyExtractor.journeys(profile, departureStationId);
        
        // Report journey count
        System.out.println("Number of journeys: " + journeys.size());
        
        // If we have enough journeys, check journey 32
        if (journeys.size() > 32) {
            Journey journey = journeys.get(32);
            
            // Print journey details
            System.out.println("Journey departure: " + journey.depStop().name());
            System.out.println("Journey arrival: " + journey.arrStop().name());
            
            // Convert to iCalendar format
            String icalEvent = JourneyIcalConverter.toIcalendar(journey);
            System.out.println("iCalendar event for journey 32:");
            System.out.println(icalEvent);
            

        } else {
            System.out.println("Not enough journeys to extract index 32");
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