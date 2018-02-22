package ui;

import javax.swing.*;

public class MainUI extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Loading config");
            new MainUI();
        });
    }

   public MainUI() {
        super("");
        //Create and show the main UI block

        setTitle("LR18 File System");
        setLocationRelativeTo(null);
        setSize(1200, 800);
        setVisible(true);
        //TODO: enable this
        //ui.pack();
        setLocationRelativeTo(null);
        setVisible(true);

        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
    }


}