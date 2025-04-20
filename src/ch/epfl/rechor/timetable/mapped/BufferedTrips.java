package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Implémentation de l'interface Trips utilisant un buffer pour stocker les données.
 * <p>
 * Format des données :
 * - ROUTE_ID : l'identifiant de la route (U16)
 * - DESTINATION_ID : l'identifiant de la destination (U16)
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public final class BufferedTrips implements Trips {
    private static final int ROUTE_ID = 0;
    private static final int DESTINATION_ID = 1;
    private final StructuredBuffer buffer;
    private final List<String> stringTable;

    /**
     * Construit une instance de BufferedTrips avec les données aplaties fournies.
     *
     * @param stringTable la table des chaînes de caractères
     * @param buffer      le ByteBuffer contenant les données des trajets
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(new Structure.Field(ROUTE_ID,
                Structure.FieldType.U16)
                , new Structure.Field(DESTINATION_ID, Structure.FieldType.U16))
                , buffer);
    }

    @Override
    public int routeId(int id) {
        return buffer.getU16(ROUTE_ID, id);
    }

    @Override
    public String destination(int id) {
        return stringTable.get(buffer.getU16(DESTINATION_ID, id));
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
