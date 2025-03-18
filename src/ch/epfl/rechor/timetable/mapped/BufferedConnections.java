package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * La classe BufferedConnections implémente l'interface Connections et permet d'accéder à une table de liaisons représentée de manière aplatie.
 * Chaque liaison est représentée par les champs suivants :
 * - DEP_STOP_ID : l'identifiant de l'arrêt de départ (U16)
 * - DEP_MINUTES : le temps de départ en minutes (U16)
 * - ARR_STOP_ID : l'identifiant de l'arrêt d'arrivée (U16)
 * - ARR_MINUTES : le temps d'arrivée en minutes (U16)
 * - TRIP_POS_ID : l'identifiant de la course et la position dans la course (S32)
 */
public final class BufferedConnections implements Connections {
    private final static int DEP_STOP_ID = 0;
    private final static int DEP_MINUTES = 1;
    private final static int ARR_STOP_ID = 2;
    private final static int ARR_MINUTES = 3;
    private final static int TRIP_POS_ID = 4;
    private final StructuredBuffer buffer;
    private final IntBuffer succBuffer;

    /**
     * Construit une instance de BufferedConnections avec les données aplaties fournies.
     *
     * @param buffer     le ByteBuffer contenant les données des liaisons
     * @param succBuffer le ByteBuffer contenant les indices des liaisons suivantes
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        this.buffer = new StructuredBuffer(new Structure(new Structure.Field(DEP_STOP_ID, Structure.FieldType.U16)
                , new Structure.Field(DEP_MINUTES, Structure.FieldType.U16)
                , new Structure.Field(ARR_STOP_ID, Structure.FieldType.U16)
                , new Structure.Field(ARR_MINUTES, Structure.FieldType.U16)
                , new Structure.Field(TRIP_POS_ID, Structure.FieldType.S32))
                , buffer);
        this.succBuffer = succBuffer.asIntBuffer();
    }

    @Override
    public int depStopId(int id) {
        return buffer.getU16(DEP_STOP_ID, id);
    }

    @Override
    public int depMins(int id) {
        return buffer.getU16(DEP_MINUTES, id);
    }

    @Override
    public int arrStopId(int id) {
        return buffer.getU16(ARR_STOP_ID, id);
    }

    @Override
    public int arrMins(int id) {
        return buffer.getU16(ARR_MINUTES, id);
    }

    @Override
    public int tripId(int id) {
        return Bits32_24_8.unpack24(buffer.getS32(TRIP_POS_ID, id));
    }

    @Override
    public int tripPos(int id) {
        return Bits32_24_8.unpack8(buffer.getS32(TRIP_POS_ID, id));
    }

    @Override
    public int nextConnectionId(int id) {
        return succBuffer.get(id);
    }

    @Override
    public int size() {
        return buffer.size();
    }
}