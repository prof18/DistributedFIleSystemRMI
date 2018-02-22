package utility;

import javax.swing.*;
import java.io.OutputStream;

public class CustomOutputStream extends OutputStream {

    private JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        //redirects text to teh area
        textArea.append(String.valueOf((char) b));
        //scroll text area to the end
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}