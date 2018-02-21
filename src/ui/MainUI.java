package ui;

import javax.swing.*;

public class MainUI extends JFrame {

    public static void main(String[] args) {

        //Create and show the main UI block
        MainUI ui = new MainUI();
        ui.setTitle("LR18 File System");
        ui.setLocationRelativeTo(null);
        ui.setSize(1200, 800);
        ui.setVisible(true);
        //TODO: enable this
        //ui.pack();
        ui.setLocationRelativeTo(null);
        ui.setVisible(true);

        SettingsDialog settingsDialog = new SettingsDialog(ui);
        settingsDialog.setVisible(true);
    }


}