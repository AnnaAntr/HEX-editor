package hex.editor.table;

import hex.editor.FileManager;

import javax.swing.table.AbstractTableModel;

public class MainTableModel extends AbstractTableModel {

    private int columnCount = 11;
    private final int visibleRowCount = 40;
    private long totalRowCount = visibleRowCount;

    private int firstVisibleRow = 0;
    private long lastVisibleRow = firstVisibleRow + visibleRowCount;

    private FileManager fileManager = null;


    public int calculateTotalRowCount() {
        if (fileManager != null) {
            long fileSize = fileManager.getFileSize();
            int bytesPerRow = columnCount - 1;

            return (int) (fileSize / bytesPerRow) + 1;
        }
        return this.visibleRowCount;
    }

    public void setTotalRowCount() {
        this.totalRowCount = calculateTotalRowCount();
    }

    public void setFirstVisibleRow(int firstVisibleRow) {
        this.firstVisibleRow = firstVisibleRow;
    }

    public void setLastVisibleRow(long lastVisibleRow) {
        this.lastVisibleRow = lastVisibleRow;
    }

    public void setColumnCount(int newColumnCount) {
        this.columnCount = newColumnCount + 1;
        setTotalRowCount();
        fireTableStructureChanged();
    }

    public int getVisibleRowCount() {
        return this.visibleRowCount;
    }

    public void setFileManager(String path) {
        if (path != null) {
            this.fileManager = new FileManager(path);
        }
        else {
            this.fileManager = null;
        }
        setTotalRowCount();
        fireTableStructureChanged();
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }

    @Override
    public int getRowCount() {
        return (int)totalRowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return rowIndex * (columnCount - 1);

        if (fileManager != null) {
            int position = rowIndex * (columnCount - 1) + columnIndex - 1;

            if (lastVisibleRow == -1)
                setLastVisibleRow(totalRowCount);

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

            int intValue = Integer.parseInt(aValue.toString(), 16);
            byte byteValue = (byte)intValue;
            fileManager.writeOneByte(position, byteValue);

            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}