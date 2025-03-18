package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BufferedRoutesTest {
    private List<String> stringTable;
    private ByteBuffer buffer;
    private Routes routes;

    @BeforeEach
    void setUp() {
        // Create a string table with sample route names
        stringTable = new ArrayList<>();
        stringTable.add("M1");       // Index 0
        stringTable.add("M2");       // Index 1
        stringTable.add("TL 1");     // Index 2
        stringTable.add("TL 2");     // Index 3
        stringTable.add("S1");       // Index 4
        stringTable.add("S2");       // Index 5
        stringTable.add("Bus 701");  // Index 6

        // Create a buffer with sample route data
        // Each route has 3 bytes: 2 bytes for name_id (U16) and 1 byte for kind (U8)
        buffer = ByteBuffer.allocate(21); // 7 routes * 3 bytes each
        
        // Route 0: M1, METRO (1)
        buffer.putShort((short) 0);  // name_id = 0 (M1)
        buffer.put((byte) 1);        // kind = 1 (METRO)
        
        // Route 1: M2, METRO (1)
        buffer.putShort((short) 1);  // name_id = 1 (M2)
        buffer.put((byte) 1);        // kind = 1 (METRO)
        
        // Route 2: TL 1, TRAM (0)
        buffer.putShort((short) 2);  // name_id = 2 (TL 1)
        buffer.put((byte) 0);        // kind = 0 (TRAM)
        
        // Route 3: TL 2, TRAM (0)
        buffer.putShort((short) 3);  // name_id = 3 (TL 2)
        buffer.put((byte) 0);        // kind = 0 (TRAM)
        
        // Route 4: S1, TRAIN (2)
        buffer.putShort((short) 4);  // name_id = 4 (S1)
        buffer.put((byte) 2);        // kind = 2 (TRAIN)
        
        // Route 5: S2, TRAIN (2)
        buffer.putShort((short) 5);  // name_id = 5 (S2)
        buffer.put((byte) 2);        // kind = 2 (TRAIN)
        
        // Route 6: Bus 701, BUS (3)
        buffer.putShort((short) 6);  // name_id = 6 (Bus 701)
        buffer.put((byte) 3);        // kind = 3 (BUS)
        
        buffer.flip(); // Prepare buffer for reading
        
        // Create the BufferedRoutes instance
        routes = new BufferedRoutes(stringTable, buffer);
    }

    @Test
    void sizeReturnsCorrectNumberOfRoutes() {
        assertEquals(7, routes.size());
    }

    @Test
    void vehicleReturnsCorrectVehicleType() {
        assertEquals(Vehicle.METRO, routes.vehicle(0));
        assertEquals(Vehicle.METRO, routes.vehicle(1));
        assertEquals(Vehicle.TRAM, routes.vehicle(2));
        assertEquals(Vehicle.TRAM, routes.vehicle(3));
        assertEquals(Vehicle.TRAIN, routes.vehicle(4));
        assertEquals(Vehicle.TRAIN, routes.vehicle(5));
        assertEquals(Vehicle.BUS, routes.vehicle(6));
    }

    @Test
    void nameReturnsCorrectRouteName() {
        assertEquals("M1", routes.name(0));
        assertEquals("M2", routes.name(1));
        assertEquals("TL 1", routes.name(2));
        assertEquals("TL 2", routes.name(3));
        assertEquals("S1", routes.name(4));
        assertEquals("S2", routes.name(5));
        assertEquals("Bus 701", routes.name(6));
    }

    @Test
    void vehicleThrowsExceptionForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(7));
    }

    @Test
    void nameThrowsExceptionForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(7));
    }

    @Test
    void constructorHandlesEmptyBuffer() {
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        Routes emptyRoutes = new BufferedRoutes(stringTable, emptyBuffer);
        assertEquals(0, emptyRoutes.size());
    }

    @Test
    void constructorHandlesAllVehicleTypes() {
        // Create a buffer with all vehicle types
        ByteBuffer allTypesBuffer = ByteBuffer.allocate(21); // 7 vehicle types * 3 bytes
        
        for (int i = 0; i < 7; i++) {
            allTypesBuffer.putShort((short) 0); // All use the same name for simplicity
            allTypesBuffer.put((byte) i);       // Vehicle type 0-6
        }
        
        allTypesBuffer.flip();
        
        Routes allTypesRoutes = new BufferedRoutes(stringTable, allTypesBuffer);
        
        // Test all vehicle types
        assertEquals(Vehicle.TRAM, allTypesRoutes.vehicle(0));
        assertEquals(Vehicle.METRO, allTypesRoutes.vehicle(1));
        assertEquals(Vehicle.TRAIN, allTypesRoutes.vehicle(2));
        assertEquals(Vehicle.BUS, allTypesRoutes.vehicle(3));
        assertEquals(Vehicle.FERRY, allTypesRoutes.vehicle(4));
        assertEquals(Vehicle.AERIAL_LIFT, allTypesRoutes.vehicle(5));
        assertEquals(Vehicle.FUNICULAR, allTypesRoutes.vehicle(6));
    }
} 