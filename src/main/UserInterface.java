import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class UserInterface {

    private static final JFrame frame = new JFrame("HEX-editor");
    private static final JTable table = new JTable(new MainTableModel());
    private static final MainTableModel tableModel = (MainTableModel) table.getModel();
    private static final TableColumnModel columnModel = table.getColumnModel();

    private static final JTextField usIntField = new JTextField(15);
    private static final JTextField sIntField = new JTextField(15);
    private static final JTextField floatField = new JTextField(15);
    private static final JTextField doubleField = new JTextField(15);

    private static final int frameHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.7);
    private static final int frameWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.7);

    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static List<Integer> matches;
    private static int currentMatchPosition;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createGUI();
            }
        });
    }

    public static void createGUI() {
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
        frame.setLocationRelativeTo(null);

        frame.setJMenuBar(createMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftSide(), createRightSide());
        splitPane.setDividerLocation((int) (frameWidth * 0.7));
        frame.add(splitPane);

        frame.setVisible(true);
    }


    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JFileChooser fileChooser = new JFileChooser();
        JMenu edit = new JMenu("Редактирование");

        // ---------------------------------------------------------------
        JMenu file = new JMenu("Файл");
        JMenuItem open = new JMenuItem("Открыть");
        file.add(open);

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

        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRows().length == 1 && tableModel.getFileManager() != null) {
                    String selectedValue = getStringOfSelectedCells(true);

                    StringSelection stringSelection = new StringSelection(selectedValue);
                    clipboard.setContents(stringSelection, null);
                }
            }
        });

        // ---------------------------------------------------------------
        JMenuItem cut = new JMenuItem("Вырезать");
        edit.add(cut);

        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        cut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                if (table.getSelectedRows().length == 1 && tableModel.getFileManager() != null) {
                    String selectedValue = getStringOfSelectedCells(true);

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
        JMenuItem paste = new JMenuItem("Вставить из буфера");
        edit.add(paste);

        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        paste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Transferable clipData = clipboard.getContents(this);

                try {
                    if (clipData != null && clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        // получаем данные из буфера в виде строки
                        String copiedData = (String) clipData.getTransferData(DataFlavor.stringFlavor);

                        if (!copiedData.matches("^[A-F0-9\\s+]+$"))
                            return;

                        int[] selectedRows = table.getSelectedRows();
                        int[] selectedColumns = table.getSelectedColumns();

                        if (selectedRows.length == 1 && tableModel.getFileManager() != null) {
                            int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
                            createPasteDialog(start, copiedData);
                        }
                    }
                }
                catch (UnsupportedFlavorException | IOException ex) {
                    JOptionPane.showMessageDialog(null, "Не удалось прочитать данные из буфера");
                }
            }
        });

        // ---------------------------------------------------------------

        menuBar.add(file);
        menuBar.add(edit);

        return menuBar;
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
                try {
                    if (shiftDeleteButton.isSelected())
                        tableModel.getFileManager().removeBytesWithShift(start, end);
                    else
                        tableModel.getFileManager().removeBytesWithZero(start, end);
                    tableModel.fireTableStructureChanged();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Не удалось удалить данные");
                }

                // закрываем диалоговое окно
                dialog.dispose();
            }
        });

        dialog.add(panelRadio, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setLocationRelativeTo(frame);
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
                try {
                    // получаем количество байт для вставки
                    int numberOfBytes = Integer.parseInt(bytesToInsertField.getText());
                    if (numberOfBytes < 1)
                        return;

                    tableModel.getFileManager().insertBytes(position, numberOfBytes);

                    tableModel.fireTableStructureChanged();
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Не удалось вставить байты");
                }

                // закрываем диалоговое окно
                dialog.dispose();
            }
        });

        dialog.add(panelTop, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setLocationRelativeTo(frame);
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

        JButton buttonPaste = new JButton("Вставить");
        JPanel panelBottom = new JPanel();
        panelBottom.add(buttonPaste);

        buttonPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (shiftPasteButton.isSelected())
                        tableModel.getFileManager().pasteBytesWithShift(start, copiedData);
                    else
                        tableModel.getFileManager().pasteBytesWithReplacement(start, copiedData);

                    tableModel.fireTableStructureChanged();
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Не удалось вставить данные из буфера");
                }

                // закрываем диалоговое окно
                dialog.dispose();
            }
        });

        dialog.add(panelRadio, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setLocationRelativeTo(frame);
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

                tableModel.setFirstVisibleRow(row1);
                tableModel.setLastVisibleRow(row2);
            }
        });

        return scrollPane;
    }


    public static String getStringOfSelectedCells(boolean sep) {
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


    public static void showValueOfSelectedCells() {
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
            catch (NumberFormatException e) {}

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


    public static JPanel createRightSide() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        SpinnerModel spinnerModel = new SpinnerNumberModel(table.getColumnCount() - 1, 2, 30, 1);
        JSpinner spinnerColumns = new JSpinner(spinnerModel);

        JPanel columnsPanel = new JPanel();
        columnsPanel.add(new JLabel("Количество столбцов"));
        columnsPanel.add(spinnerColumns);

        rightPanel.add(columnsPanel);

        spinnerColumns.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // получаем значение из спиннера
                int newValue = (int) spinnerColumns.getModel().getValue();

                tableModel.setColumnCount(newValue);
            }
        });

        Dimension minSize = new Dimension(5, 50);
        Dimension prefSize = new Dimension(5, 70);
        Dimension maxSize = new Dimension(5, 100);
        Dimension fieldSize = new Dimension(200, 35);

        rightPanel.add(new Box.Filler(minSize, prefSize, maxSize));

        // ---------------------------------------------------------------
        JLabel usIntLabel = new JLabel("Unsigned Int");
        JLabel sIntLabel = new JLabel("Signed Int");
        JLabel floatLabel = new JLabel("Float");
        JLabel doubleLabel = new JLabel("Double");

        rightPanel.add(usIntLabel);
        rightPanel.add(usIntField);
        usIntField.setMaximumSize(fieldSize);
        usIntLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(sIntLabel);
        rightPanel.add(sIntField);
        sIntField.setMaximumSize(fieldSize);
        sIntLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(floatLabel);
        rightPanel.add(floatField);
        floatField.setMaximumSize(fieldSize);
        floatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(doubleLabel);
        rightPanel.add(doubleField);
        doubleField.setMaximumSize(fieldSize);
        doubleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---------------------------------------------------------------
        JPanel searchPanel = new JPanel();
        JLabel searchLabel = new JLabel("Строка для поиска:");

        JTextField searchField = new JTextField(15);
        JButton buttonSearch = new JButton("Искать");
        JButton buttonForward = new JButton("Вперед");
        JButton buttonBackward = new JButton("Назад");

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(buttonSearch);

        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("");
        infoPanel.add(infoLabel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(buttonBackward);
        buttonsPanel.add(buttonForward);

        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String stringToSearch = searchField.getText();

                if (!stringToSearch.isEmpty() && tableModel.getFileManager() != null) {
                    // проверяем, что строка состоит из допустимых символов
                    // ? = 1 любой символ
                    if (!stringToSearch.matches("^[A-F0-9?\\s+]+$"))
                        return;

                    String[] pattern = stringToSearch.split("\\s+");

                    matches = tableModel.getFileManager().findByValue(pattern);

                    if (!matches.isEmpty()) {
                        // переходим к первому найденному совпадению
                        currentMatchPosition = 0;
                        goToMatch(matches.get(currentMatchPosition));

                        infoLabel.setText("<html><center>Всего найдено: " + matches.size() + "<br>" + currentMatchPosition + " из " + matches.size() + "</center></html>");
                    }
                    else
                        infoLabel.setText("Совпадений не найдено");
                }
            }
        });

        buttonForward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (matches.isEmpty())
                    return;

                // если текущее найденное совпадение последнее, то переходим к первому
                if (currentMatchPosition + 1 == matches.size())
                    currentMatchPosition = 0;
                // иначе к следующему
                else
                    currentMatchPosition += 1;

                infoLabel.setText("<html><center>Всего найдено: " + matches.size() + "<br>" + (currentMatchPosition + 1) + " из " + matches.size() + "</center></html>");

                goToMatch(matches.get(currentMatchPosition));
            }
        });

        buttonBackward.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (matches.isEmpty())
                    return;

                // если текущее найденное совпадение первое, то переходим к последнему
                if (currentMatchPosition - 1 == -1)
                    currentMatchPosition = matches.size() - 1;
                // иначе к предыдущему
                else
                    currentMatchPosition -= 1;

                infoLabel.setText("<html><center>Всего найдено: " + matches.size() + "<br>" + (currentMatchPosition + 1) + " из " + matches.size() + "</center></html>");

                goToMatch(matches.get(currentMatchPosition));
            }
        });

        // ---------------------------------------------------------------
        rightPanel.add(new Box.Filler(new Dimension(5, 30), new Dimension(5, 50), new Dimension(5, 100)));

        rightPanel.add(searchPanel);
        rightPanel.add(infoPanel);
        rightPanel.add(buttonsPanel);

        rightPanel.add(new Box.Filler(new Dimension(5, 5), new Dimension(5, 100), new Dimension(5, 100)));

        return rightPanel;
    }


    public static void goToMatch(int position) {
        int row_highlight = position / (tableModel.getColumnCount() - 1);
        int row = position / (tableModel.getColumnCount() - 1);
        int col = position % (tableModel.getColumnCount() - 1) + 1;

        if (row > tableModel.getVisibleRowCount()) {
            if ((row + tableModel.getVisibleRowCount() < tableModel.getRowCount()))
                row += tableModel.getVisibleRowCount();
            else
                row += (tableModel.getRowCount() - row - 1);
        }

        // переходим к найденному значению
        Rectangle cellRect = table.getCellRect(row, col, true);
        table.scrollRectToVisible(cellRect);

        table.setRowSelectionInterval(row_highlight, row_highlight);
        table.setColumnSelectionInterval(col, col);
        table.requestFocus();
    }

}