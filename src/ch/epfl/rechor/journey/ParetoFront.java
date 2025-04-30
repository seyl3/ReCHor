package ch.epfl.rechor.journey;

import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Classe représentant une frontière de Pareto de critères d'optimisation.
 * <p>
 * La frontière de Pareto est une collection de tuples où chaque tuple représente
 * des critères d'optimisation empaquetés. Ces critères incluent typiquement des
 * informations comme l'heure d'arrivée, le nombre de changements, etc. La classe est
 * immuable et fournit une API permettant de récupérer, manipuler et itérer sur ces tuples.
 * </p>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public class ParetoFront {
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);
    private final long[] front;

    /**
     * Constructeur privé qui permet de créer une instance de ParetoFront.
     *
     * @param front Tableau de critères d'optimisation empaquetés (stocké sans être copié
     *              pour garantir l'immuabilité.
     */
    private ParetoFront(long[] front) {
        this.front = front;
    }

    /**
     * Retourne la taille de la frontière de Pareto, c'est-à-dire le nombre de tuples
     * qu'elle contient.
     *
     * @return Le nombre de tuples dans la frontière de Pareto.
     */
    public int size() {
        return front.length;
    }

    /**
     * Recherche un tuple dans la frontière de Pareto correspondant aux critères
     * donnés (heure d'arrivée et nombre de changements).
     *
     * @param arrMins L'heure d'arrivée recherchée.
     * @param changes Le nombre de changements recherchés.
     * @return Le tuple empaqueté correspondant aux critères.
     * @throws NoSuchElementException Si aucun tuple ne correspond aux critères.
     */
    public long get(int arrMins, int changes) {
        for (long frontMember : front) {
            if (arrMins(frontMember) == arrMins && changes(frontMember) == changes) {
                return frontMember;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Applique une action donnée à chaque tuple de la frontière de Pareto.
     *
     * @param action L'action à appliquer à chaque tuple.
     */
    public void forEach(LongConsumer action) {
        for (long l : front) {
            action.accept(l);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < front.length; i++) {
            if (front[i] != 0) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(" (");
                if (hasDepMins(front[i])) {
                    sb.append(depMins(front[i]))
                            .append(", ")
                            .append(arrMins(front[i]))
                            .append(", ")
                            .append(changes(front[i]));
                } else {
                    sb.append(arrMins(front[i]))
                            .append(", ")
                            .append(changes(front[i]));
                }
                sb.append(") ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Le bâtisseur de la frontière de Pareto.
     * <p>
     * Cette classe permet de construire une frontière de Pareto de manière progressive.
     */
    public static class Builder {
        private int effectiveLength;
        private long[] front;

        /**
         * Constructeur qui initialise un bâtisseur avec une frontière vide.
         */
        public Builder() {
            this.front = new long[2];
            effectiveLength = 0;

        }

        /**
         * Constructeur de copie qui crée un nouveau bâtisseur avec les mêmes attributs
         * que le bâtisseur passé en argument.
         *
         * @param that Le bâtisseur à copier.
         */
        public Builder(Builder that) {
            this.front = that.front.clone();
            effectiveLength = that.effectiveLength;
        }

        /**
         * Vérifie si la frontière en construction est vide.
         *
         * @return {@code true} si la frontière est vide, sinon {@code false}.
         */
        public boolean isEmpty() {
            return effectiveLength == 0;
        }

        /**
         * Vide la frontière en supprimant tous les éléments du tableau
         * et en réinitialisant sa longueur effective à zéro.
         *
         * @return Le bâtisseur pour chaîner les appels.
         */
        public Builder clear() {
            effectiveLength = 0;
            return this;
        }

        /**
         * Ajoute un tuple empaqueté à la frontière en construction, s'il n'est pas dominé
         * ou égal à un tuple existant, tout en respectant l'ordre lexicographisuqe.
         * Les tuples dominés par ce nouveau tuple sont supprimés.
         *
         * @param packedTuple Le tuple empaqueté à ajouter.
         * @return Le bâtisseur pour chaîner les appels.
         */
        /*public Builder add(long packedTuple) {
            long[] newFront;

            // Étape 1 : Si la frontière est vide, insérer simplement le tuple
            if (isEmpty()) {
                effectiveLength++;
                front[0] = packedTuple;
                return this;
            } else {
                int insertPos = 0;

                // Étape 2 : Trouver la bonne position d’insertion
                for (int i = 0; i < effectiveLength; i++) {
                    // Si un tuple existant domine ou est égal au nouveau, ne pas l’ajouter
                    if (dominatesOrIsEqual(front[i], packedTuple)) {
                        return this;
                    }

                    // Mettre à jour la position d’insertion (garder la plus grande position valide)
                    if (withPayload(packedTuple, 0) > withPayload(front[i], 0)) {
                        insertPos = i+1;
                    }
                }

                // Étape 3 : Redimensionner la liste si sa capacité est atteinte
                if (effectiveLength == front.length) {
                    newFront = new long[(int) (front.length * 2)];
                    System.arraycopy(front, 0, newFront, 0, front.length);
                } else {
                    newFront = front.clone();
                }

                // Étape 4 : compacter le reste de la liste
                int dst = insertPos;
                for (int src = insertPos; src < effectiveLength; src += 1) {
                    if (dominatesOrIsEqual(packedTuple,front[src])) {
                        effectiveLength--;
                    }else{
                        front[dst] = front[src];
                        dst += 1;
                    }
                }

                // Étape 5 : Insérer le nouveau tuple à la bonne position
                newFront[insertPos] = packedTuple;
                effectiveLength++;

                // Étape 6 : Décaler les éléments non dominés vers la droite
                System.arraycopy(front, insertPos, newFront, insertPos + 1,
                effectiveLength-insertPos-1);

                // Étape 7 : Mettre à jour la référence de la frontière
                front = newFront;
            }

            return this;
        }*/
        public Builder add(long packedTuple) {
            // Étape 1 : Vérifier si la frontière est vide
            if (isEmpty()) {
                ensureCapacity(1);
                front[0] = packedTuple;
                effectiveLength = 1;
                return this;
            }

            // Étape 2 : Trouver la position d'insertion et vérifier si le tuple est dominé
            int insertPos = 0;
            while (insertPos < effectiveLength &&
                    withPayload(front[insertPos], 0) < withPayload(packedTuple, 0)) {
                insertPos++;
            }

            // Vérifier si un élément existant domine déjà le nouveau tuple
            for (int i = 0; i < insertPos; i++) {
                if (dominatesOrIsEqual(front[i], packedTuple)) {
                    return this; // Ne pas ajouter si dominé
                }
            }

            // Étape 3 : Compactage - Supprimer les éléments dominés après la position d'insertion
            int newLength = compact(insertPos, packedTuple);

            // Étape 4 : Redimensionner si nécessaire
            ensureCapacity(newLength + 1);

            // Étape 5 : Décaler les éléments restants pour faire place au nouvel élément
            if (insertPos < newLength) {
                System.arraycopy(front, insertPos, front, insertPos + 1, newLength - insertPos);
            }

            // Étape 6 : Insérer le nouveau tuple à la bonne position
            front[insertPos] = packedTuple;
            effectiveLength = newLength + 1;

            return this;
        }

        private void ensureCapacity(int minCapacity) {
            if (front.length < minCapacity) {
                long[] newFront = new long[Math.max(front.length * 3 / 2, minCapacity)];
                System.arraycopy(front, 0, newFront, 0, effectiveLength);
                front = newFront;
            }
        }

        /**
         * Supprime les éléments dominés par `packedTuple` après `insertPos` dans le tableau.
         *
         * @param insertPos   la position d'insertion du nouveau tuple.
         * @param packedTuple le tuple à insérer.
         * @return la nouvelle taille effective après suppression des dominés.
         */
        private int compact(int insertPos, long packedTuple) {
            int dst = insertPos;
            for (int src = insertPos; src < effectiveLength; src++) {
                if (!dominatesOrIsEqual(packedTuple, front[src])) {
                    front[dst++] = front[src];
                }
            }
            return dst; // Nouvelle taille effective après compactage
        }

        /**
         * Ajoute un tuple de critères d'optimisation à la frontière en construction
         * après avoir empaqueté les paramètres donnés.
         *
         * @param arrMins L'heure d'arrivée.
         * @param changes Le nombre de changements.
         * @param payload La charge utile associée.
         * @return Le bâtisseur pour chaîner les appels.
         */
        public Builder add(int arrMins, int changes, int payload) {
            add(pack(arrMins, changes, payload));
            return this;
        }

        /**
         * Ajoute tous les tuples d'un autre bâtisseur à la frontière en construction.
         *
         * @param that Le bâtisseur dont les tuples sont ajoutés.
         * @return Le bâtisseur pour chaîner les appels.
         */
        public Builder addAll(Builder that) {
            for (long a : that.front) {
                add(a);
            }
            return this;
        }

        /**
         * Vérifie si la frontière donnée en paramètre, après avoir fixé l'heure de départ sur la
         * valeur donné,
         * est dominée entièrement par la frontière à laquelle on applique l'appel de cette
         * méthode .
         *
         * @param that    Le bâtisseur de la frontière à tester.
         * @param depMins L'heure de départ à fixer.
         * @return {@code true} si la frontière en cours de construction est dominée entièrement
         * par l'autre,
         * sinon {@code false}.
         */
        public boolean fullyDominates(Builder that, int depMins) {
            for (int i = 0; i < that.effectiveLength; i++) {
                long thatTuple = withDepMins(that.front[i], depMins);
                boolean isDominated = false;

                for (int j = 0; j < this.effectiveLength; j++) {
                    if (dominatesOrIsEqual(this.front[j], thatTuple)) {
                        isDominated = true;
                        break;
                    }
                }
                if (!isDominated) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Applique l'action donnée à chaque tuple de la frontière en construction.
         *
         * @param action L'action à appliquer à chaque tuple de la frontière en construction.
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < effectiveLength; i++) {
                action.accept(front[i]);
            }
        }

        /**
         * Construit une instance de {@link ParetoFront} à partir des tuples actuellement
         * présents dans la frontière en construction.
         *
         * @return Une instance de {@link ParetoFront} construite à partir de la frontière en cours
         * de construction.
         */
        public ParetoFront build() {
            long[] paretoFront = new long[effectiveLength];
            System.arraycopy(front, 0, paretoFront, 0, effectiveLength);
            return new ParetoFront(paretoFront);
        }

    }

}