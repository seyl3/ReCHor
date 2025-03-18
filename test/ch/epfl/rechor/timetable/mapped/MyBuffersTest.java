package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyBuffersTest {
    private static final HexFormat hexFormat = HexFormat.ofDelimiter(" ");

    @Test
    void structuredBufferConstructorThrowsOnInvalidSize() {
        Structure structure = new Structure(
                new Structure.Field(0, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{0x00}); // Not a multiple of 2
        assertThrows(IllegalArgumentException.class, () -> new StructuredBuffer(structure, buffer));
    }

    @Test
    void constructorThrowsOnNullArguments() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        List<String> stringTable = List.of("Station1", "Station2");

        assertThrows(NullPointerException.class, () -> new BufferedStations(null, buffer));
        assertThrows(NullPointerException.class, () -> new BufferedStations(stringTable, null));
    }


    @Test
    void aliasAndStationNameReturnCorrectValues() {
        List<String> stringTable = List.of("Alias1", "StationX", "Alias2", "StationY");
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putShort(0, (short) 2); // Alias2
        buffer.putShort(2, (short) 3); // StationY

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals("Alias2", aliases.alias(0));
        assertEquals("StationY", aliases.stationName(0));
    }

    @Test
    void sizeReturnsCorrectValuee() {
        List<String> stringTable = List.of("Alias1", "StationX");
        ByteBuffer buffer = ByteBuffer.allocate(4);

        BufferedStationAliases aliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals(1, aliases.size());
    }

    @Test
    void nameAndStationIdReturnCorrectValues() {
        List<String> stringTable = List.of("PlatformA", "PlatformB");
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putShort(0, (short) 1); // PlatformB
        buffer.putShort(2, (short) 3); // StationId 3

        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        assertEquals("PlatformB", platforms.name(0));
        assertEquals(3, platforms.stationId(0));
    }


}
