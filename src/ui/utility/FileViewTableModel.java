package ui.utility;

import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class FileViewTableModel extends AbstractTableModel {

    private String[] columnNames = {"Icon", "Name"};

    private FSTreeNode currentTreeNode;
    private ArrayList<TableItem> items = new ArrayList<>();

    //private String[] columnData;


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

        switch (col) {
            case 0:
                if (item.isFile())
                    return new ImageIcon("img/file.png");
                else
                    return new ImageIcon("img/folder.png");

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

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
/*    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        return false;
    }*/

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {

      /*  System.out.println("Setting value at " + row + "," + col
                + " to " + value
                + " (an instance of "
                + value.getClass() + ")");


        data[row][col] = value;
        fireTableCellUpdated(row, col);


        System.out.println("New value of data:");
        printDebugData();*/

    }


}

