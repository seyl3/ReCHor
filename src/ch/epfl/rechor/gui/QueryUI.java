package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record QueryUI(Node rootNode,
                      ObservableValue<String> depStopO,
                      ObservableValue<String> arrStopO,
                      ObservableValue<LocalDate> dateO,
                      ObservableValue<LocalTime> timeO
                      ) {
    public static QueryUI create(StopIndex stopIndex) {
        VBox root = new VBox();
        root.getStyleClass().add("query.css");

        HBox search = new HBox();
        root.getChildren().add(search);

        Label depStop = new Label("Départ\u202f:");
        StopField depField = StopField.create(stopIndex);
        depField.textField().setId("depStop");
        depField.textField().setPromptText("Nom de l'arrêt de départ");
        search.getChildren().addAll(depStop, depField.textField());

        Label arrStop = new Label("Arrivée\u202f:");
        StopField arrField = StopField.create(stopIndex);
        arrField.textField().setPromptText("Nom de l'arrêt d'arrivée");
        search.getChildren().addAll(arrStop, arrField.textField());

        Button exchangeB = new Button();
        search.getChildren().add(exchangeB);
        exchangeB.setText("\u2194");
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

        dateTime.getChildren().addAll(time, timeField);


        return new QueryUI(root,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty());


    }

}
