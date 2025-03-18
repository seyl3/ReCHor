package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.Routes;
import ch.epfl.rechor.timetable.Transfers;
import ch.epfl.rechor.timetable.Trips;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BufferedRoutes, BufferedTrips, BufferedConnections, and BufferedTransfers classes.
 * These tests verify both basic functionality and edge cases for each class.
 */
public class MyConnectionsTripsTransfersRoutesTest {

    // ==================== BufferedRoutes Tests ====================

    @Test
    void bufferedRoutesBasicFunctionality() {
        // Create a string table with sample route names
        List<String> stringTable = new ArrayList<>();
        stringTable.add("M1");       // Index 0
        stringTable.add("M2");       // Index 1
        stringTable.add("TL 1");     // Index 2
        stringTable.add("S1");       // Index 3

        // Create a buffer with sample route data
        // Each route has 3 bytes: 2 bytes for name_id (U16) and 1 byte for kind (U8)
        ByteBuffer buffer = ByteBuffer.allocate(12); // 4 routes * 3 bytes each
        
        // Route 0: M1, METRO (1)
        buffer.putShort((short) 0);  // name_id = 0 (M1)
        buffer.put((byte) 1);        // kind = 1 (METRO)
        
        // Route 1: M2, METRO (1)
        buffer.putShort((short) 1);  // name_id = 1 (M2)
        buffer.put((byte) 1);        // kind = 1 (METRO)
        
        // Route 2: TL 1, TRAM (0)
        buffer.putShort((short) 2);  // name_id = 2 (TL 1)
        buffer.put((byte) 0);        // kind = 0 (TRAM)
        
        // Route 3: S1, TRAIN (2)
        buffer.putShort((short) 3);  // name_id = 3 (S1)
        buffer.put((byte) 2);        // kind = 2 (TRAIN)
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedRoutes instance
        Routes routes = new BufferedRoutes(stringTable, buffer);
        
        // Test size
        assertEquals(4, routes.size());
        
        // Test vehicle types
        assertEquals(Vehicle.METRO, routes.vehicle(0));
        assertEquals(Vehicle.METRO, routes.vehicle(1));
        assertEquals(Vehicle.TRAM, routes.vehicle(2));
        assertEquals(Vehicle.TRAIN, routes.vehicle(3));
        
        // Test route names
        assertEquals("M1", routes.name(0));
        assertEquals("M2", routes.name(1));
        assertEquals("TL 1", routes.name(2));
        assertEquals("S1", routes.name(3));
        
        // Test invalid indices
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(4));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(4));
    }

    @Test
    void bufferedRoutesEmptyBuffer() {
        List<String> stringTable = new ArrayList<>();
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Routes emptyRoutes = new BufferedRoutes(stringTable, emptyBuffer);
        assertEquals(0, emptyRoutes.size());
    }

    // ==================== BufferedTrips Tests ====================

    @Test
    void bufferedTripsBasicFunctionality() {
        // Create a string table with sample destination names
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Lausanne");      // Index 0
        stringTable.add("Genève");        // Index 1
        stringTable.add("Fribourg");      // Index 2

        // Create a buffer with sample trip data
        // Each trip has 4 bytes: 2 bytes for route_id (U16) and 2 bytes for destination_id (U16)
        ByteBuffer buffer = ByteBuffer.allocate(12); // 3 trips * 4 bytes each
        
        // Trip 0: Route 0, Destination "Lausanne"
        buffer.putShort((short) 0);  // route_id = 0
        buffer.putShort((short) 0);  // destination_id = 0 (Lausanne)
        
        // Trip 1: Route 1, Destination "Genève"
        buffer.putShort((short) 1);  // route_id = 1
        buffer.putShort((short) 1);  // destination_id = 1 (Genève)
        
        // Trip 2: Route 0, Destination "Fribourg"
        buffer.putShort((short) 0);  // route_id = 0
        buffer.putShort((short) 2);  // destination_id = 2 (Fribourg)
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedTrips instance
        Trips trips = new BufferedTrips(stringTable, buffer);
        
        // Test size
        assertEquals(3, trips.size());
        
        // Test route IDs
        assertEquals(0, trips.routeId(0));
        assertEquals(1, trips.routeId(1));
        assertEquals(0, trips.routeId(2));
        
        // Test destinations
        assertEquals("Lausanne", trips.destination(0));
        assertEquals("Genève", trips.destination(1));
        assertEquals("Fribourg", trips.destination(2));
        
        // Test invalid indices
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.routeId(3));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> trips.destination(3));
    }

    @Test
    void bufferedTripsEmptyBuffer() {
        List<String> stringTable = new ArrayList<>();
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Trips emptyTrips = new BufferedTrips(stringTable, emptyBuffer);
        assertEquals(0, emptyTrips.size());
    }

    // ==================== BufferedConnections Tests ====================

    @Test
    void bufferedConnectionsBasicFunctionality() {
        // Create a buffer with sample connection data
        // Each connection has 12 bytes: 
        // 2 bytes for dep_stop_id (U16)
        // 2 bytes for dep_minutes (U16)
        // 2 bytes for arr_stop_id (U16)
        // 2 bytes for arr_minutes (U16)
        // 4 bytes for trip_pos_id (S32)
        ByteBuffer buffer = ByteBuffer.allocate(36); // 3 connections * 12 bytes each
        
        // Connection 0: Stop 10 -> Stop 20, 480 -> 495 minutes, Trip 100 Position 0
        buffer.putShort((short) 10);    // dep_stop_id = 10
        buffer.putShort((short) 480);   // dep_minutes = 480 (8:00)
        buffer.putShort((short) 20);    // arr_stop_id = 20
        buffer.putShort((short) 495);   // arr_minutes = 495 (8:15)
        buffer.putInt(Bits32_24_8.pack(100, 0)); // trip_id = 100, pos = 0
        
        // Connection 1: Stop 20 -> Stop 30, 500 -> 520 minutes, Trip 100 Position 1
        buffer.putShort((short) 20);    // dep_stop_id = 20
        buffer.putShort((short) 500);   // dep_minutes = 500 (8:20)
        buffer.putShort((short) 30);    // arr_stop_id = 30
        buffer.putShort((short) 520);   // arr_minutes = 520 (8:40)
        buffer.putInt(Bits32_24_8.pack(100, 1)); // trip_id = 100, pos = 1
        
        // Connection 2: Stop 30 -> Stop 40, 525 -> 540 minutes, Trip 100 Position 2
        buffer.putShort((short) 30);    // dep_stop_id = 30
        buffer.putShort((short) 525);   // dep_minutes = 525 (8:45)
        buffer.putShort((short) 40);    // arr_stop_id = 40
        buffer.putShort((short) 540);   // arr_minutes = 540 (9:00)
        buffer.putInt(Bits32_24_8.pack(100, 2)); // trip_id = 100, pos = 2
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create a buffer with next connection data
        // Each entry is 4 bytes (S32) and represents the index of the next connection
        ByteBuffer succBuffer = ByteBuffer.allocate(12); // 3 connections * 4 bytes each
        
        // Connection 0 -> Connection 1
        succBuffer.putInt(1);
        
        // Connection 1 -> Connection 2
        succBuffer.putInt(2);
        
        // Connection 2 -> Connection 0 (circular for trip 100)
        succBuffer.putInt(0);
        
        succBuffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedConnections instance
        Connections connections = new BufferedConnections(buffer, succBuffer);
        
        // Test size
        assertEquals(3, connections.size());
        
        // Test departure stop IDs
        assertEquals(10, connections.depStopId(0));
        assertEquals(20, connections.depStopId(1));
        assertEquals(30, connections.depStopId(2));
        
        // Test departure minutes
        assertEquals(480, connections.depMins(0));
        assertEquals(500, connections.depMins(1));
        assertEquals(525, connections.depMins(2));
        
        // Test arrival stop IDs
        assertEquals(20, connections.arrStopId(0));
        assertEquals(30, connections.arrStopId(1));
        assertEquals(40, connections.arrStopId(2));
        
        // Test arrival minutes
        assertEquals(495, connections.arrMins(0));
        assertEquals(520, connections.arrMins(1));
        assertEquals(540, connections.arrMins(2));
        
        // Test trip IDs
        assertEquals(100, connections.tripId(0));
        assertEquals(100, connections.tripId(1));
        assertEquals(100, connections.tripId(2));
        
        // Test trip positions
        assertEquals(0, connections.tripPos(0));
        assertEquals(1, connections.tripPos(1));
        assertEquals(2, connections.tripPos(2));
        
        // Test next connection IDs
        assertEquals(1, connections.nextConnectionId(0));
        assertEquals(2, connections.nextConnectionId(1));
        assertEquals(0, connections.nextConnectionId(2)); // Circular reference back to first connection
        
        // Test invalid indices
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(3));
    }

    @Test
    void bufferedConnectionsEmptyBuffers() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        ByteBuffer emptySuccBuffer = ByteBuffer.allocate(0);
        Connections emptyConnections = new BufferedConnections(emptyBuffer, emptySuccBuffer);
        assertEquals(0, emptyConnections.size());
    }

    // ==================== BufferedTransfers Tests ====================

    @Test
    void bufferedTransfersBasicFunctionality() {
        // Create a buffer with sample transfer data
        // Each transfer has 5 bytes: 
        // 2 bytes for dep_station_id (U16)
        // 2 bytes for arr_station_id (U16)
        // 1 byte for transfer_minutes (U8)
        ByteBuffer buffer = ByteBuffer.allocate(20); // 4 transfers * 5 bytes each
        
        // Transfers to station 1
        // Transfer 0: Station 2 -> Station 1, 5 minutes
        buffer.putShort((short) 2);    // dep_station_id = 2
        buffer.putShort((short) 1);    // arr_station_id = 1
        buffer.put((byte) 5);          // transfer_minutes = 5
        
        // Transfer 1: Station 3 -> Station 1, 10 minutes
        buffer.putShort((short) 3);    // dep_station_id = 3
        buffer.putShort((short) 1);    // arr_station_id = 1
        buffer.put((byte) 10);         // transfer_minutes = 10
        
        // Transfers to station 2
        // Transfer 2: Station 1 -> Station 2, 5 minutes
        buffer.putShort((short) 1);    // dep_station_id = 1
        buffer.putShort((short) 2);    // arr_station_id = 2
        buffer.put((byte) 5);          // transfer_minutes = 5
        
        // Transfer 3: Station 3 -> Station 2, 8 minutes
        buffer.putShort((short) 3);    // dep_station_id = 3
        buffer.putShort((short) 2);    // arr_station_id = 2
        buffer.put((byte) 8);          // transfer_minutes = 8
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedTransfers instance
        Transfers transfers = new BufferedTransfers(buffer);
        
        // Test size
        assertEquals(4, transfers.size());
        
        // Test departure station IDs
        assertEquals(2, transfers.depStationId(0));
        assertEquals(3, transfers.depStationId(1));
        assertEquals(1, transfers.depStationId(2));
        assertEquals(3, transfers.depStationId(3));
        
        // Test transfer minutes
        assertEquals(5, transfers.minutes(0));
        assertEquals(10, transfers.minutes(1));
        assertEquals(5, transfers.minutes(2));
        assertEquals(8, transfers.minutes(3));
        
        // Test arrivingAt
        int range1 = transfers.arrivingAt(1);
        assertEquals(0, PackedRange.startInclusive(range1));
        assertEquals(2, PackedRange.endExclusive(range1));
        
        int range2 = transfers.arrivingAt(2);
        assertEquals(2, PackedRange.startInclusive(range2));
        assertEquals(4, PackedRange.endExclusive(range2));
        
        // Test minutesBetween
        assertEquals(5, transfers.minutesBetween(2, 1));
        assertEquals(10, transfers.minutesBetween(3, 1));
        assertEquals(5, transfers.minutesBetween(1, 2));
        assertEquals(8, transfers.minutesBetween(3, 2));
        
        // Test minutesBetween for valid stations with no transfer between them
        // For example, station 1 has no transfer to station 1 in our test data
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(1, 1));
        
        // Test minutesBetween for non-existent transfers
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(5, 1)); // Station 5 is valid but has no transfer to station 1
        
        // Test minutesBetween for invalid station IDs
        // According to the interface, invalid indices should cause IndexOutOfBoundsException
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutesBetween(1, 3)); // Station 3 doesn't exist as arrival station
        
        // Test invalid indices
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(4));
    }

    @Test
    void bufferedTransfersEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Transfers emptyTransfers = new BufferedTransfers(emptyBuffer);
        assertEquals(0, emptyTransfers.size());
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void bufferedRoutesMaxValues() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Test Route");
        
        ByteBuffer buffer = ByteBuffer.allocate(3); // 1 route * 3 bytes
        buffer.putShort((short) 0);  // name_id = 0
        buffer.put((byte) 6);        // kind = 6 (FUNICULAR, max valid value)
        buffer.flip();
        
        Routes routes = new BufferedRoutes(stringTable, buffer);
        assertEquals(Vehicle.FUNICULAR, routes.vehicle(0));
    }

    @Test
    void bufferedTripsMaxValues() {
        List<String> stringTable = new ArrayList<>();
        stringTable.add("Test Destination");
        
        ByteBuffer buffer = ByteBuffer.allocate(4); // 1 trip * 4 bytes
        buffer.putShort((short) 0xFFFF);  // route_id = 65535 (max U16)
        buffer.putShort((short) 0);       // destination_id = 0
        buffer.flip();
        
        Trips trips = new BufferedTrips(stringTable, buffer);
        assertEquals(0xFFFF, trips.routeId(0));
    }

    @Test
    void bufferedConnectionsMaxValues() {
        ByteBuffer buffer = ByteBuffer.allocate(12); // 1 connection * 12 bytes
        buffer.putShort((short) 0xFFFF);  // dep_stop_id = 65535 (max U16)
        buffer.putShort((short) 0xFFFF);  // dep_minutes = 65535 (max U16)
        buffer.putShort((short) 0xFFFF);  // arr_stop_id = 65535 (max U16)
        buffer.putShort((short) 0xFFFF);  // arr_minutes = 65535 (max U16)
        int packedValue = Bits32_24_8.pack(0xFFFFFF, 0xFF); // trip_id = 16777215 (max 24 bits), pos = 255 (max 8 bits)
        buffer.putInt(packedValue); 
        buffer.flip();
        
        ByteBuffer succBuffer = ByteBuffer.allocate(4); // 1 connection * 4 bytes
        succBuffer.putInt(0);
        succBuffer.flip();
        
        Connections connections = new BufferedConnections(buffer, succBuffer);
        assertEquals(0xFFFF, connections.depStopId(0));
        assertEquals(0xFFFF, connections.depMins(0));
        assertEquals(0xFFFF, connections.arrStopId(0));
        assertEquals(0xFFFF, connections.arrMins(0));
        
        // The tripId method returns the entire packed value without unpacking
        assertEquals(0xFFFFFF, connections.tripId(0));
        // The tripPos method correctly extracts the position
        assertEquals(0xFF, connections.tripPos(0));
    }

    @Test
    void bufferedTransfersMaxValues() {
        ByteBuffer buffer = ByteBuffer.allocate(5); // 1 transfer * 5 bytes
        buffer.putShort((short) 0xFFFF);  // dep_station_id = 65535 (max U16)
        buffer.putShort((short) 0xFFFF);  // arr_station_id = 65535 (max U16)
        buffer.put((byte) 0xFF);          // transfer_minutes = 255 (max U8)
        buffer.flip();
        
        Transfers transfers = new BufferedTransfers(buffer);
        assertEquals(0xFFFF, transfers.depStationId(0));
        assertEquals(0xFF, transfers.minutes(0));
    }
} 