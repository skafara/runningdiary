package kafara.uur.runningdiary;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a set of run activities.
 * @author  Stanislav Kafara
 * @version 1 2022-05-16
 */
public class RunsSet {

    /** Tests whether activity happened between including */
    private final Predicate<Run> IS_BETWEEN_INCLUDING = new Predicate<>() {
        @Override
        public boolean test(Run run) {
            boolean isAfterIncluding = run.getDate().plusDays(1).isAfter(from);
            boolean isBeforeIncluding = run.getDate().minusDays(1).isBefore(to);
            return isAfterIncluding&&isBeforeIncluding;
        }
    };

    /** Set of activities */
    private final ObservableList<Run> set = FXCollections.observableArrayList();
    /** Actvities from ... */
    private final LocalDate from;
    /** Activities to ... */
    private final LocalDate to;

    /** Activities count */
    private final IntegerProperty runsCount = new SimpleIntegerProperty();
    /** Total distance of activities */
    private final DoubleProperty totalDistance = new SimpleDoubleProperty();
    /** Total duration of activities */
    private final IntegerProperty totalDuration = new SimpleIntegerProperty();
    /** Average activity distance - automatically updates with changes in "Activities count" */
    private final DoubleBinding averageDistance = new DoubleBinding() {
        {
            bind(runsCount); // updates with runsCount update (List - remove, add)
        }
        @Override
        protected double computeValue() {
            return (runsCount.get()>0) ? totalDistance.get()/runsCount.get() : -1;
        }
    };
    /** Average activity duration - automatically updates with changes in "Activities count" */
    private final IntegerBinding averageDuration = new IntegerBinding() {
        {
            bind(runsCount);
        }
        @Override
        protected int computeValue() {
            return (runsCount.get()>0) ? totalDuration.get()/runsCount.get() : -1;
        }
    };
    /** Average activity evaluation - automatically updates with changes in "Activities count" */
    private final DoubleBinding averageEvaluation = new DoubleBinding() {
        {
            bind(runsCount);
        }
        @Override
        protected double computeValue() {
            List<Run> filteredRuns = set.stream().filter(run -> run.getEvaluation()!=-1).toList();
            if (
                    filteredRuns.size()!=0
            ) {
                return filteredRuns.stream()
                        .mapToDouble(Run::getEvaluation)
                        .average().getAsDouble();
            }
            return -1;
        }
    };
    /** Oldest activity date - automatically updates with changes in "Activities count" */
    private final ObjectBinding<LocalDate> oldestActivityDate = new ObjectBinding<>() {
        {
            bind(runsCount);
        }
        @Override
        protected LocalDate computeValue() {
            return runsCount.get() != 0 ?
                    set.stream().map(Run::getDate).min(LocalDate::compareTo).get()
                    : LocalDate.MIN;
        }
    };
    /** Latest activity date - automatically updates with changes in "Activities count" */
    private final ObjectBinding<LocalDate> latestActivityDate = new ObjectBinding<>() {
        {
            bind(runsCount);
        }
        @Override
        protected LocalDate computeValue() {
            return runsCount.get() != 0 ?
                    set.stream().map(Run::getDate).max(LocalDate::compareTo).get()
                    : LocalDate.MAX;
        }
    };

    public RunsSet(ObservableList<Run> allRuns) {
        this(allRuns, LocalDate.MIN, LocalDate.MAX);
    }

    /**
     * Creates a run activities set in date range from-to.
     * Handles (backround-)list updates - set updates with (background-)list updates.
     * @param allRuns   Run activities to be considered (selected from)
     * @param from      From
     * @param to        To
     */
    public RunsSet(ObservableList<Run> allRuns, LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
        allRuns.addListener(
            (ListChangeListener<Run>) c -> {
                c.next();
                set.removeAll(
                        c.getRemoved().stream()
                                .filter(IS_BETWEEN_INCLUDING)
                                .toList()
                );
                set.addAll(
                        c.getAddedSubList().stream()
                                .filter(IS_BETWEEN_INCLUDING)
                                .toList()
                );
                update();
            }
        );
        allRuns.addListener(
                (ListChangeListener<Run>) c ->
                        set.sort(Comparator.comparing(Run::getDate).reversed())
        );
        set.addAll(allRuns.stream().filter(IS_BETWEEN_INCLUDING).toList());
    }

    /**
     * Updates main properties.
     * - Other are bound to them
     */
    private void update() {
        totalDistance.set(
                set.stream()
                        .mapToDouble(Run::getDistance)
                        .sum()
        );
        totalDuration.set(
                set.stream()
                        .mapToInt(run -> run.getDuration().toSecondOfDay())
                        .sum()
        );
        runsCount.set(set.size());
    }

    /**
     * @return  Activities
     */
    public ObservableList<Run> getSet() {
        return FXCollections.unmodifiableObservableList(set);
    }

    public IntegerProperty runsCountProperty() {
        return runsCount;
    }

    public DoubleProperty totalDistanceProperty() {
        return totalDistance;
    }

    public IntegerProperty totalDurationProperty() {
        return totalDuration;
    }

    public DoubleBinding averageDistanceBinding() {
        return averageDistance;
    }

    public IntegerBinding averageDurationBinding() {
        return averageDuration;
    }

    public DoubleBinding averageEvaluationBinding() {
        return averageEvaluation;
    }

    public ObjectBinding<LocalDate> oldestActivityDateBinding() {
        return oldestActivityDate;
    }

    public ObjectBinding<LocalDate> latestActivityDateBinding() {
        return latestActivityDate;
    }

}
