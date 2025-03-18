package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BufferedTripsTest {
    private List<String> stringTable;
    private ByteBuffer buffer;
    private Trips trips;

    @BeforeEach
    void setUp() {
        // Create a string table with sample destination names
        stringTable = new ArrayList<>();
        stringTable.add("Lausanne");      // Index 0
        stringTable.add("Genève");        // Index 1
        stringTable.add("Fribourg");      // Index 2
        stringTable.add("Neuchâtel");     // Index 3
        stringTable.add("Bern");          // Index 4
        stringTable.add("Zürich");        // Index 5

        // Create a buffer with sample trip data
        // Each trip has 4 bytes: 2 bytes for route_id (U16) and 2 bytes for destination_id (U16)
        buffer = ByteBuffer.allocate(20); // 5 trips * 4 bytes each
        
        // Trip 0: Route 0, Destination "Lausanne"
        buffer.putShort((short) 0);  // route_id = 0
        buffer.putShort((short) 0);  // destination_id = 0 (Lausanne)
        
        // Trip 1: Route 1, Destination "Genève"
        buffer.putShort((short) 1);  // route_id = 1
        buffer.putShort((short) 1);  // destination_id = 1 (Genève)
        
        // Trip 2: Route 0, Destination "Fribourg"
        buffer.putShort((short) 0);  // route_id = 0
        buffer.putShort((short) 2);  // destination_id = 2 (Fribourg)
        
        // Trip 3: Route 2, Destination "Neuchâtel"
        buffer.putShort((short) 2);  // route_id = 2
        buffer.putShort((short) 3);  // destination_id = 3 (Neuchâtel)
        
        // Trip 4: Route 3, Destination "Bern"
        buffer.putShort((short) 3);  // route_id = 3
        buffer.putShort((short) 4);  // destination_id = 4 (Bern)
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedTrips instance
        trips = new BufferedTrips(stringTable, buffer);
    }

    @Test
    void sizeReturnsCorrectNumberOfTrips() {
        assertEquals(5, trips.size());
    }

    @Test
    void routeIdReturnsCorrectRouteId() {
        assertEquals(0, trips.routeId(0));
        assertEquals(1, trips.routeId(1));
        assertEquals(0, trips.routeId(2));
        assertEquals(2, trips.routeId(3));
        assertEquals(3, trips.routeId(4));
    }

    @Test
    void destinationReturnsCorrectDestinationName() {
        assertEquals("Lausanne", trips.destination(0));
        assertEquals("Genève", trips.destination(1));
        assertEquals("Fribourg", trips.destination(2));
        assertEquals("Neuchâtel", trips.destination(3));
        assertEquals("Bern", trips.destination(4));
    }

    @Test
    void routeIdThrowsExceptionForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(5));
    }

    @Test
    void destinationThrowsExceptionForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(5));
    }

    @Test
    void constructorHandlesEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Trips emptyTrips = new BufferedTrips(stringTable, emptyBuffer);
        assertEquals(0, emptyTrips.size());
    }

    @Test
    void constructorHandlesMaximumValidValues() {
        // Create a buffer with maximum valid values for U16 fields
        ByteBuffer maxValuesBuffer = ByteBuffer.allocate(4); // 1 trip * 4 bytes
        
        maxValuesBuffer.putShort((short) 0xFFFF);  // route_id = 65535 (max U16)
        maxValuesBuffer.putShort((short) 0xFFFF);  // destination_id = 65535 (max U16)
        
        maxValuesBuffer.flip();
        
        // Create a string table with an entry at index 65535
        List<String> largeStringTable = new ArrayList<>();
        for (int i = 0; i <= 0xFFFF; i++) {
            largeStringTable.add("Entry " + i);
        }
        
        Trips maxValueTrips = new BufferedTrips(largeStringTable, maxValuesBuffer);
        
        assertEquals(1, maxValueTrips.size());
        assertEquals(0xFFFF, maxValueTrips.routeId(0));
        assertEquals("Entry 65535", maxValueTrips.destination(0));
    }

    @Test
    void multipleTripsWithSameRouteId() {
        // Test that multiple trips can have the same route ID
        assertEquals(0, trips.routeId(0));
        assertEquals(0, trips.routeId(2));
        assertNotEquals(trips.destination(0), trips.destination(2));
    }
} 