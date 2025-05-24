package ch.epfl.rechor.gui;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
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

public class SwitchButton extends ToggleButton {
    private static final double TRACK_WIDTH = 73;
    private static final double TRACK_HEIGHT = 26;
    private static final double THUMB_SIZE = TRACK_HEIGHT - 6;
    private static final double THUMB_MARGIN = (TRACK_HEIGHT - THUMB_SIZE) / 2;

    private static final Color COLOR_BG = Color.web("#f4f4f5");
    private static final Color COLOR_STROKE_DEF = Color.GRAY;
    private static final Color COLOR_STROKE_FOCUS = Color.web("#70B9D9FF");
    private final Rectangle track;
    private final Rectangle thumb;
    private final Text label;
    private final TranslateTransition transition;
    private String offText;
    private String onText;
    private final Duration SLIDE_DURATION = Duration.millis(300);
    private PauseTransition textTransition;

    public SwitchButton() {
        offText = "";
        onText = "";
        setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        setMinSize(TRACK_WIDTH, TRACK_HEIGHT);
        setMaxSize(TRACK_WIDTH, TRACK_HEIGHT);

        track = new Rectangle(1, 1, TRACK_WIDTH - 2, TRACK_HEIGHT - 2);
        track.setFill(COLOR_BG);
        track.setArcWidth(TRACK_HEIGHT - 2);
        track.setArcHeight(TRACK_HEIGHT - 2);
        track.setStroke(COLOR_STROKE_DEF);
        track.setStrokeWidth(1);

        thumb = new Rectangle(THUMB_SIZE, THUMB_SIZE, Color.web("E4E4E4FF"));
        thumb.setArcWidth(THUMB_SIZE);
        thumb.setArcHeight(THUMB_SIZE);
        thumb.setLayoutX(THUMB_MARGIN);
        thumb.setLayoutY(THUMB_MARGIN);
        thumb.setStroke(COLOR_STROKE_DEF);
        thumb.setStrokeWidth(0.8);

        label = new Text(offText);
        label.setFill(Color.BLACK);
        label.setY(TRACK_HEIGHT / 2 + label.getFont().getSize() / 3);
        label.setX(THUMB_SIZE + 8);

        Pane graphic = new Pane(track, label, thumb);
        graphic.setPrefSize(TRACK_WIDTH, TRACK_HEIGHT);
        setGraphic(graphic);

        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        transition = new TranslateTransition(SLIDE_DURATION, thumb);

        selectedProperty().addListener((obs, oldV, newV) -> applyState(newV));

        focusedProperty().addListener((obs, oldV, newV) -> updateFocus(newV));
    }

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
        if (textTransition != null) {
            textTransition.stop();
        }
        textTransition = new PauseTransition(SLIDE_DURATION.divide(2));
        textTransition.setOnFinished(evt -> {
            label.setText(on ? onText : offText);
            positionLabel(on);
        });
        textTransition.play();
    }

    private void positionLabel(boolean on) {
        if (on) {
            label.setX(8);
        } else {
            double textWidth = label.getLayoutBounds().getWidth();
            label.setX(TRACK_WIDTH - textWidth - 8);
        }
    }

    @Override
    public double getBaselineOffset() {
        return label.getY();
    }

    private void updateFocus(boolean focused) {
        if (focused) {
            track.setFill(COLOR_BG);
            track.setStrokeWidth(2);
            track.setStroke(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, COLOR_STROKE_FOCUS),
                    new Stop(1, COLOR_STROKE_FOCUS)
            ));
        } else {
            track.setFill(COLOR_BG);
            track.setStrokeWidth(1);
            track.setStroke(COLOR_STROKE_DEF);
        }
    }
}