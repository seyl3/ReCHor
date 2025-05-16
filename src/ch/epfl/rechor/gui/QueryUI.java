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

        HBox search = new HBox();
        root.getChildren().add(search);

        Label depStop = new Label("Départ\u202f:");
        StopField depField = StopField.create(stopIndex);
        depField.textField().setId("depStop");
        depField.textField().setPromptText("Nom de l'arrêt de départ");
        search.getChildren().addAll(depStop, depField.textField());

        Button exchangeB = new Button();
        search.getChildren().add(exchangeB);
        exchangeB.setText("\u2194");

        Label arrStop = new Label("Arrivée\u202f:");
        StopField arrField = StopField.create(stopIndex);
        arrField.textField().setPromptText("Nom de l'arrêt d'arrivée");
        search.getChildren().addAll(arrStop, arrField.textField());


        exchangeB.setOnAction(e -> {
            String dep = depField.stopO().getValue();
            String arr = arrField.stopO().getValue();
            depField.setTo(arr);
            arrField.setTo(dep);
        });


        HBox dateTime = new HBox();
        root.getChildren().add(dateTime);

        Label date = new Label("Date\u202f:");
        DatePicker datePicker = new DatePicker();
        datePicker.setId("date");

        dateTime.getChildren().addAll(date, datePicker);

        Label time = new Label("Heure\u202f:");
        TextField timeField = new TextField();
        timeField.setId("time");

        DateTimeFormatter formatterDisplay = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter formatterParse = DateTimeFormatter.ofPattern("[H:mm][HH:mm]");
        LocalTimeStringConverter timeConverter = new LocalTimeStringConverter(formatterDisplay, formatterParse);
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(timeConverter);
        timeField.setTextFormatter(timeFormatter);

        timeField.setText(LocalTime.now().format(formatterDisplay));
        datePicker.setValue(LocalDate.now());

        dateTime.getChildren().addAll(time, timeField);


        return new QueryUI(root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty());


    }

}
