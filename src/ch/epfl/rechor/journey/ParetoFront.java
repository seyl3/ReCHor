package ch.epfl.rechor.journey;

import java.util.Arrays;
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
        private static final double CAPACITY_FACTOR = 1.5;


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

        public Builder add(long packedTuple) {
            // 1) filtre / copie en une seule passe
            int dst = 0;
            boolean dominatesExisting = false;

            for (int i = 0; i < effectiveLength; i++) {
                long current = front[i];

                // si égal → rien à faire
                if (current == packedTuple) return this;

                // si cur domine packed → on n'insère rien
                if (dominatesOrIsEqual(current, packedTuple)) return this;

                // si packed domine cur, on ne recopie pas cur
                if (!dominatesOrIsEqual(packedTuple, current)) {
                    front[dst++] = current;
                }
            }

            // 2) agrandir si nécessaire
            ensureCapacity(dst + 1);

            // 3) trouver la position d'insertion dans la partie conservée
            int pos = 0;
            while (pos < dst && front[pos] < packedTuple) pos++;

            // 4) décaler pour faire de la place et insérer
            System.arraycopy(front, pos, front, pos + 1, dst - pos);
            front[pos] = packedTuple;
            effectiveLength = dst + 1;
            return this;
        }

        private void ensureCapacity(int minCapacity) {
            if (front.length < minCapacity) {
                int newLength = Math.max((int) (front.length * CAPACITY_FACTOR) + 1, minCapacity);
                front = Arrays.copyOf(front, newLength);
            }
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
            for (int i = 0; i < that.effectiveLength; i++) {
                add(that.front[i]);
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