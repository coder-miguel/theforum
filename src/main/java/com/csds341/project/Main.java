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
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    private static String databaseName;
    private static String connectionString;
    private static Connection conn;
    private static String loggedinUser;
    private static Database theForum;
    private static Scanner scanner = new Scanner(System.in);

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

            /*
             * Login
             */
            {
                int loginOption = 0;
                System.out.println("Please select one of the following options:");
                System.out.println("1) Log in as existing user");
                System.out.println("2) Log in as new user");
                System.out.println("3) Quit");
                while (loginOption < 1 || loginOption > 3) {
                    try {
                        loginOption = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        loginOption = 0;
                    }
                }
                switch(loginOption) {
                    case 1:
                        loggedinUser = Main.login();
                        if (loggedinUser != null) {
                            System.out.println("You are now logged in as " + loggedinUser + ".");
                            break;
                        }
                    case 2:
                        while (loggedinUser == null) {
                          loggedinUser = Main.newUser();
                        }
                        break;
                    case 3:
                        System.out.println("Goodbye!");
                        break;
                }
            }

            /*
             * Menu Loop
             */
            {
                int selection = 0;
                while (selection != 6) {
                    Menu.print();
                    selection = 0;
                    while (selection < 1 || selection > 6) {
                        try {
                            selection = Integer.parseInt(scanner.nextLine());
                        } catch (InputMismatchException e) {
                            selection = 0;
                            System.out.println("Invalid input. Please enter a number between 1 and 6.");
                        }
                    }
                    Menu.select(selection);
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
    static String login() {
        String loginAttempt = null;
        try {
            if (theForum.getUsers().length == 0) {
                System.out.println("No users found. Please create a user first.");
            } else {
                loginAttempt = Menu.selectUser("Select a user to log in as: ");
                System.out.println("Enter in your password: ");
                String password = scanner.nextLine();
                while (!theForum.validatePassword(password, loginAttempt)) {
                    System.out.println("Invalid password. Please try again.");
                    password = scanner.nextLine();
                }
                System.out.println("You are now logged in as " + loginAttempt + ".");
            }
        } catch (SQLException e) {
            loginAttempt = null;
            System.err.println("SQL Exception: " + e.getMessage());
        }
        return loginAttempt;
    }

    static String newUser() {
        String new_user = null;
        System.out.println("Enter in your new username: ");
        String username = scanner.nextLine();
        try {
            while(theForum.userExists(username)) {
                System.out.println("Enter in your new username: ");
                username = scanner.nextLine();
            }
            String password = "";
            while(!Database.goodPassword(password)) {
                System.out.println("Enter in your new password: ");
                password = scanner.nextLine();
            }
            theForum.addnewUser(username, password);
            System.out.println("You are now logged in as " + username + ".");
            new_user = username;
            theForum.getConn().commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException s) {
                s.printStackTrace();
            }
            new_user = null;
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please try again.");
        }
        return new_user;
    }

    class Menu {
        static void select(int option) {
            System.out.println();
            try {
                switch(option) {
                    /**
                     * Create a new group
                     */
                    case 1: {
                            System.out.println("Enter a group name: ");
                            String groupName = scanner.nextLine();
                            theForum.createGroup(groupName, loggedinUser);
                            theForum.getConn().commit();
                        }
                        break;
                    /**
                     * Join a group
                     */
                    case 2:
                        if (theForum.getGroups().length == 0) {
                            System.out.println("No groups found. Please create a group first.");
                        } else {
                            String group = Menu.selectGroup("Select a group: ", false);
                            theForum.joinGroup(group, loggedinUser);
                            theForum.getConn().commit();
                        }
                        break;
                        
                    /**
                     * See group members
                     */
                    case 3:
                        if (theForum.getGroups(loggedinUser).length == 0) {
                            System.out.println("Not in a group. Try creating or joining a group first.");
                        } else {
                            String group = Menu.selectGroup("Select a group: ", true);
                            System.out.println("Group members: ");
                            for (String member : theForum.groupMembers(group)) {
                                System.out.println(member);
                            }
                            System.out.println("");
                        }
                        break;
                    /**
                     * Post a thread
                     */
                    case 4:
                    /**
                     * Post a reply
                     */
                    case 5:
                        if (option == 5 && theForum.getThreads(loggedinUser).size() == 0) {
                            System.out.println("No threads found. Try creating a thread first.");
                        } else {
                            int selectedThread;
                            if (option == 4) {
                                System.out.println("Enter a title for the thread: ");
                                String title = scanner.nextLine();
                                selectedThread = theForum.createThread(Main.loggedinUser, title);
                                System.out.println("Add thread to a group? (y/n): ");
                                if (scanner.nextLine().equalsIgnoreCase("y")) {
                                    String group = Menu.selectGroup("Select a group: ", true);
                                    theForum.addThreadToGroup(selectedThread, group);
                                }
                            } else {
                                selectedThread = selectThread("Select a thread to reply to: ");
                            }

                            // TODO: Show all replies of the selected thread
                            // for (String r : theForum.getReplies(selectedThread)) {
                            //     System.out.println(r);
                            // }

                            // Post a reply
                            System.out.println("Enter the content body: ");
                            String content = scanner.nextLine();

                            int new_reply_id = theForum.postReply(selectedThread, Main.loggedinUser, content);

                            System.out.println("Enter the number of attachments: ");
                            int num_attachments = 0;
                            try {
                                num_attachments = Integer.parseInt(scanner.nextLine());
                            } catch (NumberFormatException e) {
                                num_attachments = 0;
                            }
                            for (int i = 0; i < num_attachments; i++) {
                                System.out.println("Enter the name of attachment " + (i + 1) + ": ");
                                String name = scanner.nextLine();
                                System.out.println("Enter the content type of attachment " + (i + 1) + ": ");
                                String metadata = scanner.nextLine();
                                System.out.println("Enter the content of attachment " + (i + 1) + ": ");
                                byte[] data = scanner.nextLine().getBytes();
                                theForum.createAttachment(new_reply_id, name, metadata, data);
                            }
                            theForum.getConn().commit();
                        }
                        break;
                    default:
                        break;
                }
            } catch (SQLException e) {
                try {
                    theForum.getConn().rollback();
                } catch (SQLException s) {
                    System.err.println("SQL Exception: " + e.getMessage());
                    System.exit(1);
                }
                System.err.println("SQL Exception: " + e.getMessage());
            }
        }
    
        static void print() {
            System.out.println("Please select one of the following options:");
            System.out.println("1) Create a new group");
            System.out.println("2) Join user to a group");
            System.out.println("3) Show group members");
            System.out.println("4) Post a thread");
            System.out.println("5) Post a reply");
            System.out.println("6) Exit");
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
            int selectedUser = 0;
            try {
                selectedUser = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
            }
            while (selectedUser <= 0 || selectedUser > users.length) {
                System.out.println("Invalid input. Please enter a number between 1 and " + (users.length));
                try {
                    selectedUser = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                }
            }
            for (char c : users[selectedUser - 1].toCharArray()) {
                    sb.append(c);
            }
            return sb.toString();
        }

        static String selectGroup(String prompt, boolean userGroups) throws SQLException {
            StringBuffer sb = new StringBuffer();
            String[] groups;
            if (userGroups) {
                groups = theForum.getGroups(loggedinUser);
            } else {
                groups = theForum.getGroups();
            }
            if (prompt != null) {
                System.out.println(prompt);
            } else {
                System.out.println("Select a group:");
            }
            for (int i = 0; i < groups.length; i++) {
                System.out.println(String.valueOf(1 + i) + ") " + groups[i]);
            }
            int selectedGroup = 0;
            try {
                selectedGroup = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                selectedGroup = 0;
            }
            while (selectedGroup <= 0 || selectedGroup > groups.length) {
                System.out.println("Invalid input. Please enter a number between 1 and " + (groups.length));
                try {
                    selectedGroup = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                }
            }
            for (char c : groups[selectedGroup - 1].toCharArray()) {
                    sb.append(c);
            }
            return sb.toString();
        }

        static int selectThread(String prompt) throws SQLException {
            HashMap<Integer, String> threads = theForum.getThreads(loggedinUser);
            if (prompt != null) {
                System.out.println(prompt);
            } else {
                System.out.println("Select a thread:");
            }
            for (HashMap.Entry<Integer, String> t : threads.entrySet()) {
                System.out.println(String.valueOf(t.getKey()) + ") " + t.getValue());
            }
            int selectedThread = -1;
            try {
                selectedThread = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
            }
            while (!threads.keySet().contains(selectedThread)) {
                System.out.println("Invalid input. Please enter a number in the list above.");
                try {
                    selectedThread = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    selectedThread = -1;
                }
            }
            return selectedThread;
        }
    }
}


