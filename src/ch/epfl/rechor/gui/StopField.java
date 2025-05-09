package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.List;


public record StopField(TextField textField, ObservableValue<String> stopO) {

    public static StopField create(StopIndex stopIndex) {
        TextField tf = new TextField();
        StringProperty stopO = new SimpleStringProperty("");
        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        ListView<String> listView = new ListView<>();
        listView.setFocusTraversable(false);
        listView.setMaxHeight(240);
        popup.getContent().add(listView);

        tf.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (event.getCode() == KeyCode.UP && selectedIndex > 0) {
                listView.getSelectionModel().select(selectedIndex - 1);
                event.consume(); // Empêche le déplacement du curseur dans le texte
            }

            if (event.getCode() == KeyCode.DOWN && selectedIndex < listView.getItems().size() - 1) {
                listView.getSelectionModel().selectNext();
                event.consume(); // Empêche le déplacement du curseur dans le texte
            }
        });

        // Ajout d'un listener sur la propriété de focus du champ textuel
        tf.focusedProperty().subscribe(focus->{
            if(focus){
                popup.show(tf.getScene().getWindow());
                // Mise à jour du contenu de la liste à chaque changement de texte
                tf.textProperty().addListener((__, ___, newText) -> {
                    List<String> suggestions = stopIndex.stopsMatching(newText, 30);
                    listView.getItems().setAll(suggestions);

                    if (!suggestions.isEmpty()) {
                        listView.getSelectionModel().selectFirst(); // sélection par défaut
                    }
                });

                // Positionnement du popup juste sous le champ textuel
                Bounds bounds = tf.localToScreen(tf.getBoundsInLocal());
                popup.setAnchorX(bounds.getMinX());
                popup.setAnchorY(bounds.getMaxY());

            } else {
                // Quand le champ perd le focus

                popup.hide(); // On cache la fenêtre de suggestions

                // On récupère l'élément sélectionné (ou vide si rien)
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    stopO.set(selected); // mise à jour de stopO
                    tf.setText(selected); // mise à jour du champ avec le nom sélectionné
                } else {
                    stopO.set(""); // aucune correspondance
                }
            }
        });


        return new StopField(tf, stopO);
    }

    public void setTo(String stopName){
        textField.setText(stopName);
    }
}
