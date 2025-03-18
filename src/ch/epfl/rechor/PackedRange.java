package ch.epfl.rechor;

/**
 * Classe utilitaire permettant d'empaqueter un intervalle de valeurs entières sous la forme d'un entier de 32 bits.
 * La plage est représentée par :
 * - 24 bits pour le début de l'intervalle (inclus)
 * - 8 bits pour la longueur de l'intervalle
 */
public final class PackedRange {
    private PackedRange() {
    }

    /**
     * Encode un intervalle d'entiers sous la forme d'un entier de 32 bits.
     *
     * @param startInclusive Début de l'intervalle (inclus). Doit tenir sur 24 bits.
     * @param endExclusive   Fin de l'intervalle (exclus). La longueur de l'intervalle doit tenir sur 8 bits.
     * @return Un entier de 32 bits représentant l'intervalle encodé.
     * @throws IllegalArgumentException si `startInclusive` dépasse 24 bits ou si la longueur de l'intervalle dépasse 8 bits.
     */
    public static int pack(int startInclusive, int endExclusive) {
        Preconditions.checkArgument(endExclusive >= startInclusive);
        int length = endExclusive - startInclusive;
        // System.out.println(length); //Débugging
        Preconditions.checkArgument(startInclusive >>> 24 == 0);
        Preconditions.checkArgument(length >>> 8 == 0);
        return Bits32_24_8.pack(startInclusive, length);
    }

    /**
     * Extrait la longueur de l'intervalle à partir d'un entier encodé.
     *
     * @param interval Entier de 32 bits représentant l'intervalle encodé.
     * @return La longueur de l'intervalle.
     */
    public static int length(int interval) {
        return Bits32_24_8.unpack8(interval);
    }

    /**
     * Extrait la borne inférieure (incluse) à partir d'un entier encodé.
     *
     * @param interval Entier de 32 bits représentant l'intervalle encodé.
     * @return La valeur de début de l'intervalle.
     */
    public static int startInclusive(int interval) {
        return Bits32_24_8.unpack24(interval);
    }

    /**
     * Calcule la borne sup de l'intervalle (exclue) à partir d'un entier encodé.
     *
     * @param interval Entier de 32 bits représentant l'intervalle encodé.
     * @return La valeur de fin de l'intervalle (exclus).
     */
    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);

    }

}
