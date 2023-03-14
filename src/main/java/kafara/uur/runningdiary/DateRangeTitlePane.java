package kafara.uur.runningdiary;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Represents a date range title pane
 * @author  Stanislav Kafara
 * @version 1 2022-05-16
 */
public class DateRangeTitlePane extends VBox {

    public DateRangeTitlePane(Label titleLB, ObservableValue<LocalDate> from, ObservableValue<LocalDate> to) {
        setSpacing(8);
        Label dateRangeLB = new Label();
        {
            dateRangeLB.textProperty().bind(new StringBinding() {
                {
                    bind(from, to);
                }
                @Override
                protected String computeValue() {
                    if (from.getValue().equals(LocalDate.MIN)) {
                        return "No Activities";
                    }
                    return String.format(
                            "%s - %s",
                            from.getValue().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            to.getValue().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    );
                }
            });
        }
        getChildren().addAll(
                titleLB,
                dateRangeLB
        );
    }

}
