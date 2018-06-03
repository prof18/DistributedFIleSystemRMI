package ui.utility;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Renderer for the File Table View Cell: by default shows text, in this implementation it can show also images for the file icon
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer {


    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (leaf) {

            setIcon(new ImageIcon(getClass().getResource("/file.png")));
        } else {
            setIcon(new ImageIcon(getClass().getResource("/folder.png")));
        }
        return this;
    }
}
