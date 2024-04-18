/**
 * Project: CSDS 341 Project Group 6
 * Database: The Forum
 * Version: 1.0
 * Authors:
 *  - Mikey Maldonado (mxm1667)
 *  - Mike Zhang (zxz1233)
 *  - Tola Oshomoji (tdo18)
 */
package com.csds341.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    private static String databaseName;
    private static String connectionString;
    private static Connection conn;

    public static void main(String[] args) {

        try {
            
            /**
             * Load database name and connection string from the network properties
             */
            {
                Properties prop = new Properties();
                InputStream input = Main.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES);
                prop.load(input);
                input.close();
                databaseName = prop.getProperty("databaseName");
                connectionString = "jdbc:sqlserver://" + prop.getProperty("databaseServer") + "\\" + prop.getProperty("networkId") + ";"
                + "user=sa;"
                + "password=" + prop.getProperty("saPassword") + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=15;";
            }

            /**
             * Open Connection to SQL Server
             */
            conn = DriverManager.getConnection(connectionString);
            Database theForum = new Database(conn, databaseName);

            /**
             * TODO: Replace this block of code with an actual application
             */
            {
                // Insert sample users if they don't already exist
                {
                    DataSet aliceQuery = theForum.query("SELECT * FROM ForumUser WHERE username = 'Alice'");
                    if (aliceQuery.getData().length  == 0) {
                        String[] users = {"Alice", "Bob", "Charlie"};
                        String[] passwords = {"password1", "password2", "password3"};
        
                        for (int i = 0; i < users.length; i++) {
                            theForum.execute("INSERT INTO ForumUser (username, password, date_created) VALUES ('" + users[i] + "', '" + passwords[i] + "', GETDATE())");
                        }
                    }
                }
        
                // Query users
                {
                    DataSet usersQuery = theForum.query("SELECT * FROM ForumUser");
                    String[][] usersData = usersQuery.getData();
                    int numRows = usersData.length;
                    int numCols = usersQuery.getColumns().length;
                    for (String col : usersQuery.getColumns()) {
                        System.out.print(col + " ");
                    }
                    System.out.println();
                    for (int i = 0; i < numRows; i++) {
                        for (int j = 0; j < numCols; j++) {
                            System.out.print(usersData[i][j] + " ");
                        }
                        System.out.println();
                    }
                }
            }

        /**
         * Handle exceptions
         */
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            System.exit(1);
        } finally {

            /**
             * Close the connection
             */
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
            }
        }
    }
}
