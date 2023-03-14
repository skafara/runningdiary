package kafara.uur.runningdiary;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents an input-validating table cell.
 * @param <S>   Tableview type
 * @param <T>   Cell type
 * @author Stanislav Kafara
 * @version 1 2022-05-16
 */
public class CheckedLabelTableCell<S, T> extends TableCell<S, T> {

    private TextField textField = new TextField();

    /** Converts the input to <T> */
    private Function<String, T> fromString;

    /** Converts the <T> to string */
    private Function<T, String> toString;

    public CheckedLabelTableCell(Predicate<String> validity, Function<String, T> fromString, Function<T, String> toString) {
        this.fromString = fromString;
        this.toString = toString;
        textField.setOnAction(event -> {
            String input = textField.getText();
            if (validity.test(input)) {
                commitEdit(fromString.apply(input));
            } else {
                event.consume();
            }
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
            return;
        }
        if (isEditing()) {
            setGraphic(textField);
            textField.setText(toString.apply(getItem()));
        } else {
            setGraphic(null);
            setText(toString.apply(getItem()));
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setGraphic(textField);
        setText(null);
        textField.setText(toString.apply(getItem()));
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
        setText(toString.apply(getItem()));
    }

}
