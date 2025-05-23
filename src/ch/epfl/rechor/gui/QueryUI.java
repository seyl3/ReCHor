package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente l'interface de requête permettant à l'utilisateur de saisir un arrêt de départ, un arrêt d'arrivée,
 * une date et une heure pour une recherche d'itinéraire.
 * <p>
 * Cette interface se compose d'un champ de saisie pour l'arrêt de départ, un autre pour l'arrêt d'arrivée,
 * un sélecteur de date, et un champ formaté pour saisir l'heure. Elle fournit aussi un bouton permettant
 * d'échanger les arrêts de départ et d'arrivée.
 *
 * @param rootNode le nœud JavaFX à la racine de l'interface
 * @param depStopO la valeur observable représentant l'arrêt de départ (valide ou chaîne vide)
 * @param arrStopO la valeur observable représentant l'arrêt d'arrivée (valide ou chaîne vide)
 * @param dateO    la valeur observable représentant la date du voyage
 * @param timeO    la valeur observable représentant l'heure de départ du voyage
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public record QueryUI(Node rootNode,
                      ObservableValue<String> depStopO,
                      ObservableValue<String> arrStopO,
                      ObservableValue<LocalDate> dateO,
                      ObservableValue<LocalTime> timeO
) {

    /**
     * Crée l'interface graphique de requête, avec les champs nécessaires à la saisie des informations
     * de recherche (départ, arrivée, date, heure).
     * <p>
     * Les champs d'arrêt affichent des suggestions lors de la saisie, et le champ d'heure est formaté
     * pour accepter des formats horaires suisses (par ex. 9:30 ou 09:30). Le bouton d’échange permet
     * d’inverser les champs de départ et d’arrivée.
     *
     * @param stopIndex l'index des noms d'arrêts utilisé pour les suggestions
     * @return une instance de {@code QueryUI} initialisée
     * @throws NullPointerException si {@code stopIndex} est {@code null}
     */
    public static QueryUI create(StopIndex stopIndex) {
        VBox root = new VBox();
        root.getStylesheets().add("query.css");

        // Partie pour la recherche des arrêts
        HBox search = new HBox();
        root.getChildren().add(search);

        // Champ pour l'arrêt de départ
        Label depStop = new Label("Départ\u202f:");
        StopField depField = StopField.create(stopIndex);
        depField.textField().setId("depStop");
        depField.textField().setPromptText("Nom de l'arrêt de départ");
        search.getChildren().addAll(depStop, depField.textField());

        // Bouton pour échanger les arrêts de départ et d'arrivée
        Button exchangeB = new Button();
        search.getChildren().add(exchangeB);
        exchangeB.setText("\u2194");

        // Champ pour l'arrêt d'arrivée
        Label arrStop = new Label("Arrivée\u202f:");
        StopField arrField = StopField.create(stopIndex);
        arrField.textField().setPromptText("Nom de l'arrêt d'arrivée");
        search.getChildren().addAll(arrStop, arrField.textField());

        // Action du bouton d'échangé
        exchangeB.setOnAction(e -> {
            String dep = depField.stopO().getValue();
            String arr = arrField.stopO().getValue();
            depField.setTo(arr);
            arrField.setTo(dep);
        });

        // Partie pour la date et l'heure
        HBox dateTime = new HBox();
        root.getChildren().add(dateTime);

        // Champ d'affichage et sélectionneur pour la date requise
        Label date = new Label("Date\u202f:");
        DatePicker datePicker = new DatePicker();
        datePicker.setId("date");

        dateTime.getChildren().addAll(date, datePicker);

        // Champ pour entrer l'heure de départ requis
        Label time = new Label("Heure\u202f:");
        TextField timeField = new TextField();
        timeField.setId("time");

        // Format d'affichage de l'heure
        LocalTimeStringConverter timeConverter =
                new LocalTimeStringConverter(TimeFormat.DISPLAY_FORMATTER, TimeFormat.PARSE_FORMATTER);
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(timeConverter);
        timeField.setTextFormatter(timeFormatter);

        // Date et heure correspondent par défaut au moment du début de la recherche
        timeField.setText(LocalTime.now().format(TimeFormat.DISPLAY_FORMATTER));
        datePicker.setValue(LocalDate.now());

        dateTime.getChildren().addAll(time, timeField);


        return new QueryUI(root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty());


    }

    /**
     * Classe utilitaire contenant les constantes et formatters pour la gestion des formats d'heure
     * dans l'interface de requête.
     * <p>
     * Cette classe encapsule tous les formats d'heure utilisés dans l'interface, à la fois pour
     * l'affichage et pour l'analyse des entrées utilisateur. Elle supporte deux formats principaux :
     * <ul>
     *     <li>Format d'affichage : toujours au format "HH:mm" (ex: "09:30")</li>
     *     <li>Format d'analyse : accepte soit "H:mm" soit "HH:mm" (ex: "9:30" ou "09:30")</li>
     * </ul>
     * Les formatters sont construits à partir des patterns de base pour assurer la cohérence
     * dans toute l'application.
     *
     * @author Sarra Zghal, Elyes Ben Abid
     */
    private static final class TimeFormat {
        /**
         * Format d'affichage de l'heure (toujours HH:mm).
         */
        static final String DISPLAY_PATTERN = "HH:mm";

        /**
         * Format d'analyse pour l'heure avec un seul chiffre pour les heures (H:mm).
         */
        static final String SINGLE_DIGIT_HOUR_PATTERN = "H:mm";

        /**
         * Format d'analyse pour l'heure avec deux chiffres pour les heures (HH:mm).
         */
        static final String DOUBLE_DIGIT_HOUR_PATTERN = "HH:mm";

        /**
         * Formatter de l'affichage de l'heure.
         */
        static final DateTimeFormatter DISPLAY_FORMATTER =
                DateTimeFormatter.ofPattern(DISPLAY_PATTERN);

        /**
         * Format(s) d'analyse accepté(s) pour la saisie de l'heure (H:mm ou HH:mm).
         */
        static final DateTimeFormatter PARSE_FORMATTER =
                DateTimeFormatter.ofPattern("[" + SINGLE_DIGIT_HOUR_PATTERN + "][" + DOUBLE_DIGIT_HOUR_PATTERN + "]");
    }

}
