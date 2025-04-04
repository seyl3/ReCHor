package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * La classe BufferedPlatforms implémente l'interface Platforms et permet d'accéder à une table
 * de voies ou quais représentée de manière aplatie.
 * Chaque voie ou quai est représenté par les champs suivants :
 * - NAME_ID : l'identifiant du nom de la voie ou du quai (U16)
 * - STATION_ID : l'identifiant de la gare parente (U16)
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public final class BufferedPlatforms implements Platforms {
    private static final int NAME_ID = 0;
    private static final int STATION_ID = 1;
    private final StructuredBuffer buffer;
    private final List<String> stringTable;

    /**
     * Construit une instance de BufferedPlatforms avec les données aplaties fournies.
     *
     * @param stringTable la table des chaînes de caractères
     * @param buffer      le ByteBuffer contenant les données des voies ou quais
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = List.copyOf(stringTable);
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID,
                Structure.FieldType.U16)
                , Structure.field(STATION_ID, Structure.FieldType.U16))
                , buffer);
    }

    @Override
    public String name(int id) {
        return stringTable.get(buffer.getU16(NAME_ID, id));
    }

    @Override
    public int stationId(int id) {
        return buffer.getU16(STATION_ID, id);
    }

    @Override
    public int size() {
        return buffer.size();
    }
}