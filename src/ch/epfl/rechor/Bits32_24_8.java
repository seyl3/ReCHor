package ch.epfl.rechor;

/**
 * Classe utilitaire pour manipuler des entiers de 32 bits en les séparant / en les créant à
 * partir des :
 * - 24 bits de poids fort
 * - 8 bits de poids faible
 *
 * @author Sarra Zghal, Elyes Ben Abid
 *
 */
public final class Bits32_24_8 {
    private Bits32_24_8() {
    }

    /**
     * Combine un entier de 24 bits et un entier de 8 bits en un seul entier de 32 bits.
     *
     * @param bits24 Partie haute de 24 bits (doit être contenu dans les 24 bits les plus bas
     *               d'un entier).
     * @param bits8  Partie basse de 8 bits (doit être contenu dans les 8 bits les plus bas d'un
     *               entier).
     * @return Un entier de 32 bits combinant les valeurs de `bits24` et `bits8`.
     * @throws IllegalArgumentException si `bits24` dépasse 24 bits ou si `bits8` dépasse 8 bits.
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument(bits24 >>> 24 == 0 && bits8 >>> 8 == 0);
        return (bits24 << 8) | bits8;
    }

    /**
     * Extrait les 24 bits de poids fort d'un entier de 32 bits.
     *
     * @param bits32 L'entier de 32 bits contenant la valeur combinée.
     * @return Un entier de 24 bits correspondant à la partie haute de `bits32`.
     */
    public static int unpack24(int bits32) {
        return bits32 >>> 8;
    }

    /**
     * Extrait les 8 bits de poids faible d'un entier de 32 bits.
     *
     * @param bits32 L'entier de 32 bits contenant la valeur combinée.
     * @return Un entier de 8 bits correspondant à la partie basse de `bits32`.
     */
    public static int unpack8(int bits32) {
        return bits32 & 0xFF;
    }
}
