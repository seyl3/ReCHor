package ch.epfl.rechor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StopIndex {
    private final List<String> stopsNames;
    private final Map<String, String> alternativeNames;
    public static final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;


    public StopIndex(List<String> stopsNames, Map<String, String> alternativeNames) {
        this.stopsNames = List.copyOf(stopsNames);
        this.alternativeNames = Map.copyOf(alternativeNames);

    }

    public List<String> stopsMatching(String request, int limit) {
        if (request == null || request.isBlank()) return List.of();

        final List<String> stops = Stream.concat(stopsNames.stream(),alternativeNames.values().stream()).toList();
        Pattern pattern = Pattern.compile( buildRegex(request) , flags);
        // Étape 1 : découper la requête
        String [] subRequests = pattern.split("\\s+");

        // Étape 2 : créer les Patterns pour chaque sous-requête
        List<Pattern> subPatterns = Arrays.stream(subRequests)
                .map(StopIndex::buildRegex)
                .map(r -> Pattern.compile(r, flags))
                .toList();

        // Étape 3–4 : filtrer les noms (principaux + alternatifs)
        // On filtre tous les noms (noms principaux et alternatifs) qui matchent toutes les sous-requêtes
        return Stream.concat(stopsNames.stream(), alternativeNames.keySet().stream())
                .filter(name -> subPatterns.stream().allMatch(p -> p.matcher(name).find()))
                .map(name -> alternativeNames.getOrDefault(name, name)) // remplace le nom alternatif par son nom principal
                .distinct()
                .sorted(Comparator.comparingInt((String name) -> -pertinence(name, subRequests))) // tri par pertinence décroissante
                .limit(limit)
                .toList();
    }


    private static String buildRegex(String query) {
        final Map<Character,String> equivalences = Map.of('a', "[aáàâä]",
                'e', "[eéèêë]",
                'i', "[iíìîï]",
                'o', "[oóòôö]",
                'u', "[uúùûü]",
                'c', "[cç]");
        StringJoiner regex = new StringJoiner("+","","");
        for (char c : query.toCharArray()) {
            char lower = Character.toLowerCase(c);
            if (equivalences.containsKey(lower)) {
                regex.add(equivalences.get(lower));
            } else {
                regex.add(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }
    private static int pertinence(String stopName, String[] subRequests) {
        int score = 0;
        for (String subRequest : subRequests) {
            Pattern pattern = Pattern.compile(buildRegex(subRequest), flags);
            Matcher matcher = pattern.matcher(stopName);

            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                int baseScore = (100 * (end - start)) / stopName.length();

                boolean atWordStart = start == 0 || !Character.isLetter(stopName.charAt(start - 1));
                boolean atWordEnd = end == stopName.length() || !Character.isLetter(stopName.charAt(end));

                int factor = 1;
                if (atWordStart) factor *= 4;
                if (atWordEnd) factor *= 2;

                score += baseScore * factor;
            }
        }
        return score;
    }

}
