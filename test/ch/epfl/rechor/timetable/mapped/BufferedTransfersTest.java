package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class BufferedTransfersTest {
    private ByteBuffer buffer;
    private Transfers transfers;

    @BeforeEach
    void setUp() {
        // Create a buffer with sample transfer data
        // Each transfer has 5 bytes: 
        // 2 bytes for dep_station_id (U16)
        // 2 bytes for arr_station_id (U16)
        // 1 byte for transfer_minutes (U8)
        buffer = ByteBuffer.allocate(35); // 7 transfers * 5 bytes each
        
        // IMPORTANT: In the constructor, BufferedTransfers processes transfers by arr_station_id
        // and uses the station ID as an index into arrivingAt array.
        // Therefore, we must order transfers by arr_station_id AND
        // ensure the arr_station_id values start from 0 and are consecutive.
        
        // Transfers to station 0
        // Transfer 0: Station 2 -> Station 0, 5 minutes
        buffer.putShort((short) 2);    // dep_station_id = 2
        buffer.putShort((short) 0);    // arr_station_id = 0 (was 1 before)
        buffer.put((byte) 5);          // transfer_minutes = 5
        
        // Transfer 1: Station 3 -> Station 0, 10 minutes
        buffer.putShort((short) 3);    // dep_station_id = 3
        buffer.putShort((short) 0);    // arr_station_id = 0 (was 1 before)
        buffer.put((byte) 10);         // transfer_minutes = 10
        
        // Transfers to station 1
        // Transfer 2: Station 0 -> Station 1, 5 minutes
        buffer.putShort((short) 0);    // dep_station_id = 0 (was 1 before)
        buffer.putShort((short) 1);    // arr_station_id = 1 (was 2 before)
        buffer.put((byte) 5);          // transfer_minutes = 5
        
        // Transfer 3: Station 3 -> Station 1, 8 minutes
        buffer.putShort((short) 3);    // dep_station_id = 3
        buffer.putShort((short) 1);    // arr_station_id = 1 (was 2 before)
        buffer.put((byte) 8);          // transfer_minutes = 8
        
        // Transfers to station 2
        // Transfer 4: Station 0 -> Station 2, 10 minutes
        buffer.putShort((short) 0);    // dep_station_id = 0 (was 1 before)
        buffer.putShort((short) 2);    // arr_station_id = 2 (was 3 before)
        buffer.put((byte) 10);         // transfer_minutes = 10
        
        // Transfer 5: Station 1 -> Station 2, 8 minutes
        buffer.putShort((short) 1);    // dep_station_id = 1 (was 2 before)
        buffer.putShort((short) 2);    // arr_station_id = 2 (was 3 before)
        buffer.put((byte) 8);          // transfer_minutes = 8
        
        // Transfer within the same station
        // Transfer 6: Station 3 -> Station 3, 3 minutes (internal transfer)
        buffer.putShort((short) 3);    // dep_station_id = 3 (was 4 before)
        buffer.putShort((short) 3);    // arr_station_id = 3 (was 4 before)
        buffer.put((byte) 3);          // transfer_minutes = 3
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedTransfers instance
        transfers = new BufferedTransfers(buffer);
    }

    @Test
    void sizeReturnsCorrectNumberOfTransfers() {
        assertEquals(7, transfers.size());
    }

    @Test
    void depStationIdReturnsCorrectDepartureStationId() {
        assertEquals(2, transfers.depStationId(0));
        assertEquals(3, transfers.depStationId(1));
        assertEquals(0, transfers.depStationId(2));
        assertEquals(3, transfers.depStationId(3));
        assertEquals(0, transfers.depStationId(4));
        assertEquals(1, transfers.depStationId(5));
        assertEquals(3, transfers.depStationId(6));
        assertEquals(3, transfers.depStationId(6));
    }

    @Test
    void minutesReturnsCorrectTransferDuration() {
        assertEquals(5, transfers.minutes(0));
        assertEquals(10, transfers.minutes(1));
        assertEquals(5, transfers.minutes(2));
        assertEquals(8, transfers.minutes(3));
        assertEquals(10, transfers.minutes(4));
        assertEquals(8, transfers.minutes(5));
        assertEquals(3, transfers.minutes(6));
    }

    @Test
    void arrivingAtReturnsCorrectPackedRange() {
        // Transfers to station 0 are at indices 0-1
        int range0 = transfers.arrivingAt(0);
        assertEquals(0, PackedRange.startInclusive(range0));
        assertEquals(2, PackedRange.endExclusive(range0));
        
        // Transfers to station 1 are at indices 2-3
        int range1 = transfers.arrivingAt(1);
        assertEquals(2, PackedRange.startInclusive(range1));
        assertEquals(4, PackedRange.endExclusive(range1));
        
        // Transfers to station 2 are at indices 4-5
        int range2 = transfers.arrivingAt(2);
        assertEquals(4, PackedRange.startInclusive(range2));
        assertEquals(6, PackedRange.endExclusive(range2));
        
        // Transfers to station 3 are at index 6
        int range3 = transfers.arrivingAt(3);
        assertEquals(6, PackedRange.startInclusive(range3));
        assertEquals(7, PackedRange.endExclusive(range3));
    }

    @Test
    void minutesBetweenReturnsCorrectDuration() {
        // Station 2 -> Station 0: 5 minutes
        assertEquals(5, transfers.minutesBetween(2, 0));
        
        // Station 3 -> Station 0: 10 minutes
        assertEquals(10, transfers.minutesBetween(3, 0));
        
        // Station 0 -> Station 1: 5 minutes
        assertEquals(5, transfers.minutesBetween(0, 1));
        
        // Station 3 -> Station 1: 8 minutes
        assertEquals(8, transfers.minutesBetween(3, 1));
        
        // Station 0 -> Station 2: 10 minutes
        assertEquals(10, transfers.minutesBetween(0, 2));
        
        // Station 1 -> Station 2: 8 minutes
        assertEquals(8, transfers.minutesBetween(1, 2));
        
        // Station 3 -> Station 3: 3 minutes (internal transfer)
        assertEquals(3, transfers.minutesBetween(3, 3));
    }

    @Test
    void minutesBetweenThrowsExceptionForNonExistentTransfer() {
        // No transfer from Station 0 to Station 3
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(0, 3));
        
        // No transfer from Station 3 to Station 0 to 1
        assertThrows(NoSuchElementException.class, () -> transfers.minutesBetween(1, 0));
    }

    @Test
    void methodsThrowExceptionForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.depStationId(7));
        
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.minutes(7));
        
        // Testing invalid station ID
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> transfers.arrivingAt(4)); // Only 0-3 are valid
    }

    @Test
    void constructorHandlesEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Transfers emptyTransfers = new BufferedTransfers(emptyBuffer);
        assertEquals(0, emptyTransfers.size());
    }

    @Test
    void constructorHandlesMaximumValidValues() {
        // Create a buffer with maximum valid values for U16 and U8 fields
        ByteBuffer maxValuesBuffer = ByteBuffer.allocate(5); // 1 transfer * 5 bytes
        
        maxValuesBuffer.putShort((short) 0xFFFF);  // dep_station_id = 65535 (max U16)
        maxValuesBuffer.putShort((short) 0);       // arr_station_id = 0 (must be valid index)
        maxValuesBuffer.put((byte) 0xFF);          // transfer_minutes = 255 (max U8)
        
        maxValuesBuffer.flip();
        
        Transfers maxValueTransfers = new BufferedTransfers(maxValuesBuffer);
        
        assertEquals(1, maxValueTransfers.size());
        assertEquals(0xFFFF, maxValueTransfers.depStationId(0));
        assertEquals(0xFF, maxValueTransfers.minutes(0));
    }

    @Test
    void handlesMultipleTransfersBetweenSameStations() {
        // Create a buffer with multiple transfers between the same stations
        ByteBuffer multipleTransfersBuffer = ByteBuffer.allocate(15); // 3 transfers * 5 bytes
        
        // Transfer 0: Station 1 -> Station 0, 5 minutes
        multipleTransfersBuffer.putShort((short) 1);
        multipleTransfersBuffer.putShort((short) 0);
        multipleTransfersBuffer.put((byte) 5);
        
        // Transfer 1: Station 1 -> Station 0, 10 minutes (alternate route)
        multipleTransfersBuffer.putShort((short) 1);
        multipleTransfersBuffer.putShort((short) 0);
        multipleTransfersBuffer.put((byte) 10);
        
        // Transfer 2: Station 1 -> Station 0, 15 minutes (another alternate route)
        multipleTransfersBuffer.putShort((short) 1);
        multipleTransfersBuffer.putShort((short) 0);
        multipleTransfersBuffer.put((byte) 15);
        
        multipleTransfersBuffer.flip();
        
        Transfers multipleTransfers = new BufferedTransfers(multipleTransfersBuffer);
        
        // The minutesBetween method should return the first matching transfer duration
        assertEquals(5, multipleTransfers.minutesBetween(1, 0));
        
        // The arrivingAt method should return a range that includes all transfers to station 0
        int range = multipleTransfers.arrivingAt(0);
        assertEquals(0, PackedRange.startInclusive(range));
        assertEquals(3, PackedRange.endExclusive(range));
    }
} 