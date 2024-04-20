package com.csds341.project;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    private Connection conn;
    private String dbName;

    public Database(Connection conn, String dbName) throws SQLException, IOException {
        if (this.conn != null && !this.conn.isClosed()) {
            conn.close();
        }
        this.conn = conn;
        this.dbName = dbName;
        if (!exists()) {
            create(dbName + ".sql");
        }
        conn.createStatement().execute("USE " + dbName);
    }

    public void finalize() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public void create(String createFile) throws SQLException, IOException {
        // Check if the connection is closed
        if (conn == null || conn.isClosed()) {
            System.out.println("Connection is closed");
            return;
        }

        // Open the theforum.sql file and execute each statement
        InputStream input = Main.class.getClassLoader().getResourceAsStream(createFile);
        byte[] buffer = new byte[input.available()];
        input.read(buffer);
        input.close();
        String sql = new String(buffer);
        String[] sqlStatements = sql.split(";");
        for (String statement : sqlStatements) {
            conn.createStatement().execute(statement.replace("\n", " "));
        }
        System.out.println("Database created");
    }

    public boolean exists() throws SQLException {
        ResultSet resultSet = conn.getMetaData().getCatalogs();
        while (resultSet.next()) {
            String databaseName = resultSet.getString(1);
            if (databaseName.equals(dbName)) {
                return true;
            }
        }
        return false;
    }

    public String[] getUsers() throws SQLException {
        DataSet ds = new DataSet(conn.createStatement().executeQuery("SELECT username FROM ForumUser"));
        String[] users = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            users[i] = ds.getData()[i][0];
        }
        return users;
    }

    public String[] getGroups() throws SQLException {
        DataSet ds = new DataSet(conn.createStatement().executeQuery("SELECT name FROM ForumGroup"));
        String[] groups = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            groups[i] = ds.getData()[i][0];
        }
        return groups;
    }
}
