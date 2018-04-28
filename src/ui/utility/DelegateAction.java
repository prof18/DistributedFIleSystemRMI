package ui.utility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Delegate to handle the Enter Key press in the UI
 */
public class DelegateAction extends AbstractAction {

    private ActionListener action;
    public DelegateAction(ActionListener customAction) {
        this.action = customAction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
    }
}
