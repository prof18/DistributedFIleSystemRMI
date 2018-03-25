package ui.dialog;

import ui.frame.MainUI;
import utils.Constants;
import utils.PropertiesHelper;
import utils.Util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingsDialog extends JDialog {

    private String ipHost, ipToConnect, portToConnect, fsName, fsDir;

    public SettingsDialog(boolean exit) {

        //prevent closing the dialog
        if (exit) {
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    showErrorMessage();
                    System.exit(1);
                }
            });
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        //padding
        cs.insets = new Insets(8, 15, 8, 15);
        cs.fill = GridBagConstraints.HORIZONTAL;

        //ipHost host
        JLabel ipHostLabel = new JLabel("IP Host: ");
        cs.gridx = 0;
        cs.gridy = 0;
        panel.add(ipHostLabel, cs);

        JTextField ipHostTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        panel.add(ipHostTextField, cs);

        //ipHost to connect
        JLabel ipToConnectLabel = new JLabel("IP to Connect: ");
        cs.gridx = 0;
        cs.gridy = 1;
        panel.add(ipToConnectLabel, cs);

        JTextField ipToConnectTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        panel.add(ipToConnectTextField, cs);

        //port to connect
        //ipFs
        JLabel portLabel = new JLabel("Port to Connect: ");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(portLabel, cs);

        JTextField portTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 2;
        panel.add(portTextField, cs);

        //fs name
        //file system name
        JLabel nameFSLabel = new JLabel("File System Name: ");
        cs.gridx = 0;
        cs.gridy = 3;
        panel.add(nameFSLabel, cs);

        JTextField nameFSTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 3;
        panel.add(nameFSTextField, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        //fs folder
        JLabel folderChooserLabel = new JLabel("Working directory:");
        cs.gridx = 0;
        cs.gridy = 4;
        panel.add(folderChooserLabel, cs);

        JTextField folderChooserTF = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 4;
        panel.add(folderChooserTF, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        JButton chooseFolderBtn = new JButton("Choose");
        cs.gridx = 3;
        cs.gridy = 4;
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
                this.fsDir = chooser.getSelectedFile().toString();
                System.out.println("Selected Directory : " + chooser.getSelectedFile());
                folderChooserTF.setText(fsDir);
            } else {
                System.out.println("No Directory selected");
            }
        });

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener((ActionListener) -> {
            this.ipHost = ipHostTextField.getText();
            this.ipToConnect = ipToConnectTextField.getText();
            this.portToConnect = portTextField.getText();
            this.fsName = nameFSTextField.getText();
            this.fsDir = folderChooserTF.getText();

            PropertiesHelper helper = PropertiesHelper.getInstance();
            helper.writeConfig(Constants.IP_HOST_CONFIG, ipHost);
            helper.writeConfig(Constants.IP_FS_CONFIG, ipToConnect);
            helper.writeConfig(Constants.PORT_RET_CONFIG, portToConnect);
            helper.writeConfig(Constants.DFS_NAME_CONFIG, fsName);
            helper.writeConfig(Constants.WORKING_DIR_CONFIG, fsDir);

            dispose();

            new MainUI();
        });

      /*  //connect button
        JButton connectBtn = new JButton("Connect to FS");
        connectBtn.addActionListener((ActionListener) -> {
            this.ipHost = portTextField.getText();
            if (ipHost.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected ipHost = " + ipHost);
                PropertiesHelper.getInstance().writeConfig(Constants.IP_HOST_CONFIG, ipHost);
                //load connect dialog
                dispose();
                ConnectDialog connectDialog = new ConnectDialog();
                connectDialog.setVisible(true);
            }
        });

        //create button
        JButton createBtn = new JButton("Create a FS");
        createBtn.addActionListener((ActionListener) -> {
            this.ipHost = portTextField.getText();
            if (ipHost.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected ipHost = " + ipHost);
                PropertiesHelper.getInstance().writeConfig(Constants.IP_HOST_CONFIG, ipHost);
                //load create dialog
                dispose();
                CreateDialog createDialog = new CreateDialog();
                createDialog.setVisible(true);
            }
        });*/

        JPanel bp = new JPanel();
        bp.add(okBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        //load saved data

        PropertiesHelper helper = PropertiesHelper.getInstance();
        ipHostTextField.setText(helper.loadConfig(Constants.IP_HOST_CONFIG));
        ipToConnectTextField.setText(helper.loadConfig(Constants.IP_FS_CONFIG));
        portTextField.setText(helper.loadConfig(Constants.PORT_RET_CONFIG));
        nameFSTextField.setText(helper.loadConfig(Constants.DFS_NAME_CONFIG));
        folderChooserTF.setText(helper.loadConfig(Constants.WORKING_DIR_CONFIG));

        //TODO: uncomment this to generate a fake fs
        //Util.saveFSExample();

      //  new MainUI();
    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}