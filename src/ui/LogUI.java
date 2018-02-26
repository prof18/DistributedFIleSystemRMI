package ui;

import utility.CustomOutputStream;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class LogUI extends JFrame {

    public LogUI(JFrame parent) {
        super("Log");
        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(400, 700);
        setVisible(true);
        setLocation(parent.getX() + parent.getWidth() + 10, parent.getY());
        setVisible(true);

        JTextArea textArea = new JTextArea();
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);

        JScrollPane console = new JScrollPane(textArea);
        textArea.setMargin(new Insets(10,10,10,10));
        add(console, BorderLayout.CENTER);

    }


}
