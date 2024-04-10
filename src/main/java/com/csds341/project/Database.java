package com.csds341.project;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        execute("USE " + dbName);
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

        // Create the database
        execute("DROP DATABASE IF EXISTS " + dbName);
        execute("CREATE DATABASE " + dbName);
        // Open the theforum.sql file and execute each statement
        InputStream input = Main.class.getClassLoader().getResourceAsStream(createFile);
        byte[] buffer = new byte[input.available()];
        input.read(buffer);
        input.close();
        String sql = new String(buffer);
        String[] sqlStatements = sql.split(";");
        for (String statement : sqlStatements) {
            statement = statement.replace("\n", "");
            execute(statement);
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

    public void execute(String statement) throws SQLException {
        // Check if the connection is closed
        if (conn == null || conn.isClosed()) {
            System.out.println("Connection is closed");
            return;
        }
        Statement stmt = conn.createStatement();
        stmt.execute(statement);
        stmt.close();
    }

    public DataSet query(String statement) throws SQLException {
        // Check if the connection is closed
        if (conn == null || conn.isClosed()) {
            System.out.println("Connection is closed");
            return null;
        }
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(statement);
        DataSet ds = new DataSet(rs);
        stmt.close();
        return ds;
    }
}
