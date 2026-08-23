package hex.editor.view;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.math.BigInteger;

public class ValueDisplayManager {
    private final JFrame frame;
    private final JTable table;
    private final JTextField usIntField;
    private final JTextField sIntField;
    private final JTextField floatField;
    private final JTextField doubleField;

    public ValueDisplayManager(JFrame frame, JTable table) {
        this.frame = frame;
        this.table = table;

        this.usIntField = new JTextField(15);
        this.sIntField = new JTextField(15);
        this.floatField = new JTextField(15);
        this.doubleField = new JTextField(15);

        this.usIntField.setEditable(false);
        this.sIntField.setEditable(false);
        this.floatField.setEditable(false);
        this.doubleField.setEditable(false);

        Dimension fieldSize = new Dimension(200, 35);
        this.usIntField.setMaximumSize(fieldSize);
        this.sIntField.setMaximumSize(fieldSize);
        this.floatField.setMaximumSize(fieldSize);
        this.doubleField.setMaximumSize(fieldSize);
    }

    public JTextField getUsIntField() {
        return usIntField;
    }

    public JTextField getsIntField() {
        return sIntField;
    }

    public JTextField getFloatField() {
        return floatField;
    }

    public JTextField getDoubleField() {
        return doubleField;
    }

    public String getStringOfSelectedCells(boolean sep) {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        String selectedValue = "";

        for (int column : selectedColumns) {
            Object value = table.getValueAt(selectedRows[0], column);

            if (value != null) {
                selectedValue += value;

                if (sep)
                    selectedValue += " ";
            }
        }

        return selectedValue;
    }

    public void showValueOfSelectedCells() {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        if (selectedRows.length == 1 && (selectedColumns.length == 2 || selectedColumns.length == 4 || selectedColumns.length == 8)) {
            String selectedValue = getStringOfSelectedCells(false);

            Object usInt = 0, sInt = 0;

            float f = 0;
            double d = .0;

            try {
                f = Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16));
                d = Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue());

                switch (selectedColumns.length) {
                    case 2:
                        usInt = Integer.parseInt(selectedValue, 16);
                        sInt = (short) Integer.parseInt(selectedValue, 16);
                        break;

                    case 4:
                        usInt = Long.parseLong(selectedValue, 16);
                        sInt = (int) Long.parseLong(selectedValue, 16);
                        break;

                    case 8:
                        usInt = new BigInteger(selectedValue, 16);
                        sInt = new BigInteger(selectedValue, 16).longValue();
                        break;
                }
            }
            catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Не удалось интерпретировать значение выделенного блока");
            }

            usIntField.setText(String.valueOf(usInt));
            sIntField.setText(String.valueOf(sInt));
            floatField.setText(String.valueOf(f));
            doubleField.setText(String.valueOf(d));
        }
        else {
            usIntField.setText("");
            sIntField.setText("");
            floatField.setText("");
            doubleField.setText("");
        }
    }
}
