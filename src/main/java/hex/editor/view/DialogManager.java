package hex.editor.view;

import hex.editor.table.MainTableModel;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class DialogManager {
    private final JFrame frame;
    private final int frameWidth;
    private final int frameHeight;
    private final MainTableModel tableModel;

    public DialogManager(JFrame parent, int frameWidth, int frameHeight, MainTableModel tableModel) {
        this.frame = parent;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.tableModel = tableModel;
    }

    public void createDeleteDialog(int start, int end, int cut) {
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
                    JOptionPane.showMessageDialog(frame, "Не удалось удалить данные");
                }

                dialog.dispose();
            }
        });

        dialog.add(panelRadio, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    public void createInsertDialog(int position) {
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
                    int numberOfBytes = Integer.parseInt(bytesToInsertField.getText());
                    if (numberOfBytes < 1)
                        return;

                    tableModel.getFileManager().insertBytes(position, numberOfBytes);

                    tableModel.fireTableStructureChanged();
                }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Не удалось вставить байты");
                }

                dialog.dispose();
            }
        });

        dialog.add(panelTop, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    public void createPasteDialog(int start, String copiedData) {
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
                    JOptionPane.showMessageDialog(frame, "Не удалось вставить данные из буфера");
                }

                dialog.dispose();
            }
        });

        dialog.add(panelRadio, BorderLayout.CENTER);
        dialog.add(panelBottom, BorderLayout.AFTER_LAST_LINE);

        dialog.setSize((int) (frameWidth * 0.4), (int) (frameHeight * 0.25));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}