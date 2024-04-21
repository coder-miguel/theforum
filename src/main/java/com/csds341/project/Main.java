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

import java.sql.SQLException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {
    private static String loggedinUser;
    private static Database theForum;
    private static Scanner scanner = new Scanner(System.in);

    /**
     * The main method.
     * Loads the database name and connection string from the network properties.
     * Opens a connection to SQL Server.
     * Allows the user to login as an existing user or create a new user.
     * Allows the user to create a new group, join a group, create a thread, post a thread, post a reply, see available threads in the user's group, and see group members.
     * Closes the connection to SQL Server when the user chooses to exit.
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            /**
             * Open Connection to Database
             */
            theForum = new Database();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Closing connection.");
                    if (theForum != null) {
                        theForum.finalize();
                        theForum = null;
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                } finally {
                    System.out.println("Goodbye!");
                }
            }));

            /*
             * Login
             */
            {
                LoginMenu.show();
                loggedinUser = LoginMenu.login();
                if (loggedinUser == null) {
                    System.exit(0);
                }
            }

            /*
             * Menu Loop
             */
            {
                do {
                    MainMenu.show();
                } while (MainMenu.select() != MainMenu.EXIT);
            }

        /**
         * Handle exceptions
         */
        } catch (IOException e) {
            System.err.println("Main IO Exception: " + e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Main SQL Exception: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Login Menu
     * Allows the user to login as an existing user or create a new user.
     * Returns the username of the logged in user or null if the user chooses to exit.
     */
    class LoginMenu {
        static void show() {
            System.out.println("""

                0) Quit
                1) Log in as existing user
                2) Log in as new user

                """);
        }

        /**
         * Login Menu Selection
         * Allows the user to login as an existing user or create a new user.
         * @return the username of the logged in user or null if the user chooses to exit.
         */
        static String login() {
            String user = null;
            int selection = -1;
            while (selection < 0 || selection > 2) {
                try {
                    selection = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    selection = -1;
                    System.out.println("Invalid input. Please enter a number between 0 and 2.");
                }
            }
            switch(selection) {
                case 1:
                    user = existingUser();
                    if (user != null) break;
                case 2:
                    user = newUser();
                    break;
                default:
                    break;
                }
                return user;
        }

        /**
         * Prompts the user to select an existing user.
         * If no users exist, prompts the user to create a new user.
         * @return the username of the selected user or null if an error occurs.
         */
        static String existingUser() {
            String loginAttempt = null;
            try {
                if (theForum.getUsers().length == 0) {
                    System.out.println("No users found. Please create a user first.");
                } else {
                    loginAttempt = MainMenu.selectUser("Select a user to log in as: ");
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
                System.err.println("Login Error: " + e.getMessage());
            }
            return loginAttempt;
        }

        /**
         * Prompts the user to create a new user.
         * @return the username of the new user or null if an error occurs.
         */
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
                    theForum.getConn().rollback();
                } catch (SQLException s) {
                    System.err.println("Error rolling back new user: " + s.getMessage());
                }
                new_user = null;
                System.err.println("Error creating new user: " + e.getMessage());
                System.out.println("Please try again.");
            }
            return new_user;
        }
    }

    /**
     * Main Menu
     * Allows the user to create a new group, join a group, create a thread, post a thread, post a reply, see available threads in the user's group, and see group members.
     */
    class MainMenu {
        /** User input value to exit the program. */
        static final int EXIT = 0;

        /**
         * Show
         * Displays the main menu.
         */
        static void show() {
            System.out.println("""

                0) Exit
                1) Create a new group
                2) Join user to a group
                3) Show group members
                4) Post a thread
                5) Post a reply

                """);
        }

        /**
         * Select
         * Prompts the user to select an option from the main menu.
         * The selected option is then executed.
         * @return the selected option.
         */
        static int select() {
            int selection = -1;
            while (selection < 0 || selection > 5) {
                try {
                    selection = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    selection = -1;
                    System.out.println("Invalid input. Please enter a number between 0 and 5.");
                }
            }

            System.out.println();
            try {
                switch(selection) {
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
                            String group = MainMenu.selectGroup("Select a group: ", false);
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
                            String group = MainMenu.selectGroup("Select a group: ", true);
                            System.out.println("Group members: ");
                            for (String member : theForum.groupMembers(group)) {
                                System.out.println(member);
                            }
                            System.out.println("");
                        }
                        break;
                    /**
                     * 4: Post a thread
                     * 5: Post a reply
                     */
                    case 4:
                    case 5:
                        // Inform user if no viewable threads exist
                        if (selection == 5 && theForum.getThreads(loggedinUser).size() == 0) {
                            System.out.println("No threads found. Try creating a thread first.");
                        } else {
                            int selectedThread;
                            // Make a new thread if option is 4
                            if (selection == 4) {
                                System.out.println("Enter a title for the thread: ");
                                String title = scanner.nextLine();
                                selectedThread = theForum.createThread(Main.loggedinUser, title);
                                if (theForum.getGroups(loggedinUser).length > 0) {
                                    System.out.println("Add thread to a group? (y/n): ");
                                    if (scanner.nextLine().equalsIgnoreCase("y")) {
                                        String group = MainMenu.selectGroup("Select a group: ", true);
                                        theForum.addThreadToGroup(selectedThread, group);
                                    }
                                }
                            // Select a thread if option is 5
                            } else {
                                selectedThread = selectThread("Select a thread to reply to: ");
                            }

                            // Show all replies of the selected thread
                            for (String r : theForum.getReplies(selectedThread)) {
                                System.out.println(r);
                            }

                            // Post a reply
                            System.out.println("Enter the content body: ");
                            String content = scanner.nextLine();
                            int new_reply_id = theForum.postReply(selectedThread, Main.loggedinUser, content);

                            // Post attachments to the reply
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
                    System.err.println("Rollback Error: " + e.getMessage());
                    System.exit(1);
                }
                System.err.println("SQL Exception in menu: " + e.getMessage());
            }
            return selection;
        }
    
        private static String selectUser(String prompt) throws SQLException {
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

        private static String selectGroup(String prompt, boolean userGroups) throws SQLException {
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

        private static int selectThread(String prompt) throws SQLException {
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


