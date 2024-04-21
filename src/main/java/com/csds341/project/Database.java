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

    /** Properties file containing database connection information */
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    /** Connection to the database */
    private Connection conn;
    /** Name of the database */
    private String dbName;

    /**
     * Connection to the database. Creates the database if it doesn't exist.
     * @param dbName name of the database
     * @throws SQLException
     * @throws IOException
     */
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

    /**
     * Close the connection to the database
     * @throws SQLException
     */
    public void finalize() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    /**
     * Reads and executes SQL statements from a file
     * @param file the name of the file containing the SQL statements
     * @throws SQLException
     * @throws IOException
     */
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

    /**
     * Checks if the database exists
     * @return true if the database exists, false otherwise
     * @throws SQLException
     */
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

    /**
     * Get a list of all the users in the database
     * @return the list of users
     * @throws SQLException
     */
    public String[] getUsers() throws SQLException {
        DataSet ds = new DataSet(conn.createStatement().executeQuery("SELECT username FROM ForumUser"));
        String[] users = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            users[i] = ds.getData()[i][0];
        }
        return users;
    }

    /**
     * Get a list of all the groups in the database
     * @return the list of groups
     * @throws SQLException
     */
    public String[] getGroups() throws SQLException {
        DataSet ds = new DataSet(conn.createStatement().executeQuery("SELECT name FROM ForumGroup"));
        String[] groups = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            groups[i] = ds.getData()[i][0];
        }
        return groups;
    }

    /**
     * Get a list of all the user's groups in the database
     * @return the list of groups
     * @param user the user to get groups for
     * @throws SQLException
     */
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

    /**
     * Get a list of all threads visible to the user
     * @return the list of threads
     * @param user the user to get threads for
     * @throws SQLException
     */
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

    /**
     * Validate a user's password
     * @param password the user's password
     * @param username the user's username
     * @return true if the password is valid, false otherwise
     * @throws SQLException
     */
    public boolean validatePassword(String password, String username) throws SQLException {
        String sql = "SELECT password FROM ForumUser WHERE username = ?;";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        DataSet ds = new DataSet(ps.executeQuery());
        if (ds.getData().length > 0) {
            return password.equals(ds.getData()[0][0]);
        }
        return false;
    }

    /**
     * Create a new group
     * @param group_name the name of the group
     * @param owner_name the owner of the group
     * @throws SQLException
     */
    public void createGroup(String group_name, String owner_name) throws SQLException {
        String sql = "INSERT INTO ForumGroup (name, owner_name, date_created) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, group_name);
        ps.setString(2, owner_name);
        ps.executeUpdate();
    }

    /**
     * Join a group
     * @param group_name the name of the group
     * @param username the user joining the group
     * @throws SQLException
     */
    public void joinGroup(String group_name, String username) throws SQLException {
        String sql = "INSERT INTO UserGroup (username, group_name, date_joined) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, group_name);
        ps.executeUpdate();
    }

    /**
     * Create a new user
     * @param username the username
     * @param password the password
     * @throws SQLException
     */
    public void addnewUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO ForumUser (username, password, date_created) VALUES (?, ?, GETDATE());";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, password);
        ps.executeUpdate();
    }

    /**
     * Create a new thread
     * @param username the user creating the thread
     * @param title the title
     * @return the thread id
     * @throws SQLException
     */
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

    /**
     * Post a reply to a thread
     * @param thread_id the id of the thread
     * @param username the user posting the reply
     * @param content the content of the reply
     * @return the id of the reply
     * @throws SQLException
     */
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

    public String[] getReplies(int thread_id) throws SQLException {
        String sql = "SELECT id, username, content, date_created FROM Reply WHERE thread_id = ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, thread_id);
        DataSet ds = new DataSet(ps.executeQuery());
        String[] replies = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            replies[i] = String.format(
                """
                    ==============================
                    username: %s
                    date_created: %s
                    content:
                    %s
                """, ds.getData()[i][1], ds.getData()[i][3], ds.getData()[i][2]);
                String[] attachments = getAttachments(Integer.parseInt(ds.getData()[i][0]));
                for (String attachment : attachments) {
                    replies[i] += attachment;
                }
                replies[i] += "=============================\n";
        }
        return replies;
    }

    public String[] getAttachments(int reply_id) throws SQLException {
        String sql = "SELECT name, metadata, data FROM Attachment WHERE reply_id = ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reply_id);
        DataSet ds = new DataSet(ps.executeQuery());
        String[] attachments = new String[ds.getData().length];
        for (int i = 0; i < ds.getData().length; i++) {
            attachments[i] = String.format(
                """
                    ------------------------------
                    name: %s
                    metadata: %s
                    data:
                    %s
                    ------------------------------
                """, ds.getData()[i][0], ds.getData()[i][1], ds.getData()[i][2]);
        }
        return attachments;
    }

    /**
     * Create an attachment for a reply
     * @param reply_id the id of the reply whose attachment is being created
     * @param name the name of the attachment
     * @param metadata the metadata of the attachment
     * @param binary the binary data of the attachment
     * @throws SQLException
     */
    public void createAttachment(int reply_id, String name, String metadata, byte[] binary) throws SQLException {
        String sql = "INSERT INTO Attachment (reply_id, name, metadata, data) VALUES (?, ?, ?, ?);";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reply_id);
        ps.setString(2, name);
        ps.setString(3, metadata);
        ps.setBytes(4, binary);
        ps.executeUpdate();
    }

    /**
     * Get the members of a group
     * @param group_name the name of the group
     * @return the members of the group
     * @throws SQLException
     */
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

    /**
     * Add a thread to a group
     * @param thread_id the id of the thread
     * @param group_name the name of the group
     * @throws SQLException
     */
    public void addThreadToGroup(int thread_id, String group_name) throws SQLException {
        String sql = "INSERT INTO ThreadGroup (thread_id, group_name) VALUES (?, ?);";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, thread_id);
        ps.setString(2, group_name);
        ps.executeUpdate();
    }
    /**
     * Check if a user exists
     * @param username the username
     * @return true if the user exists, false otherwise
     * @throws SQLException
     */
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT * FROM ForumUser WHERE username = ?;";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        DataSet ds = new DataSet(ps.executeQuery());
        return ds.getData().length > 0;
    }

    /**
     * Check a password is good
     * @param pw the password
     * @return true if the password is between 3 and 16 characters, false otherwise
     */
    public static boolean goodPassword(String pw) {
        return pw.length() >= 3 && pw.length() <= 16;
      }

    /**
     * Get the connection to the database
     * @return the connection to the database
     */
    public Connection getConn() {
        return conn;
    }

}
