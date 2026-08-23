package hex.editor.view;

import hex.editor.table.MainTableModel;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static hex.editor.UserInterface.APP_NAME;

public class MenuBarManager {
    private final JFrame frame;
    private final JTable table;
    private final MainTableModel tableModel;
    private final DialogManager dialogManager;
    private final ValueDisplayManager valueDisplayManager;

    private final Clipboard clipboard;

    public MenuBarManager(JFrame frame, JTable table, MainTableModel tableModel, DialogManager dialogManager, ValueDisplayManager valueDisplayManager) {
        this.frame = frame;
        this.table = table;
        this.tableModel = tableModel;
        this.dialogManager = dialogManager;
        this.valueDisplayManager = valueDisplayManager;
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());

        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem open = new JMenuItem("Открыть");
        open.addActionListener(e -> openFile());
        fileMenu.add(open);

        JMenuItem saveAs = new JMenuItem("Сохранить как");
        saveAs.addActionListener(e -> saveFileAs());
        fileMenu.add(saveAs);

        JMenuItem close = new JMenuItem("Закрыть");
        close.addActionListener(e -> closeFile());
        fileMenu.add(close);

        return fileMenu;
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int userChoice = fileChooser.showDialog(null, "Открыть");

        if (userChoice == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\");

            tableModel.setFileManager(filePath);
            frame.setTitle(tableModel.getFileManager().getFileName());
        }
    }

    public void saveFileAs() {
        if (tableModel.getFileManager() == null) {
            JOptionPane.showMessageDialog(frame, "Нет открытого файла");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(tableModel.getFileManager().getFileName()));

        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                File sourceFile = new File(tableModel.getFileManager().getFilePath());
                File destFile = fileChooser.getSelectedFile();

                copyFile(sourceFile, destFile);

                tableModel.setFileManager(destFile.getAbsolutePath());
                frame.setTitle(tableModel.getFileManager().getFileName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Не удалось сохранить файл");
            }
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(source); FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }

    private void closeFile() {
        if (tableModel.getFileManager() != null) {
            tableModel.getFileManager().closeFile();
            tableModel.setFileManager(null);
            frame.setTitle(APP_NAME);
        }
    }

    private JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Редактирование");



        JMenuItem delete = new JMenuItem("Удалить");
        delete.addActionListener(e -> deleteAction());
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(delete);

        JMenuItem insert = new JMenuItem("Вставить");
        insert.addActionListener(e -> insertAction());
        insert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(insert);

        JMenuItem copy = new JMenuItem("Копировать");
        copy.addActionListener(e -> copyAction());
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(copy);

        JMenuItem cut = new JMenuItem("Вырезать");
        cut.addActionListener(e -> cutAction());
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(cut);

        JMenuItem paste = new JMenuItem("Вставить из буфера");
        paste.addActionListener(e -> pasteAction());
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        editMenu.add(paste);

        return editMenu;
    }

    private void deleteAction() {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        if (selectedRows.length == 1 && tableModel.getFileManager() != null) {
            int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
            int end = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[selectedColumns.length - 1] - 1;

            dialogManager.createDeleteDialog(start, end, 0);
        }
    }

    private void insertAction() {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        if (selectedColumns.length == 1 && selectedRows.length == 1 && tableModel.getFileManager() != null) {
            int position = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
            dialogManager.createInsertDialog(position);
        }
    }

    private void copyAction() {
        if (table.getSelectedRows().length == 1 && tableModel.getFileManager() != null) {
            String selectedValue = valueDisplayManager.getStringOfSelectedCells(true);

            StringSelection stringSelection = new StringSelection(selectedValue);
            clipboard.setContents(stringSelection, null);
        }
    }

    private void cutAction() {
        int[] selectedRows = table.getSelectedRows();
        int[] selectedColumns = table.getSelectedColumns();

        if (table.getSelectedRows().length == 1 && tableModel.getFileManager() != null) {
            String selectedValue = valueDisplayManager.getStringOfSelectedCells(true);

            StringSelection stringSelection = new StringSelection(selectedValue);
            clipboard.setContents(stringSelection, null);

            int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
            int end = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[selectedColumns.length - 1] - 1;

            dialogManager.createDeleteDialog(start, end, 1);
        }
    }

    private void pasteAction() {
        Transferable clipData = clipboard.getContents(this);

        try {
            if (clipData != null && clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String copiedData = (String) clipData.getTransferData(DataFlavor.stringFlavor);

                if (!copiedData.matches("^[A-F0-9\\s+]+$"))
                    return;

                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                if (selectedRows.length == 1 && tableModel.getFileManager() != null) {
                    int start = selectedRows[0] * (tableModel.getColumnCount() - 1) + selectedColumns[0] - 1;
                    dialogManager.createPasteDialog(start, copiedData);
                }
            }
        }
        catch (UnsupportedFlavorException | IOException ex) {
            JOptionPane.showMessageDialog(frame, "Не удалось прочитать данные из буфера");
        }
    }
}
