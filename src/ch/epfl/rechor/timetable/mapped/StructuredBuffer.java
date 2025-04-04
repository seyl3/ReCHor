package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Permet d'accéder aux données d'un buffer selon une structure prédéfinie.
 * <p>
 * Cette classe offre une interface simple pour lire des données de différents types
 * (entiers 8/16 bits non signés, entiers 32 bits signés) stockées dans un buffer
 * selon une structure définie par la classe {@link Structure}.
 * </p>
 * <p>
 * Le buffer contient une séquence d'éléments de même taille, chacun composé de
 * plusieurs champs stockés les uns à la suite des autres. On accède aux données en
 * spécifiant :
 * - L'index du champ dans la structure
 * - L'index de l'élément dans le buffer
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public class StructuredBuffer {
    private final Structure structure;
    private final ByteBuffer buffer;

    /**
     * Crée un nouveau buffer structuré.
     *
     * @param structure définit l'organisation des données
     * @param buffer   contient les données à lire
     * @throws IllegalArgumentException si la taille du buffer n'est pas un multiple de la taille de la structure
     * @throws NullPointerException si structure ou buffer est null
     */
    public StructuredBuffer(Structure structure, ByteBuffer buffer) {
        Preconditions.checkArgument(buffer.capacity() % structure.totalSize() == 0);
        this.structure = Objects.requireNonNull(structure);
        this.buffer = Objects.requireNonNull(buffer);
    }

    public int size() {
        return buffer.capacity() / structure.totalSize();
    }

    /**
     * Lit un entier non signé de 8 bits.
     *
     * @throws IndexOutOfBoundsException si un des index est invalide
     */
    public int getU8(int fieldIndex, int elementIndex) {
        if (fieldIndex >= structure.totalSize() || elementIndex >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Byte.toUnsignedInt(buffer.get(structure.offset(fieldIndex, elementIndex)));
    }

    /**
     * Lit un entier non signé de 16 bits.
     *
     * @throws IndexOutOfBoundsException si un des index est invalide
     */
    public int getU16(int fieldIndex, int elementIndex) {
        if (fieldIndex >= structure.totalSize() || elementIndex >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Short.toUnsignedInt(buffer.getShort(structure.offset(fieldIndex, elementIndex)));
    }

    /**
     * Lit un entier signé de 32 bits.
     *
     * @throws IndexOutOfBoundsException si un des index est invalide
     */
    public int getS32(int fieldIndex, int elementIndex) {
        if (fieldIndex >= structure.totalSize() || elementIndex >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return buffer.getInt(structure.offset(fieldIndex, elementIndex));
    }
}

