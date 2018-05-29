import ui.dialog.SettingsDialog;
import ui.frame.LogUI;
import utils.Constants;

import javax.swing.*;

public class ExecuteAll {

    /**
     * This class is used in order to launch the system , this method loads the Graphical Interface
     *
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
