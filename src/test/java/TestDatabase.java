import com.csds341.project.Database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDatabase {
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    String databaseName;
    String connectionString;
    Connection conn;
    Database db;

    /**
     * Set up the connection to the database before each test.
     */
    @Before
    public void setUp() {
        try {

            InputStream input = TestDatabase.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES);
            Properties prop = new Properties();
            prop.load(input);
            input.close();
    
            String networkId = prop.getProperty("networkId");
            String databaseServer = prop.getProperty("databaseServer");
            String saPassword = prop.getProperty("saPassword");
            
            databaseName = prop.getProperty("databaseName");
            connectionString = "jdbc:sqlserver://" + databaseServer + "\\" + networkId + ";"
                    + "user=sa;"
                    + "password=" + saPassword + ";"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
                    + "loginTimeout=15;";
            conn = DriverManager.getConnection(connectionString);
            db = new Database(conn, databaseName);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }


    /**
     * Close the connection to the database after each test.
     */
    @After
    public void tearDown() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    /**
     * Test the connection to the database.
     * This test ensures that the connection is not null and not closed.
     */
    @Test
    public void testConnection() {
        try {
            assertTrue("Connection is not null", conn != null);
            assertTrue("Connection is not closed", !conn.isClosed());
            assertTrue("Database is not null", db != null);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    /**
     * Test the tables in the database.
     * This test ensures that the expected tables exist in the database.
     * It also ensures that an exception is thrown when a non-existent table is queried.
     */
    @Test
    public void testTables() {
        try {

            // Assert no exceptions thrown from existing tables
            String[] tables = {
                "ForumUser",
                "Thread",
                "Reply",
                "Attachment",
                "ForumGroup",
                "UserGroup",
                "ThreadGroup"
            };
            for (String table : tables) {
                conn.createStatement().execute("SELECT * FROM " + table);
            }

            // Assert throw for non-existent table
            assertThrows(SQLException.class, () -> {
                conn.createStatement().execute("SELECT * FROM NonExistentTable");
            });

        } catch (SQLException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testDropDatabase() {
        try {
            conn.createStatement().execute("USE master");
            conn.createStatement().execute("DROP DATABASE IF EXISTS " + databaseName);
        } catch (SQLException e) {
            fail("Exception: " + e.getMessage());
        }
    }
}
