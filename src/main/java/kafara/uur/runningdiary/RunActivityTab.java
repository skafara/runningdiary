package kafara.uur.runningdiary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Represents a run activity Tab in TabPane
 * @author  Stanislav Kafara
 * @version 1 2022-05-16
 */
public class RunActivityTab extends Tab {

    /** Unlabeled activity label */
    public static final String UNLABELED_ACTIVITY_LABEL = "Unlabeled Activity";

    /** All recorded run activities */
    private final ObservableList<Run> allRuns;
    /** All opened application tabs */
    private final ObservableList<Tab> allTabs;
    /** "Activity editing"-Tabs mapped to the activities to be edited. */
    private final HashMap<Run, RunActivityTab> runActivityTabs;

    /** Run activity to be edited */
    private Run runToBeEdited;
    /** Run activity which properties are actually being edited */
    private Run runBeingEdited;

    private Label runLabelLB;
    private TextField labelTF;
    private ChoiceBox<Run.Type> typeCB;
    private DatePicker dateDP;
    private Button deleteBT;
    private TableView<Run.Segment> segmentsTV;
    private ToggleGroup terrainTG;
    private Slider evaluationSL;
    private TextArea noteTA;

    public RunActivityTab(ObservableList<Run> allRuns, ObservableList<Tab> allTabs, HashMap<Run, RunActivityTab> runActivityTabs) {
        this.allRuns = allRuns;
        this.allTabs = allTabs;
        this.runActivityTabs = runActivityTabs;
        setContent(getTabContent());
        setRunActivity(null);
        setOnCloseRequest(event -> {
            if (! isChanged() || runToBeEdited==null) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Changes Not Saved");
            alert.setContentText("Make sure to save the changes.");
            alert.getButtonTypes().add(new ButtonType("Discard Changes"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get()==ButtonType.OK) {
                event.consume();
            } // else Discard Changes
        });
        setOnClosed(event -> {
            if (runToBeEdited!=null) {
                runActivityTabs.remove(runToBeEdited);
            } else {
                for (Map.Entry<Run, RunActivityTab> tabEntry : runActivityTabs.entrySet()) {
                    if (tabEntry.getValue()==RunActivityTab.this) {
                        runActivityTabs.remove(tabEntry.getKey());
                        return;
                    }
                }
            }
        });
    }

    private Node getTabContent() {
        BorderPane borderPane = new BorderPane();
        {
            VBox vBox = new VBox(16);
            {
                vBox.setPadding(new Insets(24));
            }
            {
                vBox.getChildren().addAll(
                        getRunActivityTop(),
                        getRunActivityForm()
                );
            }
            borderPane.setCenter(vBox);
            borderPane.setBottom(getSaveActivityButtonPane());
        }
        return borderPane;
    }

    /**
     * Fills in the run activity data.
     * @param run   Run to be shown/edited, null=new
     */
    public void setRunActivity(Run run) {
        runToBeEdited = run;
        runBeingEdited = (run!=null) ? new Run(run) : null;
        if (runBeingEdited==null) {
            setText(UNLABELED_ACTIVITY_LABEL);
            runLabelLB.setText(UNLABELED_ACTIVITY_LABEL);
            typeCB.setValue(Run.Type.EASY_RUN);
            dateDP.setValue(LocalDate.now());
            deleteBT.setVisible(false);
            segmentsTV.setItems(FXCollections.observableArrayList(new Run.Segment()));
            terrainTG.selectToggle(null);
            evaluationSL.setValue(-1);
            noteTA.setText(null);
        } else {
            String label = UNLABELED_ACTIVITY_LABEL;
            if (runBeingEdited.getLabel()!=null) {
                label = runBeingEdited.getLabel();
                labelTF.setText(label);
            }
            setText(label);
            runLabelLB.setText(label);
            typeCB.setValue(runBeingEdited.getType());
            dateDP.setValue(runBeingEdited.getDate());
            deleteBT.setVisible(true);
            segmentsTV.setItems(FXCollections.observableList(runBeingEdited.getSegments()));
            terrainTG.selectToggle(
                    terrainTG.getToggles().stream()
                            .filter(toggle -> toggle.getUserData()==runBeingEdited.getTerrain())
                            .findFirst().orElse(null)
            );
            evaluationSL.setValue(runBeingEdited.getEvaluation());
            noteTA.setText(runBeingEdited.getNote());
        }
    }

    private Node getRunActivityTop() {
        BorderPane borderPane = new BorderPane();
        {
            runLabelLB = RunningDiary.getTabTitleLabel(null);
            borderPane.setLeft(runLabelLB);
            borderPane.setRight(getDeleteActivityButton());
        }
        return borderPane;
    }

    private Node getDeleteActivityButton() {
        deleteBT = new Button("Delete Activity");
        {
            deleteBT.setOnAction(event -> deleteRunActivity());
        }
        return deleteBT;
    }

    private Node getRunActivityForm() {
        VBox vBox = new VBox(16);
        {
            vBox.getChildren().addAll(
                    getRunActivityFormHeader(),
                    getRunActivityFormSegments(),
                    getRunActivityFormOther()
            );
        }
        return vBox;
    }

    private Node getRunActivityFormHeader() {
        BorderPane borderPane = new BorderPane();
        {
            VBox labelVB = new VBox(8);
            {
                labelVB.setAlignment(Pos.CENTER);
            }
            {
                Label labelLB = new Label("Label");
                labelTF = new TextField();
                {
                    labelTF.setPrefWidth(320);
                }
                labelVB.getChildren().addAll(labelLB, labelTF);
            }
            VBox typeVB = new VBox(8);
            {
                typeVB.setAlignment(Pos.CENTER);
            }
            {
                Label typeLB = new Label("Type");
                typeCB = new ChoiceBox<>(
                        FXCollections.observableArrayList(Arrays.stream(Run.Type.values()).toList())
                );
                {
                    typeCB.setPrefWidth(160);
                }
                typeVB.getChildren().addAll(typeLB, typeCB);
            }
            VBox dateVB = new VBox(8);
            {
                dateVB.setAlignment(Pos.CENTER);
            }
            {
                Label dateLB = new Label("Date");
                dateDP = new DatePicker();
                {
                    dateDP.setPrefWidth(160);
                }
                dateVB.getChildren().addAll(dateLB, dateDP);
            }
            borderPane.setLeft(labelVB);
            borderPane.setCenter(typeVB);
            borderPane.setRight(dateVB);
        }
        return borderPane;
    }

    private Node getRunActivityFormSegments() {
        VBox vBox = new VBox(8);
        {
            Label segmentsLB = new Label("Segments");
            segmentsTV = new TableView<>();
            {
                segmentsTV.setMinHeight(160);
                segmentsTV.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                segmentsTV.setEditable(true);
            }
            {
                TableColumn<Run.Segment, LocalTime> indexCM = new TableColumn<>("Index");
                {
                    indexCM.setCellFactory(column -> new IndexCell());
                }
                TableColumn<Run.Segment, LocalTime> durationCM = new TableColumn<>("Duration [m:ss]");
                {
                    durationCM.setCellValueFactory(new PropertyValueFactory<>("duration"));
                    durationCM.setCellFactory(column -> new CheckedLabelTableCell<>(
                            string -> {
                                if (string.equals("")) {
                                    return true;
                                }
                                try {
                                    if (isSegmentDurationValidFormat(string)) {
                                        return true;
                                    }
                                } catch (InputMismatchException e) {
                                    Alert alert = new Alert(Alert.AlertType.WARNING);
                                    alert.setHeaderText("Invalid Input");
                                    alert.setContentText(e.getMessage());
                                    alert.showAndWait();
                                }
                                return false;
                            },
                            string -> {
                                if (string.equals("")) {
                                    return null;
                                }
                                String[] parts = string.split(":");
                                int min = Integer.parseInt(parts[0]);
                                int sec = Integer.parseInt(parts[1]);
                                return LocalTime.ofSecondOfDay(60L*min+sec);
                            },
                            localTime -> {
                                if (localTime==null) {
                                    return "";
                                }
                                return String.format(
                                        "%d:%02d",
                                        60*localTime.getHour()+localTime.getMinute(),
                                        localTime.getSecond()
                                );
                                //return localTime.format(DateTimeFormatter.ofPattern("m:ss"));
                            }
                    ));
                }
                TableColumn<Run.Segment, Double> distanceCM = new TableColumn<>("Distance [km]");
                {
                    distanceCM.setCellValueFactory(new PropertyValueFactory<>("distance"));
                    distanceCM.setCellFactory(column -> new CheckedLabelTableCell<>(
                            string -> {
                                try {
                                    if (string.equals("")) {
                                        return true;
                                    }
                                    double distance = Double.parseDouble(string);
                                    if (distance<=0) {
                                        return false;
                                    }
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            },
                            s -> {
                                if (s.equals("")) {
                                    return -1.0;
                                }
                                return Double.parseDouble(s);
                            },
                            t -> {
                                if (t==-1) {
                                    return "";
                                }
                                return t.toString();
                            }
                    ));
                    //distanceCM.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
                }
                TableColumn<Run.Segment, Integer> hrCM = new TableColumn<>("HR [bpm]");
                {
                    hrCM.setCellValueFactory(new PropertyValueFactory<>("hr"));
                    hrCM.setCellFactory(column -> new CheckedLabelTableCell<>(
                            string -> {
                                try {
                                    if (string.equals("")) {
                                        return true;
                                    }
                                    int hr = Integer.parseInt(string);
                                    if (hr<=0) {
                                        return false;
                                    }
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            },
                            string -> {
                                if (string.equals("")) {
                                    return -1;
                                }
                                return Integer.parseInt(string);
                            },
                            integer -> {
                                if (integer==-1) {
                                    return "";
                                }
                                return integer.toString();
                            }
                    ));
                }
                TableColumn<Run.Segment, Integer> cadenceCM = new TableColumn<>("Cadence [spm]");
                {
                    cadenceCM.setCellValueFactory(new PropertyValueFactory<>("cadence"));
                    cadenceCM.setCellFactory(column -> new CheckedLabelTableCell<>(
                            string -> {
                                try {
                                    if (string.equals("")) {
                                        return true;
                                    }
                                    int cadence = Integer.parseInt(string);
                                    if (cadence<=0) {
                                        return false;
                                    }
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            },
                            string -> {
                                if (string.equals("")) {
                                    return -1;
                                }
                                return Integer.parseInt(string);
                            },
                            integer -> {
                                if (integer==-1) {
                                    return "";
                                }
                                return integer.toString();
                            }
                    ));
                }
                TableColumn<Run.Segment, Integer> elevationCM = new TableColumn<>("Elevation [m]");
                {
                    elevationCM.setCellValueFactory(new PropertyValueFactory<>("elevation"));
                    elevationCM.setCellFactory(column -> new CheckedLabelTableCell<>(
                            string -> {
                                try {
                                    if (string.equals("")) {
                                        return true;
                                    }
                                    Integer.parseInt(string);
                                    return true;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            },
                            string -> {
                                if (string.equals("")) {
                                    return Integer.MIN_VALUE;
                                }
                                return Integer.parseInt(string);
                            },
                            integer -> {
                                if (integer==Integer.MIN_VALUE) {
                                    return "";
                                }
                                return integer.toString();
                            }
                    ));
                }
                segmentsTV.getColumns().addAll(indexCM, durationCM, distanceCM, hrCM, cadenceCM, elevationCM);
                for (TableColumn column : segmentsTV.getColumns()) {
                    column.setSortable(false);
                }
            }
            HBox segmentsBT = new HBox(8);
            {
                segmentsBT.setAlignment(Pos.CENTER_RIGHT);
            }
            {
                Button addSegmentBT = new Button("Add Segment");
                {
                    addSegmentBT.setOnAction(event -> addSegment());
                }
                Button deleteSegmentBT = new Button("Delete Segment");
                {
                    deleteSegmentBT.setOnAction(event -> deleteSelectedSegment());
                }
                segmentsBT.getChildren().addAll(deleteSegmentBT, addSegmentBT);
            }
            vBox.getChildren().addAll(segmentsLB, segmentsTV, segmentsBT);
        }
        return vBox;
    }

    private boolean isSegmentDurationValidFormat(String input) throws InputMismatchException {
        try {
            String[] parts = input.split(":");
            if (parts.length != 2 || parts[1].length() != 2) {
                throw new InputMismatchException("Duration must be provided in m:ss format.");
            }
            int min = Integer.parseInt(parts[0]);
            int sec = Integer.parseInt(parts[1]);
            if (min < 0 || sec < 0) {
                return false;
            }
            if (min==0 && sec==0) {
                return false;
            }
            if (min>=24*60) {
                throw new InputMismatchException("Segment must be shorter than 24 hours.");
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Node getRunActivityFormOther() {
        VBox vBox = new VBox(8);
        {
            VBox terrainVB = new VBox(8);
            {
                Label terrainLB = new Label("Terrain");
                terrainTG = new ToggleGroup();
                HBox terrainRBVHB = new HBox(8);
                {
                    terrainRBVHB.setAlignment(Pos.CENTER);
                }
                {
                    for (Run.Terrain terrain : Run.Terrain.values()) {
                        RadioButton radioButton = new RadioButton(terrain.toString());
                        radioButton.setUserData(terrain);
                        terrainTG.getToggles().add(radioButton);
                        terrainRBVHB.getChildren().add(radioButton);
                    }
                }
                terrainVB.getChildren().addAll(terrainLB, terrainRBVHB);
            }
            VBox evaluationVB = new VBox(8);
            {
                Label evaluationLB = new Label("Evaluation");
                HBox evaluationSLHB = new HBox();
                {
                    evaluationSLHB.setAlignment(Pos.CENTER);
                }
                {
                    evaluationSL = new Slider(-1, 10, -1);
                    {
                        evaluationSL.setPrefWidth(240);
                        evaluationSL.setMajorTickUnit(1);
                        evaluationSL.setMinorTickCount(0);
                        evaluationSL.setBlockIncrement(1);
                        evaluationSL.setSnapToTicks(true);
                        evaluationSL.setShowTickMarks(true);
                        evaluationSL.setShowTickLabels(true);
                    }
                    evaluationSLHB.getChildren().add(evaluationSL);
                }
                evaluationVB.getChildren().addAll(evaluationLB, evaluationSLHB);
            }
            VBox noteVB = new VBox(8);
            {
                Label noteLB = new Label("Note");
                noteTA = new TextArea();
                {
                    noteTA.setMinHeight(80);
                }
                noteVB.getChildren().addAll(noteLB, noteTA);
            }
            vBox.getChildren().addAll(terrainVB, evaluationVB, noteVB);
        }
        return vBox;
    }

    private Node getSaveActivityButtonPane() {
        HBox hBox = new HBox();
        {
            hBox.setPadding(new Insets(0, 24, 24, 24));
            hBox.setAlignment(Pos.CENTER_RIGHT);
        }
        {
            Button saveBT = new Button("Save Activity");
            saveBT.setOnAction(event -> saveRunActivity());
            hBox.getChildren().add(saveBT);
        }
        return hBox;
    }

    /**
     * Deletes the run activity.
     * - Removes it from (background-)list
     * - Closes the Tab
     */
    private void deleteRunActivity() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Are you sure you want to delete this activity?");
        alert.setContentText(
                String.format(
                        "Activity *%s (%s)* will be permanently deleted.",
                        runToBeEdited.getLabel()!=null ? runToBeEdited.getLabel() : UNLABELED_ACTIVITY_LABEL, runToBeEdited.getDate()
                )
        );
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get()==ButtonType.YES) {
            allRuns.remove(runToBeEdited);
            allTabs.remove(this);
            runActivityTabs.remove(runToBeEdited);
        }
    }

    /**
     * Adds a dummy segment to the tableview.
     */
    private void addSegment() {
        segmentsTV.getItems().add(new Run.Segment());
    }

    /**
     * Deletes the selected segment in the tableview.
     */
    private void deleteSelectedSegment() {
        Run.Segment selectedSegment = segmentsTV.getSelectionModel().getSelectedItem();
        if (selectedSegment==null) {
            return;
        }
        segmentsTV.getItems().remove(selectedSegment);
        segmentsTV.getSelectionModel().clearSelection();
    }

    /**
     * Saves the run activity.
     * - Replaces the activity to be edited with the new (edited) activity
     * - Updates the Tab
     */
    private void saveRunActivity() {
        if (dateDP.getValue().compareTo(LocalDate.now()) > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Invalid input");
            alert.setContentText("Recorded activity cannot happen in future.");
            alert.showAndWait();
            return;
        }
        if (segmentsTV.getItems().size()==0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Invalid input");
            alert.setContentText("You need to fill in run activity segments.");
            alert.showAndWait();
            return;
        }
        for (Run.Segment segment : segmentsTV.getItems()) {
            if (segment.getDuration()==null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Invalid input");
                alert.setContentText("All segment durations must be filled in.");
                alert.showAndWait();
                return;
            }
        }
        String label = labelTF.getText();
        if (label.equals("")) {
            label = null;
        }
        Run newRun = new Run(
                label,
                typeCB.getValue(),
                dateDP.getValue(),
                new ArrayList<>(segmentsTV.getItems()),
                (terrainTG.getSelectedToggle()!=null) ?
                        (Run.Terrain) terrainTG.getSelectedToggle().getUserData() : null,
                (int) evaluationSL.getValue(),
                noteTA.getText()
        );
        allRuns.remove(runToBeEdited);
        runActivityTabs.remove(runToBeEdited);
        allRuns.add(newRun);
        runActivityTabs.put(newRun, this);
        setRunActivity(newRun);
    }

    /**
     * Determines whether a change has been made.
     * @return  True, if a change has been made, else false.
     */
    public boolean isChanged() {
        if (runToBeEdited==null) {
            if (
                    ! labelTF.getText().equals("") ||
                    typeCB.getValue()!=Run.Type.EASY_RUN ||
                    ! dateDP.getValue().equals(LocalDate.now()) ||
                    segmentsTV.getItems().size()!=1 ||
                    segmentsTV.getItems().get(0).getDuration()!=null ||
                    segmentsTV.getItems().get(0).getDistance()!=1 ||
                    segmentsTV.getItems().get(0).getHr()!=-1 ||
                    segmentsTV.getItems().get(0).getCadence()!=-1 ||
                    segmentsTV.getItems().get(0).getElevation()!=-1 ||
                    terrainTG.getSelectedToggle()!=null ||
                    evaluationSL.getValue()!=-1 ||
                    noteTA.getText()!=null
            ) {
                return true;
            }
        } else {
            if (runToBeEdited.getLabel()==null) {
                if (! labelTF.getText().equals("")) {
                    return true;
                }
            } else {
                if (! labelTF.getText().equals(runToBeEdited.getLabel())) {
                    return true;
                }
            }
            if (
                    typeCB.getValue()!=runToBeEdited.getType() ||
                    ! dateDP.getValue().equals(runToBeEdited.getDate()) ||
                    segmentsTV.getItems().size()!=runToBeEdited.getSegments().size() ||
                    evaluationSL.getValue()!=runToBeEdited.getEvaluation()
            ) {
                return true;
            }
            if (runToBeEdited.getTerrain()==null) {
                if (terrainTG.getSelectedToggle()!=null) {
                    return true;
                }
            } else {
                if (terrainTG.getSelectedToggle().getUserData()!=runToBeEdited.getTerrain()) {
                    return true;
                }
            }
            if (runToBeEdited.getNote()==null) {
                if (noteTA.getText()!=null) {
                    return true;
                }
            } else {
                if (! runToBeEdited.getNote().equals(noteTA.getText())) {
                    return true;
                }
            }
            for (int i=0; i<segmentsTV.getItems().size(); i++) {
                Run.Segment segment = segmentsTV.getItems().get(i);
                Run.Segment segmentToBeEdited = runToBeEdited.getSegments().get(i);
                if (
                        segment.getDuration()==null ||
                        ! segment.getDuration().equals(segmentToBeEdited.getDuration()) ||
                        segment.getDistance()!=segmentToBeEdited.getDistance() ||
                        segment.getHr()!=segmentToBeEdited.getHr() ||
                        segmentsTV.getItems().get(i).getCadence()!=segmentToBeEdited.getCadence() ||
                        segment.getElevation()!=segmentToBeEdited.getElevation()
                ) {
                    return true;
                }
            }
        }
        return false;
    }

}
