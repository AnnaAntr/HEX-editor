package hex.editor;

import hex.editor.table.MainTableModel;
import hex.editor.view.MenuBarManager;
import hex.editor.view.DialogManager;
import hex.editor.view.LeftSideManager;
import hex.editor.view.RightSideManager;
import hex.editor.view.ValueDisplayManager;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JSplitPane;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserInterface {
    public static final String APP_NAME = "HEX-editor";
    private final JFrame frame;
    private final JTable table;
    private final MainTableModel tableModel;
    private final int frameHeight;
    private final int frameWidth;

    private final MenuBarManager menuBarManager;
    private final DialogManager dialogManager;
    private final LeftSideManager leftSideManager;
    private final RightSideManager rightSideManager;
    private final ValueDisplayManager valueDisplayManager;

    public UserInterface() {
        this.frame = new JFrame(APP_NAME);
        this.tableModel = new MainTableModel();
        this.table = new JTable(tableModel);
        this.frameHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.7);
        this.frameWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.7);

        this.valueDisplayManager = new ValueDisplayManager(frame, table);
        this.dialogManager = new DialogManager(frame, frameWidth, frameHeight, tableModel);
        this.menuBarManager = new MenuBarManager(frame, table, tableModel, dialogManager, valueDisplayManager);
        this.leftSideManager = new LeftSideManager(table, tableModel, valueDisplayManager);
        this.rightSideManager = new RightSideManager(table, tableModel, valueDisplayManager);
    }

    public void createGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tableModel.getFileManager() != null)
                    tableModel.getFileManager().closeFile();
            }
        });

        frame.setSize(frameWidth, frameHeight);
        frame.setLocationRelativeTo(null);
        frame.setJMenuBar(menuBarManager.createMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSideManager.createLeftSide(), rightSideManager.createRightSide());
        splitPane.setDividerLocation((int) (frameWidth * 0.7));
        frame.add(splitPane);

        frame.setVisible(true);
    }
}