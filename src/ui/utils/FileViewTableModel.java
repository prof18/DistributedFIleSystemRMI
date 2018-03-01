package ui.utils;

import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import java.io.File;

public class FileViewTableModel extends AbstractTableModel {

    private String[] columnNames = {"Icon", "FileWrapper"};


    private String[] columnData;

    public void setColumnData(String[] columnData) {
        this.columnData = columnData;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return columnData.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        switch (col) {
/*
            case 0:

                return FileSystemView.getFileSystemView().getSystemIcon(new File("/home/marco/img"));
*/

            case 1:
                return columnData[row];

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
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        return false;
    }

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

