package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;

import java.nio.ByteBuffer;
import java.util.List;

import static java.lang.Math.scalb;


/**
 * La classe BufferedStations implémente l'interface Stations et permet d'accéder à une table de
 * gares représentée de manière aplatie.
 * Chaque gare est représentée par les champs suivants :
 * - NAME_ID : l'identifiant du nom de la gare (U16)
 * - LON : la longitude de la gare (S32)
 * - LAT : la latitude de la gare (S32)
 */
public final class BufferedStations implements Stations {
    private static final int NAME_ID = 0;
    private static final int LON = 1;
    private static final int LAT = 2;
    private final double LongitudeLatitudeConstant = scalb(360, -32);
    private final StructuredBuffer buffer;
    private final List<String> stringTable;

    /**
     * Construit une instance de BufferedStations avec les données aplaties fournies.
     *
     * @param stringTable la table des chaînes de caractères
     * @param buffer      le ByteBuffer contenant les données des gares
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = List.copyOf(stringTable);
        this.buffer = new StructuredBuffer(new Structure(Structure.field(NAME_ID,
                Structure.FieldType.U16)
                , Structure.field(LON, Structure.FieldType.S32)
                , Structure.field(LAT, Structure.FieldType.S32))
                , buffer);

    }

    @Override
    public String name(int id) {
        return stringTable.get(buffer.getU16(NAME_ID, id));
    }

    @Override
    public double longitude(int id) {
        return buffer.getS32(LON, id) * LongitudeLatitudeConstant;
    }

    @Override
    public double latitude(int id) {
        return buffer.getS32(LAT, id) * LongitudeLatitudeConstant;
    }

    @Override
    public int size() {
        return buffer.size();
    }


}
