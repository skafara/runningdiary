package kafara.uur.runningdiary;

import javafx.application.Application;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Represents a run activities diary
 * @author  Stanislav Kafara
 * @version 1 2022-05-16
 */
public class RunningDiary extends Application {

    private static final Path SAVE_FILE_PATH = Paths.get("runningdiary.dat");

    private static final int RECENT_DAYS = 6;

    /** All recorded activities */
    private ObservableList<Run> allRuns;
    /** RunsSet of all recorded activities */
    private RunsSet allRunsSet;
    /** RunsSet of recent activities */
    private RunsSet recentRunsSet;

    private TabPane tabPane;
    /** Maps activities to be edited with editing Tabs */
    private HashMap<Run, RunActivityTab> runActivityTabs;
    private TableView<Run> recentRunsTV;

    private VBox historyTVPane;
    private CheckBox runTypesVisibleCB;

    /**
     * Launches application.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes the application
     * - initializes (+loads from file) activities
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        super.init();
        allRuns = FXCollections.observableArrayList();
        allRunsSet = new RunsSet(allRuns);
        recentRunsSet = new RunsSet(allRuns, LocalDate.now().minusDays(RECENT_DAYS), LocalDate.now());
        runActivityTabs = new HashMap<>();
        try {
            loadFile(SAVE_FILE_PATH);
        } catch (IOException e) {
            // allRuns will remain empty
        }
    }

    /**
     * Writes diary data to save file.
     * - all recorded activities
     * @param path  Path to save file
     * @throws IOException
     */
    private void saveToFile(Path path) throws IOException {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            for (Run run : allRuns) {
                bufferedWriter.write(run.getLabel()!=null ? run.getLabel() : "");                   bufferedWriter.newLine();
                bufferedWriter.write(run.getType().toString());                                     bufferedWriter.newLine();
                bufferedWriter.write(""+run.getDate().toEpochDay());                            bufferedWriter.newLine();
                bufferedWriter.write(""+run.getSegments().size());                              bufferedWriter.newLine();
                for (Run.Segment segment : run.getSegments()) {
                    bufferedWriter.write(""+segment.getDuration().toSecondOfDay());             bufferedWriter.newLine();
                    bufferedWriter.write(""+segment.getDistance());                             bufferedWriter.newLine();
                    bufferedWriter.write(""+segment.getHr());                                   bufferedWriter.newLine();
                    bufferedWriter.write(""+segment.getCadence());                              bufferedWriter.newLine();
                    bufferedWriter.write(""+segment.getElevation());                            bufferedWriter.newLine();
                }
                bufferedWriter.write(run.getTerrain()!=null ? run.getTerrain().toString() : "");    bufferedWriter.newLine();
                bufferedWriter.write(""+run.getEvaluation());                                   bufferedWriter.newLine();
                bufferedWriter.write(
                        run.getNote()!=null ?
                                URLEncoder.encode(run.getNote(), StandardCharsets.UTF_8) : ""
                );                                                                                  bufferedWriter.newLine();
            }
        }
    }

    /**
     * Clears current recorded activities and replaces them with activities from the save file.
     * @param path  Path to save file
     * @throws IOException
     */
    private void loadFile(Path path) throws IOException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            allRuns.clear();
            Collection<Run> runs = new ArrayList<>();
            String label;
            while ((label=bufferedReader.readLine()) != null) {
                if (label.equals("")) {
                    label = null;
                }
                String typeString = bufferedReader.readLine();
                Run.Type type = Arrays.stream(Run.Type.values())
                                    .filter(t -> t.toString().equals(typeString))
                                    .findFirst().orElse(null);
                LocalDate date = LocalDate.ofEpochDay(Long.parseLong(bufferedReader.readLine()));
                List<Run.Segment> segments = new ArrayList<>();
                LocalTime duration; double distance; int hr; int cadence; int elevation;
                int segmentsCount = Integer.parseInt(bufferedReader.readLine());
                for (int i=0; i<segmentsCount; i++) {
                    duration = LocalTime.ofSecondOfDay(Long.parseLong(bufferedReader.readLine()));
                    distance = Double.parseDouble(bufferedReader.readLine());
                    hr = Integer.parseInt(bufferedReader.readLine());
                    cadence = Integer.parseInt(bufferedReader.readLine());
                    elevation = Integer.parseInt(bufferedReader.readLine());
                    segments.add(new Run.Segment(duration, distance, hr, cadence, elevation));
                }
                String terrainString = bufferedReader.readLine();
                Run.Terrain terrain = Arrays.stream(Run.Terrain.values())
                                        .filter(t -> t.toString().equals(terrainString))
                                        .findFirst().orElse(null);
                int evaluation = Integer.parseInt(bufferedReader.readLine());
                String note = URLDecoder.decode(bufferedReader.readLine(), StandardCharsets.UTF_8);
                if (note.equals("")) {
                    note = null;
                }
                runs.add(new Run(label, type, date, segments, terrain, evaluation, note));
            }
            allRuns.addAll(runs);
        }
    }

    /**
     * Starts the application.
     * - configures the primary stage
     * - adds a close application handler
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(getRootPane(), 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.setTitle("Stanislav Kafara, A21B0160P; Running Diary");
        primaryStage.setOnCloseRequest(event -> {
            for (Map.Entry<Run,RunActivityTab> entry : runActivityTabs.entrySet()) {
                if (entry.getValue().isChanged()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Changes Not Saved");
                    alert.setContentText(
                            String.format(
                                    "Make sure to save changes in *%s (%s)* before closing the application.",
                                    entry.getKey().getLabel()!=null ?
                                            entry.getKey().getLabel() :
                                            RunActivityTab.UNLABELED_ACTIVITY_LABEL,
                                    entry.getKey().getDate()!=null ?
                                            entry.getKey().getDate() :
                                            "New Activity"
                            )
                    );
                    alert.getButtonTypes().add(new ButtonType("Discard Changes"));
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isEmpty() || result.get()==ButtonType.OK) {
                        event.consume();
                        return;
                    } // else Discard Changes
                }
            }
        });
        primaryStage.show();
    }

    /**
     * Stops the application.
     * - saves the recorded activities to the save file
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        try {
            saveToFile(SAVE_FILE_PATH);
        } catch (Exception e) {
            // bad luck
        }
    }

    private Parent getRootPane() {
        BorderPane borderPane = new BorderPane();
        {
            borderPane.setTop(getMenuBar());
            borderPane.setCenter(getTabPane());
        }
        return borderPane;
    }

    private Node getMenuBar() {
        MenuBar menuBar = new MenuBar();
        {
            Menu fileMN = new Menu("_File");
            {
                MenuItem newActivityMI = new MenuItem("_New Activity");
                {
                    newActivityMI.setOnAction(event -> createOpenNewActivityTab());
                }
                MenuItem aboutApplicationMI = new MenuItem("_About Application");
                {
                    aboutApplicationMI.setOnAction(event -> alertAboutApplication());
                }
                fileMN.getItems().addAll(newActivityMI, new SeparatorMenuItem(), aboutApplicationMI);
            }
            menuBar.getMenus().add(fileMN);
        }
        return menuBar;
    }

    /**
     * @return  Application TabPane
     */
    private Node getTabPane() {
        tabPane = new TabPane();
        {
            tabPane.getTabs().addAll(getOverviewTab(), getHistoryTab(), new StatisticsTab(allRuns));
        }
        return tabPane;
    }

    /**
     * Creates and returns a "title"-label.
     * @param title Text of the label
     * @return  Title-label
     */
    public static Label getTabTitleLabel(String title) {
        Label titleLB = new Label(title);
        {
            titleLB.setFont(Font.font(24));
        }
        return titleLB;
    }

    /**
     * Creates and returns the application "Overview"-Tab.
     * @return  Application Overview-Tab
     */
    private Tab getOverviewTab() {
        Tab tab = new Tab("Overview");
        {
            tab.setClosable(false);
        }
        {
            BorderPane borderPane = new BorderPane();
            {
                VBox vBox = new VBox(16);
                {
                    vBox.setPadding(new Insets(24));
                }
                {
                    BorderPane headerBP = new BorderPane();
                    {
                        headerBP.setLeft(
                                new DateRangeTitlePane(
                                    getTabTitleLabel("Recent Activities"),
                                    new SimpleObjectProperty<>(LocalDate.now().minusDays(RECENT_DAYS)),
                                    new SimpleObjectProperty<>(LocalDate.now())
                                )
                        );
                        headerBP.setRight(getRecordNewActivityButton());
                    }
                    Node summary = new SummaryPane(recentRunsSet);
                    BorderPane activitiesBP = new BorderPane();
                    {
                        activitiesBP.setCenter(getRecentActivitiesTableView());
                    }
                    vBox.getChildren().addAll(headerBP, summary, activitiesBP);
                }
                borderPane.setCenter(vBox);
            }
            tab.setContent(borderPane);
        }
        return tab;
    }

    /**
     * Creates and returns "Record New Activity"-Button.
     * @return  Record New Activity button
     */
    private Node getRecordNewActivityButton() {
        Button newActivityBT = new Button("Record New Activity");
        {
            newActivityBT.setPrefHeight(32);
            BorderPane.setAlignment(newActivityBT, Pos.CENTER);
        }
        {
            newActivityBT.setOnAction(event -> createOpenNewActivityTab());
        }
        return newActivityBT;
    }

    /**
     * Creates and opens new "Activity"-Tab.
     */
    private void createOpenNewActivityTab() {
        RunActivityTab runActivityTab = new RunActivityTab(allRuns, tabPane.getTabs(), runActivityTabs);
        runActivityTabs.put(Run.getEmptyRun(), runActivityTab);
        tabPane.getTabs().add(runActivityTab);
        tabPane.getSelectionModel().select(runActivityTab);
    }

    private void alertAboutApplication() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Application");
        alert.setHeaderText("Running Diary");
        alert.setContentText("FAV ZČU Plzeň\nKIV/UUR Semester Assignment\n\nAuthor: Stanislav Kafara");
        alert.show();
    }

    /**
     * Creates and returns "Recent Activities"-TableView.
     * @return  Recent Activities tableview
     */
    private Node getRecentActivitiesTableView() {
        recentRunsTV = new TableView<>(recentRunsSet.getSet());
        {
            recentRunsTV.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            recentRunsTV.setOnMouseClicked(event -> {
                if (event.getClickCount()==2) {
                    openRunActivityTabFromTable();
                }
            });
            recentRunsTV.setOnKeyPressed(event -> {
                if (event.getCode()==KeyCode.ENTER) {
                    openRunActivityTabFromTable();
                }
            });
        }
        {
            TableColumn<Run, String> dateCM = new TableColumn<>("Date");
            {
                dateCM.setCellValueFactory(value -> new StringBinding() {
                    @Override
                    protected String computeValue() {
                        return value.getValue().getDate()
                                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
                    }
                });
            }
            TableColumn<Run, Run.Type> typeCM = new TableColumn<>("Type");
            {
                typeCM.setCellValueFactory(new PropertyValueFactory<>("type"));
            }
            TableColumn<Run, String> distanceCM = new TableColumn<>("Distance [km]");
            {
                distanceCM.setCellValueFactory(value -> new StringBinding() {
                    @Override
                    protected String computeValue() {
                        return String.format("%.2f", value.getValue().getDistance());
                    }
                });
            }
            TableColumn<Run, String> durationCM = new TableColumn<>("Duration [h:mm]");
            {
                durationCM.setCellValueFactory(value -> new StringBinding() {
                    @Override
                    protected String computeValue() {
                        return value.getValue().getDuration().format(DateTimeFormatter.ofPattern("H:mm"));
                    }
                });
            }
            TableColumn<Run, String> paceCM = new TableColumn<>("Pace [min/km]");
            {
                paceCM.setCellValueFactory(value -> new StringBinding() {
                    @Override
                    protected String computeValue() {
                        LocalTime pace = value.getValue().getPace();
                        return String.format(
                                "%d:%02d",
                                60*pace.getHour()+pace.getMinute(),
                                pace.getSecond()
                        );
                        //return value.getValue().getPace().format(DateTimeFormatter.ofPattern("m:ss"));
                    }
                });
            }
            TableColumn<Run, String> hrCM = new TableColumn<>("HR [bpm]");
            {
                hrCM.setCellValueFactory(value -> new StringBinding() {
                    @Override
                    protected String computeValue() {
                        int hr = value.getValue().getHR();
                        return hr!=-1 ? ""+hr : "---";
                    }
                });
            }
            recentRunsTV.getColumns().addAll(dateCM, typeCM, distanceCM, durationCM, paceCM, hrCM);
        }
        return recentRunsTV;
    }

    /**
     * Opens new run activity tab for the activity or selects opened one if exists.
     */
    private void openRunActivityTab(Run run) {
        if (! runActivityTabs.containsKey(run)) {
            RunActivityTab runActivityTab = new RunActivityTab(allRuns, tabPane.getTabs(), runActivityTabs);
            runActivityTabs.put(run, runActivityTab);
            runActivityTab.setRunActivity(run);
            tabPane.getTabs().add(runActivityTab);
            tabPane.getSelectionModel().select(runActivityTab);
        } else {
            tabPane.getSelectionModel().select(runActivityTabs.get(run));
        }
    }

    private void openRunActivityTabFromTable() {
        Run selectedRun = recentRunsTV.getSelectionModel().getSelectedItem();
        openRunActivityTab(selectedRun);
        /*if (! runActivityTabs.containsKey(selectedRun)) {
            RunActivityTab runActivityTab = new RunActivityTab(allRuns, tabPane.getTabs(), runActivityTabs);
            runActivityTabs.put(selectedRun, runActivityTab);
            runActivityTab.setRunActivity(selectedRun);
            tabPane.getTabs().add(runActivityTab);
            tabPane.getSelectionModel().select(runActivityTab);
        } else {
            tabPane.getSelectionModel().select(runActivityTabs.get(selectedRun));
        }*/
    }

    /**
     * Creates and returns application "History"-Tab.
     * @return  History-Tab
     */
    private Tab getHistoryTab() {
        Tab tab = new Tab("History");
        {
            tab.setClosable(false);
        }
        {
            BorderPane borderPane = new BorderPane();
            {
                VBox vBox = new VBox(16);
                {
                    vBox.setPadding(new Insets(24));
                }
                {
                    BorderPane headerBP = new BorderPane();
                    {
                        headerBP.setLeft(
                                new DateRangeTitlePane(
                                        getTabTitleLabel("All Activities"),
                                        allRunsSet.oldestActivityDateBinding(), // allRuns oldest
                                        allRunsSet.latestActivityDateBinding() // allRuns newest
                                )
                        );
                        headerBP.setRight(getRecordNewActivityButton());
                    }
                    Node summary = new SummaryPane(allRunsSet);
                    BorderPane activitiesBP = new BorderPane();
                    {
                        historyTVPane = new VBox();
                        activitiesBP.setCenter(historyTVPane);
                        allRuns.addListener((ListChangeListener<Run>) c -> updateHistoryTreeView());
                        VBox runTypesVisibleVB = new VBox();
                        {
                            runTypesVisibleVB.setPadding(new Insets(24, 0, 24, 0));
                        }
                        {
                            runTypesVisibleCB = new CheckBox("Run Activity Types Visible");
                            {
                                runTypesVisibleCB.setOnAction(event -> updateHistoryTreeView());
                            }
                            runTypesVisibleVB.getChildren().add(runTypesVisibleCB);
                        }
                        activitiesBP.setBottom(runTypesVisibleVB);
                    }
                    updateHistoryTreeView();
                    vBox.getChildren().addAll(headerBP, summary, activitiesBP);
                }
                borderPane.setCenter(vBox);
            }
            tab.setContent(borderPane);
        }
        return tab;
    }

    private void updateHistoryTreeView() {
        TreeView<RunTreeItemWrapper> historyTV = new TreeView<>();
        {
            historyTV.setOnMouseClicked(event -> {
                if (event.getClickCount()!=2 || historyTV.getSelectionModel().getSelectedItem()==null) {
                    return;
                }
                RunTreeItemWrapper selectedWrapper = historyTV.getSelectionModel().getSelectedItem().getValue();
                if (selectedWrapper.getRun()==null) {
                    return;
                }
                openRunActivityTab(selectedWrapper.getRun());
            });
            historyTV.setOnKeyPressed(event -> {
                if (event.getCode()!=KeyCode.ENTER || historyTV.getSelectionModel().getSelectedItem()==null) {
                    return;
                }
                RunTreeItemWrapper selectedWrapper = historyTV.getSelectionModel().getSelectedItem().getValue();
                if (selectedWrapper.getRun()==null) {
                    return;
                }
                openRunActivityTab(selectedWrapper.getRun());
            });
        }
        TreeItem<RunTreeItemWrapper> root = new TreeItem<>();
        {
            int[] years = allRunsSet.getSet().stream()
                            .mapToInt(run -> run.getDate().getYear())
                            .distinct()
                            .sorted().toArray();
            for (int i=years.length-1; i>=0; i--) {
                int year = years[i];
                TreeItem<RunTreeItemWrapper> yearTI =
                        new TreeItem<>(new RunTreeItemWrapper(null, ""+year));
                {
                    List<Run> yearFiltered = allRunsSet.getSet().stream()
                                                .filter(run -> run.getDate().getYear()==year).toList();
                    int[] months = yearFiltered.stream()
                                    .mapToInt(run -> run.getDate().getMonthValue())
                                    .distinct()
                                    .sorted().toArray();
                    for (int j=months.length-1; j>=0; j--) {
                        int month = months[j];
                        TreeItem<RunTreeItemWrapper> monthTI =
                                new TreeItem<>(
                                        new RunTreeItemWrapper(
                                                null,
                                                Month.of(month)
                                                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()))
                                );
                        {
                            List<Run> monthYearFiltered = yearFiltered.stream()
                                    .filter(run -> run.getDate().getMonthValue()==month)
                                    .toList();
                            List<Run.Type> types = monthYearFiltered.stream()
                                                    .map(Run::getType)
                                                    .distinct()
                                                    .sorted(Comparator.comparing(Run.Type::toString))
                                                    .toList();
                            for (Run.Type type : types) {
                                TreeItem<RunTreeItemWrapper> typeTI =
                                        new TreeItem<>(new RunTreeItemWrapper(null, type.toString()));
                                {
                                    TreeItem<RunTreeItemWrapper> finalTI =
                                            (runTypesVisibleCB.isSelected()) ? typeTI : monthTI;
                                    monthYearFiltered.stream()
                                            .filter(run -> run.getType()==type)
                                            .forEach(run -> finalTI.getChildren().add(
                                                    new TreeItem<>(
                                                            new RunTreeItemWrapper(run, run.toString())
                                                    )
                                            ));
                                }
                                if (runTypesVisibleCB.isSelected()) {
                                    monthTI.getChildren().add(typeTI);
                                }
                            }
                        }
                        yearTI.getChildren().add(monthTI);
                    }
                }
                root.getChildren().add(yearTI);
            }
        }
        historyTV.setRoot(root);
        historyTV.setShowRoot(false);
        TreeItem<RunTreeItemWrapper> latestRunTI = historyTV.getRoot();
        for (int i=0; i<3; i++) {
            latestRunTI.setExpanded(true);
            if (latestRunTI.getChildren().size()==0) {
                break;
            }
            latestRunTI = latestRunTI.getChildren().get(0);
        }
        historyTVPane.getChildren().clear();
        historyTVPane.getChildren().add(historyTV);
    }

}
