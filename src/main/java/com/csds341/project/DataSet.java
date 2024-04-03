package com.csds341.project;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataSet {
    private String name;
    private String[] columns;
    private String[][] data;

    public DataSet(ResultSet rs) throws SQLException{
        name = rs.getMetaData().getTableName(1);
        int columnCount = rs.getMetaData().getColumnCount();
        columns = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columns[i] = rs.getMetaData().getColumnName(i + 1);
        }
        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();
        data = new String[rowCount][columnCount];
        int row = 0;
        while (rs.next()) {
            for (int i = 0; i < columnCount; i++) {
                data[row][i] = rs.getString(i + 1);
            }
            row++;
        }
    }

    public String getName() {
        return name;
    }

    public String[] getColumns() {
        return columns;
    }

    public String[][] getData() {
        return data;
    }
}
