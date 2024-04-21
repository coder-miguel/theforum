package com.csds341.project;

import java.io.InputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;


public class Database {

    private static final String CONNECTION_PROPERTIES = "connection.properties";
    private Connection conn;
    private String dbName;

    public Database(String dbName) throws SQLException, IOException {
            
        /**
         * Load database name and connection string from the network properties
         */
        Properties prop = new Properties();
        InputStream input = Main.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES);
        prop.load(input);
        input.close();
        dbName = prop.getProperty("databaseName");
        String connectionString = "jdbc:sqlserver://" + prop.getProperty("databaseServer") + "\\" + prop.getProperty("networkId") + ";"
        + "user=sa;"
        + "password=" + prop.getProperty("saPassword") + ";"
        + "encrypt=true;"
        + "trustServerCertificate=true;"
        + "loginTimeout=15;";
        conn = DriverManager.getConnection(connectionString);
        this.dbName = dbName;
        if (!exists()) {
            // create the database
            read(dbName + ".sql");
        }
        conn.createStatement().execute("USE " + dbName);
        conn.setAutoCommit(false);
    }

    public void finalize() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public void read(String file) throws SQLException, IOException {
        // Check if the connection is closed
        if (conn == null || conn.isClosed()) {
            System.out.println("Connection is closed");
            return;
        }

        // Open the file and execute each statement
        InputStream input = Main.class.getClassLoader().getResourceAsStream(file);
        byte[] buffer = new byte[input.available()];
        input.read(buffer);
        input.close();
        String sql = new String(buffer);
        String[] sqlStatements = sql.split(";");
        for (String statement : sqlStatements) {
            conn.createStatement().execute(statement.replace("\n", " "));
        }
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
    public String[] getGroups(String user) throws SQLException {
        String sql = """
            SELECT DISTINCT
                UserGroups.name
            FROM (
                SELECT
                    ForumGroup.name,
                    UserGroup.group_name,
                    UserGroup.username
                FROM ForumGroup
                LEFT JOIN UserGroup
                ON ForumGroup.name = UserGroup.group_name
                WHERE UserGroup.username = ?
                OR ForumGroup.owner_name = ?
            ) as UserGroups;
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, user);
        ps.setString(2, user);
        DataSet ds = new DataSet(ps.executeQuery());
        String[] groups = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            groups[i] = ds.getData()[i][0];
        }
        return groups;
    }

    public HashMap<Integer, String> getThreads(String user) throws SQLException {
        String sql = """
            SELECT
                Thread.id,
                Thread.title,
                Thread.username,
                ThreadGroup.group_name
            FROM Thread
            LEFT JOIN ThreadGroup
            ON Thread.id = ThreadGroup.thread_id
            WHERE ThreadGroup.group_name IS NULL
            OR Thread.username = ?
            OR ThreadGroup.group_name IN (
                SELECT group_name
                FROM ForumGroup
                WHERE owner_name = ?
            )
            OR ThreadGroup.group_name IN (
                SELECT group_name
                FROM UserGroup
                WHERE username = ?
            );
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, user);
        ps.setString(2, user);
        ps.setString(3, user);
        DataSet ds = new DataSet(ps.executeQuery());
        HashMap<Integer, String> threads = new HashMap<Integer, String>();
        for (int i = 0; i < ds.getData().length; i++) {
            threads.put(Integer.parseInt(ds.getData()[i][0]), ds.getData()[i][1]);
        }
        return threads;
    }

    public boolean validatePassword(String password, String username) throws SQLException {
        String sql = "SELECT * FROM ForumUser WHERE username = ? and password = ?;";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        ps.setString(2, password);
        DataSet ds = new DataSet(ps.executeQuery());
        return ds.getData().length > 0;
    }

    public void createGroup(String group_name, String owner_name) throws SQLException {
        String sql = "INSERT INTO ForumGroup (name, owner_name, date_created) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, group_name);
        ps.setString(2, owner_name);
        ps.executeUpdate();
    }

    public void joinGroup(String group_name, String username) throws SQLException {
        String sql = "INSERT INTO UserGroup (username, group_name, date_joined) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, group_name);
        ps.executeUpdate();
    }

    public void addnewUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO ForumUser (username, password, date_created) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    public int createThread(String username, String title) throws SQLException {
        String sql = "INSERT INTO Thread (username, title, date_created) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        ps.setString(2, title);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    public int postReply(int thread_id, String username, String content) throws SQLException {
        String sql = "INSERT INTO Reply (thread_id, username, content, date_created) VALUES (?, ?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, thread_id);
        ps.setString(2, username);
        ps.setString(3, content);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getInt(1);
    }

    public void createAttachment(int reply_id, String name, String metadata, byte[] binary) throws SQLException {
        String sql = "INSERT INTO Attachment (reply_id, name, metadata, data) VALUES (?, ?, ?, ?);";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reply_id);
        ps.setString(2, name);
        ps.setString(3, metadata);
        ps.setBytes(4, binary);
        ps.executeUpdate();
    }

    public String[] groupMembers(String group_name) throws SQLException {
        String sql = "SELECT username FROM UserGroup WHERE group_name = ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, group_name);
        DataSet ds = new DataSet(ps.executeQuery());
        String[] members = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            members[i] = ds.getData()[i][0];
        }
        return members;
    }

    public void addThreadToGroup(int thread_id, String group_name) throws SQLException {
        String sql = "INSERT INTO ThreadGroup (thread_id, group_name) VALUES (?, ?);";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, thread_id);
        ps.setString(2, group_name);
        ps.executeUpdate();
    }

    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT * FROM ForumUser WHERE username = ?;";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        DataSet ds = new DataSet(ps.executeQuery());
        return ds.getData().length > 0;
    }

    public static boolean goodPassword(String pw) {
        return pw.length() >= 3 && pw.length() <= 16;
      }

    public Connection getConn() {
        return conn;
    }

}
