package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;
import ch.epfl.rechor.timetable.mapped.Structure.Field;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class MyStrcutureTests {

    /*--------------------------- STRUCTURE TESTS ---------------------------*/

    @Test
    void testStructure_CorrectFieldOrder() {
        assertDoesNotThrow(() -> new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.S32)
        ));
    }

    @Test
    void testStructure_IncorrectFieldOrder_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                Structure.field(1, Structure.FieldType.U16),  // Index should start at 0
                Structure.field(2, Structure.FieldType.S32)
        ));
    }

    @Test
    void testStructure_TotalSizeCalculation() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        assertEquals(7, structure.totalSize()); // 1 + 2 + 4 = 7
    }

    @Test
    void testStructure_OffsetCalculation() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.S32),
                Structure.field(2, Structure.FieldType.S32)
        );

        assertEquals(0, structure.offset(0, 0));
        assertEquals(2, structure.offset(1, 0));
        assertEquals(6, structure.offset(2, 0));
    }

    @Test
    void testStructure_OffsetOutOfBounds_ThrowsException() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.S32)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(3, 0));
    }

    /*---------------------- STRUCTURED BUFFER TESTS ----------------------*/

    @Test
    void testStructuredBuffer_CorrectInitialization() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(3 * structure.totalSize());

        assertDoesNotThrow(() -> new StructuredBuffer(structure, buffer));
    }

    @Test
    void testStructuredBuffer_InvalidBufferSize_ThrowsException() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(5); // Not a multiple of structure size

        assertThrows(IllegalArgumentException.class, () -> new StructuredBuffer(structure, buffer));
    }

    @Test
    void testStructuredBuffer_SizeComputation() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(6); // 2 elements

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(2, structuredBuffer.size());
    }

    @Test
    void testStructuredBuffer_GetU8_CorrectValue() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());
        buffer.put(0, (byte) 42);

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(42, structuredBuffer.getU8(0, 0));
    }

    @Test
    void testStructuredBuffer_GetU16_CorrectValue() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());
        buffer.putShort(0, (short) 54321);

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(54321, structuredBuffer.getU16(0, 0));
    }

    @Test
    void testStructuredBuffer_GetS32_CorrectValue() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());
        buffer.putInt(0, -123456789);

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(-123456789, structuredBuffer.getS32(0, 0));
    }

    @Test
    void testStructuredBuffer_AccessOutOfBounds_ThrowsException() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU8(1, 0));
    }

    @Test
    void testStructuredBuffer_LargeDataHandling() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize() * 1000); // Large buffer

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(1000, structuredBuffer.size());
    }

    @Test
    void testStructuredBuffer_MultipleFieldAccess() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());

        buffer.putShort(0, (short) 1234);
        buffer.putInt(2, 567890);

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(1234, structuredBuffer.getU16(0, 0));
        assertEquals(567890, structuredBuffer.getS32(1, 0));
    }

    @Test
    void testStructuredBuffer_NegativeValuesForS32() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());

        buffer.putInt(0, -987654321);
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(-987654321, structuredBuffer.getS32(0, 0));
    }

    @Test
    void testStructuredBuffer_MaxValues() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8),
                Structure.field(1, Structure.FieldType.U16),
                Structure.field(2, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(structure.totalSize());

        buffer.put(0, (byte) 255);
        buffer.putShort(1, (short) 65535);
        buffer.putInt(3, Integer.MAX_VALUE);

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(255, structuredBuffer.getU8(0, 0));
        assertEquals(65535, structuredBuffer.getU16(1, 0));
        assertEquals(Integer.MAX_VALUE, structuredBuffer.getS32(2, 0));
    }
}