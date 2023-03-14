package kafara.uur.runningdiary;

/**
 * Represents a tree item wrapper for a run activity
 * @author Stanislav Kafara
 * @version 1 2022-05-16
 */
public class RunTreeItemWrapper {

    private final Run run;
    private final String string;

    public RunTreeItemWrapper(Run run, String string) {
        this.run = run;
        this.string = string;
    }

    public Run getRun() {
        return run;
    }

    @Override
    public String toString() {
        return string;
    }

}
