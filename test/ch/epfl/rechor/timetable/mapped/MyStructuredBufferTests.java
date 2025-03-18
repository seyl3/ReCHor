package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class MyStructuredBufferTests {
    @Test
    void structureShouldComputeOffsetsCorrectly() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16),
                Structure.field(1, Structure.FieldType.U16)
        );
        assertEquals(0, structure.offset(0, 0));
        assertEquals(2, structure.offset(1, 0));
        assertEquals(4, structure.offset(0, 1));
        assertEquals(6, structure.offset(1, 1));
    }

    @Test
    void fieldConstructorThrowsOnNullType() {
        assertThrows(NullPointerException.class, () -> new Structure.Field(0, null));
        assertThrows(NullPointerException.class, () -> new Structure.Field(0, null));
    }

    @Test
    void structureConstructorThrowsOnUnorderedFields() {
        assertThrows(IllegalArgumentException.class, () -> new Structure(
                new Structure.Field(1, Structure.FieldType.U8),
                new Structure.Field(0, Structure.FieldType.U16)
        ));
    }

    @Test
    void totalSizeComputesCorrectly() {
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16)
        );
        assertEquals(3, structure.totalSize());
    }

    @Test
    void offsetComputesCorrectly() {
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16)
        );
        assertEquals(7, structure.offset(1, 2)); // (2 * 3) + 1
    }

    @Test
    void offsetThrowsOnInvalidFieldIndex() {
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8)
        );
        assertThrows(IndexOutOfBoundsException.class, () -> structure.offset(1, 0));
    }
    @Test
    void structuredBufferConstructorThrowsIfBufferNotMultipleOfTotalSize() {
        ByteBuffer buffer = ByteBuffer.allocate(5); // Total size d'une structure correcte doit être un multiple de sa taille
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16)
        );

        assertThrows(IllegalArgumentException.class, () -> new StructuredBuffer(structure, buffer));
    }

    @Test
    void getU8ThrowsOnInvalidIndex() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16)
        );
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU8(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU8(2, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU8(1, 2)); // Dépasse la taille
    }

    @Test
    void getU16ThrowsOnInvalidIndex() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U8),
                new Structure.Field(1, Structure.FieldType.U16)
        );
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU16(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU16(2, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getU16(1, 2)); // Dépasse la taille
    }

    @Test
    void getS32ThrowsOnInvalidIndex() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.S32)
        );
        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getS32(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getS32(1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> structuredBuffer.getS32(0, 4)); // Dépasse la taille
    }
}

