import com.csds341.project.Database;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDatabase {
    String databaseName;
    Database db;

    /**
     * Set up the connection to the database before each test.
     */
    @Before
    public void setUp() {
        try {
            db = new Database(databaseName);
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
            db.finalize();
        } catch (SQLException e) {
            fail("Close Connection Error: " + e.getMessage());
        }
    }

    /**
     * Test the connection to the database.
     * This test ensures that the connection is not null and not closed.
     */
    @Test
    public void testConnection() {
        try {
            assertTrue("Database is not null", db != null);
            assertTrue("Connection is not null", db.getConn() != null);
            assertTrue("Connection is not closed", !db.getConn().isClosed());
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
                db.getConn().createStatement().execute("SELECT * FROM " + table);
            }

            // Assert throw for non-existent table
            assertThrows(SQLException.class, () -> {
                db.getConn().createStatement().execute("SELECT * FROM NonExistentTable");
            });

        } catch (SQLException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testDropDatabase() {
        try {
            db.getConn().createStatement().execute("USE master");
            db.getConn().createStatement().execute("DROP DATABASE IF EXISTS " + databaseName);
        } catch (SQLException e) {
            fail("Exception: " + e.getMessage());
        }
    }
}
