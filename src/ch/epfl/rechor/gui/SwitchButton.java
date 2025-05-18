package ch.epfl.rechor.gui;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A toggle switch with smooth sliding animation and fixed track bounds.
 */
public class SwitchButton extends ToggleButton {
    private static final double TRACK_WIDTH = 73;
    private static final double TRACK_HEIGHT = 26;
    private static final double THUMB_SIZE = TRACK_HEIGHT - 6;
    // horizontal/vertical margin for thumb within track
    private static final double THUMB_MARGIN = (TRACK_HEIGHT - THUMB_SIZE) / 2;
    private final Rectangle track;
    private final Rectangle thumb;
    private final Text label;
    private final TranslateTransition transition;
    private String offText;
    private String onText;
    private Duration SLIDE_DURATION = Duration.millis(300);
    private PauseTransition textTransition;

    /**
     * Duration of the slide animation; adjustable via setter.
     */

    public SwitchButton() {
        // initialize labels for off/on states
        offText = "";
        onText = "";
        setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        // Fix height for proper alignment
        setMinSize(TRACK_WIDTH, TRACK_HEIGHT);
        setMaxSize(TRACK_WIDTH, TRACK_HEIGHT);

        // Track background (inset by 1px for border)
        track = new Rectangle(1, 1, TRACK_WIDTH - 2, TRACK_HEIGHT - 2);
        track.setFill(Color.web("#f4f4f5"));
        track.setArcWidth(TRACK_HEIGHT - 2);
        track.setArcHeight(TRACK_HEIGHT - 2);
        // visible border
        track.setStroke(Color.GRAY);
        track.setStrokeWidth(1);

        // Sliding thumb
        thumb = new Rectangle(THUMB_SIZE, THUMB_SIZE, Color.web("E4E4E4FF"));
        thumb.setArcWidth(THUMB_SIZE);
        thumb.setArcHeight(THUMB_SIZE);
        // position thumb centered within track
        thumb.setLayoutX(THUMB_MARGIN);
        thumb.setLayoutY(THUMB_MARGIN);
        // visible border on thumb
        thumb.setStroke(Color.GRAY);
        thumb.setStrokeWidth(0.8);

        // Label text
        label = new Text(offText);
        label.setFill(Color.BLACK);
        label.setY(TRACK_HEIGHT / 2 + label.getFont().getSize() / 3);
        label.setX(THUMB_SIZE + 8);

        // build graphic pane and assign to ToggleButton
        Pane graphic = new Pane(track, label, thumb);
        graphic.setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        setGraphic(graphic);

        // show only the graphic and remove default button styling
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        transition = new TranslateTransition(SLIDE_DURATION, thumb);

        // Reflect state change
        selectedProperty().addListener((obs, oldV, newV) -> applyState(newV));

        // change border color and fill on focus to match enhanced style
        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldV, Boolean newV) {
                if (newV) {
                    // focused: gradient border, interior unchanged
                    track.setFill(Color.web("#f4f4f5"));
                    track.setStrokeWidth(2.0);
                    track.setStroke(new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.web("#70B9D9FF")),
                            new Stop(1, Color.web("#70B9D9FF"))
                    ));
                } else {
                    // unfocused: default fill and stroke
                    track.setFill(Color.web("#f4f4f5"));
                    track.setStrokeWidth(1.0);
                    track.setStroke(Color.GRAY);
                }
            }
        });
    }

    /**
     * Create a SwitchButton with separate texts for off and on states.
     *
     * @param offText text to display when the switch is off
     * @param onText  text to display when the switch is on
     */
    public SwitchButton(String offText, String onText) {
        this();
        this.offText = offText;
        this.onText = onText;
        label.setText(offText);
    }

    private void applyState(boolean on) {
        double targetX = on ? TRACK_WIDTH - THUMB_SIZE - THUMB_MARGIN : THUMB_MARGIN;
        transition.stop();
        transition.setToX(targetX - thumb.getLayoutX());
        transition.play();
        thumb.toFront();
        track.setFill(Color.WHITE);
        // schedule text update at mid-animation
        if (textTransition != null) {
            textTransition.stop();
        }
        textTransition = new PauseTransition(SLIDE_DURATION.divide(2));
        textTransition.setOnFinished(evt -> {
            label.setText(on ? onText : offText);
            if (on) {
                label.setX(8);
            } else {
                double textWidth = label.getLayoutBounds().getWidth();
                label.setX(TRACK_WIDTH - textWidth - 8);
            }
        });
        textTransition.play();
    }

    /**
     * Set the duration of the slide animation.
     *
     * @param duration the new animation duration
     */
    public void setSLIDE_DURATION(Duration duration) {
        this.SLIDE_DURATION = duration;
        transition.setDuration(duration);
    }

    /**
     * Provide a baseline offset so that baseline alignment in HBox works correctly.
     */
    @Override
    public double getBaselineOffset() {
        // Align on the label's baseline position
        return label.getY();
    }
}