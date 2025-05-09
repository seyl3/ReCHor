package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.rechor.FormatterFr.*;
import static ch.epfl.rechor.gui.VehicleIcons.iconFor;
import static ch.epfl.rechor.journey.JourneyGeoJsonConverter.toGeoJson;
import static ch.epfl.rechor.journey.JourneyIcalConverter.toIcalendar;
import static java.awt.Desktop.getDesktop;
import static javafx.scene.layout.GridPane.*;

/**
 * Interface graphique détaillée d'un voyage.
 * <p>
 * Affiche les détails d'un voyage avec ses étapes, incluant :
 * <ul>
 *   <li>les heures et lieux de départ/arrivée</li>
 *   <li>les modes de transport avec leurs icônes</li>
 *   <li>les arrêts intermédiaires (dépliables)</li>
 *   <li>les connexions entre étapes</li>
 * </ul>
 *
 * @param rootNode le nœud racine du graphe de scène
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public record DetailUI(Node rootNode) {

    /**
     * Crée une nouvelle interface détaillée pour un voyage observable.
     * <p>
     * L'interface se met à jour automatiquement lorsque le voyage change.
     * En l'absence de voyage, affiche "Aucun voyage" au centre.
     *
     * @param journey0 le voyage à afficher, potentiellement nul
     * @return une nouvelle interface détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journey0) {
        ScrollPane root = new ScrollPane();
        root.setId("detail");
        root.getStylesheets().add("detail.css");

        StackPane mainStack = new StackPane();
        root.setContent(mainStack);

        VBox noJourney = new VBox();
        noJourney.setId("no-journey");
        mainStack.getChildren().add(noJourney);

        Text noJourneyText = new Text("Aucun voyage.");
        noJourney.getChildren().add(noJourneyText);

        VBox withJourney = new VBox();
        mainStack.getChildren().add(withJourney);

        StackPane journeyStack = new StackPane();
        withJourney.getChildren().add(journeyStack);

        final Pane annotations = new Pane();
        annotations.setId("annotations");
        journeyStack.getChildren().add(annotations);

        StepGrid legsGrid = new StepGrid(annotations);
        legsGrid.setId("legs");
        journeyStack.getChildren().add(legsGrid);

        HBox buttons = new HBox();
        buttons.setId("buttons");
        withJourney.getChildren().add(buttons);

        Button calendarButton = new Button("Calendrier");
        Button mapButton = new Button("Carte");
        buttons.getChildren().addAll(calendarButton, mapButton);
        wireButtons(calendarButton, mapButton, journey0);

        noJourney.visibleProperty().bind(
                Bindings.createBooleanBinding(() -> journey0.getValue() == null, journey0)
        );

        withJourney.visibleProperty().bind(
                Bindings.createBooleanBinding(() -> journey0.getValue() != null, journey0)
        ); // peut être a supprimer ?

        journey0.subscribe(newJourney -> {
            legsGrid.clearPairs();
            legsGrid.getChildren().clear();

            if (journey0.getValue() != null) {
                int currentRow = 0;

                for (Journey.Leg leg : journey0.getValue().legs()) {
                    switch (leg) {
                        case Journey.Leg.Foot footLeg -> {
                            Text foot = new Text(formatLeg((Journey.Leg.Foot) leg));
                            legsGrid.add(foot, 2, currentRow);
                            setColumnSpan(foot, 2);
                            currentRow++;
                        }
                        case Journey.Leg.Transport transportLeg -> {
                            int imageSize = 31;

                            // Ajout de l'heure de départ, cercle de départ, nom de la gare et sa
                            // plateforme
                            // de départ
                            Circle depCircle = addStopRow(legsGrid, currentRow, leg.depTime(), leg.depStop(), true);

                            // Ajout de l'icone du véhicule, et du nom de la destination
                            ImageView vehicleIcon =
                                    new ImageView(iconFor(((Journey.Leg.Transport) leg).vehicle()));
                            vehicleIcon.setPreserveRatio(true);
                            vehicleIcon.setFitWidth(imageSize);
                            legsGrid.add(vehicleIcon, 0, currentRow + 1);
                            setHalignment(vehicleIcon, HPos.CENTER);

                            Text transport =
                                    new Text(formatRouteDestination((Journey.Leg.Transport) leg));
                            legsGrid.add(transport, 2, currentRow + 1);
                            setColumnSpan(transport, 2);

                            // Ajout d'un menu dépliant, si il y'a des arrêts intermédiaires
                            if (!leg.intermediateStops().isEmpty()) {
                                setRowSpan(vehicleIcon, 2);

                                Accordion interAccordion = new Accordion();
                                legsGrid.add(interAccordion, 2, currentRow + 2);
                                setColumnSpan(interAccordion, 2);

                                GridPane interGrid = new GridPane();
                                interGrid.getStyleClass().add("intermediate-stops");
                                TitledPane titledGrid =
                                        new TitledPane(leg.intermediateStops().size() +
                                                " arrêts, " + formatDuration(leg.duration()),
                                                interGrid);

                                int interCurrentRow = 0;
                                for (Journey.Leg.IntermediateStop intermediateStop :
                                        leg.intermediateStops()) {
                                    Text interArrTime =
                                            new Text(formatTime(intermediateStop.arrTime()));
                                    interGrid.add(interArrTime, 0, interCurrentRow);

                                    Text interDepTime =
                                            new Text(formatTime(intermediateStop.depTime()));
                                    interGrid.add(interDepTime, 1, interCurrentRow);

                                    Text interDepStation =
                                            new Text(intermediateStop.stop().name());
                                    interGrid.add(interDepStation, 2, interCurrentRow);

                                    interCurrentRow++;
                                }
                                interAccordion.getPanes().add(titledGrid);
                                currentRow++;
                            }

                            // Ajout de l'heure d'arrivée, cercle d'arrivée, nom de la gare et sa
                            // plateforme
                            // d'arrivée
                            Circle arrCircle = addStopRow(legsGrid, currentRow + 2, leg.arrTime(),
                                    leg.arrStop(), false);

                            currentRow = currentRow + 3;
                            legsGrid.addPair(depCircle, arrCircle);
                        }
                    }
                }
            }
        });

        return new DetailUI(root);
    }

    private static Circle addStopRow(GridPane grid,
                                     int row,
                                     LocalDateTime time,
                                     Stop stop,
                                     boolean isDeparture) {

        Text timeTxt = new Text(formatTime(time));
        Text platformTxt = new Text(formatPlatformName(stop));
        Text stationTxt = new Text(stop.name());
        Circle circle = new Circle(3, Color.BLACK);

        if (isDeparture) {
            timeTxt.getStyleClass().add("departure");
            platformTxt.getStyleClass().add("departure");
        }

        grid.add(timeTxt, 0, row);
        grid.add(circle, 1, row);
        grid.add(stationTxt, 2, row);
        grid.add(platformTxt, 3, row);

        return circle;
    }

    private static void wireButtons(Button calendarButton,
                                    Button mapButton,
                                    ObservableValue<Journey> journeyO) {

        calendarButton.setOnAction(e -> {
            Journey j = journeyO.getValue();
            if (j == null) return;

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisissez l'emplacement d'enregistrement du fichier.");
            chooser.setInitialFileName(
                    "voyage_" + j.depTime().format(DateTimeFormatter.ISO_DATE) + ".ics");

            File target = chooser.showSaveDialog(calendarButton.getScene().getWindow());
            if (target != null) {
                try {
                    Files.writeString(target.toPath(), toIcalendar(j), StandardOpenOption.CREATE);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
        });

        mapButton.setOnAction(e -> {
            Journey j = journeyO.getValue();
            if (j == null) return;

            try {
                URI uri = new URI("https", "umap.osm.ch", "/fr/map",
                                  "data=" + toGeoJson(j), "null");
                getDesktop().browse(uri);
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
    }


    // Classe interne pour la grille des étapes, permettant de gérer les annotations
    private static final class StepGrid extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotationLayer;
        private final List<Line> lineList = new ArrayList<>();


        public StepGrid(Pane annotationLayer) {
            this.annotationLayer = annotationLayer;
        }

        public void addPair(Circle dep, Circle arr) {
            circlePairs.add(new Pair<>(dep, arr));
        }

        public void clearPairs() {
            circlePairs.clear();
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            lineList.clear();

            for (Pair<Circle, Circle> pair : circlePairs) {
                Circle start = pair.getKey();
                Circle end = pair.getValue();

                double startX = start.getBoundsInParent().getCenterX();
                double startY = start.getBoundsInParent().getCenterY();
                double endX = end.getBoundsInParent().getCenterX();
                double endY = end.getBoundsInParent().getCenterY();

                Line line = new Line(startX, startY, endX, endY);
                line.setStroke(Color.RED);
                line.setStrokeWidth(2);

                lineList.add(line);
            }

            annotationLayer.getChildren().setAll(lineList);
        }
    }
}
