package ui.dialog;

import connection.Connect;
import ui.MainUI;
import utility.Constants;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class ConnectDialog extends JDialog{

    private String ipFs;
    private String directory;
    private String fSName;
    private String ipHost;

    private File configFile = new File("config.properties");
    private Properties props;

    public ConnectDialog() {

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

        //ipFs
        JLabel ipLabel = new JLabel("FS IP: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(ipLabel, cs);

        JTextField ipTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(ipTextField, cs);

        //file system name
        JLabel nameFSLabel = new JLabel("File System Name: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(nameFSLabel, cs);

        JTextField nameFSTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(nameFSTextField, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        //folder chooser
        JLabel folderChooserLabel = new JLabel("Working directory:");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(folderChooserLabel, cs);

        JTextField folderChooserTF = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(folderChooserTF, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        JButton chooseFolderBtn = new JButton("Choose");
        cs.gridx = 3;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(chooseFolderBtn, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
        chooseFolderBtn.addActionListener((ActionListener) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Select the working directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.directory = chooser.getSelectedFile().toString();
                System.out.println("Selected Directory : " + chooser.getSelectedFile());
                folderChooserTF.setText(directory);
            } else {
                System.out.println("No Directory selected");
            }
        });

        //confirm button
        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.addActionListener((ActionListener) -> {
            this.ipFs = ipTextField.getText();
            this.fSName = nameFSTextField.getText();
            this.directory = folderChooserTF.getText();
            if (ipFs.equals("") || fSName.equals("") ||directory.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected ipFs = " + ipFs);
                System.out.println("Selected fSName = " + fSName);
                System.out.println("Selected directory = " + directory);
                try {
                    //save the configurations
                    saveConfig();
                } catch (IOException e) {
                    System.out.println("Unable to save properties file");
                    e.printStackTrace();
                }
                //launch the main ui
                dispose();

                Connect.join(ipFs,fSName,ipHost);

               // new MainUI();
            }

        });

        JPanel bp = new JPanel();
        bp.add(confirmBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        //load config
        try {
            loadConfig();
            ipHost = props.getProperty(Constants.IP_HOST_CONFIG);
            ipTextField.setText(props.getProperty(Constants.IP_FS_CONFIG));
            nameFSTextField.setText(props.getProperty(Constants.DFS_NAME_CONFIG));
            folderChooserTF.setText(props.getProperty(Constants.WORKING_DIR_CONFIG));
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
        props.setProperty(Constants.DFS_NAME_CONFIG, this.fSName);
        props.setProperty(Constants.IP_FS_CONFIG, this.ipFs);
        props.setProperty(Constants.WORKING_DIR_CONFIG, this.directory);
        //props.setProperty(Constants.IP_HOST_CONFIG, this.ipHost);
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
