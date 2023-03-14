package kafara.uur.runningdiary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the application "Statistics"-Tab.
 * @author Stanislav Kafara
 * @version 1 2022-05-16
 */
public class StatisticsTab extends Tab {

    /** Runs to be considered in the statistics */
    private final ObservableList<Run> allRuns;

    private ChoiceBox<Timeframe> timeframeCB;
    private ChoiceBox<Data> dataCB;

    /** Chart pane */
    private final VBox chartPane;

    private BarChart<String, Number> distanceBC;
    private BarChart<String, Number> durationBC;
    private PieChart typePC;

    public StatisticsTab(ObservableList<Run> allRuns) {
        this.allRuns = allRuns;
        setText("Statistics");
        setClosable(false);
        VBox vBox = new VBox(48);
        {
            vBox.setPadding(new Insets(24));
        }
        {
            Node chartSelection = getChartSelection();
            chartPane = new VBox();
            {
                chartPane.setAlignment(Pos.CENTER);
            }
            vBox.getChildren().addAll(chartSelection, chartPane);
        }
        initCharts();
        setContent(vBox);
        setOnSelectionChanged(event -> {
            if (! StatisticsTab.this.isSelected()) {
                return;
            }
            updateChartPane();
        });
    }

    /**
     * Initializes the charts.
     */
    private void initCharts() {
        {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            distanceBC = new BarChart<>(xAxis, yAxis);
            distanceBC.setTitle("Distance Ran");
            yAxis.setLabel("Distance [km]");
        }
        {
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            durationBC = new BarChart<>(xAxis, yAxis);
            durationBC.setTitle("Time Ran");
            yAxis.setLabel("Duration [min]");
        }
        {
            typePC = new PieChart();
            typePC.setTitle("Activity Types");
        }
    }

    private Node getChartSelection() {
        HBox hBox = new HBox(32);
        {
            hBox.setAlignment(Pos.CENTER);
        }
        {
            VBox timeframeVB = new VBox(8);
            {
                timeframeVB.setAlignment(Pos.CENTER);
            }
            {
                Label label = new Label("Timeframe");
                timeframeCB = new ChoiceBox<>(
                        FXCollections.observableList(Arrays.stream(Timeframe.values()).toList())
                );
                {
                    timeframeCB.setPrefWidth(120);
                    timeframeCB.setValue(Timeframe.DAYS7);
                    timeframeCB.setOnAction(event -> updateChartPane());
                }
                timeframeVB.getChildren().addAll(label, timeframeCB);
            }
            VBox dataVB = new VBox(8);
            {
                dataVB.setAlignment(Pos.CENTER);
            }
            {
                Label label = new Label("Data");
                dataCB = new ChoiceBox<>(
                        FXCollections.observableList(Arrays.stream(Data.values()).toList())
                );
                {
                    dataCB.setPrefWidth(120);
                    dataCB.setValue(Data.DISTANCE);
                    dataCB.setOnAction(event -> updateChartPane());
                }
                dataVB.getChildren().addAll(label, dataCB);
            }
            hBox.getChildren().addAll(timeframeVB, dataVB);
        }
        return hBox;
    }

    /**
     * Sets correct chart to the chart pane.
     */
    private void updateChartPane() {
        chartPane.getChildren().clear();
        chartPane.getChildren().add(getChart(timeframeCB.getValue(), dataCB.getValue()));
    }

    /**
     * Determines which chart should be used and returns it.
     * @param timeframe Timeframe
     * @param data      Data
     * @return          Chart
     */
    private Node getChart(Timeframe timeframe, Data data) {
        if (data==Data.DISTANCE) {
            return getDistanceChart(timeframe);
        } else if (data==Data.DURATION) {
            return getDurationChart(timeframe);
        } else if (data==Data.TYPE) {
            return getTypeChart(timeframe);
        }
        return null;
    }

    private Chart getDistanceChart(Timeframe timeframe) {
        distanceBC.getXAxis().setLabel("Days");
        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
        if (timeframe.getDays()<=7) {
            for (int i=timeframe.getDays()-1; i>=0; i--) {
                int minusDays = i;
                data.add(
                        new XYChart.Data<>(
                                LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("EEEE")),
                                allRuns.stream()
                                        .filter(run -> run.getDate().equals(LocalDate.now().minusDays(minusDays)))
                                        .mapToDouble(Run::getDistance)
                                        .sum()
                        )
                );
            }
        } else if (timeframe.getDays()<=30) {
            for (int i=-timeframe.getDays(); i<=0; i++) {
                int plusDays = i;
                data.add(
                        new XYChart.Data<>(
                                ""+plusDays,
                                allRuns.stream()
                                        .filter(run -> run.getDate().equals(LocalDate.now().plusDays(plusDays)))
                                        .mapToDouble(Run::getDistance)
                                        .sum()
                        )
                );
            }
        } else {
            LocalDate now = LocalDate.now();
            LocalDate beginDate = LocalDate.of(now.getYear()-1, (now.getMonthValue()%12)+1, 1);
            Map<Integer, Double> monthsDistances = allRuns.stream()
                                                    .filter(run -> run.getDate().compareTo(beginDate) >= 0)
                                                    .collect(
                                                            Collectors.groupingBy(
                                                                    run -> run.getDate().getMonthValue(),
                                                                    Collectors.summingDouble(Run::getDistance)
                                                            )
                                                    );
            int month = beginDate.getMonthValue();
            do {
                double monthDistance = monthsDistances.getOrDefault(month, 0.0);
                data.add(new XYChart.Data<>(
                        LocalDate.of(0, month, 1).format(DateTimeFormatter.ofPattern("LLLL")),
                        monthDistance
                ));
                month = month%12+1;
            } while (month!=LocalDate.now().getMonthValue()+1);
            distanceBC.getXAxis().setLabel("Months");
        }
        distanceBC.getData().clear();
        distanceBC.getData().add(new XYChart.Series<>("Distance", data));
        distanceBC.getData().forEach(stringNumberSeries -> {
            stringNumberSeries.getData().forEach(stringNumberData -> {
                double km = stringNumberData.getYValue().doubleValue();
                Tooltip.install(
                        stringNumberData.getNode(),
                        new Tooltip(String.format(
                                "%.2f",
                                km
                        ))
                );
            });
        });
        return distanceBC;
    }

    private Chart getDurationChart(Timeframe timeframe) {
        durationBC.getXAxis().setLabel("Days");
        ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
        if (timeframe.getDays()<=7) {
            for (int i=timeframe.getDays()-1; i>=0; i--) {
                int minusDays = i;
                data.add(
                        new XYChart.Data<>(
                                LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("EEEE")),
                                allRuns.stream()
                                        .filter(run -> run.getDate().equals(LocalDate.now().minusDays(minusDays)))
                                        .mapToDouble(run -> run.getDuration().toSecondOfDay()/60.0)
                                        .sum()
                        )
                );
            }
        } else if (timeframe.getDays()<=30) {
            for (int i=-timeframe.getDays(); i<=0; i++) {
                int plusDays = i;
                data.add(
                        new XYChart.Data<>(
                                ""+plusDays,
                                allRuns.stream()
                                        .filter(run -> run.getDate().equals(LocalDate.now().plusDays(plusDays)))
                                        .mapToDouble(run -> run.getDuration().toSecondOfDay()/60.0)
                                        .sum()
                        )
                );
            }
        } else {
            LocalDate now = LocalDate.now();
            LocalDate beginDate = LocalDate.of(now.getYear()-1, (now.getMonthValue()%12)+1, 1);
            Map<Integer, Double> monthsDurations = allRuns.stream()
                    .filter(run -> run.getDate().compareTo(beginDate) >= 0)
                    .collect(
                            Collectors.groupingBy(
                                    run -> run.getDate().getMonthValue(),
                                    Collectors.summingDouble(run -> run.getDuration().toSecondOfDay()/60.0)
                            )
                    );
            int month = beginDate.getMonthValue();
            do {
                double monthDuration = monthsDurations.getOrDefault(month, 0.0);
                data.add(new XYChart.Data<>(
                        LocalDate.of(0, month, 1).format(DateTimeFormatter.ofPattern("LLLL")),
                        monthDuration
                ));
                month = month%12+1;
            } while (month!=LocalDate.now().getMonthValue()+1);
            durationBC.getXAxis().setLabel("Months");
        }
        durationBC.getData().clear();
        durationBC.getData().add(new XYChart.Series<>("Duration", data));
        durationBC.getData().forEach(stringNumberSeries -> {
            stringNumberSeries.getData().forEach(stringNumberData -> {
                double min = stringNumberData.getYValue().doubleValue();
                Tooltip.install(
                        stringNumberData.getNode(),
                        new Tooltip(String.format(
                                "%d:%02d",
                                (int) min,
                                (int)((60*min)%60)
                        ))
                );
            });
        });
        return durationBC;
    }

    private Chart getTypeChart(Timeframe timeframe) {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        LocalDate beginDate = LocalDate.now().minusDays(timeframe.getDays());
        if (timeframe==Timeframe.MONTHS12) {
            LocalDate now = LocalDate.now();
            beginDate = LocalDate.of(now.getYear()-1, (now.getMonthValue()%12)+1, 1);
        }
        new RunsSet(allRuns, beginDate, LocalDate.now())
                .getSet().stream()
                .collect(Collectors.groupingBy(Run::getType, Collectors.counting()))
                .forEach((type, count) -> data.add(new PieChart.Data(type.toString(), count)));
        typePC.setData(data);
        typePC.getData().forEach(d -> {
            Tooltip.install(
                    d.getNode(),
                    new Tooltip(""+(int)d.getPieValue())
            );
        });
        return typePC;
    }

    /**
     * Represents available timeframes.
     */
    private static enum Timeframe {
        DAYS7(7), DAYS30(30), MONTHS12(365);

        private final int days;

        private Timeframe(int days) {
            this.days = days;
        }

        public int getDays() {
            return days;
        }

        @Override
        public String toString() {
            switch (this) {
                case DAYS7:
                case DAYS30:
                    return days+" Days";
                case MONTHS12:
                    return "12 Months";
            }
            return null;
        }
    }

    /**
     * Represents available data.
     */
    private static enum Data {
        DISTANCE, DURATION, TYPE;

        @Override
        public String toString() {
            switch (this) {
                case DISTANCE:
                    return "Distance";
                case DURATION:
                    return "Duration";
                case TYPE:
                    return "Type";
            }
            return null;
        }
    }

}
