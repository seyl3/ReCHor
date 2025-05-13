package ch.epfl.rechor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Un index permettant de rechercher des arrêts de transport public par nom de manière flexible.
 * <p>
 * La recherche est tolérante aux différences d'accents, de casse (si la requête ne contient pas
 * de majuscules),
 * à l'ordre des mots, et accepte les noms alternatifs des arrêts.
 *
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public class StopIndex {
    public static final int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static List<String> stopsNames;
    private final Map<String, String> alternativeNames;

    /**
     * Construit un index de noms d'arrêts avec leurs noms alternatifs.
     *
     * @param stopsNames       la liste des noms principaux d'arrêts à indexer
     * @param alternativeNames une table associant les noms alternatifs à leur nom principal
     * @throws NullPointerException si l'un des arguments est nul
     */
    public StopIndex(List<String> stopsNames, Map<String, String> alternativeNames) {
        this.stopsNames = List.copyOf(stopsNames);
        this.alternativeNames = Map.copyOf(alternativeNames);
    }

    /**
     * Construit une expression régulière à partir d'une requête textuelle, en tenant compte
     * des équivalences de caractères accentués / maj-min.
     * <p>
     * Chaque caractère de la requête est converti en un groupe d'alternatives s'il existe
     * dans la table des équivalences.Les groupes sont concaténés pour former une expression
     * régulière qui matche les chaînes contenant les caractères de la requête dans le bon
     * ordre.
     *
     * @param query la requête = la chaîne de caractères à convertir en expression régulière
     * @return une expression régulière correspondant à la requête, insensible aux accents
     * et aux majuscules
     * @throws NullPointerException si {@code query} est {@code null}
     */
    private static String buildRegex(String query) {
        final Map<Character, String> equivalences = Map.of('a', "[aáàâä]",
                'e', "[eéèêë]",
                'i', "[iíìîï]",
                'o', "[oóòôö]",
                'u', "[uúùûü]",
                'c', "[cç]");
        StringJoiner regex = new StringJoiner("","","");
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
    private static List<Pattern> buildPatterns(String request) {
        Pattern spaceSplitter = Pattern.compile("\\s+"); // un ou plusieurs espaces
        String[] subRequests = spaceSplitter.split(request.trim());

        // Étape 2 : créer les Patterns pour chaque sous-requête
        List<Pattern> subPatterns = new ArrayList<>();
        for(String subRequest : subRequests) {
            String regex = buildRegex(subRequest);
            boolean hasUpperCase = false;
            for(char c : subRequest.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    hasUpperCase = true;
                }
            }
            if(hasUpperCase) {
                subPatterns.add(Pattern.compile(regex));
            }else{
                subPatterns.add(Pattern.compile(regex, flags));
            }

        }
        return subPatterns;
    }
    /**
     * Calcule le score de pertinence d'un nom d'arrêt par rapport aux sous-requêtes données.
     * <p>
     * Pour chaque sous-requête, le score est calculé comme suit :
     * <ul>
     *   <li>Score de base : pourcentage du nom correspondant à la sous-requête</li>
     *   <li>Multiplicateur ×4 si la sous-requête est au début d'un mot</li>
     *   <li>Multiplicateur ×2 si la sous-requête est à la fin d'un mot</li>
     * </ul>
     * Le score final est la somme des scores de toutes les sous-requêtes.
     * Seule la première occurrence de chaque sous-requête est considérée.
     *
     * @param stopName    le nom de l'arrêt à évaluer
    // A REMPLIR
     * @return le score de pertinence total
     */
    private static int pertinence(String stopName, List<Pattern> patterns) {
        int score = 0;
        for (Pattern subPatttern : patterns ) {
            Matcher matcher = subPatttern.matcher(stopName);

            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                int baseScore = (100 * (end - start)) / stopName.length();

                // Vérification stricte du début de mot
                boolean atWordStart = start == 0 || !Character.isLetter(stopName.charAt(start - 1));
                // Vérification stricte de la fin de mot
                boolean atWordEnd = end == stopName.length() || !Character.isLetter(stopName.charAt(end));

                int factor = 1;
                if (atWordStart) factor *= 4;
                if (atWordEnd) factor *= 2;
                score += baseScore * factor;

            }else{
                return 0;
            }
        }
        return score;
    }

    /**
     * Retourne les noms d'arrêts correspondant à la requête donnée, triés par pertinence.
     * <p>
     * La requête est découpée en sous-requêtes selon les espaces. Un arrêt correspond s'il contient
     * toutes les sous-requêtes, en ignorant les accents et la casse. Les noms alternatifs sont
     * automatiquement convertis en leurs noms principaux dans les résultats.
     *
     * @param request la requête de recherche
     * @param limit   le nombre maximum de résultats à retourner
     * @return la liste des noms d'arrêts correspondants, triés par pertinence décroissante,
     * sans doublons et de taille au plus {@code limit}
     */
    public List<String> stopsMatching(String request, int limit) {
        List<Pattern> subPatterns = buildPatterns(request);

        // Étape 3–4 : filtrer les noms (principaux + alternatifs)
        // On filtre tous les noms (noms principaux et alternatifs) qui matchent toutes les
        // sous-requêtes
        return Stream.concat(stopsNames.stream(), alternativeNames.keySet().stream())
                .filter(name -> subPatterns.stream().anyMatch(p -> p.matcher(name).find()))
                .sorted(Comparator.comparingInt((String name) -> pertinence(name, subPatterns)).reversed()) // tri par pertinence décroissante
                .map(name -> alternativeNames.getOrDefault(name, name)) // remplace le nom alternatif par son nom principal
                .distinct()
                .limit(limit)
                .toList();
    }


}
