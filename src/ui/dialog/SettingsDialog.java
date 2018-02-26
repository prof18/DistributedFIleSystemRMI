package ui.dialog;

import utility.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class SettingsDialog extends JDialog {

    private String ip;

    private File configFile = new File("config.properties");
    private Properties props;

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
                  try {
                    //save the configurations
                    saveConfig();
                } catch (IOException e) {
                    System.out.println("Unable to save properties file");
                    e.printStackTrace();
                }
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
                try {
                    //save the configurations
                    saveConfig();
                } catch (IOException e) {
                    System.out.println("Unable to save properties file");
                    e.printStackTrace();
                }
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

        //load config
        try {
            loadConfig();
            ipTextField.setText(props.getProperty(Constants.IP_HOST_CONFIG));
            } catch (IOException e) {
            System.out.println("Properties doesn't exits.");
            System.out.println("Loading settings dialog");
        }
    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void saveConfig() throws IOException {

        props.setProperty(Constants.IP_HOST_CONFIG, this.ip);
        OutputStream outputStream = new FileOutputStream(configFile);
        props.store(outputStream, "DFS settings");
        outputStream.close();
        System.out.println("Configuration saved");

    }

    private void loadConfig() throws IOException {
        props = new Properties();
        InputStream inputStream = new FileInputStream(configFile);
        props.load(inputStream);
        inputStream.close();
    }

}