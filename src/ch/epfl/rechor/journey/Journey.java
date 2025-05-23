package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * Représente un voyage composé d'une liste d'étapes (legs).
 * <p>
 * Une instance de Journey doit respecter les contraintes suivantes :
 * <ul>
 *   <li>La liste d'étapes n'est pas vide.</li>
 *   <li>Les étapes alternent entre Transport et Foot.</li>
 * </ul>
 *
 * @author Sarra Zghal, Elyes Ben Abid
 */
public record Journey(List<Leg> legs) {

    /**
     * Construit un voyage à partir d'une liste d'étapes.
     *
     * @param legs la liste d'étapes formant ce voyage
     * @throws NullPointerException     si legs est null
     * @throws IllegalArgumentException si la liste est vide ou si l'alternance entre Transport
     *                                  et Foot n'est pas respectée
     */
    public Journey {
        Objects.requireNonNull(legs);
        Preconditions.checkArgument(!legs.isEmpty());
        legs = List.copyOf(legs);
        for (int i = 0; i < legs.size() - 1; i++) {
            if (legs.get(i) instanceof Leg.Transport) {
                Preconditions.checkArgument(legs.get(i + 1) instanceof Leg.Foot);
            } else {
                Preconditions.checkArgument(legs.get(i + 1) instanceof Leg.Transport);
            }

        }
    }
    /**
     * Donne l'arrêt de départ du voyage soit celui de sa première étape
     *
     * @return l'arrêt de départ du voyage
     */
    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    /**
     * Donne l'arrêt d'arrivée du voyage soit celui de sa dernière étape
     *
     * @return l'arrêt d'arrivée du voyage
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    /**
     * Donne l'heure de départ du voyage soit celle de sa première étape
     *
     * @return l'heure de départ du voyage
     */
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    /**
     * Donne l'heure d'arrivée du voyage soit celle de sa dernière étape
     *
     * @return l'heure d'arrivée du voyage
     */
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }

    /**
     * Calcule la durée totale du voyage, entre l'heure de départ de la première étape
     * et l'heure d'arrivée de la dernière étape.
     *
     * @return la durée totale du voyage
     */
    public Duration duration() {
        return Duration.between(legs.getFirst().depTime(), legs.getLast().arrTime());
    }

    /**
     * Représente une étape du voyage.
     */
    public sealed interface Leg {
        /**
         * Donne l'arrêt de départ de l'étape
         *
         * @return l'arrêt de départ de l'étape
         */
        Stop depStop();

        /**
         * Donne l'arrêt d'arrivée de l'étape
         *
         * @return l'arrêt d'arrivée de l'étape
         */
        Stop arrStop();

        /**
         * Donne l'heure de départ de l'étape
         *
         * @return l'heure de départ de l'étape
         */
        LocalDateTime depTime();

        /**
         * Donne l'heure d'arrivée de l'étape
         *
         * @return l'heure d'arrivée de l'étape
         */
        LocalDateTime arrTime();

        /**
         * Donne une liste des arrêts intermédiaires de l'étape
         *
         * @return une liste des arrêts intermédiaires
         */
        List<IntermediateStop> intermediateStops();

        /**
         * Calcule la durée de l'étape.
         *
         * @return la durée de l'étape
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Représente un arrêt intermédiaire dans une étape.
         *
         * @param stop    l'arrêt concerné
         * @param arrTime l'heure d'arrivée à cet arrêt
         * @param depTime l'heure de départ de cet arrêt
         * @throws NullPointerException     si stop est null
         * @throws IllegalArgumentException si arrTime n'est pas avant ou égale à depTime
         */
        record IntermediateStop(Stop stop, LocalDateTime arrTime, LocalDateTime depTime) {

            public IntermediateStop {
                Objects.requireNonNull(stop);
                Preconditions.checkArgument(arrTime.isBefore(depTime) || depTime.isEqual(arrTime));
            }

        }

        /**
         * Représente une étape de transport (train, bus, etc.).
         *
         * @param depStop           l'arrêt de départ
         * @param depTime           l'heure de départ
         * @param arrStop           l'arrêt d'arrivée
         * @param arrTime           l'heure d'arrivée
         * @param intermediateStops la liste des arrêts intermédiaires
         * @param vehicle           le véhicule utilisé
         * @param route             l'itinéraire ou la ligne (ex. "Ligne 1")
         * @param destination       la destination finale (ex. "Lausanne")
         * @throws NullPointerException     si un des paramètres est null
         * @throws IllegalArgumentException si depTime n'est pas avant ou égale à arrTime
         */
        record Transport(Stop depStop,
                         LocalDateTime depTime,
                         Stop arrStop,
                         LocalDateTime arrTime,
                         List<IntermediateStop> intermediateStops,
                         Vehicle vehicle,
                         String route,
                         String destination) implements Leg {
            public Transport {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);
                Preconditions.checkArgument(depTime.isBefore(arrTime) || depTime.isEqual(arrTime));
                intermediateStops = List.copyOf(intermediateStops);
            }


        }

        /**
         * Représente une étape à pied.
         *
         * @param depStop l'arrêt de départ
         * @param depTime l'heure de départ
         * @param arrStop l'arrêt d'arrivée
         * @param arrTime l'heure d'arrivée
         * @throws NullPointerException     si un des paramètres est null
         * @throws IllegalArgumentException si depTime n'est pas avant ou égale à arrTime
         */
        record Foot(Stop depStop, LocalDateTime depTime, Stop arrStop,
                    LocalDateTime arrTime) implements Leg {
            public Foot {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Preconditions.checkArgument(depTime.isBefore(arrTime) || depTime.isEqual(arrTime));

            }

            /**
             * Donne la liste des arrêts intermédiaires de l'étape à pied
             * L'étape à pied ne pouvant pas avoir d'arrêts intermédiaires, la méthode
             * retourne une liste vide
             *
             * @return une liste vide représentant les arrêts intermédiaires.
             */
            public List<IntermediateStop> intermediateStops() {
                return List.of();
            }

            /**
             * Indique s'il s'agit d'un simple transfert (même nom pour l'arrêt de départ et
             * d'arrivée).
             *
             * @return true si les noms sont identiques, false sinon
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }


    }
}
