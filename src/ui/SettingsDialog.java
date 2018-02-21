package ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingsDialog extends JDialog {

    private String ip;
    private String FSName;

    public SettingsDialog(JFrame parent) {

        super(parent, "Settings", true);

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
        JLabel ipLabel = new JLabel("IP: ");
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

        //confirm button
        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.addActionListener((ActionListener) -> {
            //TODO: use these values
            this.ip = ipTextField.getText();
            this.FSName = nameFSTextField.getText();
            System.out.println("Selected ip = " + ip);
            System.out.println("Selected FSName = " + FSName);
            if (ip.equals("") || FSName.equals(""))
                showErrorMessage();
            else {
                //launch the login dialog
                dispose();
                LoginDialog loginDialog = new LoginDialog(parent);
                loginDialog.setVisible(true);
            }

        });

        JPanel bp = new JPanel();
        bp.add(confirmBtn);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void showErrorMessage() {
        JOptionPane.showMessageDialog(this,
                "You have to provide some info to go forward",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

}