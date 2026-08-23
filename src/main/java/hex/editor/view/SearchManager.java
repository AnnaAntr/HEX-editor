package hex.editor.view;

import hex.editor.table.MainTableModel;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class SearchManager {
    private final JTable table;
    private final MainTableModel tableModel;
    private List<Integer> matches = new ArrayList<>();;
    private int currentMatchPosition = 0;

    public SearchManager(JTable table, MainTableModel tableModel) {
        this.table = table;
        this.tableModel = tableModel;
    }

    public void search(String stringToSearch, JLabel infoLabel) {
        if (!stringToSearch.isEmpty() && tableModel.getFileManager() != null) {
            if (!stringToSearch.matches("^[A-F0-9?\\s+]+$"))
                return;

            String[] pattern = stringToSearch.split("\\s+");

            matches = tableModel.getFileManager().findByValue(pattern);

            if (!matches.isEmpty()) {
                currentMatchPosition = 0;
                goToMatch(matches.get(currentMatchPosition));
                infoLabel.setText("<html><center>Всего найдено: " + matches.size() + "<br>" + currentMatchPosition + " из " + matches.size() + "</center></html>");
            }
            else
                infoLabel.setText("Совпадений не найдено");
        }
    }

    public void goForward(JLabel infoLabel) {
        if (matches.isEmpty())
            return;

        if (currentMatchPosition + 1 == matches.size())
            currentMatchPosition = 0;
        else
            currentMatchPosition += 1;

        infoLabel.setText("<html><center>Всего найдено: " + matches.size() + "<br>" + (currentMatchPosition + 1) + " из " + matches.size() + "</center></html>");
        goToMatch(matches.get(currentMatchPosition));
    }

    public void goBackward(JLabel infoLabel) {
        if (matches.isEmpty())
            return;

        if (currentMatchPosition - 1 == -1)
            currentMatchPosition = matches.size() - 1;
        else
            currentMatchPosition -= 1;

        infoLabel.setText("<html><center>Всего найдено: " + matches.size() + "<br>" + (currentMatchPosition + 1) + " из " + matches.size() + "</center></html>");
        goToMatch(matches.get(currentMatchPosition));
    }

    public void goToMatch(int position) {
        int row_highlight = position / (tableModel.getColumnCount() - 1);
        int row = position / (tableModel.getColumnCount() - 1);
        int col = position % (tableModel.getColumnCount() - 1) + 1;

        if (row > tableModel.getVisibleRowCount()) {
            if ((row + tableModel.getVisibleRowCount() < tableModel.getRowCount()))
                row += tableModel.getVisibleRowCount();
            else
                row += (tableModel.getRowCount() - row - 1);
        }

        Rectangle cellRect = table.getCellRect(row, col, true);
        table.scrollRectToVisible(cellRect);

        table.setRowSelectionInterval(row_highlight, row_highlight);
        table.setColumnSelectionInterval(col, col);
        table.requestFocus();
    }
}