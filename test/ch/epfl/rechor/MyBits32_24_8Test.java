package ch.epfl.rechor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBits32_24_8Test {
    @Test
    void testPackValidValues() {
        assertEquals(0x12345678, Bits32_24_8.pack(0x123456, 0x78));
        assertEquals(0x00FFFF00, Bits32_24_8.pack(0x00FFFF, 0x00));
        assertEquals(0xFFFFFF7F, Bits32_24_8.pack(0xFFFFFF, 0x7F));
    }

    @Test
    void testPackInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Bits32_24_8.pack(0x1000000, 0x00)); // Dépasse 24 bits
        assertThrows(IllegalArgumentException.class, () -> Bits32_24_8.pack(0x123456, 0x100)); // Dépasse 8 bits
    }

    @Test
    void testUnpack24() {
        assertEquals(0x123456, Bits32_24_8.unpack24(0x12345678));
        assertEquals(0x00FFFF, Bits32_24_8.unpack24(0x00FFFF00));
        assertEquals(0xFFFFFF, Bits32_24_8.unpack24(0xFFFFFF7F));
    }

    @Test
    void testUnpack8() {
        assertEquals(0x78, Bits32_24_8.unpack8(0x12345678));
        assertEquals(0x00, Bits32_24_8.unpack8(0x00FFFF00));
        assertEquals(0x7F, Bits32_24_8.unpack8(0xFFFFFF7F));
    }
}


