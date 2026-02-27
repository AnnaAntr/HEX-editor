import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;

public class UserInterface {

    private static JTable table = new JTable(new MainTableModel());

    private static JTextField usIntField = new JTextField(15);
    private static JTextField sIntField = new JTextField(15);
    private static JTextField floatField = new JTextField(15);
    private static JTextField doubleField = new JTextField(15);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }


    public static void createGUI() {
        JFrame frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int frameWidth = (int) (screenSize.width * 0.6);
        int frameHeight = (int) (screenSize.height * 0.6);

        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);


        frame.setJMenuBar(createMenuBar());


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftSide(), createRightSide());
        splitPane.setDividerLocation((int) (frameWidth * 0.7));
        frame.add(splitPane);



    }

    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JFileChooser fileChooser = new JFileChooser();



        JMenu file = new JMenu("Файл");
        JMenuItem open = new JMenuItem("Открыть");
        file.add(open);

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int userChoice = fileChooser.showDialog(null, "Открыть");

                if (userChoice == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");

                    MainTableModel tableModel = (MainTableModel) table.getModel();
                    tableModel.setFileManager(filePath);
                }
            }
        });




        menuBar.add(file);

        return menuBar;
    }

    public static JScrollPane createLeftSide() {
        //JTable table = new JTable(new MyTableModel());
        table.setRowHeight(30);
        table.setGridColor(Color.GRAY);

        TableColumn col0 = table.getColumnModel().getColumn(0);
        //col0.setHeaderValue("");
        col0.setMaxWidth(30);
        col0.setMinWidth(20);
        col0.setPreferredWidth(30);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        ListSelectionModel selectionModel = table.getSelectionModel();



//        selectionModel.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
////                int[] selectedRows = table.getSelectedRows();
////                int[] selectedColumns = table.getSelectedColumns();
////                TableModel tableModel = table.getModel();
//                // TODO
//
//                printSelectedCells(table);
//            }
//        });

        table.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting())
                    getValueOfSelectedCells(table);
            }
        });


//        table.addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                int row = table.rowAtPoint(e.getPoint());
//                int column = table.columnAtPoint(e.getPoint());
//                Object cellValue = table.getValueAt(row, column);
//                //System.out.println(cellValue);
//
//                JToolTip cellTip = new JToolTip();
//
//            }
//        });

        table.getColumnModel().getColumn(0).setCellRenderer(new MainCellRenderer());



        return new JScrollPane(table);
    }


    // -----------------------------------------------------------------
    public static void getValueOfSelectedCells(JTable table) {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        if (selectedRows.length == 1 && (selectedColumns.length == 2 || selectedColumns.length == 4 || selectedColumns.length == 8)) {
            String selectedValue = "";
            for (int i = 0; i < selectedColumns.length; i++) {
                int row = selectedRows[0];
                int column = selectedColumns[i];
                Object value = table.getValueAt(row, column);

                //System.out.println("row: " + row + " | column: " + column + " | value: " + value);
                if (value != null) {
                    selectedValue += value;
                }
            }
            //System.out.println("full value: " + selectedValue);

            //selectedValue = "FFFFFFFFFFFFFFFB";


//            long l = Long.parseLong(selectedValue, 16);
//
//            Float f = Float.intBitsToFloat((int) l);
//            Double d = Double.longBitsToDouble(l);
//
//            usIntField.setText(String.valueOf(Long.parseUnsignedLong(selectedValue, 16)));
//            sIntField.setText(String.valueOf(Long.valueOf(selectedValue, 16).intValue()));
//            floatField.setText(String.valueOf(f));
//            doubleField.setText(String.valueOf(d));


            // 2 bytes ok
            selectedValue = "FFFB";
            System.out.println("us: " + Integer.parseInt(selectedValue, 16));
            System.out.println(" s: " + (short) Integer.parseInt(selectedValue, 16));
            System.out.println(" f: " + Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16)));
            System.out.println(" d: " + Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue()));

            // 4 bytes ok
            selectedValue = "AAAAFFFB";
            System.out.println("\nus: " + Long.parseLong(selectedValue, 16));
            System.out.println(" s: " + (int) Long.parseLong(selectedValue, 16));
            System.out.println(" f: " + Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16)));
            System.out.println(" d: " + Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue()));


            // 8 bytes ok
            selectedValue = "AAFFFFFAAFFFFFFB";
            System.out.println("\nus: " + new BigInteger(selectedValue, 16));
            System.out.println(" s: " + new BigInteger(selectedValue, 16).longValue());

            System.out.println(" f: " + Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16)));

            System.out.println(" d: " + Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue()));





        }

    }




    public static JPanel createRightSide() {
        JPanel rightPanel = new JPanel();
        //rightPanel.setLayout(new BorderLayout(BorderLayout.CENTER));

//        SpinnerModel spinnerModel = new SpinnerNumberModel(10, 2, 30, 1);
//        JSpinner spinnerColumns = new JSpinner(spinnerModel);
//
//        rightPanel.add(new JLabel("Количество столбцов:"));
//        rightPanel.add(spinnerColumns);

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(Box.createHorizontalGlue());

        SpinnerModel spinnerModel = new SpinnerNumberModel(table.getColumnCount() - 1, 2, 30, 1);
        JSpinner spinnerColumns = new JSpinner(spinnerModel);

        JPanel columnsPanel = new JPanel();
        columnsPanel.add(new JLabel("Количество столбцов"));
        columnsPanel.add(spinnerColumns);
        //columnsPanel.setBounds(10, 50, 50, 50);
        //columnsPanel.setBounds(BorderFactory.createEmptyBorder(50, 10, 10, 10));

        rightPanel.add(columnsPanel);

        spinnerColumns.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) throws NullPointerException {
                int newValue = (int) spinnerColumns.getModel().getValue();

                MainTableModel tableModel = (MainTableModel) table.getModel();
                tableModel.setColumnCount(newValue);
            }
        });


        // TODO здесь будет просмотр значения блоков данных

        JLabel usIntLabel = new JLabel("Unsigned Int");
        //usIntLabel.setHorizontalAlignment(JLabel.LEFT);
        JLabel sIntLabel = new JLabel("Signed Int");
        JLabel floatLabel = new JLabel("Float");
        JLabel doubleLabel = new JLabel("Double");





        rightPanel.add(usIntLabel);
        rightPanel.add(usIntField);
        usIntField.setMaximumSize(new Dimension(200, 35));
        //usIntField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usIntLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        rightPanel.add(sIntLabel);
        rightPanel.add(sIntField);
        sIntField.setMaximumSize(new Dimension(200, 35));
        //sIntField.setAlignmentX(Component.CENTER_ALIGNMENT);
        sIntLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        rightPanel.add(floatLabel);
        rightPanel.add(floatField);
        floatField.setMaximumSize(new Dimension(200, 35));
        //floatField.setAlignmentX(Component.CENTER_ALIGNMENT);
        floatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(doubleLabel);
        rightPanel.add(doubleField);
        doubleField.setMaximumSize(new Dimension(200, 35));
        //doubleField.setAlignmentX(Component.CENTER_ALIGNMENT);
        doubleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);



        Dimension minSize = new Dimension(5, 200);
        Dimension prefSize = new Dimension(5, 300);
        Dimension maxSize = new Dimension(Short.MAX_VALUE, 100);

        rightPanel.add(new Box.Filler(minSize, prefSize, maxSize));




        return rightPanel;
    }



}


class MainTableModel extends AbstractTableModel {
    private int rowCount = 40;
    private int columnCount = 11;

    private FileManager fileManager = null;

    public void setColumnCount(int newColumnCount) {
        this.columnCount = newColumnCount + 1;
        fireTableStructureChanged();
    }

    public void setFileManager(String path) {
        this.fileManager = new FileManager(path);
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
            return rowIndex * (getColumnCount() - 1);

        if (fileManager != null) {
            int position = rowIndex * (getColumnCount() - 1) + columnIndex - 1;

            if (position < fileManager.getSize()) {
                return fileManager.readOneByte(position);
            }
        }

        // TODO добавлять строки, если файл большой

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

class MainCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null)
            setToolTipText(String.valueOf(Integer.parseInt(value.toString(), 16)));

        return this;
    }
}