package ui.frame;

import ui.utility.CustomOutputStream;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class LogUI extends JFrame {

    public LogUI() {
        super("Log");
        //Create and show the main UI block
        setLocationRelativeTo(null);
        setSize(400, 700);
        setVisible(true);
        setLocationRelativeTo(null);

        //Set the location of the window to the up right
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - getWidth();
        int y = 0;
        setLocation(x, y);

        JTextArea textArea = new JTextArea();

        textArea.setEditable(false);
        //Redirect the output and error stream to the window
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);

        JScrollPane console = new JScrollPane(textArea);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        add(console, BorderLayout.CENTER);
    }
}
