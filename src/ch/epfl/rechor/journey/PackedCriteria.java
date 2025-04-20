package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import static java.lang.Integer.toUnsignedLong;

/**
 * Classe utilitaire permettant de compresser et manipuler des critères de recherche d'itinéraire
 * sous forme d'une valeur long.
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public final class PackedCriteria {
    private PackedCriteria() {
    }

    /**
     * Embpaquette les critères donnés en une valeur long.
     *
     * @param arrMins Temps d'arrivée en minutes avec minuit comme référence.
     * @param changes Nombre de changements d'itinéraire.
     * @param payload Charge utile.
     * @return Une valeur long représentant les critères empaquetés.
     */
    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument((changes >>> 7 == 0) && (arrMins >= -240 && arrMins < 2880));
        int arrMinsConv = arrMins + 240;
        return ((long) arrMinsConv) << 39 | ((long) changes) << 32 | toUnsignedLong(payload);
    }

    /**
     * Vérifie si les minutes de départ sont présentes dans les critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @return true si les minutes de départ sont présentes, sinon false.
     */
    public static boolean hasDepMins(long criteria) {
        return !(criteria >>> 51 == 0);
    }

    /**
     * Récupère les minutes de départ stockées dans les critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @return Temps de départ en minutes avec minuit comme référence.
     * @throws IllegalArgumentException si les minutes de départ ne sont pas présentes.
     */
    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        return (int) ~(criteria >> 51) - 240;
    }

    /**
     * Récupère les minutes d'arrivée stockées dans les critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @return Temps d'arrivée en minutes avec minuit comme référence.
     */
    public static int arrMins(long criteria) {
        long mask = (1L << 12) - 1;
        return ((int) (criteria >>> 39 & mask)) - 240;
    }

    /**
     * Récupère le nombre de changements stockés dans les critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @return Nombre de changements.
     */
    public static int changes(long criteria) {
        long mask = (1L << 7) - 1;
        return (int) ((criteria >>> 32) & mask);
    }

    /**
     * Récupère la valeur du payload stockée dans les critères.
     *
     * @param criteria Critères empaquetés sous forme de long.
     * @return Valeur du payload.
     */
    public static int payload(long criteria) {
        long mask = (1L << 32) - 1;
        return (int) (criteria & mask);
    }

    /**
     * Vérifie si un ensemble de critères domine ou est égal à un autre.
     *
     * @param criteria1 Premier ensemble de critères empaquetés.
     * @param criteria2 Deuxième ensemble de critères empaquetés.
     * @return true si criteria1 est dominant ou égal à criteria2, sinon false.
     * @throws IllegalArgumentException si un seul des deux critères possède des minutes de départ.
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument((hasDepMins(criteria1) && hasDepMins(criteria2)) ||
                !(hasDepMins(criteria2) && !hasDepMins(criteria1)));

        int a1 = arrMins(criteria1);
        int a2 = arrMins(criteria2);
        int c1 = changes(criteria1);
        int c2 = changes(criteria2);

        if (hasDepMins(criteria1) && hasDepMins(criteria2)) {
            int d1 = depMins(criteria1);
            int d2 = depMins(criteria2);
            return (d1 >= d2) && (a1 <= a2) && (c1 <= c2);
        } else return (a1 <= a2) && (c1 <= c2);
    }

    /**
     * Supprime les minutes de départ des critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @return Nouveaux critères sans les minutes de départ.
     */
    public static long withoutDepMins(long criteria) {
        long mask = (1L << 51) - 1;
        return criteria & mask;
    }

    /**
     * Ajoute des minutes de départ aux critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @param depMins1 Minutes de départ à ajouter.
     * @return Nouveaux critères avec le complément des minutes de départ ajouté.
     * @throws IllegalArgumentException si depMins1 est hors limites.
     */
    public static long withDepMins(long criteria, int depMins1) {
        Preconditions.checkArgument(depMins1 >= -240 && depMins1 < 2880);
        int depMinsConv = ~(depMins1 + 240);
        long depMins = ((long) depMinsConv) << 51;
        return criteria | depMins;
    }


    /**
     * Ajoute un changement supplémentaire aux critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @return Nouveaux critères avec un changement supplémentaire.
     * @throws IllegalArgumentException si le nombre de changements est déjà maximal.
     */
    public static long withAdditionalChange(long criteria) {
        Preconditions.checkArgument(changes(criteria) < 127);
        int additionalChange = changes(criteria) + 1;
        long packed = pack(arrMins(criteria), additionalChange, payload(criteria));
        return hasDepMins(criteria) ? withDepMins(packed, depMins(criteria)) : packed;
    }

    /**
     * Modifie le payload des critères.
     *
     * @param criteria Critères compressés sous forme de long.
     * @param payload  Nouvelle valeur du payload.
     * @return Nouveaux critères avec le payload modifié.
     */
    public static long withPayload(long criteria, int payload) {
        long mask = ((1L << 32) - 1) << 32;
        long withoutPayload = criteria & mask;
        return (withoutPayload | toUnsignedLong(payload));


    }


}
