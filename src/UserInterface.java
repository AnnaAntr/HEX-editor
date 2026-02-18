import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class UserInterface {

    private static JTable table = new JTable(new MainTableModel());

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }


    public static void createGUI() {
        JFrame frame = new JFrame("Frame");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(screenSize.width - 800, screenSize.height - 500);
        frame.setVisible(true);


        frame.setJMenuBar(createMenuBar());


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftSide(), createRightSide());
        frame.add(splitPane);



    }

    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();


        JMenu file = new JMenu("Файл");
        JMenuItem open = new JMenuItem("Открыть");
        file.add(open);

//        open.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//            }
//        });

        // adding
        menuBar.add(file);

        return menuBar;
    }

    public static JScrollPane createLeftSide() {
        //JTable table = new JTable(new MyTableModel());
        table.setRowHeight(30);
        table.setGridColor(Color.GRAY);

        TableColumn col0 = table.getColumnModel().getColumn(0);
        //col0.setHeaderValue("");
        col0.setMaxWidth(50);
        col0.setMinWidth(20);
        col0.setPreferredWidth(40);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        //table.setColumnSelectionInterval(1, table.getColumnCount() - 1);


        return new JScrollPane(table);
    }

    public static JPanel createRightSide() {
        JPanel rightPanel = new JPanel();
        //rightPanel.setLayout(new BorderLayout(BorderLayout.CENTER));

        SpinnerModel spinnerModel = new SpinnerNumberModel(10, 2, 30, 1);
        JSpinner spinnerColumns = new JSpinner(spinnerModel);

        rightPanel.add(new JLabel("Количество столбцов:"));
        rightPanel.add(spinnerColumns);

        spinnerColumns.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) throws NullPointerException {
                int newValue = (int) spinnerColumns.getModel().getValue();

                MainTableModel tableModel = (MainTableModel) table.getModel();
                tableModel.setColumnCount(newValue);


                // TODO перерисовать таблицу (данные) ???
            }
        });


        // TODO здесь будет просмотр значения блоков данных

        JTextField usIntField = new JTextField(15);
        JTextField sIntField = new JTextField(15);
        JTextField floatField = new JTextField(15);
        JTextField doubleField = new JTextField(15);

        rightPanel.add(usIntField);
        rightPanel.add(sIntField);
        rightPanel.add(floatField);
        rightPanel.add(doubleField);




        return rightPanel;
    }



}


class MainTableModel extends AbstractTableModel {
    private int rowCount = 40;
    private int columnCount = 10;

    public void setColumnCount(int newColumnCount) {
        this.columnCount = newColumnCount;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return rowIndex;
        return null;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0)
            return "";
        return Integer.toString(column - 1);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    //    @Override
//    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//
//    }
}

//class FirstColumnModel extends AbstractTableModel {
//
//    @Override
//    public int getRowCount() {
//        return 40;
//    }
//
//    @Override
//    public int getColumnCount() {
//        return 1;
//    }
//
//    @Override
//    public Object getValueAt(int rowIndex, int columnIndex) {
//        return rowIndex;
//    }
//}
