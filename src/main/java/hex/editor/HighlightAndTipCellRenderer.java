package hex.editor;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Color;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

class HighlightAndTipCellRenderer extends DefaultTableCellRenderer {
    private int hoverRow = -1;
    private int hoverCol = -1;

    private static final Logger logger = Logger.getLogger(HighlightAndTipCellRenderer.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("logs/cell_renderer.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Не удалось создать лог-файл: " + e.getMessage());
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // если курсор наведен на ячейку, то подсвечиваем ее
        if (row == hoverRow && column == hoverCol && !isSelected && column != 0)
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
                logger.log(Level.WARNING,"Нулевой указатель мыши" , e);
            }
        }

        if (column != 0) {
            if (value != null)    {
                try {
                    int intValue = Integer.parseInt(value.toString(), 16);
                    byte b = (byte) intValue;
                    String tip = String.valueOf(b) + " \n"         // signed
                                                   + (b & 0xFF);   // unsigned
                    setToolTipText(tip);
                }
                catch (NumberFormatException e) {
                    logger.log(Level.WARNING,"Ошибка при создании подсказки" , e);
                }
            }
        }

        return c;
    }
}