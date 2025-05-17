package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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

/**
 * Interface graphique de synthèse des voyages.
 * <p>
 * Affiche une liste de voyages sous forme de liste, incluant pour chaque voyage :
 * <ul>
 *   <li>heure de départ et d'arrivée</li>
 *   <li>durée et mode de transport principal</li>
 *   <li>visuel des transferts à pied sous forme de cercles</li>
 * </ul>
 * <p>
 * La sélection du voyage se fait automatiquement en fonction de l'heure désirée,
 * et peut être récupérée via la valeur observable fournie.
 *
 * @param rootNode         le nœud racine affichant la liste des voyages
 * @param selectedJourneyO observable du voyage actuellement sélectionné
 * @author Sarra Zghal, Elyes Ben Abid
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    /**
     * Construit l’UI de la liste de voyages.
     *
     * @param journeyList liste observable des voyages à afficher
     * @param desiredTime heure désirée pour sélectionner automatiquement le voyage le plus proche
     * @param loadingO    observable indiquant si le chargement est en cours
     * @return une instance de SummaryUI
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeyList,
                                   ObservableValue<LocalTime> desiredTime,
                                   ObservableBooleanValue loadingO,
                                   ObjectProperty<Number> progressO,
                                   ObservableValue<Boolean> arrivalO) {

        ListView<Journey> listView = new ListView<>();
        listView.getStylesheets().add("summary.css");
        listView.setCellFactory(lv -> new JourneyCell());

        VBox loadingOverlay = new VBox();
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.visibleProperty().bind(loadingO);
        loadingOverlay.getStylesheets().add("progress.css");
        loadingOverlay.setId("loading-overlay");

        Text loadingText = new Text("Calcul de trajet en cours...");
        loadingText.setId("loading-text");
        loadingOverlay.getChildren().add(loadingText);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setId("loading-bar");
        progressBar.setPrefWidth(150);
        progressBar.progressProperty().bind(progressO);
        loadingOverlay.getChildren().add(progressBar);

        StackPane summaryPane = new StackPane(listView, loadingOverlay);

        progressBar.visibleProperty().bind(loadingO);
        listView.visibleProperty().bind(Bindings.not(loadingO));

        // Met à jour le voyage séléctionné
        journeyList.subscribe(newList -> {
            listView.getItems().setAll(newList);
            updateSelection(listView, desiredTime, arrivalO.getValue());
        });

        // Met à jour et sélectionne automatiquement le voyage correspondant a l'heure désirée
        desiredTime.subscribe(newTime -> {
            updateSelection(listView, desiredTime, arrivalO.getValue());
        });
        arrivalO.subscribe(v -> updateSelection(listView, desiredTime, v));
        ObservableValue<Journey> selectedJourney =
                listView.getSelectionModel().selectedItemProperty();

        return new SummaryUI(summaryPane, selectedJourney);
    }

    /**
     * Méthode auxiliaire, sélectionne et fait défiler la liste jusqu’au voyage correspondant à
     * l’heure
     * désirée.
     *
     * @param listView     la ListView contenant les voyages
     * @param desiredTimeO observable de l’heure désirée pour la sélection
     * @param useArrivalTime booléen indiquant s'il faut utiliser l'heure d'arrivée pour la sélection
     */
    private static void updateSelection(ListView<Journey> listView,
                                        ObservableValue<LocalTime> desiredTimeO,
                                        boolean useArrivalTime) {

        List<Journey> journeys = listView.getItems();
        LocalTime target = desiredTimeO.getValue();

        if (journeys.isEmpty()) return;

        int journeyIndex = IntStream.range(0, journeys.size())
                .filter(i -> {
                    Journey j = journeys.get(i);
                    LocalTime time = useArrivalTime
                            ? j.arrTime().toLocalTime()
                            : j.depTime().toLocalTime();
                    return !time.isBefore(target);
                })
                .findFirst()
                .orElse(journeys.size() - 1);

        listView.getSelectionModel().select(journeyIndex);
        listView.scrollTo(journeyIndex);
    }

    /**
     * Cellule "customisée" pour afficher un résumé d’un voyage dans la liste.
     */
    private static final class JourneyCell extends ListCell<Journey> {
        private static final double HORIZONTAL_MARGIN = 5;
        private static final double VERTICAL_POSITION = 10;
        private static final double CIRCLE_RADIUS = 3;

        private final BorderPane journey;
        private final ImageView vehicleIcon;
        private final int iconSize = 20;
        private final Text routeAndDestination;
        private final Text departureTime;
        private final Text arrivalTime;
        private final Text durationTime;
        private final Pane transferLinePane;
        private final List<Circle> transferCircles;
        private final Circle startCircle;
        private final Circle endCircle;
        private final Line backgroundLine;

        /**
         * Initialise les composants graphiques de la cellule : icône du véhicule,
         * textes, ligne de transfert et cercles de changement.
         */
        public JourneyCell() {
            vehicleIcon = new ImageView();
            routeAndDestination = new Text();
            departureTime = new Text();
            arrivalTime = new Text();
            durationTime = new Text();
            transferCircles = new ArrayList<>();

            transferLinePane = new Pane() {
                @Override
                protected void layoutChildren() {
                    double width = getWidth();

                    startCircle.setCenterX(HORIZONTAL_MARGIN);
                    startCircle.setCenterY(VERTICAL_POSITION);

                    endCircle.setCenterX(width - HORIZONTAL_MARGIN);
                    endCircle.setCenterY(VERTICAL_POSITION);

                    for (Circle circle : transferCircles) {
                        double relativePosition = (double) circle.getUserData();
                        double x = HORIZONTAL_MARGIN + relativePosition * (width - 2 * HORIZONTAL_MARGIN);
                        circle.setCenterX(x);
                        circle.setCenterY(VERTICAL_POSITION);
                    }
                }
            };
            transferLinePane.setPrefSize(0, 0);

            startCircle = new Circle(CIRCLE_RADIUS);
            startCircle.getStyleClass().add("dep-arr");

            endCircle = new Circle(CIRCLE_RADIUS);
            endCircle.getStyleClass().add("dep-arr");

            backgroundLine = new Line(HORIZONTAL_MARGIN, VERTICAL_POSITION, HORIZONTAL_MARGIN, VERTICAL_POSITION);
            backgroundLine.endXProperty().bind(transferLinePane.widthProperty().subtract(HORIZONTAL_MARGIN));

            transferLinePane.getChildren().addAll(backgroundLine, startCircle, endCircle);

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

        /**
         * Met à jour l’affichage de la cellule pour chaque item de la liste.
         * Affiche les heures de départ/arrivée, la durée, l’icône du véhicule,
         * et les cercles de transfert pour chaque changement.
         *
         * @param item  le voyage à afficher dans la cellule
         * @param empty true si la cellule doit être vide
         */
        @Override
        protected void updateItem(Journey item, boolean empty) {
            super.updateItem(item, empty);
            transferLinePane.getChildren().setAll(backgroundLine, startCircle, endCircle);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                setGraphic(journey);

                departureTime.setText(formatTime(item.legs().getFirst().depTime()));
                arrivalTime.setText(formatTime(item.legs().getLast().arrTime()));
                durationTime.setText(formatDuration(item.duration()));

                List<Journey.Leg> legs = item.legs();
                Journey.Leg.Transport firstTransport = null;

                for (Journey.Leg leg : legs) {
                    if (leg instanceof Journey.Leg.Transport) {
                        firstTransport = (Journey.Leg.Transport) leg;
                        break;
                    }
                }

                vehicleIcon.setImage(iconFor(firstTransport.vehicle()));
                routeAndDestination.setText(formatRouteDestination(firstTransport));

                transferCircles.clear();

                LocalDateTime departureTime = legs.getFirst().depTime();
                double totalDurationMinutes = (double) item.duration().toMinutes();

                IntStream.range(1, legs.size() - 1).mapToObj(legs::get)
                        .filter(leg -> leg instanceof Journey.Leg.Foot)
                        .map(leg -> (Journey.Leg.Foot) leg)
                        .mapToDouble(footLeg -> Duration.between(departureTime, footLeg.depTime()).toMinutes())
                        .map(minutesFromStart -> minutesFromStart / totalDurationMinutes)
                        .forEach(relativePosition -> {
                            Circle changeCircle = new Circle(CIRCLE_RADIUS);
                            changeCircle.getStyleClass().add("transfer");
                            changeCircle.setUserData(relativePosition);
                            transferCircles.add(changeCircle);
                        });

                transferLinePane.getChildren().addAll(transferCircles);
            }
        }
    }
}
