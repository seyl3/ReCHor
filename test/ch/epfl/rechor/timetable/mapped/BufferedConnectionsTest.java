package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;

class BufferedConnectionsTest {
    private ByteBuffer buffer;
    private ByteBuffer succBuffer;
    private Connections connections;

    @BeforeEach
    void setUp() {
        // Create a buffer with sample connection data
        // Each connection has 12 bytes: 
        // 2 bytes for dep_stop_id (U16)
        // 2 bytes for dep_minutes (U16)
        // 2 bytes for arr_stop_id (U16)
        // 2 bytes for arr_minutes (U16)
        // 4 bytes for trip_pos_id (S32)
        buffer = ByteBuffer.allocate(48); // 4 connections * 12 bytes each
        
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
        
        // Connection 3: Stop 50 -> Stop 60, 600 -> 630 minutes, Trip 200 Position 0
        buffer.putShort((short) 50);    // dep_stop_id = 50
        buffer.putShort((short) 600);   // dep_minutes = 600 (10:00)
        buffer.putShort((short) 60);    // arr_stop_id = 60
        buffer.putShort((short) 630);   // arr_minutes = 630 (10:30)
        buffer.putInt(Bits32_24_8.pack(200, 0)); // trip_id = 200, pos = 0
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create a buffer with next connection data
        // Each entry is 4 bytes (S32) and represents the index of the next connection
        succBuffer = ByteBuffer.allocate(16); // 4 connections * 4 bytes each
        
        // Connection 0 -> Connection 1
        succBuffer.putInt(1);
        
        // Connection 1 -> Connection 2
        succBuffer.putInt(2);
        
        // Connection 2 -> Connection 0 (circular for trip 100)
        succBuffer.putInt(0);
        
        // Connection 3 -> Connection 3 (circular for trip 200, only one connection)
        succBuffer.putInt(3);
        
        succBuffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedConnections instance
        connections = new BufferedConnections(buffer, succBuffer);
    }

    @Test
    void sizeReturnsCorrectNumberOfConnections() {
        assertEquals(4, connections.size());
    }

    @Test
    void depStopIdReturnsCorrectDepartureStopId() {
        assertEquals(10, connections.depStopId(0));
        assertEquals(20, connections.depStopId(1));
        assertEquals(30, connections.depStopId(2));
        assertEquals(50, connections.depStopId(3));
    }

    @Test
    void depMinsReturnsCorrectDepartureMinutes() {
        assertEquals(480, connections.depMins(0));
        assertEquals(500, connections.depMins(1));
        assertEquals(525, connections.depMins(2));
        assertEquals(600, connections.depMins(3));
    }

    @Test
    void arrStopIdReturnsCorrectArrivalStopId() {
        assertEquals(20, connections.arrStopId(0));
        assertEquals(30, connections.arrStopId(1));
        assertEquals(40, connections.arrStopId(2));
        assertEquals(60, connections.arrStopId(3));
    }

    @Test
    void arrMinsReturnsCorrectArrivalMinutes() {
        assertEquals(495, connections.arrMins(0));
        assertEquals(520, connections.arrMins(1));
        assertEquals(540, connections.arrMins(2));
        assertEquals(630, connections.arrMins(3));
    }

    @Test
    void tripIdReturnsCorrectTripId() {
        assertEquals(100, connections.tripId(0));
        assertEquals(100, connections.tripId(1));
        assertEquals(100, connections.tripId(2));
        assertEquals(200, connections.tripId(3));
    }

    @Test
    void tripPosReturnsCorrectPosition() {
        assertEquals(0, connections.tripPos(0));
        assertEquals(1, connections.tripPos(1));
        assertEquals(2, connections.tripPos(2));
        assertEquals(0, connections.tripPos(3));
    }

    @Test
    void nextConnectionIdReturnsCorrectNextConnectionId() {
        assertEquals(1, connections.nextConnectionId(0));
        assertEquals(2, connections.nextConnectionId(1));
        assertEquals(0, connections.nextConnectionId(2)); // Circular reference back to first connection
        assertEquals(3, connections.nextConnectionId(3)); // Self-reference for single connection trip
    }

    @Test
    void methodsThrowExceptionForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depStopId(4));
        
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depMins(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.depMins(4));
        
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrStopId(4));
        
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrMins(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.arrMins(4));
        
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripId(4));
        
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripPos(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.tripPos(4));
        
        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> connections.nextConnectionId(4));
    }

    @Test
    void constructorHandlesEmptyBuffers() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        ByteBuffer emptySuccBuffer = ByteBuffer.allocate(0);
        Connections emptyConnections = new BufferedConnections(emptyBuffer, emptySuccBuffer);
        assertEquals(0, emptyConnections.size());
    }

    @Test
    void constructorHandlesMaximumValidValues() {
        // Create a buffer with maximum valid values for U16 fields
        ByteBuffer maxValuesBuffer = ByteBuffer.allocate(12); // 1 connection * 12 bytes
        
        maxValuesBuffer.putShort((short) 0xFFFF);  // dep_stop_id = 65535 (max U16)
        maxValuesBuffer.putShort((short) 0xFFFF);  // dep_minutes = 65535 (max U16)
        maxValuesBuffer.putShort((short) 0xFFFF);  // arr_stop_id = 65535 (max U16)
        maxValuesBuffer.putShort((short) 0xFFFF);  // arr_minutes = 65535 (max U16)
        maxValuesBuffer.putInt(Bits32_24_8.pack(0xFFFFFF, 0xFF)); // trip_id = 16777215 (max 24 bits), pos = 255 (max 8 bits)
        
        maxValuesBuffer.flip();
        
        // Create a succBuffer with maximum valid value
        ByteBuffer maxSuccBuffer = ByteBuffer.allocate(4); // 1 connection * 4 bytes
        maxSuccBuffer.putInt(Integer.MAX_VALUE);
        maxSuccBuffer.flip();
        
        Connections maxValueConnections = new BufferedConnections(maxValuesBuffer, maxSuccBuffer);
        
        assertEquals(1, maxValueConnections.size());
        assertEquals(0xFFFF, maxValueConnections.depStopId(0));
        assertEquals(0xFFFF, maxValueConnections.depMins(0));
        assertEquals(0xFFFF, maxValueConnections.arrStopId(0));
        assertEquals(0xFFFF, maxValueConnections.arrMins(0));
        assertEquals(0xFFFFFF, maxValueConnections.tripId(0));
        assertEquals(0xFF, maxValueConnections.tripPos(0));
        assertEquals(Integer.MAX_VALUE, maxValueConnections.nextConnectionId(0));
    }
} 