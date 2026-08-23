package hex.editor.view;

import hex.editor.table.MainTableModel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RightSideManager {
    private final JTable table;
    private final MainTableModel tableModel;
    private final SearchManager searchManager;
    private final ValueDisplayManager valueDisplayManager;

    public RightSideManager(JTable table, MainTableModel tableModel, ValueDisplayManager valueDisplayManager) {
        this.table = table;
        this.tableModel = tableModel;
        this.valueDisplayManager = valueDisplayManager;
        this.searchManager = new SearchManager(table, tableModel);
    }

    public JPanel createRightSide() {
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
                int newValue = (int) spinnerColumns.getModel().getValue();
                tableModel.setColumnCount(newValue);
            }
        });

        rightPanel.add(new Box.Filler(new Dimension(5, 50), new Dimension(5, 70), new Dimension(5, 100)));

        JLabel usIntLabel = new JLabel("Unsigned Int");
        JLabel sIntLabel = new JLabel("Signed Int");
        JLabel floatLabel = new JLabel("Float");
        JLabel doubleLabel = new JLabel("Double");

        rightPanel.add(usIntLabel);
        rightPanel.add(valueDisplayManager.getUsIntField());
        usIntLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(sIntLabel);
        rightPanel.add(valueDisplayManager.getsIntField());
        sIntLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(floatLabel);
        rightPanel.add(valueDisplayManager.getFloatField());
        floatLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(doubleLabel);
        rightPanel.add(valueDisplayManager.getDoubleField());
        doubleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel searchPanel = new JPanel();
        JLabel searchLabel = new JLabel("Строка для поиска:");
        JTextField searchField = new JTextField(15);
        JButton buttonSearch = new JButton("Искать");

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(buttonSearch);

        JPanel infoPanel = new JPanel();
        JLabel infoLabel = new JLabel("");
        infoPanel.add(infoLabel);

        JPanel buttonsPanel = new JPanel();
        JButton buttonForward = new JButton("Вперед");
        JButton buttonBackward = new JButton("Назад");
        buttonsPanel.add(buttonBackward);
        buttonsPanel.add(buttonForward);

        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String stringToSearch = searchField.getText();
                searchManager.search(stringToSearch, infoLabel);
            }
        });

        buttonForward.addActionListener(e -> searchManager.goForward(infoLabel));
        buttonBackward.addActionListener(e -> searchManager.goBackward(infoLabel));

        rightPanel.add(new Box.Filler(new Dimension(5, 10), new Dimension(5, 30), new Dimension(5, 80)));
        rightPanel.add(searchPanel);
        rightPanel.add(infoPanel);
        rightPanel.add(buttonsPanel);
        rightPanel.add(new Box.Filler(new Dimension(5, 5), new Dimension(5, 70), new Dimension(5, 80)));

        return rightPanel;
    }
}
