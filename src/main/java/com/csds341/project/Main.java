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
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    private static String databaseName;
    private static String connectionString;
    private static Connection conn;
    static Database theForum;
    static Scanner scanner = new Scanner(System.in);

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
            theForum = new Database(conn, databaseName);

            {
                int selection = 0;
                while (selection != 8) {
                    Menu.print();
                    while (selection < 1 || selection > 8) {
                        try {
                            selection = scanner.nextInt();
                            scanner.nextLine();
                        } catch (InputMismatchException e) {
                            selection = 0;
                            System.out.println("Invalid input. Please enter a number between 1 and 8.");
                        }
                    }
                    Menu.select(selection);
                    selection = 0;
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
    class Menu {
        static Method method = new Method();
        static void select(int option) {
            System.out.println();
            try {
                switch(option) {
                    /**
                     * Create a new user
                     */
                    case 1: {
                        System.out.print("Enter a username: ");
                        String username = scanner.nextLine();
                        System.out.print("Enter a password: ");
                        String password = scanner.nextLine();
                        Method.addnewUser(username, password);
                        break;
                    }
                    /**
                     * Create a new group
                     */
                    case 2:
                        if (theForum.getUsers().length == 0) {
                            System.out.println("No users found. Please create a user first.");
                        } else {
                            System.out.println("Enter a group name: ");
                            String groupName = scanner.nextLine();
                            String owner = Menu.selectUser("Select an owner for the new group: ");
                            method.createGroup(groupName, owner);
                        }
                        break;
                    /**
                     * Join user to a group
                     */
                    case 3:
                        if (theForum.getUsers().length == 0) {
                            System.out.println("No users found. Please create a user first.");
                        } else if (theForum.getGroups().length == 0) {
                            System.out.println("No groups found. Please create a group first.");
                        } else {
                            String group = Menu.selectGroup("Select a group: ");
                            String user = Menu.selectUser("Select a user to join: ");
                            method.joinGroup(user, group);
                        }
                        break;
                    /**
                     * Post a thread
                     */
                    case 4: 
                        if (theForum.getUsers().length == 0) {
                            System.out.println("No users found. Please create a user first.");
                        } else {
                            String user = Menu.selectUser("Select a user to post the thread: ");
                            System.out.println("Enter a title for the thread: ");
                            String title = scanner.nextLine();
                            System.out.println("Enter a body for the thread: ");
                            String content = scanner.nextLine();
                            System.out.println("Enter the number of attachments: ");
                            int num_attachments = scanner.nextInt();
                            scanner.nextLine();
                            Attachment[] attachments = new Attachment[num_attachments];
                            for (int i = 0; i < num_attachments; i++) {
                                System.out.println("Enter the name of attachment " + (i + 1) + ": ");
                                String name = scanner.nextLine();
                                System.out.println("Enter the content type of attachment " + (i + 1) + ": ");
                                String metadata = scanner.nextLine();
                                System.out.println("Enter the content of attachment " + (i + 1) + ": ");
                                String data = scanner.nextLine();
                                attachments[i] = new Attachment(name, metadata, data);
                            }

                            // I need the thread_id to post a reply to this thread
                            method.postThread(user, title);
                            
                            // int thread_id = TODO;
                            // method.postReply(user, thread_id, content);

                            // get reply_id of reply that was posted
                            // int reply_id = TODO;

                            // for (Attachment a : attachments) {
                            //     method.attachToReply(reply_id, a.name, a.metadata, a.data);
                            // }
                        }
                        break;
                    /**
                     * Post a reply
                     */
                    case 5:
                        if (theForum.getUsers().length == 0) {
                            System.out.println("No users found. Please create a user first.");
                        } else {
                            String user = Menu.selectUser("Select a user to post the reply: ");
                            
                            // Get threads available to this user ( all public threads and threads they are in )
                            
                            // String thread_id = Menu.selectThread("Select a thread: ", user);

                            // Post a reply to this thread

                        }
                        break;
                    /**
                     * See group members
                     */
                    case 6:
                        if (theForum.getUsers().length == 0) {
                            System.out.println("No users found. Please create a user first.");
                        } else if (theForum.getGroups().length == 0) {
                            System.out.println("No groups found. Please create a group first.");
                        } else {
                            String user = Menu.selectUser("Select a user: ");
                            // String group = Menu.selectGroup("Select a group: ");
                        }
                        break;
                    default:
                        break;
                }
            } catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
            }
        }
    
        static void print() {
            System.out.println("Please select one of the following options:");
            System.out.println("1) Create new user");
            System.out.println("2) Create a new group");
            System.out.println("3) Join user to a group");
            System.out.println("4) Show group members");
            System.out.println("5) Post a thread");
            System.out.println("6) Post a reply");
            System.out.println("7) See available threads");
            System.out.println("8) Exit");
        }
    
        static String selectUser(String prompt) throws SQLException {
            StringBuffer sb = new StringBuffer();
            String[] users = theForum.getUsers();
            if (prompt != null) {
                System.out.println(prompt);
            } else {
                System.out.println("Select a user:");
            }
            for (int i = 0; i < users.length; i++) {
                System.out.println(String.valueOf(1 + i) + ") " + users[i]);
            }
            int nextInt = 0;
            try {
                nextInt = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {}
            while (nextInt <= 0 || nextInt > users.length) {
                System.out.println("Invalid input. Please enter a number between 1 and " + (users.length));
                try {
                    nextInt = scanner.nextInt();
                    scanner.nextLine();
                } catch (InputMismatchException e) {}
            }
            for (char c : users[nextInt - 1].toCharArray()) {
                    sb.append(c);
            }
            return sb.toString();
        }

        static String selectGroup(String prompt) throws SQLException {
            StringBuffer sb = new StringBuffer();
            String[] groups = theForum.getGroups();
            if (prompt != null) {
                System.out.println(prompt);
            } else {
                System.out.println("Select a group:");
            }
            for (int i = 0; i < groups.length; i++) {
                System.out.println(String.valueOf(1 + i) + ") " + groups[i]);
            }
            int nextInt = 0;
            try {
                nextInt = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {}
            while (nextInt <= 0 || nextInt > groups.length) {
                System.out.println("Invalid input. Please enter a number between 1 and " + (groups.length));
                try {
                    nextInt = scanner.nextInt();
                    scanner.nextLine();
                } catch (InputMismatchException e) {}
            }
            for (char c : groups[nextInt - 1].toCharArray()) {
                    sb.append(c);
            }
            return sb.toString();
        }
    }
}


