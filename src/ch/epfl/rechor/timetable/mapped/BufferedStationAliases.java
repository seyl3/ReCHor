package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * La classe BufferedStationAliases implémente l'interface StationAliases et permet d'accéder à
 * une table de noms alternatifs de gares représentée de manière aplatie.
 * Chaque nom alternatif est représenté par les champs suivants :
 * - ALIAS_ID : l'identifiant du nom alternatif (U16)
 * - STATION_NAME_ID : l'identifiant du nom de la gare (U16)
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public final class BufferedStationAliases implements StationAliases {
    private static final int ALIAS_ID = 0;
    private static final int STATION_NAME_ID = 1;
    private final StructuredBuffer buffer;
    private final List<String> stringTable;

    /**
     * Construit une instance de BufferedStationAliases avec les données aplaties fournies.
     *
     * @param stringTable la table des chaînes de caractères
     * @param buffer      le ByteBuffer contenant les données des noms alternatifs
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = List.copyOf(stringTable);
        this.buffer = new StructuredBuffer(new Structure(Structure.field(ALIAS_ID,
                Structure.FieldType.U16)
                , Structure.field(STATION_NAME_ID, Structure.FieldType.U16))
                , buffer);
    }

    @Override
    public String alias(int id) {
        return stringTable.get(buffer.getU16(ALIAS_ID, id));
    }

    @Override
    public String stationName(int id) {
        return stringTable.get(buffer.getU16(STATION_NAME_ID, id));
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
