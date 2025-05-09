package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static ch.epfl.rechor.FormatterFr.*;
import static ch.epfl.rechor.gui.VehicleIcons.iconFor;

public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    public static SummaryUI create(ObservableValue<List<Journey>> journeyList,
                                   ObservableValue<LocalTime> desiredTime) {

        ListView<Journey> listView = new ListView<>();
        listView.getStylesheets().add("summary.css");
        listView.setCellFactory(lv -> new JourneyCell());

        journeyList.subscribe(newList -> listView.getItems().setAll(newList));

        ObservableValue<Journey> selectedJourney =
                listView.getSelectionModel().selectedItemProperty();

        // Sélectionne automatiquement le voyage correspondant a l'heure désirée
        // est ce que on garde ça en subscribre uniquement du temps ou bien
        // quand on change de journeys ça doit se faire automatiquement aussi ?
        desiredTime.subscribe(newTime -> {
            List<Journey> journeys = listView.getItems();
            if (journeys.isEmpty()) return;

            for (Journey journey : journeys) {
                LocalTime departure = journey.depTime().toLocalTime();
                if (!departure.isBefore(newTime)) {
                    listView.getSelectionModel().select(journey);
                    listView.scrollTo(journey);
                    return;
                }
            }

            // Si aucun voyage ne correspond, on prend le dernier
            listView.getSelectionModel().select(journeys.getLast());
            listView.scrollTo(journeys.getLast());
        });

        return new SummaryUI(listView, selectedJourney);
    }

    private static final class JourneyCell extends ListCell<Journey> {
        private final BorderPane journey;
        private final ImageView vehicleIcon;
        private final int iconSize = 20;
        private final Text routeAndDestination;
        private final Text departureTime;
        private final Text arrivalTime;
        private final Text durationTime;
        private final Pane transferLinePane;
        private final List<Circle> transferCircles = new ArrayList<>();
        private Circle startCircle;
        private Circle endCircle;

        public JourneyCell() {
            vehicleIcon = new ImageView();
            routeAndDestination = new Text();
            departureTime = new Text();
            arrivalTime = new Text();
            durationTime = new Text();

            transferLinePane = new Pane() {
                @Override
                protected void layoutChildren() {
                    double width = getWidth();

                    startCircle.setCenterX(5);
                    startCircle.setCenterY(10);

                    endCircle.setCenterX(width - 5);
                    endCircle.setCenterY(10);

                    for (Circle circle : transferCircles) {
                        double relativePosition = (double) circle.getUserData();
                        double x = 5 + relativePosition * (width - 10);
                        circle.setCenterX(x);
                        circle.setCenterY(10);
                    }
                }
            };
            transferLinePane.setPrefSize(0, 0);

            BorderPane journey = new BorderPane();
            journey.getStyleClass().add("journey");
            this.journey = journey;

            HBox route = new HBox();
            route.getStyleClass().add("route");
            vehicleIcon.setPreserveRatio(true);
            vehicleIcon.setFitWidth(iconSize);
            route.getChildren().addAll(vehicleIcon, routeAndDestination);
            journey.setTop(route);

            departureTime.getStyleClass().add("departure");
            journey.setLeft(departureTime);

            journey.setRight(arrivalTime);

            HBox duration = new HBox();
            duration.getStyleClass().add("duration");
            duration.getChildren().add(durationTime);
            journey.setBottom(duration);

            journey.setCenter(transferLinePane);
        }

        @Override
        protected void updateItem(Journey item, boolean empty) {
            super.updateItem(item, empty);
            transferLinePane.getChildren().clear();
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(journey);

                departureTime.setText(formatTime(item.legs().getFirst().depTime()));
                arrivalTime.setText(formatTime(item.legs().getLast().arrTime()));
                durationTime.setText(formatDuration(item.duration()));

                Journey.Leg firstLeg = item.legs().get(0);
                if(firstLeg instanceof Journey.Leg.Transport) {
                    vehicleIcon.setImage(iconFor(((Journey.Leg.Transport) firstLeg).vehicle()));
                    routeAndDestination.setText(
                            formatRouteDestination((Journey.Leg.Transport) firstLeg));
                } else {
                    vehicleIcon.setImage(iconFor(((Journey.Leg.Transport) item.legs().get(1)).vehicle()));
                    routeAndDestination.setText(
                            formatRouteDestination((Journey.Leg.Transport) item.legs().get(1)));
                }

                //TODO : nettoyer ça, cast un peu degeu non?

                Line line = new Line(5, 10, 195, 10);
                line.endXProperty().bind(transferLinePane.widthProperty().subtract(5));
                //TODO : revoir ça
                transferLinePane.getChildren().add(line);

                startCircle = new Circle(3);
                startCircle.getStyleClass().add("dep-arr");

                endCircle = new Circle(3);
                endCircle.getStyleClass().add("dep-arr");

                transferCircles.clear();

                if (!item.legs().isEmpty()) { // vérification utile ?
                    List<Journey.Leg> legs = item.legs();
                    LocalDateTime departureTime = legs.getFirst().depTime();
                    double totalDurationMinutes = (double) item.duration().toMinutes();

                    IntStream.range(1, legs.size() - 1).mapToObj(legs::get)
                            .filter(leg -> leg instanceof Journey.Leg.Foot)
                            .map(leg -> (Journey.Leg.Foot) leg)
                            .mapToDouble(footLeg -> Duration.between(departureTime, footLeg.depTime()).toMinutes())
                            .map(minutesFromStart -> minutesFromStart / totalDurationMinutes)
                            .forEach(relativePosition -> {
                                Circle changeCircle = new Circle(3);
                                changeCircle.getStyleClass().add("transfer");
                                changeCircle.setUserData(relativePosition);
                                transferCircles.add(changeCircle);
                            });
                }

                transferLinePane.getChildren().add(startCircle);
                transferLinePane.getChildren().add(endCircle);
                transferLinePane.getChildren().addAll(transferCircles);
            }
        }
    }
}
