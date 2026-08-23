package hex.editor.view;

import hex.editor.table.HighlightAndTipCellRenderer;
import hex.editor.table.MainTableModel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class LeftSideManager {
    private final JTable table;
    private final MainTableModel tableModel;
    private final TableColumnModel columnModel;
    private final ValueDisplayManager valueDisplayManager;

    public LeftSideManager(JTable table, MainTableModel tableModel, ValueDisplayManager valueDisplayManager) {
        this.table = table;
        this.tableModel = tableModel;
        this.columnModel = table.getColumnModel();
        this.valueDisplayManager = valueDisplayManager;
    }

    public JScrollPane createLeftSide() {
        table.setRowHeight(30);
        table.setGridColor(Color.GRAY);

        TableColumn col0 = columnModel.getColumn(0);
        col0.setMaxWidth(30);
        col0.setMinWidth(20);
        col0.setPreferredWidth(30);

        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        DefaultTableCellRenderer cellRenderer = new HighlightAndTipCellRenderer();
        cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, cellRenderer);

        columnModel.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting())
                    valueDisplayManager.showValueOfSelectedCells();
            }
        });

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                table.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        JViewport viewport = scrollPane.getViewport();

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
}
