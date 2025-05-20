package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.util.List;

/**
 * Représente un champ de saisie permettant de sélectionner un arrêt de transport public
 * à l'aide d'une fenêtre de suggestions interactives.
 * <p>
 * Un {@code StopField} associe un champ textuel et une valeur observable contenant le nom
 * de l'arrêt sélectionné. La valeur observable est mise à jour uniquement lorsque le champ
 * perd le focus, et contient soit un nom d'arrêt valide, soit la chaîne vide si aucun arrêt
 * n'est sélectionné.
 *
 * @param textField le champ textuel de saisie
 * @param stopO     la valeur observable représentant l'arrêt sélectionné
 * @author : Sarra Zghal, Elyes Ben Abid
 */
public record StopField(TextField textField, ObservableValue<String> stopO) {

    /**
     * Crée un champ de saisie avec fenêtre de suggestions pour la recherche d'arrêts,
     * en utilisant l'index fourni pour effectuer les correspondances.
     * <p>
     * Lorsqu'on clique dans le champ, une liste de suggestions apparaît sous le champ,
     * et se met à jour dynamiquement selon la saisie. Lorsque le champ perd le focus,
     * l'élément sélectionné dans la liste est conservé comme valeur choisie.
     *
     * @param stopIndex l'index à utiliser pour rechercher les arrêts correspondants
     * @return une instance de {@code StopField} avec suggestions dynamiques
     * @throws NullPointerException si {@code stopIndex} est {@code null}
     */
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
            listView.scrollTo(listView.getSelectionModel().getSelectedIndex());
        });

        // Mise à jour de l'apparance du champ textuel en fonction du focus
        tf.focusedProperty().subscribe(focus -> {
            // Si le focus est bien sur le text Field → on effectue la recherche
            if (focus) {
                popup.show(tf.getScene().getWindow());
                setListView(stopIndex, tf.textProperty().getValue(), listView);
                // Mise à jour du contenu de la liste à chaque changement de texte
                tf.textProperty().subscribe(() -> {
                    setListView(stopIndex, tf.textProperty().getValue(), listView);
                });

                // Positionnement du popup juste sous le champ textuel
                Bounds bounds = tf.localToScreen(tf.getBoundsInLocal());
                popup.setAnchorX(bounds.getMinX());
                popup.setAnchorY(bounds.getMaxY());

            } else {
                // Quand le champ perd le focus → on affiche le nom sélectionné et on range le pop-up

                // On récupère l'élément sélectionné (ou vide si rien)
                String selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    stopO.set(selected); // mise à jour de stopO
                    tf.setText(selected); // mise à jour du champ avec le nom sélectionné
                } else {
                    stopO.set(""); // aucune correspondance
                }

                // On cache la fenêtre de suggestions
                popup.hide();
            }
        });


        return new StopField(tf, stopO);
    }

    /**
     * Met à jour le contenu d'une liste de suggestions d'arrêts en fonction d'une requête utilisateur.
     * <p>
     * Cette méthode interroge l'index des arrêts avec la requête donnée et affiche jusqu'à 30
     * résultats dans la {@code ListView}. Si des suggestions sont trouvées, le premier élément
     * est automatiquement sélectionné.
     *
     * @param stopIndex l'index contenant les noms d'arrêts
     * @param request   la chaîne de requête saisie par l'utilisateur
     * @param listView  la liste affichant les suggestions
     * @throws NullPointerException si l'un des arguments est {@code null}
     */
    private static void setListView(StopIndex stopIndex, String request, ListView<String> listView) {
        List<String> suggestionss = stopIndex.stopsMatching(request, 30);
        listView.getItems().setAll(suggestionss);
        if (!suggestionss.isEmpty()) {
            listView.getSelectionModel().selectFirst(); // sélection par défaut
        }
    }

    /**
     * Définit manuellement la valeur du champ textuel et de la valeur observable
     * à un nom d'arrêt donné.
     *
     * @param stopName le nom d'arrêt à associer au champ
     * @throws NullPointerException si {@code stopName} est {@code null}
     */
    public void setTo(String stopName) {
        textField.setText(stopName);
        ((SimpleStringProperty) stopO).set(stopName);
    }
}
