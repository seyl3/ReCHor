package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Représente un document JSON.
 * <p>
 * Cette interface scellée est implémentée par quatre enregistrements imbriqués
 * représentant les types de données JSON utiles au projet : tableaux, objets,
 * chaînes et nombres.
 *
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public sealed interface Json {

    /**
     * Représente un tableau JSON.
     *
     * @param elements la liste des éléments du tableau
     */
    record JArray(List<Json> elements) implements Json {
        public JArray(List<Json> elements) {
            this.elements = List.copyOf(elements);
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(",", "[", "]");

            for (Json element : elements) {
                sj.add(element.toString());
            }
            return sj.toString();
        }
    }

    /**
     * Représente un objet JSON, similaire à une table associative.
     *
     * @param attributes la table associant des chaînes à des valeurs JSON
     */
    record JObject(Map<String, Json> attributes) implements Json {
        @Override
        public String toString() {
            return attributes.entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\"" + ":" + e.getValue().toString())
                    .collect(Collectors.joining(",", "{", "}"));
        }
    }

    /**
     * Représente une chaîne de caractères JSON.
     *
     * @param value la chaîne de caractères
     */
    record JString(String value) implements Json {
        @Override
        public String toString() {
            return ("\"" + value + "\"");
        }
    }

    /**
     * Représente un nombre JSON.
     *
     * @param value la valeur numérique
     */
    record JNumber(double value) implements Json {
        @Override
        public String toString() {
            return Double.toString(value);
        }
    }
}


