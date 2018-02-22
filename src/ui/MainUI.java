package ui;

import javax.swing.*;

public class MainUI extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainUI mainUI = new MainUI();
        });
    }

    public MainUI() {
        super("LR18 File System");
        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(1000, 700);
        setVisible(true);
        setLocationRelativeTo(null);
        setVisible(true);

        new LogUI(this);
        System.out.println("Loading config");

        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
    }
}