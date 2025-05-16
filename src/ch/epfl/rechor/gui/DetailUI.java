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
 * <p>
 * Cette interface met à jour automatiquement son affichage en fonction de la valeur observable
 * du voyage fourni. Elle contient deux enfants principaux : un affichage "aucun voyage" lorsque
 * aucun voyage n'est sélectionné, et un affichage détaillé lorsque le voyage est présent.
 *
 * @param rootNode le nœud racine du graphe de scène
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public record DetailUI(Node rootNode) {

    private static final double STOP_CIRCLE_RADIUS = 3.0;
    private static final int VEHICLE_ICON_SIZE = 31;
    private static final double LINE_STROKE_WIDTH = 2.0;

    /**
     * Crée une nouvelle interface détaillée pour un voyage observable.
     * <p>
     * Cette méthode crée l'interface graphique qui s'adapte automatiquement à la valeur
     * observable du voyage. Elle lie la visibilité des deux affichages (aucun voyage
     * et avec voyage) à la présence ou non d'un voyage. Lorsque le voyage change,
     * l'interface se met à jour pour afficher les détails correspondants.
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
        );

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
                            // Ajout de l'heure de départ, cercle de départ, nom de la gare et sa
                            // plateforme de départ
                            Circle depCircle = addRow(legsGrid, currentRow, leg.depTime(), leg.depStop(), true);

                            // Ajout de l'icone du véhicule, et du nom de la destination
                            ImageView vehicleIcon =
                                    new ImageView(iconFor(((Journey.Leg.Transport) leg).vehicle()));
                            vehicleIcon.setPreserveRatio(true);
                            vehicleIcon.setFitWidth(VEHICLE_ICON_SIZE);
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
                            // plateforme d'arrivée
                            Circle arrCircle = addRow(legsGrid, currentRow + 2, leg.arrTime(),
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

    /**
     * Méthode auxiliaire, ajoute une ligne dans la grille représentant un arrêt avec l'heure, un
     * cercle,
     * le nom de la station et le nom de la plateforme.
     *
     * @param grid        la grille dans laquelle ajouter la ligne
     * @param row         l'indice de la ligne où ajouter les éléments
     * @param time        l'heure à afficher
     * @param stop        l'arrêt (station) concerné
     * @param isDeparture indique si l'arrêt est un départ (true) ou une arrivée (false)
     * @return le cercle créé représentant l'arrêt, utilisé pour les annotations graphiques
     */
    private static Circle addRow(GridPane grid,
                                 int row,
                                 LocalDateTime time,
                                 Stop stop,
                                 boolean isDeparture) {

        Text timeTxt = new Text(formatTime(time));
        Text platformTxt = new Text(formatPlatformName(stop));
        Text stationTxt = new Text(stop.name());
        Circle circle = new Circle(STOP_CIRCLE_RADIUS, Color.BLACK);

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

    /**
     * Méthode auxiliaire, connecte les boutons calendrier et carte à leurs actions respectives.
     * <p>
     * Le bouton calendrier permet de sauvegarder le voyage au format iCalendar.
     * Le bouton carte ouvre une carte web avec le trajet au format GeoJSON.
     *
     * @param calendarButton le bouton pour exporter le calendrier
     * @param mapButton      le bouton pour afficher la carte
     * @param journeyO       l'observable du voyage utilisé pour récupérer les données
     */
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


    /**
     * Grille interne pour afficher les étapes de transport avec des lignes d'annotation.
     * <p>
     * Cette classe gère le dessin des lignes rouges reliant les cercles de départ et d'arrivée
     * des étapes de transport dans l'interface graphique.
     */
    private static final class StepGrid extends GridPane {
        private final List<Pair<Circle, Circle>> circlePairs = new ArrayList<>();
        private final Pane annotationLayer;
        private final List<Line> lineList = new ArrayList<>();


        public StepGrid(Pane annotationLayer) {
            this.annotationLayer = annotationLayer;
        }

        /**
         * Ajoute une paire de cercles représentant un départ et une arrivée,
         * pour dessiner une ligne entre eux.
         *
         * @param dep le cercle du point de départ
         * @param arr le cercle du point d'arrivée
         */
        public void addPair(Circle dep, Circle arr) {
            circlePairs.add(new Pair<>(dep, arr));
        }

        /**
         * Vide la liste des paires de cercles, supprimant ainsi les annotations.
         */
        public void clearPairs() {
            circlePairs.clear();
        }

        /**
         * Méthode appelée lors de la mise en page des enfants.
         * <p>
         * Elle dessine les lignes rouges reliant les paires de cercles sur le calque d'annotation.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            lineList.clear();

            circlePairs.stream()
                    .map(this::createLine)
                    .forEach(lineList::add);

            annotationLayer.getChildren().setAll(lineList);
            annotationLayer.getParent().requestLayout();
        }

        /**
         * Méthode auxiliaire, crée une ligne rouge reliant deux cercles donnés.
         *
         * @param pair la paire de cercles (départ, arrivée)
         * @return la ligne rouge entre les deux cercles
         */
        private Line createLine(Pair<Circle, Circle> pair) {
            Circle start = pair.getKey();
            Circle end = pair.getValue();
            double startX = start.getBoundsInParent().getCenterX();
            double startY = start.getBoundsInParent().getCenterY();
            double endX = end.getBoundsInParent().getCenterX();
            double endY = end.getBoundsInParent().getCenterY();
            Line line = new Line(startX, startY, endX, endY);
            line.setStroke(Color.RED);
            line.setStrokeWidth(LINE_STROKE_WIDTH);
            return line;
        }
    }
}
