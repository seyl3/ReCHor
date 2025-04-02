package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * La classe BufferedTransfers implémente l'interface Transfers et permet d'accéder à une table
 * de transferts représentée de manière aplatie.
 * Chaque transfert est représenté par les champs suivants :
 * - FROM_STOP_ID : l'identifiant de l'arrêt de départ (U16)
 * - TO_STOP_ID : l'identifiant de l'arrêt d'arrivée (U16)
 * - MIN_TRANSFER_TIME : le temps de transfert minimum en minutes (U16)
 */
public final class BufferedTransfers implements Transfers {

    private final static int DEP_STATION_ID = 0;
    private final static int ARR_STATION_ID = 1;
    private final static int TRANSFERS_MINUTES = 2;
    private final StructuredBuffer buffer;
    private final int[] arrivingAt;

    /**
     * Construit une instance de BufferedTransfers avec les données aplaties fournies.
     *
     * @param buffer le ByteBuffer contenant les données des transferts
     */

    public BufferedTransfers(ByteBuffer buffer) {
        this.buffer = new StructuredBuffer(new Structure(new Structure.Field(DEP_STATION_ID,
                Structure.FieldType.U16)
                , new Structure.Field(ARR_STATION_ID, Structure.FieldType.U16)
                , new Structure.Field(TRANSFERS_MINUTES, Structure.FieldType.U8))
                , buffer);

        // Étape 1 : Vérifier si le buffer est vide et créer un tableau vide si c'est le cas
        if (this.buffer.size() == 0) {
            this.arrivingAt = new int[0];
            return;
        }

        // Étape 2 : Trouver l'identifiant de la station d'arrivée maximale pour déterminer la
        // taille du tableau
        int maxStationId = 0;
        for (int i = 0; i < this.buffer.size(); i++) {
            int stationId = this.buffer.getU16(ARR_STATION_ID, i);
            if (stationId > maxStationId) {
                maxStationId = stationId;
            }
        }

        // Étape 3 : Créer un tableau de taille maxStationId + 1 pour contenir tous les
        // identifiants de station (0 à maxStationId)
        this.arrivingAt = new int[maxStationId + 1];

        // Étape 4 : Regrouper les transferts par identifiant de station d'arrivée
        int currentStationId = this.buffer.getU16(ARR_STATION_ID, 0);
        int startIndex = 0;

        for (int i = 1; i < this.buffer.size(); i++) {
            int stationId = this.buffer.getU16(ARR_STATION_ID, i);

            // Étape 5 : Lorsqu'un identifiant de station différent est trouvé, stocker
            // l'intervalle pour la station précédente
            if (stationId != currentStationId) {
                arrivingAt[currentStationId] = PackedRange.pack(startIndex, i);
                currentStationId = stationId;
                startIndex = i;
            }
        }

        // Étape 6 : Stocker la plage pour le dernier groupe de transferts
        arrivingAt[currentStationId] = PackedRange.pack(startIndex, this.buffer.size());
    }

    @Override
    public int depStationId(int id) {
        return buffer.getU16(DEP_STATION_ID, id);
    }

    @Override
    public int minutes(int id) {
        return buffer.getU8(TRANSFERS_MINUTES, id);
    }

    @Override
    public int arrivingAt(int stationId) {
        return arrivingAt[stationId];
    }

    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        int range = arrivingAt[arrStationId]; // Lève naturellement une
        // IndexOutOfBoundsException, pas besoin de précondition
        int start = PackedRange.startInclusive(range);
        int end = PackedRange.endExclusive(range) - 1;

        for (int i = start; i <= end; i++) {
            if (buffer.getU16(DEP_STATION_ID, i) == depStationId) {
                return buffer.getU8(TRANSFERS_MINUTES, i);
            }
        }

        throw new NoSuchElementException();
    }

    @Override
    public int size() {
        return buffer.size();
    }
}
