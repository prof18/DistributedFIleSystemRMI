package ui.dialog;

import ui.frame.MainUI;
import utils.Constants;
import utils.PropertiesHelper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import static java.net.NetworkInterface.getNetworkInterfaces;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * A dialog prompted at start time to provide all the necessary information
 */
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

        //ip host
        JLabel ipHostLabel = new JLabel("IP Host: ");
        cs.gridx = 0;
        cs.gridy = 0;
        panel.add(ipHostLabel, cs);

        JComboBox<Object> petList = new JComboBox<>(getAddress().toArray());
        cs.gridx = 1;
        cs.gridy = 0;
        panel.add(petList, cs);

        //ip to connect
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

        //hostname to connect
        JLabel nameFSLabel = new JLabel("Hostname to Connect:");
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
                if (!fsDir.endsWith("/")) {
                    fsDir = fsDir + "/";
                }
                System.out.println("Selected Directory : " + chooser.getSelectedFile());
                folderChooserTF.setText(fsDir);
            } else {
                System.out.println("No Directory selected");
            }
        });

        //ok button
        JButton okBtn = new JButton("OK");
        okBtn.addActionListener((ActionListener) -> {
            this.ipHost = (String) petList.getSelectedItem();
            this.ipToConnect = ipToConnectTextField.getText();
            this.portToConnect = portTextField.getText();
            this.fsName = nameFSTextField.getText();
            this.fsDir = folderChooserTF.getText();

            PropertiesHelper helper = PropertiesHelper.getInstance();
            PropertiesHelper.setPropFile(fsDir + "/" + "config.properties");
            helper.writeConfig(Constants.IP_HOST_CONFIG, ipHost);
            helper.writeConfig(Constants.IP_FS_CONFIG, ipToConnect);
            helper.writeConfig(Constants.PORT_RET_CONFIG, portToConnect);
            helper.writeConfig(Constants.HOST_NAME_CONFIG, fsName);
            helper.writeConfig(Constants.WORKING_DIR_CONFIG, fsDir);

            MainUI main;

            try {
                dispose();
                main = new MainUI();
                main.showUI(true);
            } catch (NotBoundException e ) {
                //if the info aren't correct exit from the program
                new SettingsDialog(true);
                showMessageDialog(null, "Settings are incorrect! [NotBoundException]");
                e.printStackTrace();
                System.exit(0);
            }
            catch (NullPointerException e){
                new SettingsDialog(true);
                showMessageDialog(null, "Settings are incorrect! [NullPointerException]");
                e.printStackTrace();
                System.exit(0);
            }
        });

        JPanel bp = new JPanel();
        bp.add(okBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        //folderChooserTF.setText("/home/marco/Projects/University/Distribuiti/fs1/");

        //TODO: uncomment this to generate a fake fs
        //Util.saveFSExample();
    }

    /**
     * Shows a pop-up error
     */
    private void showErrorMessage() {
        showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Gets all the Network Interfaces of the host PC
     *
     * @return An Array List with all the Address
     */
    private ArrayList<String> getAddress() {

        ArrayList<String> list = new ArrayList<>();

        try {
            Iterator<NetworkInterface> iterator = getNetworkInterfaces().asIterator();
            while (iterator.hasNext()) {
                NetworkInterface net = iterator.next();
                Enumeration<InetAddress> ee = net.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = ee.nextElement();
                    list.add(i.getHostName());
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return list;
    }

}