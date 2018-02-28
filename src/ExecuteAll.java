import ui.LogUI;
import ui.dialog.SettingsDialog;

import javax.swing.*;

public class ExecuteAll {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //new LogUI();
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.setVisible(true);
        });
    }
}
