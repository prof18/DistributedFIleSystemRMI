import ui.frame.LogUI;
import ui.dialog.SettingsDialog;
import utils.Constants;

import javax.swing.*;

public class ExecuteAll {

    /**
     * This class is used in order to launch the system , this method loads the Graphical Interface
     *
     * @param args
     */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            if (Constants.OUT_TO_UI)
                new LogUI();

            SettingsDialog settingsDialog = new SettingsDialog(true);
            settingsDialog.setVisible(true);
        });
    }
}
