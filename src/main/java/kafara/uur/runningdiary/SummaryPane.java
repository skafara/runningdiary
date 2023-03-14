package kafara.uur.runningdiary;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a run activities summary pane
 * @author  Stanislav Kafara
 * @version 1 2022-05-16
 */
public class SummaryPane extends BorderPane {

    /** Activities to be summarized */
    private final RunsSet runsSet;

    public SummaryPane(RunsSet runsSet) {
        this.runsSet = runsSet;
        setLeft(getTotalPane());
        setRight(getAveragePane());
    }

    private Node getTotalPane() {
        Map<String, ObservableValue<String>> content = new HashMap<>();
        content.put("Activities", runsSet.runsCountProperty().asString());
        content.put("Distance", runsSet.totalDistanceProperty().asString("%.2f km"));
        content.put("Duration", new StringBinding() {
            {
                bind(runsSet.totalDurationProperty());
            }
            @Override
            protected String computeValue() {
                return String.format(
                        "%s h",
                        LocalTime.ofSecondOfDay(
                            runsSet.totalDurationProperty().get())
                                .format(DateTimeFormatter.ofPattern("H:mm"))
                );
            }
        });
        return new SummaryContentPane("In Total", content);
    }

    private Node getAveragePane() {
        Map<String, ObservableValue<String>> content = new HashMap<>();
        content.put("Evaluation", new StringBinding() {
            {
                bind(runsSet.averageEvaluationBinding());
            }
            @Override
            protected String computeValue() {
                return String.format(
                        "%s / 10",
                        runsSet.averageEvaluationBinding().get()!=-1 ?
                                String.format("%.1f", runsSet.averageEvaluationBinding().get()) : "---"
                );
            }
        });
        content.put("Distance", new StringBinding() {
            {
                bind(runsSet.averageDistanceBinding());
            }
            @Override
            protected String computeValue() {
                return String.format(
                        "%s km",
                        runsSet.averageDistanceBinding().get()!=-1 ?
                                String.format("%.2f", runsSet.averageDistanceBinding().get()) : "---"
                );
            }
        });
        content.put("Duration", new StringBinding() {
            {
                bind(runsSet.averageDurationBinding());
            }
            @Override
            protected String computeValue() {
                return String.format(
                        "%s h",
                        runsSet.averageDurationBinding().get()!=-1 ? LocalTime.ofSecondOfDay(
                            runsSet.averageDurationBinding().get())
                                .format(DateTimeFormatter.ofPattern("H:mm")) : "---"
                );
            }
        });
        return new SummaryContentPane("On Average", content);
    }

    /**
     * Represents generic summary content.
     */
    private static class SummaryContentPane extends VBox {

        public SummaryContentPane(String title, Map<String, ObservableValue<String>> content) {
            setSpacing(16);
            getChildren().addAll(getTitle(title), getGridPane(content));
        }

        private Node getTitle(String title) {
            Label titleLB = new Label(title);
            {
                titleLB.setFont(Font.font(18));
            }
            return titleLB;
        }

        /**
         * Creates the grid and binds the values.
         * @param content   Content to be placed in the grid
         * @return          Grid
         */
        private Node getGridPane(Map<String, ObservableValue<String>> content) {
            GridPane gridPane = new GridPane();
            {
                gridPane.setVgap(8);
            }
            {
                int i = 0;
                for (Map.Entry<String, ObservableValue<String>> entry : content.entrySet()) {
                    Label contentLB = new Label();
                    contentLB.textProperty().bind(entry.getValue());
                    gridPane.addRow(
                            i++,
                            new Label(String.format("%s: ", entry.getKey())),
                            contentLB
                    );
                }
            }
            return gridPane;
        }

    }

}
