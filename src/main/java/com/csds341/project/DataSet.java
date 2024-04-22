package com.csds341.project;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class DataSet {
    public final int length;
    private String name;
    private String[] columns;
    private HashMap<String, Integer> columnMap = new HashMap<>();
    private String[][] data;

    public DataSet(ResultSet rs) throws SQLException{
        name = rs.getMetaData().getTableName(1);
        int columnCount = rs.getMetaData().getColumnCount();
        columns = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columns[i] = rs.getMetaData().getColumnName(i + 1);
            columnMap.put(columns[i], i);
        }
        int rowCount = 0;
        List<String[]> listeddata = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getString(i + 1);
            }
            listeddata.add(row);
            rowCount++;
        }
        data = new String[rowCount][columnCount];
        for (int i = 0; i < rowCount; i++) {
            data[i] = listeddata.get(i);
        }
        length = rowCount;
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

    public String getData(int i, String j) {
        return data[i][columnMap.get(j)];
    }
}
