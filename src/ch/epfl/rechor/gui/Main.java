package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.*;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
 * <b>Fonctionnalités principales :</b>
 * <ul>
 *     <li>Charge les données horaires depuis le dossier "timetable".</li>
 *     <li>Crée et connecte les éléments de l'interface utilisateur.</li>
 *     <li>Maintient un cache des profils pour améliorer les performances.</li>
 *     <li>Lie dynamiquement la liste des voyages à afficher à l’entrée utilisateur.</li>
 * </ul>
 *
 * <b>Structure de l’interface graphique :</b>
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
    // Strcuture dela map (Date du voyage -> (l'indice de la station d'arrivée -> le profile))
    private final Map<LocalDate, Map<Integer, Profile>> profileCache = new ConcurrentHashMap<>();
    private final SimpleObjectProperty<List<Journey>> journeysO = new SimpleObjectProperty<>(List.of());
    private final SimpleBooleanProperty loadingO = new SimpleBooleanProperty(false);
    /** Avancement actuel du calcul CSA. */
    private final ObjectProperty<Number> progressO = new SimpleObjectProperty<>(-1);

    /**
     * Largeur minimale de la fenêtre principale (en px).
     */
    private static final double MIN_WINDOW_WIDTH = 800;
    /**
     * Hauteur minimale de la fenêtre principale (en px).
     */
    private static final double MIN_WINDOW_HEIGHT = 600;

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

        Map<String, String> alternativeNames = new HashMap<>();
        for (int i = 0; i < tt.stationAliases().size(); i++) {
            String alias = tt.stationAliases().alias(i);
            String stationName = tt.stationAliases().stationName(i);
            alternativeNames.put(alias, stationName);
        } // boucle remplaçable par un stream mais pour le moment c'est plus rapide comme ça

        StopIndex stopIndex = new StopIndex(stopNames, alternativeNames);
        QueryUI queryUI = QueryUI.create(stopIndex);


        Router router = new Router(tt);

        // AJOUT PERSONNEL : Bonus
        // --- Recherche asynchrone des voyages
        ObjectProperty<Task<List<Journey>>> currentTask = new SimpleObjectProperty<>();
        Runnable launchSearch = () -> {
            String depStop = queryUI.depStopO().getValue();
            String arrStop = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();
            LocalTime time = queryUI.timeO().getValue();

            // paramètres incomplets -> liste vide et on ne lance rien
            if (depStop.isEmpty() || arrStop.isEmpty() || date == null || time == null) {
                journeysO.set(List.of());
                loadingO.set(false);
                return;
            }

            // --- 2) Si le profil est déjà en cache, pas besoin de lancer une tâche asynchrone :
            String depMain = alternativeNames.getOrDefault(depStop, depStop);
            String arrMain = alternativeNames.getOrDefault(arrStop, arrStop);
            int depId = stopNames.indexOf(depMain);
            int arrId = stopNames.indexOf(arrMain);
            Map<Integer, Profile> byDate = profileCache.get(date);
            if (byDate != null && byDate.containsKey(arrId)) {
                Profile cachedProfile = byDate.get(arrId);
                journeysO.set(JourneyExtractor.journeys(cachedProfile, depId));
                loadingO.set(false);
                return;                     // rien de long : on s'arrête ici
            }

            // annule la recherche précédente le cas échéant
            if (currentTask.get() != null && currentTask.get().isRunning()) {
                currentTask.get().cancel();
            }

            loadingO.set(true);
            progressO.set(-1);

            Task<List<Journey>> task = new Task<>() {
                @Override
                protected List<Journey> call() {
                    ProgressListener listener = p -> updateProgress(p, 1); // p est déjà entre 0 et 1
                    Profile profile = profileCache
                            .computeIfAbsent(date, d -> new ConcurrentHashMap<>())
                            .computeIfAbsent(arrId, id -> router.profile(date, id, listener));
                    return JourneyExtractor.journeys(profile, depId);
                }
            };
            progressO.bind(task.progressProperty());

            task.setOnSucceeded(e -> {
                journeysO.set(task.getValue());
                loadingO.set(false);
                progressO.unbind();
                progressO.set(1);   // terminé
            });
            task.setOnFailed(e -> {
                journeysO.set(List.of());
                loadingO.set(false);
                progressO.unbind();
                progressO.set(1);   // terminé
            });

            currentTask.set(task);
            new Thread(task, "CSA-worker").start();
        };

        // on relance la recherche dès qu'un paramètre change
        queryUI.depStopO().addListener((o, oldV, newV) -> launchSearch.run());
        queryUI.arrStopO().addListener((o, oldV, newV) -> launchSearch.run());
        queryUI.dateO().addListener((o, oldV, newV) -> launchSearch.run());
        queryUI.timeO().addListener((o, oldV, newV) -> launchSearch.run());
        queryUI.arrivalModeO().addListener((o, oldV, newV) -> launchSearch.run());
        queryUI.excludedVehiclesO().addListener((SetChangeListener<Vehicle>) change -> launchSearch.run());

        // première recherche (si champs pré‑remplis)
        launchSearch.run();

        SummaryUI summaryUI = SummaryUI.create(
            journeysO,
            queryUI.timeO(),
            loadingO,
            progressO,
            queryUI.arrivalModeO(),
            queryUI.excludedVehiclesO()
        );
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
