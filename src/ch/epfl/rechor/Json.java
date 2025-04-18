package ch.epfl.rechor;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public sealed interface Json {

    record JArray(List<Json> jsons) implements Json {
        public JArray(List<Json> jsons) {
            this.jsons = List.copyOf(jsons);
        }

        @Override
        public String toString() {

            StringJoiner sj = new StringJoiner(",", "[", "]");

            for (Json element : jsons) {
                sj.add(element.toString());
            }
            return sj.toString();
        }
    }

    record JString(String string) implements Json {
        @Override
        public String toString() {
            return ("\"" + string + "\"");
        }
    }

    record JNumber(double number) implements Json {
        @Override
        public String toString() {
            return Double.toString(number);
        }
    }

    record JObject(Map<String, Json> map) implements Json {

        @Override
        public String toString() {
            return map.entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\"" + ":" + e.getValue())
                    .collect(Collectors.joining(",", "{", "}"));
        }

        private static String formatJsonValue(Json value) {
            return switch (value) {
                case JString s -> "\"" + s.toString() + "\"";
                default -> value.toString();
            };
        }
    }
}


