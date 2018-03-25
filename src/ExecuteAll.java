import ui.frame.LogUI;
import ui.dialog.SettingsDialog;
import utils.Constants;

import javax.swing.*;

public class ExecuteAll {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            if (Constants.OUT_TO_UI)
                new LogUI();

            SettingsDialog settingsDialog = new SettingsDialog(true);
            settingsDialog.setVisible(true);
        });
    }
}
