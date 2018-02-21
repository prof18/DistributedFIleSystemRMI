package ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {

    private String username;
    private String pwd;

    public LoginDialog(JFrame parent) {

        super(parent, "Login", true);

        //prevent closing the dialog
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showErrorMessage("You have to insert your credentials to proceed");
                System.exit(1);
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        //padding
        cs.insets = new Insets(8, 15, 8, 15);
        cs.fill = GridBagConstraints.HORIZONTAL;

        //Username
        JLabel usernameLabel = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(usernameLabel, cs);

        JTextField usernameTextField = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(usernameTextField, cs);

        //Password
        JLabel pwdLabel = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(pwdLabel, cs);

        JPasswordField pwdTextField = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pwdTextField, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        //Login button
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener((ActionListener) -> {

            this.username = usernameTextField.getText();
            this.pwd = new String(pwdTextField.getPassword());


            if (username.equals("") || pwd.equals("")) {
                showErrorMessage("You have to insert your credentials to proceed");
            } else if (false) {
                showErrorMessage("Your credentials aren't correct. Please try again");
                usernameTextField.setText("");
                pwdTextField.setText("");
            } else {
                //all correct
                System.out.println("Select username = " + username);
                System.out.println("Selected pwd = " + pwd);
                //TODO: use these values
                //return to the main UI
                dispose();
            }
        });

        JPanel bp = new JPanel();
        bp.add(btnLogin);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}