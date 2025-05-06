package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.List;


public record StopField(TextField textField, ObservableValue<String> stopO) {
    public StopField  {
        stopO = new SimpleStringProperty("");

    }

    public static StopField create(StopIndex stopIndex) {
        TextField tf = new TextField();
        StringProperty stopO = new SimpleStringProperty("");
        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        ListView<String> listView = new ListView<>();
        listView.setFocusTraversable(false);
        listView.setMaxHeight(240);
        popup.getContent().add(listView);

        // Ajout d'un listener sur la propriété de focus du champ textuel
        tf.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (!newFocused) {
                // Le champ vient de perdre le focus
                String query = tf.getText().strip(); // texte actuel dans le champ
                // On essaie de trouver l'arrêt exact correspondant à la requête
                // listView.setItems(stopIndex.stopsMatching(query, 30));

                if () {
                    // on met à jour la valeur observable
                } else {
                    // Si aucun arrêt exact ne correspond : on vide le champ
                    tf.clear();
                    stopO.set("");                   // stopO est mis à chaîne vide
                }




            }
        });

        tf.addEventHandler(KeyEvent.KEY_PRESSED, event -> {});
        return new StopField(tf, stopO);
    }

    public void setTo(String stopName){
        textField.setText(stopName);
        ((SimpleStringProperty)stopO).set(stopName);

    }
}
