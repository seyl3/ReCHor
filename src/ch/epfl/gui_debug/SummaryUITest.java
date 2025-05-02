package ch.epfl.gui_debug;


/*
 * @author : Sarra Zghal, Elyes Ben Abid
 */

import ch.epfl.rechor.gui.DetailUI;
import ch.epfl.rechor.gui.SummaryUI;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Vehicle;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

public class SummaryUITest extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static List<Journey> journeysExample() {
        var s1 = new Stop("Ecublens VD, EPFL", "3", 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", "1", 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Fribourg", "7", 7.150000, 46.800000);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);
        var s7 = new Stop("Bern", "9", 7.440000, 46.948000);

        var inter1 = new Stop("UNIL-Dorigny", null, 6.568889, 46.525000);
        var inter2 = new Stop("Prilly-Malley", null, 6.596666, 46.529444);
        var inter3 = new Stop("Puidoux", null, 6.781667, 46.491389);
        var inter4 = new Stop("Palézieux", null, 6.837870, 46.542760);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);

        var journey1 = new Journey(List.of(
                new Journey.Leg.Transport(s1, d.atTime(8, 0), s2, d.atTime(8, 6),
                        List.of(new Journey.Leg.IntermediateStop(inter1, d.atTime(8, 3),
                                d.atTime(8, 4))),
                        Vehicle.METRO, "m1", "Renens VD, gare"),
                new Journey.Leg.Foot(s2, d.atTime(8, 6), s3, d.atTime(8, 10)),
                new Journey.Leg.Transport(s3, d.atTime(8, 15), s4, d.atTime(8, 25),
                        List.of(), Vehicle.TRAIN, "S1", "Lausanne")
        ));

        var journey2 = new Journey(List.of(
                new Journey.Leg.Transport(s1, d.atTime(9, 0), s2, d.atTime(9, 5),
                        List.of(), Vehicle.BUS, "L5", "Renens VD"),
                new Journey.Leg.Foot(s2, d.atTime(9, 5), s3, d.atTime(9, 10)),
                new Journey.Leg.Transport(s3, d.atTime(9, 15), s4, d.atTime(9, 30),
                        List.of(), Vehicle.BUS, "R8", "Lausanne"),
                new Journey.Leg.Foot(s4, d.atTime(9, 30), inter2, d.atTime(9, 35)),
                new Journey.Leg.Transport(inter2, d.atTime(9, 40), s5, d.atTime(10, 0),
                        List.of(), Vehicle.BUS, "R9", "Fribourg")
        ));

        var journey3 = new Journey(List.of(
                new Journey.Leg.Transport(s1, d.atTime(10, 0), s6, d.atTime(10, 45),
                        List.of(new Journey.Leg.IntermediateStop(inter3, d.atTime(10, 25),
                                        d.atTime(10, 30)),
                                new Journey.Leg.IntermediateStop(inter4, d.atTime(10, 35),
                                        d.atTime(10, 40))),
                        Vehicle.TRAIN, "IR15", "Romont"),
                new Journey.Leg.Foot(s6, d.atTime(10, 45), s5, d.atTime(10, 50)),
                new Journey.Leg.Transport(s5, d.atTime(10, 55), s7, d.atTime(11, 30),
                        List.of(), Vehicle.TRAIN, "IC5", "Bern")
        ));

        return List.of(journey1, journey2, journey3);
    }

    private static Journey journeyExample() {
        var s1 = new Stop("Ecublens VD, EPFL", "3", 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", "1", 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);

        var inter1 = new Stop("UNIL-Dorigny", null, 6.568889, 46.525000);
        var inter2 = new Stop("Prilly-Malley", null, 6.596666, 46.529444);
        var inter3 = new Stop("Puidoux", null, 6.781667, 46.491389);
        var inter4 = new Stop("Palézieux", null, 6.837870, 46.542760);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var l1 = new Journey.Leg.Transport(
                s1,
                d.atTime(16, 13),
                s2,
                d.atTime(16, 19),
                List.of(new Journey.Leg.IntermediateStop(inter1, d.atTime(16, 15),
                        d.atTime(16, 16))),
                Vehicle.METRO,
                "m1",
                "Renens VD, gare");

        var l2 = new Journey.Leg.Foot(s2, d.atTime(16, 19), s3, d.atTime(16, 22));

        var l3 = new Journey.Leg.Transport(
                s3,
                d.atTime(16, 26),
                s4,
                d.atTime(16, 33),
                List.of(new Journey.Leg.IntermediateStop(inter3, d.atTime(16, 50),
                        d.atTime(16, 52))),
                Vehicle.TRAIN,
                "R4",
                "Bex");

        var l4 = new Journey.Leg.Foot(s4, d.atTime(16, 33), s5, d.atTime(16, 38));

        var l5 = new Journey.Leg.Transport(
                s5,
                d.atTime(16, 40),
                s6,
                d.atTime(17, 13),
                List.of(
                        new Journey.Leg.IntermediateStop(inter3, d.atTime(16, 50),
                                d.atTime(16, 52)),
                        new Journey.Leg.IntermediateStop(inter4, d.atTime(17, 0), d.atTime(17, 2))
                ),
                Vehicle.TRAIN,
                "IR15",
                "Luzern");

        return new Journey(List.of(l1, l2, l3, l4, l5));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableValue<List<Journey>> journeysO =
                new SimpleObjectProperty<>(journeysExample());
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
