import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class UserInterface {

    private static JFrame frame = new JFrame("HEX-editor");
    private static JTable table = new JTable(new MainTableModel());
    private static MainTableModel tableModel = (MainTableModel) table.getModel();
    private static TableColumnModel columnModel = table.getColumnModel();

    private static JTextField usIntField = new JTextField(15);
    private static JTextField sIntField = new JTextField(15);
    private static JTextField floatField = new JTextField(15);
    private static JTextField doubleField = new JTextField(15);

    private static int frameHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.7);
    private static int frameWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.7);

    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }

    public static void createGUI() {
        //JFrame frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // при закрытии окна закрываем файл
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tableModel.getFileManager() != null)
                    tableModel.getFileManager().closeFile();
            }
        });

        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);

        frame.setJMenuBar(createMenuBar());
        table.setComponentPopupMenu(createPopupMenu());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftSide(), createRightSide());
        splitPane.setDividerLocation((int) (frameWidth * 0.7));
        frame.add(splitPane);
    }

    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JFileChooser fileChooser = new JFileChooser();

        // ---------------------------------------------------------------
        JMenu file = new JMenu("Файл");
        JMenuItem open = new JMenuItem("Открыть");
        file.add(open);

        // TODO replace dialogs
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int userChoice = fileChooser.showDialog(null, "Открыть");

                if (userChoice == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");

                    tableModel.setFileManager(filePath);
                }
            }
        });

        // ---------------------------------------------------------------
        JMenu edit = new JMenu("Редактирование");
        JMenuItem delete = new JMenuItem("Удалить");
        edit.add(delete);

        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                if (selectedRows.length == 1 && tableModel.getFileManager() != null) {
                    int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
                    int end = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[selectedColumns.length - 1] - 1;

                    createDeleteDialog(start, end, 0);
                }
            }
        });

        // ---------------------------------------------------------------
        JMenuItem insert = new JMenuItem("Вставить");
        edit.add(insert);

        insert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        insert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                // реагируем только если выделена 1 ячейка
                if (selectedColumns.length == 1 && selectedRows.length == 1 && tableModel.getFileManager() != null) {
                    int position = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
                    createInsertDialog(position);
                }
            }
        });

        // ---------------------------------------------------------------
        JMenuItem copy = new JMenuItem("Копировать");
        edit.add(copy);

        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRows().length == 1 && tableModel.getFileManager() != null) {
                    String selectedValue = getStringOfSelectedCells();

                    StringSelection stringSelection = new StringSelection(selectedValue);
                    clipboard.setContents(stringSelection, null);
                }
            }
        });

        // ---------------------------------------------------------------
        JMenuItem cut = new JMenuItem("Вырезать");
        edit.add(cut);

        // TODO shortcut
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                if (table.getSelectedRows().length == 1 && tableModel.getFileManager() != null) {
                    String selectedValue = getStringOfSelectedCells();

                    StringSelection stringSelection = new StringSelection(selectedValue);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);

                    // delete
                    int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
                    int end = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[selectedColumns.length - 1] - 1;

                    createDeleteDialog(start, end, 1);
                }
            }
        });

        // ---------------------------------------------------------------
        JMenuItem paste = new JMenuItem("Вставить");
        edit.add(paste);

        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Transferable clipData = clipboard.getContents(this);

                try {
                    if (clipData != null && clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String copiedData = (String) clipData.getTransferData(DataFlavor.stringFlavor);

//                        // парсим строку
//                        String[] bytes = copiedData.split(" ");

                        // create dialog
                        int[] selectedRows = table.getSelectedRows();
                        int[] selectedColumns = table.getSelectedColumns();

                        if (selectedRows.length == 1 && tableModel.getFileManager() != null) {
                            int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
                            //int end = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[selectedColumns.length - 1] - 1;

                            createPasteDialog(start, copiedData);
                        }




                    }
                }
                catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });





        menuBar.add(file);
        menuBar.add(edit);

        return menuBar;
    }





    public static JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem popupDelete = new JMenuItem("Удалить");
        JMenuItem popupInsert = new JMenuItem("Вставить");

        popupMenu.add(popupDelete);
        popupMenu.add(popupInsert);


        return popupMenu;
    }


    public static void createDeleteDialog(int start, int end, int cut) {
        String title = "Удаление байт";
        if (cut == 1)
            title = "Вырезка байт";

        JDialog dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton shiftDeleteButton = new JRadioButton("Со сдвигом", true);
        JRadioButton zeroDeleteButton = new JRadioButton("С обнулением");
        buttonGroup.add(shiftDeleteButton);
        buttonGroup.add(zeroDeleteButton);

        JPanel panelRadio = new JPanel();
        panelRadio.add(shiftDeleteButton);
        panelRadio.add(zeroDeleteButton);

        String btn = "Удалить";
        if (cut == 1)
            btn = "Вырезать";

        JButton buttonDelete = new JButton(btn);
        JPanel panelBottom = new JPanel();
        panelBottom.add(buttonDelete);

        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shiftDeleteButton.isSelected())
                    tableModel.getFileManager().removeBytesWithShift(start, end);
                else
                    tableModel.getFileManager().removeBytesWithZero(start, end);
                tableModel.fireTableStructureChanged();

                // закрываем диалоговое окно
                dialog.dispose();
            }
        });

        dialog.add(panelRadio, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setVisible(true);
    }


    public static void createInsertDialog(int position) {
        JDialog dialog = new JDialog(frame, "Вставка байт", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        JLabel labelInsert = new JLabel("Введите количество байт для вставки:");
        JTextField bytesToInsertField = new JTextField(5);
        JPanel panelTop = new JPanel();
        panelTop.add(labelInsert);
        panelTop.add(bytesToInsertField);

        JButton buttonInsert = new JButton("Вставить");
        JPanel panelBottom = new JPanel();
        panelBottom.add(buttonInsert);

        buttonInsert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // получаем количество байт для вставки
                int numberOfBytes = Integer.parseInt(bytesToInsertField.getText());
                tableModel.getFileManager().insertBytes(position, numberOfBytes);
                tableModel.fireTableStructureChanged();
                // закрываем диалоговое окно
                dialog.dispose();
            }
        });

        dialog.add(panelTop, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setVisible(true);
    }


    public static void createPasteDialog(int start, String copiedData) {
        JDialog dialog = new JDialog(frame, "Вставка из буфера", true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton shiftPasteButton = new JRadioButton("Со сдвигом", true);
        JRadioButton replacementPasteButton = new JRadioButton("С заменой");
        buttonGroup.add(shiftPasteButton);
        buttonGroup.add(replacementPasteButton);

        JPanel panelRadio = new JPanel();
        panelRadio.add(shiftPasteButton);
        panelRadio.add(replacementPasteButton);

        JButton buttonDelete = new JButton("Вставить");
        JPanel panelBottom = new JPanel();
        panelBottom.add(buttonDelete);

        buttonDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shiftPasteButton.isSelected())
                    //tableModel.getFileManager().replaceBytes(start, copiedData);
                    System.out.println("shift");
                else
                    tableModel.getFileManager().replaceBytes(start, copiedData);

                tableModel.fireTableStructureChanged();

                // закрываем диалоговое окно
                dialog.dispose();
            }
        });

        dialog.add(panelRadio, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setVisible(true);
    }


    public static JScrollPane createLeftSide() {
        table.setRowHeight(30);
        table.setGridColor(Color.GRAY);

        TableColumn col0 = columnModel.getColumn(0);
        col0.setMaxWidth(30);
        col0.setMinWidth(20);
        col0.setPreferredWidth(30);

        // режим выделения
        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        // кастомный рендерер ячеек
        DefaultTableCellRenderer cellRenderer = new HighlightAndTipCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, cellRenderer);

        // слушатель для выделенных ячеек
        columnModel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting())
                    showValueOfSelectedCells();
            }
        });

        // слушатель для подсветки ячейки под курсором
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                table.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        JViewport viewport = scrollPane.getViewport();

        // при прокрутке получаем и устанавливаем первый и последний видимый ряд
        viewport.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Point p = viewport.getViewPosition();
                int row1 = table.rowAtPoint(p);
                int row2 = table.rowAtPoint(new Point(p.x, p.y + viewport.getHeight()));
                //System.out.println(p + " " + row1 + " | " + row2);

                tableModel.setFirstVisibleRow(row1);
                tableModel.setLastVisibleRow(row2);
            }
        });

        return scrollPane;
    }


    public static String getStringOfSelectedCells() {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        String selectedValue = "";
        for (int i = 0; i < selectedColumns.length; i++) {
            int row = selectedRows[0];
            int column = selectedColumns[i];
            Object value = table.getValueAt(row, column);

            if (value != null) {
                selectedValue += value;
                selectedValue += " ";
            }
        }

        return selectedValue;
    }


    public static void showValueOfSelectedCells() {
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


//            long l = Long.parseLong(selectedValue, 16);
//
//            Float f = Float.intBitsToFloat((int) l);
//            Double d = Double.longBitsToDouble(l);
//
//            usIntField.setText(String.valueOf(Long.parseUnsignedLong(selectedValue, 16)));
//            sIntField.setText(String.valueOf(Long.valueOf(selectedValue, 16).intValue()));
//            floatField.setText(String.valueOf(f));
//            doubleField.setText(String.valueOf(d));

            Object usInt = 0, sInt = 0;
            float f = 0;
            double d = .0;

            // TODO fix ???
            try {
                d = Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue());
                f = Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16));

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
                System.out.println("Number Format Exception");
                // TODO handle exception
            }

            usIntField.setText(String.valueOf(usInt));
            sIntField.setText(String.valueOf(sInt));
            floatField.setText(String.valueOf(f));
            doubleField.setText(String.valueOf(d));


            // 2 bytes
//            selectedValue = "FFFB";
//            System.out.println("us: " + Integer.parseInt(selectedValue, 16));
//            System.out.println(" s: " + (short) Integer.parseInt(selectedValue, 16));
//            System.out.println(" f: " + Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16)));
//            System.out.println(" d: " + Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue()));
//
//            // 4 bytes
//            selectedValue = "AAAAFFFB";
//            System.out.println("\nus: " + Long.parseLong(selectedValue, 16));
//            System.out.println(" s: " + (int) Long.parseLong(selectedValue, 16));
//            System.out.println(" f: " + Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16)));
//            System.out.println(" d: " + Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue()));
//
//
//            // 8 bytes
//            selectedValue = "AAFFFFFAAFFFFFFB";
//            System.out.println("\nus: " + new BigInteger(selectedValue, 16));
//            System.out.println(" s: " + new BigInteger(selectedValue, 16).longValue());
//
//            System.out.println(" f: " + Float.intBitsToFloat((int) Long.parseLong(selectedValue, 16)));
//
//            System.out.println(" d: " + Double.longBitsToDouble(new BigInteger(selectedValue, 16).longValue()));

        }
        else {
            usIntField.setText("");
            sIntField.setText("");
            floatField.setText("");
            doubleField.setText("");
        }
    }


    public static JPanel createRightSide() {
        JPanel rightPanel = new JPanel();

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

                tableModel.setColumnCount(newValue);
            }
        });

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

// -------------------------------------------------------------------------------------------

class MainTableModel extends AbstractTableModel {

    private int columnCount = 11;
    private final int visibleRowCount = 40;
    private int totalRowCount = visibleRowCount;

    private int firstVisibleRow = 0;
    private int lastVisibleRow = firstVisibleRow + visibleRowCount;

    private FileManager fileManager = null;


    private int calculateTotalRowCount() {
        if (fileManager != null) {
            long fileSize = fileManager.getFileSize();
            int bytesPerRow = columnCount - 1;

            return (int) (fileSize / bytesPerRow) + 1;
        }
        return 0;
    }

    public void setTotalRowCount() {
        this.totalRowCount = calculateTotalRowCount();
    }

    public void setFirstVisibleRow(int firstVisibleRow) {
        this.firstVisibleRow = firstVisibleRow;
    }

    public void setLastVisibleRow(int lastVisibleRow) {
        this.lastVisibleRow = lastVisibleRow;
    }

    public void setColumnCount(int newColumnCount) {
        this.columnCount = newColumnCount + 1;
        setTotalRowCount();
        fireTableStructureChanged();
    }

    public void setFileManager(String path) {
        this.fileManager = new FileManager(path);
        setTotalRowCount();
        fireTableStructureChanged();
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    @Override
    public int getRowCount() {
        return totalRowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // номер строки
        if (columnIndex == 0)
            return rowIndex * (columnCount - 1);


        // если открыт файл
        if (fileManager != null) {
            // long fileSize = fileManager.getFileSize();
            int position = rowIndex * (columnCount - 1) + columnIndex - 1;

            // если последний видимый ряд = -1, то он самый последний в таблице
            if (lastVisibleRow == -1)
                setLastVisibleRow(totalRowCount);

            // если текущий row попадает в [firstVisible - n; lastVisible + n], то читаем из файла
            if (rowIndex >= (firstVisibleRow - visibleRowCount / 2) && rowIndex <= (lastVisibleRow + visibleRowCount / 2)) {
                if (position < fileManager.getFileSize()) {
                    return fileManager.readOneByte(position);
                }
            }
        }

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

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (fileManager != null) {
            int position = rowIndex * (columnCount - 1) + columnIndex - 1;
            fileManager.writeOneByte(position, Byte.parseByte(aValue.toString(), 16));
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}


class HighlightAndTipCellRenderer extends DefaultTableCellRenderer {
    private int hoverRow = -1;
    private int hoverCol = -1;

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // если курсор наведен на ячейку, то подсвечиваем ее
        if (row == hoverRow && column == hoverCol && !isSelected)
            c.setBackground(new Color(156, 197, 255));
        // если ячейка выделена, то ей назначается цвет выделения
        else if (isSelected)
            c.setBackground(table.getSelectionBackground());
        else
            c.setBackground(table.getBackground());

        // обновление позиции мыши
        if (table.getMousePosition() == null) {
            this.hoverRow = -1;
            this.hoverCol = -1;
        }
        else {
            try {
                this.hoverRow = table.rowAtPoint(table.getMousePosition());
                this.hoverCol = table.columnAtPoint(table.getMousePosition());
            }
            catch (NullPointerException e) {
                //System.out.println("null point");
            }
        }

        // TODO ??? сломалос...
        if (value != null) {
            try {
                int intValue = Integer.parseInt(value.toString(), 16);
                byte b = (byte) intValue;
                String tip = String.valueOf(b) + " \n"  // signed
                                        + (b & 0xFF);   // unsigned
                setToolTipText(tip);
            }
            catch (NumberFormatException e) {
                System.out.println("Number format exception from tool tip");
            }
        }

        return c;
    }
}