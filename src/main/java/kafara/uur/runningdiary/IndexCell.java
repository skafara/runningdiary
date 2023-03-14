package kafara.uur.runningdiary;

import javafx.scene.control.TableCell;

/**
 * Represents an index cell for tableview.
 * @author Stanislav Kafara
 * @version 1 2022-05-16
 */
public class IndexCell extends TableCell {

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty==true) {
            setText(null);
        } else {
            setText(""+(getIndex() + 1));
        }
    }
}
