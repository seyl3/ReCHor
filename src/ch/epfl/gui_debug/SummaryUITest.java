package ch.epfl.gui_debug;


/*
 * @author : Sarra Zghal, Elyes Ben Abid
 */

import ch.epfl.rechor.gui.DetailUI;
import ch.epfl.rechor.gui.SummaryUI;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

public class SummaryUITest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    static int stationId(Stations stations, String stationName) {
        for (int i = 0; i < stations.size(); i++) {
            if (stations.name(i).equals(stationName)) {
                System.out.println("Index trouvé : " + i);
                return i;
            }
        }
        return -1;

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        TimeTable timeTable =
                new CachedTimeTable(FileTimeTable.in(Path.of("timetable")));
        Stations stations = timeTable.stations();
        LocalDate date = LocalDate.of(2025, Month.MAY, 15);
        int depStationId = stationId(stations, "Morges, St-Jean nord");
        int arrStationId = stationId(stations, "Morges");
        Router router = new Router(timeTable);
        Profile profile = router.profile(date, arrStationId);
        List<Journey> journeys = JourneyExtractor
                .journeys(profile, depStationId);

        ObservableValue<List<Journey>> journeysO =
                new SimpleObjectProperty<>(journeys);
        ObservableValue<LocalTime> depTimeO =
                new SimpleObjectProperty<>(LocalTime.of(9, 00));
        SummaryUI summaryUI = SummaryUI.create(journeysO, depTimeO);
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        // Clear pour faire des test
        Button clearButton = new Button("Change Journeys");
        clearButton.setOnAction(e -> ((SimpleObjectProperty<List<Journey>>) journeysO).set(
                List.of()));

        SplitPane horizontalSplit = new SplitPane();
        horizontalSplit.getItems().addAll(summaryUI.rootNode(), detailUI.rootNode());

        SplitPane verticalSplit = new SplitPane();
        verticalSplit.setOrientation(Orientation.VERTICAL);
        verticalSplit.getItems().addAll(clearButton, horizontalSplit);

        Scene scene = new Scene(verticalSplit);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }
}
