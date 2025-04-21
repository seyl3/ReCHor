package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Journey;
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
     * @param journey le voyage à afficher, potentiellement nul
     * @return une nouvelle interface détaillée
     */
    public static DetailUI create(ObservableValue<Journey> journey) {
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

        Pane annotations = new Pane();
        annotations.setId("annotations");
        journeyStack.getChildren().add(annotations);

        StepGrid legsGrid = new StepGrid(annotations);
        legsGrid.setId("legs");
        journeyStack.getChildren().add(legsGrid);

        if (journey.getValue() != null) {
            noJourney.setVisible(false);
            withJourney.setVisible(true);


            int currentRow = 0;

            for (Journey.Leg leg : journey.getValue().legs()) {
                switch (leg) {
                    case Journey.Leg.Foot footLeg -> {
                        // mauvaise pratique de cast ou pas ??
                        Text footText = new Text(formatLeg((Journey.Leg.Foot) leg));
                        legsGrid.add(footText, 2, currentRow);
                        setColumnSpan(footText, 2);
                        currentRow++;
                    }
                    case Journey.Leg.Transport transportLeg -> {

                        // Ajout de l'heure de départ, cercle de départ, nom de la gare et sa
                        // plateforme
                        // de départ
                        Text depTimeText = new Text(formatTime(leg.depTime()));
                        depTimeText.getStyleClass().add("departure");
                        Text depPlatformText = new Text(formatPlatformName(leg.depStop()));
                        depPlatformText.getStyleClass().add("departure");
                        Text depStationText = new Text(leg.depStop().name());
                        Circle depCircle = new Circle(3, Color.BLACK);

                        legsGrid.add(depTimeText, 0, currentRow);
                        legsGrid.add(depCircle, 1, currentRow);
                        legsGrid.add(depStationText, 2, currentRow);
                        legsGrid.add(depPlatformText, 3, currentRow);

                        // Ajout de l'icone du véhicule, et du nom de la destination
                        ImageView vehicleIcon =
                                new ImageView(iconFor(((Journey.Leg.Transport) leg).vehicle()));
                        vehicleIcon.setFitWidth(31);
                        vehicleIcon.setPreserveRatio(true);
                        legsGrid.add(vehicleIcon, 0, currentRow + 1);
                        setHalignment(vehicleIcon, HPos.CENTER);

                        Text transportText =
                                new Text(formatRouteDestination((Journey.Leg.Transport) leg));
                        legsGrid.add(transportText, 2, currentRow + 1);
                        setColumnSpan(transportText, 2);

                        // Ajout d'un menu dépliant, si il y'a des arrêts intermédiaires
                        if (!leg.intermediateStops().isEmpty()) {
                            setRowSpan(vehicleIcon, 2);

                            Accordion interAccordion = new Accordion();
                            legsGrid.add(interAccordion, 2, currentRow + 2);
                            setColumnSpan(interAccordion, 2);

                            GridPane interGrid = new GridPane();
                            interGrid.getStyleClass().add("intermediate-stops");
                            TitledPane titledGrid = new TitledPane(leg.intermediateStops().size() +
                                    " arrêts, " + formatDuration(leg.duration()),
                                    interGrid);

                            int interCurrentRow = 0;
                            for (Journey.Leg.IntermediateStop intermediateStop :
                                    leg.intermediateStops()) {
                                Text interArrTimeText =
                                        new Text(formatTime(intermediateStop.arrTime()));
                                interGrid.add(interArrTimeText, 0, interCurrentRow);

                                Text interDepTimeText =
                                        new Text(formatTime(intermediateStop.depTime()));
                                interGrid.add(interDepTimeText, 1, interCurrentRow);

                                Text interDepStationText =
                                        new Text(intermediateStop.stop().name());
                                interGrid.add(interDepStationText, 2, interCurrentRow);

                                interCurrentRow++;
                            }
                            interAccordion.getPanes().add(titledGrid);
                            currentRow++;
                        }


                        // Ajout de l'heure d'arrivée, cercle d'arrivée, nom de la gare et sa
                        // plateforme
                        // d'arrivée
                        Text arrTimeText = new Text(formatTime(leg.arrTime()));
                        Text arrPlatformText = new Text(formatPlatformName(leg.arrStop()));
                        Text arrStationText = new Text(leg.arrStop().name());
                        Circle arrCircle = new Circle(3, Color.BLACK);

                        legsGrid.add(arrTimeText, 0, currentRow + 2);
                        legsGrid.add(arrCircle, 1, currentRow + 2);
                        legsGrid.add(arrStationText, 2, currentRow + 2);
                        legsGrid.add(arrPlatformText, 3, currentRow + 2);

                        currentRow = currentRow + 3;
                        legsGrid.addPair(depCircle, arrCircle);
                    }
                }
            }

            // pas sur de la valeur nulle, devrait on mettre autre chose ?


            // Boutons permettant de télécharger le trajet sous format .ical
            // et de le visualiser sur internet
            HBox buttons = new HBox();
            buttons.setId("buttons");
            withJourney.getChildren().add(buttons);

            Button calendarButton = new Button("Calendrier");
            Button mapButton = new Button("Carte");
            buttons.getChildren().addAll(calendarButton, mapButton);

            calendarButton.setOnAction(a -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choisissez l'emplacement d'enregistrement du fichier.");
                fileChooser.setInitialFileName("voyage_" + journey.getValue().depTime().format(
                        DateTimeFormatter.ISO_DATE) + ".ics");

                File selectedFile =
                        fileChooser.showSaveDialog(calendarButton.getScene().getWindow());

                if (selectedFile != null) {
                    try {
                        String icalText = toIcalendar(journey.getValue());

                        Files.writeString(selectedFile.toPath(), icalText,
                                StandardOpenOption.CREATE);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            });

            mapButton.setOnAction(a -> {
                try {
                    URI targetURL = new URI("https", "umap.osm.ch", "/fr/map",
                            ("data=" + toGeoJson(journey.getValue())), "null");

                    getDesktop().browse(targetURL);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            noJourney.setVisible(true);
            withJourney.setVisible(false);
        }

        return new DetailUI(root);
    }

    // Classe interne pour la grille des étapes, permettant de gérer les annotations
    private static final class StepGrid extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotationLayer;

        public StepGrid(Pane annotationLayer) {
            this.annotationLayer = annotationLayer;
        }

        public void addPair(Circle dep, Circle arr) {
            circlePairs.add(new Pair<>(dep, arr));
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            annotationLayer.getChildren().clear();

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

                annotationLayer.getChildren().add(line);
            }
        }
    }
}

