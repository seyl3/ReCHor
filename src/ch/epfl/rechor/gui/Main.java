package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Classe principale de l'application ReCHor.
 * <p>
 * Cette classe hérite de {@link javafx.application.Application} et configure l'interface graphique
 * en combinant les composants {@link QueryUI}, {@link SummaryUI} et {@link DetailUI}.
 * Elle charge également les données horaires à partir du dossier <code>timetable</code>,
 * construit les profils de voyage en cache pour éviter les recomputations,
 * et établit les liaisons entre les éléments de l’interface via des valeurs observables.
 * </p>
 *
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *     <li>Charge les données horaires depuis le dossier "timetable".</li>
 *     <li>Crée et connecte les éléments de l'interface utilisateur.</li>
 *     <li>Maintient un cache des profils pour améliorer les performances.</li>
 *     <li>Lie dynamiquement la liste des voyages à afficher à l’entrée utilisateur.</li>
 * </ul>
 *
 * <h2>Structure de l’interface graphique :</h2>
 * <pre>
 * ┌────────────────────────────┐
 * │        QueryUI             │ ← zone de recherche (top)
 * └────────────────────────────┘
 * ┌────────────┬──────────────┐
 * │ SummaryUI  │  DetailUI    │ ← vue des trajets et détails (center, SplitPane)
 * └────────────┴──────────────┘
 * </pre>
 *
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public class Main extends Application {
    /**
     * Largeur minimale de la fenêtre principale (en px).
     */
    private static final double MIN_WINDOW_WIDTH = 800;
    /**
     * Hauteur minimale de la fenêtre principale (en px).
     */
    private static final double MIN_WINDOW_HEIGHT = 600;
    /**
     * Cache qui permet de gagner en rapidité et en efficacité
     * Structure de la Map : Date du voyage → (l'indice de la station d'arrivée → le profile)
     */
//
    private final Map<LocalDate, Map<Integer, Profile>> profileCache = new ConcurrentHashMap<>();
    private ObservableValue<List<Journey>> journeysO;

    /**
     * Point d'entrée principal de l'application ReCHor.
     * <p>
     * Appelle simplement la méthode {@link #launch(String...)} pour démarrer
     * l'application JavaFX.
     * </p>
     *
     * @param args les arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Méthode appelée automatiquement au lancement de l'application JavaFX.
     * <p>
     * Cette méthode :
     * <ul>
     *     <li>Charge les données horaires depuis le dossier <code>timetable</code>.</li>
     *     <li>Construit et connecte les trois composants principaux de l'interface graphique :
     *         {@link QueryUI}, {@link SummaryUI} et {@link DetailUI}.</li>
     *     <li>Crée une valeur observable contenant la liste des {@link Journey}
     *         en fonction de l’entrée utilisateur, en mettant en cache les profils calculés
     *         pour chaque date et station d’arrivée.</li>
     *     <li>Configure la scène principale et donne le focus au champ de recherche de départ.</li>
     * </ul>
     * </p>
     *
     * @param primaryStage la fenêtre principale de l'application
     * @throws IOException si le chargement des données horaires échoue
     */
    public void start(Stage primaryStage) throws IOException {
        TimeTable tt = FileTimeTable.in(Path.of("timetable"));

        List<String> stopNames = IntStream.range(0, tt.stations().size())
                .mapToObj(i -> tt.stations().name(i))
                .toList();

        Map<String, String> alternativeNames =
                IntStream.range(0, tt.stationAliases().size())
                        .boxed()
                        .collect(Collectors.toMap(
                                i -> tt.stationAliases().alias(i),
                                i -> tt.stationAliases().stationName(i)
                        ));


        StopIndex stopIndex = new StopIndex(stopNames, alternativeNames);
        QueryUI queryUI = QueryUI.create(stopIndex);


        Router router = new Router(tt);

        // Création de la valeur observable des voyages
        // Vérification des paramètres
        journeysO = Bindings.createObjectBinding(() -> {
            String depStop = queryUI.depStopO().getValue();
            String arrStop = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();
            LocalTime time = queryUI.timeO().getValue();

            // Vérification des paramètres
            if (depStop.isEmpty() || arrStop.isEmpty() || date == null || time == null) {
                return List.of();
            }

            try {
                String depStopMain = alternativeNames.getOrDefault(depStop, depStop);
                String arrStopMain = alternativeNames.getOrDefault(arrStop, arrStop);

                int depId = stopNames.indexOf(depStopMain);
                int arrId = stopNames.indexOf(arrStopMain);

                Profile profile = profileCache
                        .computeIfAbsent(date, d -> new ConcurrentHashMap<>())
                        .computeIfAbsent(arrId, id -> router.profile(date, id));

                return JourneyExtractor.journeys(profile, depId);
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }, queryUI.depStopO(), queryUI.arrStopO(), queryUI.dateO(), queryUI.timeO());

        SummaryUI summaryUI = SummaryUI.create(journeysO, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());


        primaryStage.setTitle("ReCHor");
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        BorderPane root = new BorderPane();
        root.setTop(queryUI.rootNode());
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(summaryUI.rootNode(), detailUI.rootNode());
        root.setCenter(splitPane);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(() -> scene.lookup("#depStop").requestFocus());


    }
}
