package kafara.uur.runningdiary;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a run activity
 * @author  Stanislav Kafara
 * @version 1 2022-05-16
 */
public class Run {

    /** Activity label */
    private String label;

    /** Activity type */
    private Type type;

    /** Run segments */
    private List<Segment> segments;

    /** Activity date */
    private LocalDate date;

    /** Activity terrain */
    private Terrain terrain;

    /**
     * Activity evaluation
     * -1: none
     * else 1-10
     */
    private int evaluation;

    /** Activity note */
    private String note;

    public Run(String label, Type type, LocalDate date, List<Segment> segments, Terrain terrain, int evaluation, String note) {
        this.label = label;
        this.type = type;
        this.date = date;
        this.segments = segments;
        this.terrain = terrain;
        this.evaluation = evaluation;
        this.note = note;
    }

    /**
     * Run activity copy
     * @param run   Run activity to be copied
     */
    public Run(Run run) {
        this.label = run.label;
        this.type = run.type;
        this.date = run.date;
        this.segments = new ArrayList<>();
        for (Segment segment : run.segments) {
            this.segments.add(new Segment(segment));
        }
        this.terrain = run.terrain;
        this.evaluation = run.evaluation;
        this.note = run.note;
    }

    /**
     * Returns an empty activity.
     * @return  Empty activity (dummy)
     */
    public static Run getEmptyRun() {
        return new Run(null, null, null, null, null, -1, null);
    }

    public String getLabel() {
        return label;
    }

    public Type getType() {
        return type;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * Calculates and returns the duration of the activity.
     * @return  Duration of the activity
     */
    public LocalTime getDuration() {
        return LocalTime.ofSecondOfDay(getDurationSeconds());
    }

    /**
     * Calculates and returns the duration of the activity in seconds.
     * @return  Duration of the activity in seconds
     */
    private int getDurationSeconds() {
        return segments.stream()
                .mapToInt(segment -> segment.getDuration().toSecondOfDay())
                .sum();
    }

    public double getDistance() {
        return segments.stream()
                .mapToDouble(Segment::getDistance)
                .sum();
    }

    public LocalTime getPace() {
        return LocalTime.ofSecondOfDay((long) (getDurationSeconds()/getDistance()));
    }

    /**
     * Returns average HR over segments containing HR value.
     * @return  Average HR over segments containing HR value
     */
    public int getHR() {
        if (
                segments.stream().anyMatch(segment -> segment.getHr()!=-1)
        ) {
            List<Segment> filteredSegmnts = segments.stream().filter(segment -> segment.getHr()!=-1).toList();
            return (int) (
                    filteredSegmnts.stream()
                        .mapToLong(segment -> (long) segment.getHr()*segment.getDuration().toSecondOfDay())
                        .sum()
                    /
                    filteredSegmnts.stream()
                        .mapToInt(segment -> segment.getDuration().toSecondOfDay())
                        .sum()
            );
        }
        return -1;
    }

    public int getEvaluation() {
        return evaluation;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toString() {
        return String.format(
                "%-12s  Distance:  %5.2f km  Duration:  %s h",
                date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                getDistance(),
                getDuration().format(DateTimeFormatter.ofPattern("H:mm"))
        );
    }

    public static enum Type {
        EASY_RUN, LONG_RUN, STEADY_RUN, INTERVAL_RUN, HILLS, FARTLEK, RACE;

        @Override
        public String toString() {
            switch (this) {
                case EASY_RUN:
                    return "Easy Run";
                case LONG_RUN:
                    return "Long Run";
                case STEADY_RUN:
                    return "Steady Run";
                case INTERVAL_RUN:
                    return "Interval Run";
                case HILLS:
                    return "Hills";
                case FARTLEK:
                    return "Fartlek";
                case RACE:
                    return "Race";
            }
            return null;
        }
    }

    public static enum Terrain {
        ASPHALT, DIRT, MIX;

        @Override
        public String toString() {
            switch (this) {
                case ASPHALT:
                    return "Asphalt";
                case DIRT:
                    return "Dirt";
                case MIX:
                    return "Mix";
            }
            return null;
        }
    }

    /**
     * Represents a segment of a run
     */
    public static class Segment {

        /** Segment duration */
        private final ObjectProperty<LocalTime> durationProperty = new SimpleObjectProperty<>(null);
        /** Segment distance */
        private final DoubleProperty distanceProperty = new SimpleDoubleProperty(1);
        /** Segment average hr */
        private final IntegerProperty hrProperty = new SimpleIntegerProperty(-1);
        /** Segment average cadence */
        private final IntegerProperty cadenceProperty = new SimpleIntegerProperty(-1);
        /** Segment elevation */
        private final IntegerProperty elevationProperty = new SimpleIntegerProperty(Integer.MIN_VALUE);

        /**
         * Dummy segment
         */
        public Segment() {}

        public Segment(LocalTime duration, double distance, int hr, int cadence, int elevation) {
            durationProperty.set(duration);
            distanceProperty.set(distance);
            hrProperty.set(hr);
            cadenceProperty.set(cadence);
            elevationProperty.set(elevation);
        }

        /**
         * Segment copy
         * @param segment   Segment to be copied
         */
        public Segment(Segment segment) {
            this.durationProperty.set(segment.durationProperty.get());
            this.distanceProperty.set(segment.distanceProperty.get());
            this.hrProperty.set(segment.hrProperty.get());
            this.cadenceProperty.set(segment.cadenceProperty.get());
            this.elevationProperty.set(segment.elevationProperty.get());
        }

        public ObjectProperty<LocalTime> durationProperty() {
            return durationProperty;
        }

        public LocalTime getDuration() {
            return durationProperty.get();
        }

        public DoubleProperty distanceProperty() {
            return distanceProperty;
        }

        public double getDistance() {
            return distanceProperty.get();
        }

        public IntegerProperty hrProperty() {
            return hrProperty;
        }

        public int getHr() {
            return hrProperty.get();
        }

        public IntegerProperty cadenceProperty() {
            return cadenceProperty;
        }

        public int getCadence() {
            return cadenceProperty.get();
        }

        public IntegerProperty elevationProperty() {
            return elevationProperty;
        }

        public int getElevation() {
            return elevationProperty.get();
        }
    }

}
