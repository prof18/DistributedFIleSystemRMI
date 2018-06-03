package ui.utility;

import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * A model for the File Table View
 */
public class FileViewTableModel extends AbstractTableModel {

    private String[] columnNames = {"Icon", "Name"};

    private FSTreeNode currentTreeNode;
    private ArrayList<TableItem> items = new ArrayList<>();

    public ArrayList<TableItem> getItems() {
        return items;
    }

    public FSTreeNode getCurrentTreeNode() {
        return currentTreeNode;
    }

    public void setNode(FSTreeNode node) {
        items = new ArrayList<>();
        this.currentTreeNode = node;
        //add children
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (FSTreeNode child : node.getChildren()) {
                TableItem tableItem = new TableItem();
                tableItem.setTreeNode(child);
                tableItem.setFile(false);
                items.add(tableItem);
            }
        }
        //add files
        if (node.getFiles() != null && !node.getFiles().isEmpty()) {
            for (FileWrapper file : node.getFiles()) {
                TableItem tableItem = new TableItem();
                tableItem.setFileWrapper(file);
                tableItem.setFile(true);
                items.add(tableItem);
            }
        }
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }


    @Override
    public Object getValueAt(int row, int col) {
        TableItem item = items.get(row);

        //Setup the columns: icon or name
        switch (col) {
            case 0:
                if (item.isFile())
                    return new ImageIcon(getClass().getResource("/file.png"));
                else
                    return new ImageIcon(getClass().getResource("/folder.png"));

            case 1:
                if (item.isFile()) {
                    FileWrapper fileWrapper = item.getFileWrapper();
                    return fileWrapper.getFileName();
                } else {
                    FSTreeNode treeNode = item.getTreeNode();
                    return treeNode.getNameNode();
                }

            default:
                return "";
        }
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}

