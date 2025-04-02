package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * La classe BufferedRoutes implémente l'interface Routes et permet d'accéder à une table de
 * routes représentée de manière aplatie.
 * Chaque route est représentée par les champs suivants :
 * - NAME_ID : l'identifiant du nom de la route (U16)
 * - TYPE : le type de la route (U8)
 */
public final class BufferedRoutes implements Routes {
    private static final int NAME_ID = 0;
    private static final int KIND_ID = 1;
    private final StructuredBuffer buffer;
    private final List<String> stringTable;

    /**
     * Construit une instance de BufferedRoutes avec les données aplaties fournies.
     *
     * @param stringTable la table des chaînes de caractères
     * @param buffer      le ByteBuffer contenant les données des routes
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID,
                Structure.FieldType.U16)
                , Structure.field(KIND_ID, Structure.FieldType.U8))
                , buffer);

    }

    @Override
    public Vehicle vehicle(int id) {
        return Vehicle.ALL.get(buffer.getU8(KIND_ID, id));
    }

    @Override
    public String name(int id) {
        return stringTable.get(buffer.getU16(NAME_ID, id));
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
