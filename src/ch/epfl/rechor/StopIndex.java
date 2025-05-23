package ch.epfl.rechor;

import java.util.*;
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
    public static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private final List<String> stopsNames;
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
        final Map<Character, String> equivalences = Map.of(
                'a', "[aáàâä]",
                'e', "[eéèêë]",
                'i', "[iíìîï]",
                'o', "[oóòôö]",
                'u', "[uúùûü]",
                'c', "[cç]");
        StringJoiner regex = new StringJoiner("", "", "");
        for (char c : query.toCharArray()) {
            if (equivalences.containsKey(c)) {
                regex.add(equivalences.get(c));
            } else {
                regex.add(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }

    /**
     * Construit une liste de patterns pour une requête de la part de l'utilisateur
     * La requête est divisée en sous-requête à chaque espace entré par l'utilisateur.
     * <p>
     * Chaque sous-requête est alors transformée :
     * <ol>
     *     <li>en expression régulière (<i>Regular Expression</i>) via {@code builRegex()} </li>
     *     <li>en pattern avec des fanions activés si et seulement si l'utilisateur ne
     *     demande pas explicitement des majuscules → activés, la différence entre les
     *     majuscules et les minuscules est ignorée lors de la recherche</li>
     * </ol>
     * La liste de pattern ainsi construite est retournée.
     *
     * @param request la chaine de caractères entrée par l'utilisateur dans sa recherche
     * @return la liste des {@link java.util.regex.Pattern}  compilés en fonction de la chaine de caractères entrée en requête
     */
    private static List<Pattern> buildPatterns(String request) {
        Pattern spaceSplitter = Pattern.compile("\\s+"); // un ou plusieurs espaces
        String[] subRequests = spaceSplitter.split(request.trim());

        Iterator<String> it = Arrays.asList(subRequests).iterator();

        return Arrays.stream(subRequests)
                .map(StopIndex::buildRegex)
                .map(regex -> {
                    String subRequest = it.next();
                    return subRequest.chars().anyMatch(Character::isUpperCase) ?
                            Pattern.compile(regex) : Pattern.compile(regex,FLAGS);
                })
                .toList();
    }

    /**
     * Calcule le score de pertinence d'un nom d'arrêt par rapport aux patterns des sous-requêtes donnés.
     * <p>
     * Pour chaque pattern de sous-requête, le score est calculé comme suit :
     * <ul>
     *   <li>Score de base : pourcentage du nom correspondant à la sous-requête</li>
     *   <li>Multiplicateur ×4 si la sous-requête est au début d'un mot</li>
     *   <li>Multiplicateur ×2 si la sous-requête est à la fin d'un mot</li>
     *   <li> 0 si une des sous requête de correspond pas au nom de station</li>
     * </ul>
     * Le score final est la somme des scores de toutes les sous-requêtes.
     * Seule la première occurrence de chaque sous-requête est considérée.
     *
     * @param stopName    le nom de l'arrêt à évaluer
     * @param subPatterns une liste des patterns correspondant aux sous requêtes
     * @return le score de pertinence total
     */
    private static int pertinence(String stopName, List<Pattern> subPatterns) {
        int score = 0;
        for (Pattern subPattern : subPatterns) {
            Matcher matcher = subPattern.matcher(stopName);

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

            } else {
                return 0;
            }
        }
        return score;
    }

    /**
     * Retourne les noms d'arrêts correspondant à la requête donnée, triés par pertinence.
     * <p>
     * Un arrêt correspond s'il contient toutes les sous-requêtes, en ignorant les accents
     * et la casse. Les noms alternatifs sont automatiquement convertis en leurs noms
     * principaux dans les résultats.
     *
     * @param request la requête de recherche
     * @param limit   le nombre maximum de résultats à retourner
     * @return la liste des noms d'arrêts correspondants, triés par pertinence décroissante,
     * sans doublons et, au plus, de taille {@code limit}
     */
    public List<String> stopsMatching(String request, int limit) {
        //Construire les patterns correspondants à la requête
        List<Pattern> subPatterns = buildPatterns(request);

        //1. recherche dans tous les noms de stations (alternatifs et principaux)
        //2. trouve ceux qui matchent
        //3. tri par pertinence décroissante
        //4. remplace tout nom alternatif par son nom principal
        //5. enlève les doublons
        //6. limite la taille
        return Stream.concat(stopsNames.stream(), alternativeNames.keySet().stream())
                .filter(name -> subPatterns.stream().allMatch(p -> p.matcher(name).find()))
                .sorted(Comparator.comparingInt((String name) -> pertinence(name, subPatterns)).reversed())
                .map(name -> alternativeNames.getOrDefault(name, name))
                .distinct()
                .limit(limit)
                .toList();
    }


}
