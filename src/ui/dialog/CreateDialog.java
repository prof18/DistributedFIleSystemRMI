package ui.dialog;

import ui.frame.MainUI;
import utils.Constants;
import utils.PropertiesHelper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateDialog extends JDialog {

    private String ip;
    private String fSName;
    private String directory;

    public CreateDialog() {

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

        //file system name
        JLabel nameFSLabel = new JLabel("File System Name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(nameFSLabel, cs);

        JTextField nameFSTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(nameFSTextField, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        //folder chooser
        JLabel folderChooserLabel = new JLabel("Working directory:");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(folderChooserLabel, cs);

        JTextField folderChooserTF = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(folderChooserTF, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        JButton chooseFolderBtn = new JButton("Choose");
        cs.gridx = 3;
        cs.gridy = 1;
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
            this.fSName = nameFSTextField.getText();
            this.directory = folderChooserTF.getText();
            if (ip.equals("") || fSName.equals("") || directory.equals(""))
                showErrorMessage();
            else {
                System.out.println("Selected ip = " + ip);
                System.out.println("Selected fSName = " + fSName);
                System.out.println("Selected directory = " + directory);

                PropertiesHelper.getInstance().writeConfig(Constants.IP_HOST_CONFIG, this.ip);
                PropertiesHelper.getInstance().writeConfig(Constants.DFS_NAME_CONFIG, this.fSName);
                PropertiesHelper.getInstance().writeConfig(Constants.WORKING_DIR_CONFIG, this.directory);
                PropertiesHelper.getInstance().writeConfig(Constants.IP_FS_CONFIG, this.ip);

                //launch the main ui
                dispose();

              //  Create.create(ip, fSName);

                new MainUI();
            }

        });

        JPanel bp = new JPanel();
        bp.add(confirmBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        ip = PropertiesHelper.getInstance().loadConfig(Constants.IP_HOST_CONFIG);
        nameFSTextField.setText(PropertiesHelper.getInstance().loadConfig(Constants.DFS_NAME_CONFIG));
        folderChooserTF.setText(PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG));

    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}