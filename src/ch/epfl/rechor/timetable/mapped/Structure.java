package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Preconditions;

/**
 * Classe permettant de définir la structure d'un buffer de données aplaties.
 * <p>
 * Une structure est composée d'une séquence de champs, chacun ayant :
 * - un index unique (commençant à 0)
 * - un type (U8, U16 ou S32)
 * Les champs sont stockés de manière contiguë en mémoire, et leur position est calculée
 * en fonction de leur taille respective.
 * </p>
 * <p>
 * Cette classe est utilisée en conjonction avec {@code StructuredBuffer} pour accéder
 * efficacement aux données stockées dans un format binaire aplati.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public class Structure {
    private final Field[] fields;
    private final int[] fieldOffsets;
    private final int totalSize;

    /**
     * Construit une nouvelle structure à partir d'une séquence de champs.
     * Les champs doivent être fournis dans l'ordre, avec des index consécutifs commençant à 0.
     *
     * @param fields les champs définissant la structure
     * @throws IllegalArgumentException si la séquence de champs est vide ou si les index ne sont
     *                                  pas consécutifs
     */
    public Structure(Field... fields) {
        Preconditions.checkArgument(fields.length != 0 && fields[0].index() == 0);

        for (int i = 1; i < fields.length; i++) {
            Preconditions.checkArgument(fields[i].index == i);
        }
        this.fields = fields.clone();
        this.fieldOffsets = new int[fields.length];

        int offset = 0;
        for (int i = 0; i < fields.length; i++) {
            fieldOffsets[i] = offset;
            offset += fields[i].type().size;
        }
        this.totalSize = offset;
    }

    /**
     * Crée un nouveau champ avec l'index et le type spécifiés.
     *
     * @param index l'index du champ
     * @param type  le type du champ
     * @return un nouveau champ avec l'index et le type donnés
     */
    public static Field field(int index, FieldType type) {
        return new Field(index, type);
    }

    public int totalSize() {
        return totalSize;
    }

    /**
     * Calcule le décalage en octets pour accéder à un champ spécifique d'un élément.
     *
     * @param fieldIndex   l'index du champ dans la structure
     * @param elementIndex l'index de l'élément dans le tampon
     * @return le décalage en octets
     * @throws IndexOutOfBoundsException si fieldIndex ou elementIndex est invalide
     */
    public int offset(int fieldIndex, int elementIndex) {
        if (fieldIndex < 0 || fieldIndex >= fields.length) {
            throw new IndexOutOfBoundsException();
        }
        if (elementIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        return elementIndex * totalSize + fieldOffsets[fieldIndex];
    }

    /**
     * Types de champs supportés par la structure.
     * <p>
     * Chaque type a une taille fixe en octets :
     * - U8  : entier non signé sur 8 bits (1 octet)
     * - U16 : entier non signé sur 16 bits (2 octets)
     * - S32 : entier signé sur 32 bits (4 octets)
     * </p>
     */
    public enum FieldType {
        U8(1), U16(2), S32(4);

        final int size;

        FieldType(int size) {
            this.size = size;
        }
    }

    /**
     * Représente un champ dans la structure avec son index et son type.
     *
     * @param index l'index du champ dans la structure
     * @param type  le type du champ
     * @throws NullPointerException si type est null
     */
    public record Field(int index, FieldType type) {
        public Field {
            if (type == null) {
                throw new NullPointerException();
            }
        }
    }
}

