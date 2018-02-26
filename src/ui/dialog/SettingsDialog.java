package ui.dialog;

import utils.Constants;
import utils.PropertiesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class SettingsDialog extends JDialog {

    private String ip;

    public SettingsDialog() {

        //prevent closing the dialog
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showErrorMessage();
                System.exit(1);
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        //padding
        cs.insets = new Insets(8, 15, 8, 15);
        cs.fill = GridBagConstraints.HORIZONTAL;

        //ip
        JLabel ipLabel = new JLabel("Host IP: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(ipLabel, cs);

        JTextField ipTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(ipTextField, cs);

        //connect button
        JButton connectBtn = new JButton("Connect to FS");
        connectBtn.addActionListener((ActionListener) -> {
            this.ip = ipTextField.getText();
            if (ip.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected ip = " + ip);
                PropertiesHelper.getInstance().writeConfig(Constants.IP_HOST_CONFIG, ip);
                //load connect dialog
                dispose();
                ConnectDialog connectDialog = new ConnectDialog();
                connectDialog.setVisible(true);
            }
        });

        //create button
        JButton createBtn = new JButton("Create a FS");
        createBtn.addActionListener((ActionListener) -> {
            this.ip = ipTextField.getText();
            if (ip.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected ip = " + ip);
                PropertiesHelper.getInstance().writeConfig(Constants.IP_HOST_CONFIG, ip);
                //load create dialog
                dispose();
                CreateDialog createDialog = new CreateDialog();
                createDialog.setVisible(true);
            }
        });

        JPanel bp = new JPanel();
        bp.add(connectBtn);
        bp.add(createBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        ipTextField.setText(PropertiesHelper.getInstance().loadConfig(Constants.IP_HOST_CONFIG));
    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}