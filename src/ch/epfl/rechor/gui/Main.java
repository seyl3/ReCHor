package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class Main extends Application {
    private final Map<LocalDate, Map<Integer, Profile>> profileCache = new ConcurrentHashMap<>();
    // Date du voyage -> (l'indice de la station d'arrivée -> le profile)
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws IOException {
        TimeTable tt = FileTimeTable.in(Path.of("timetable_19"));

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

        // Création de la valeur observable des voyages
        // Vérification des paramètres
        ObservableValue<List<Journey>> journeysO = Bindings.createObjectBinding(() -> {
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
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
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
