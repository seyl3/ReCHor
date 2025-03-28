package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import ch.epfl.rechor.timetable.TimeTable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
            
            // Verify iCalendar formatting using assertions
            verifyIcalendarFormat(icalEvent, journey);
        } else {
            System.out.println("Not enough journeys to extract index 32");
        }
    }
    
    /**
     * Verify that the iCalendar format is correct for a journey
     */
    private void verifyIcalendarFormat(String icalContent, Journey journey) {
        var dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        var vcalendarContext = List.of("VCALENDAR");
        var veventContext = List.of("VCALENDAR", "VEVENT");

        var unfoldedActual = icalContent.replaceAll("\r\n ", "");
        var context = new ArrayList<String>();
        var requiredNames = new HashSet<>(
                Set.of("VERSION", "PRODID", "UID", "DTSTAMP", "DTSTART", "DTEND", "SUMMARY", "DESCRIPTION"));

        for (var line : unfoldedActual.split("\r\n")) {
            var colonLoc = line.indexOf(':');
            if (colonLoc <= 0) continue; // Skip invalid lines
            
            var name = line.substring(0, colonLoc);
            var value = line.substring(colonLoc + 1);
            requiredNames.remove(name);
            switch (name) {
                case "BEGIN" ->
                        context.addLast(value);
                case "END" -> {
                    assertFalse(context.isEmpty());
                    context.removeLast();
                }
                case "VERSION" -> {
                    assertEquals("2.0", value);
                    assertEquals(vcalendarContext, context);
                }
                case "PRODID" -> {
                    assertEquals("ReCHor", value);
                    assertEquals(vcalendarContext, context);
                }
                case "UID" -> {
                    assertFalse(value.isBlank());
                    assertEquals(veventContext, context);
                }
                case "DTSTAMP" -> {
                    var timeStamp = LocalDateTime.parse(value, dateFmt);
                    var elapsed = Duration.between(timeStamp, LocalDateTime.now()).abs();
                    assertTrue(elapsed.compareTo(Duration.ofHours(1)) < 0);
                    assertEquals(veventContext, context);
                }
                case "DTSTART" -> {
                    var timeStart = LocalDateTime.parse(value, dateFmt);
                    assertEquals(journey.depTime(), timeStart);
                    assertEquals(veventContext, context);
                }
                case "DTEND" -> {
                    var timeEnd = LocalDateTime.parse(value, dateFmt);
                    assertEquals(journey.arrTime(), timeEnd);
                    assertEquals(veventContext, context);
                }
                case "SUMMARY" -> {
                    assertEquals(journey.depStop().name() + " → " + journey.arrStop().name(), value);
                    assertEquals(veventContext, context);
                }
                case "DESCRIPTION" -> {
                    // Verify description contains journey legs info
                    for (Journey.Leg leg : journey.legs()) {
                        if (leg instanceof Journey.Leg.Transport t) {
                            assertTrue(value.contains(t.depStop().name()) || 
                                       value.contains(t.arrStop().name()),
                                       "Description should contain transport stations");
                        } else if (leg instanceof Journey.Leg.Foot) {
                            assertTrue(value.contains("trajet à pied") || 
                                       value.contains("changement"),
                                       "Description should mention foot travel");
                        }
                    }
                    assertEquals(veventContext, context);
                }
            }
        }
        assertEquals(Set.of(), requiredNames, "Missing required iCalendar fields");
    }
    
    /**
     * Helper method to read a profile from a file
     */
    private Profile readProfile(TimeTable timeTable, 
                                LocalDate date, 
                                int arrStationId) throws IOException {
        Path path = Path.of("test/ch/epfl/rechor/profile_" + date + "_" + arrStationId + ".txt");
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