package hex.editor;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        UserInterface ui = new UserInterface();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ui.createGUI();
            }
        });

    }
}
