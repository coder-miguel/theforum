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

            do {
                /*
                 * Login
                 */
                do {
                    LoginMenu.show();
                    loggedinUser = LoginMenu.login();
                    if (loggedinUser == null)
                        System.exit(0);
                } while (!theForum.userExists(loggedinUser));

                /*
                 * Menu Loop
                 */
                do {
                    MainMenu.show();
                } while (MainMenu.select() != MainMenu.EXIT);
                loggedinUser = null;
            } while (true);

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

            // loop until a valid selection is made
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
                    break;
                case 2:
                    user = newUser();
                    break;
                default:
                    break;
                }
                return user;
        }

        /**
         * Prompts the user to select an existing user and login.
         * @return
         * <ul>
         *   <li>the selected username if login successful</li>
         *   <li>an empty string if login unsuccessful</li>
         *   <li>null if error or exit</li>
         * </ul>
         */
        static String existingUser() {
            String loginAttempt = null;
            int attemtps = 0;
            try {
                if (theForum.getUsers().length == 0) {
                    System.out.println("No users found. Please create a user first.");
                } else {

                    // If users exist, prompt user to select one
                    loginAttempt = MainMenu.selectUser("Select a user to log in as: ");
                    if (loginAttempt == null) {
                        return "";
                    }

                    // Validate password for selected user
                    System.out.println("Enter in your password: ");
                    String password = scanner.nextLine();
                    while (!theForum.validatePassword(password, loginAttempt)) {
                        attemtps++;
                        // If too many attempts, exit
                        if (attemtps >= 3) {
                            System.out.println("Too many attempts. Please try again later.");
                            return "";
                        }
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
            String newUser = null;
            System.out.println("Enter in your new username (leave blank to go back): ");
            String username = scanner.nextLine();
            try {
                // Loop until a valid username is entered
                while(true) {
                    if (username.length() == 0)
                        return "";
                    else if (theForum.userExists(username))
                        System.out.println("Username already exists. Try again.");
                    else if (!theForum.goodUsername(username))
                        System.out.println("Username must be between 3 and 16 characters.");
                    else
                        break;
                    username = scanner.nextLine();
                }
                String password = "";
                // Loop until a valid password is entered
                while(!Database.goodPassword(password)) {
                    System.out.println("Enter in your new password: ");
                    password = scanner.nextLine();
                }

                // Add the new user to the database
                theForum.addnewUser(username, password);
                System.out.println("You are now logged in as " + username + ".");
                newUser = username;
                theForum.getConn().commit();
            
            // Rollback if there was an error
            } catch (SQLException e) {
                try {
                    theForum.getConn().rollback();
                } catch (SQLException s) {
                    System.err.println("Error rolling back new user: " + s.getMessage());
                }
                newUser = null;
                System.err.println("Error creating new user: " + e.getMessage());
                System.out.println("Please try again.");
            }
            return newUser;
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

                0) Logout
                1) Create a new group
                2) Join a group
                3) Group members
                4) Start a thread
                5) Reply to a thread
                6) Browse threads

                """);
        }

        /**
         * Select
         * Prompts the user to select an option from the main menu.
         * The selected option is then executed.
         * @return the selected option.
         */
        static int select() {

            // Loop until a valid option is selected
            int selection = -1;
            while (selection < 0 || selection > 6) {
                try {
                    selection = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    selection = -1;
                    System.out.println("Invalid input. Please enter a number between 0 and 6.");
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
                            String group = selectGroup("Select a group: ", false);
                            if (group != null) {
                                theForum.joinGroup(group, loggedinUser);
                                theForum.getConn().commit();
                            }
                        }
                        break;
                        
                    /**
                     * See group members
                     */
                    case 3:
                        if (theForum.getGroups(loggedinUser).length == 0) {
                            System.out.println("Not in a group. Try creating or joining a group first.");
                        } else {
                            String group = selectGroup("Select a group: ", true);
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
                     * 6: View threads
                     */
                    case 4:
                    case 5:
                    case 6:
                        // Inform user if no viewable threads exist
                        if (selection > 4 && theForum.getThreads(loggedinUser).size() == 0) {
                            System.out.println("No threads found. Try creating a thread first.");
                        } else {
                            int selectedThread;

                            // Make a new thread if seledction is 4
                            if (selection == 4) {
                                System.out.println("Enter a title for the thread: ");
                                String title = scanner.nextLine();
                                if (title.length() == 0) title = "[No title]";
                                selectedThread = theForum.createThread(loggedinUser, title);
                                if (theForum.getGroups(loggedinUser).length > 0) {
                                    boolean addingGroup = false;
                                    do {
                                        System.out.println("Post thread to a group? y/(n): ");
                                        String yn = scanner.nextLine();
                                        if (yn.length() > 0 && String.valueOf(yn.charAt(0)).equalsIgnoreCase("y")) {
                                            addingGroup = true;
                                            String group = MainMenu.selectGroup("Select a group: ", true);
                                            if (group != null) {
                                                theForum.addThreadToGroup(selectedThread, group);
                                                addingGroup = false;
                                            }
                                        } else {
                                            addingGroup = false;
                                        }
                                    } while (addingGroup);
                                }
                            // Select a thread if replying to an existing one
                            } else {
                                selectedThread = selectThread("View a thread: ");
                            }

                            if (selectedThread == 0) return selection;
                            
                            // Show all replies of the selected thread
                            for (String r : theForum.getReplies(selectedThread)) {
                                System.out.println(r);
                            }

                            if (selection == 6) return selection;

                            // Post a reply
                            System.out.println("Enter the content body: ");
                            String content = scanner.nextLine();
                            if (content.length() == 0) content = "[Empty]";
                            int new_reply_id = theForum.postReply(selectedThread, loggedinUser, content);

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
                                if (name.length() == 0)     name = "attachment" + i;
                                if (metadata.length() == 0) metadata = "binary";
                                theForum.createAttachment(new_reply_id, name, metadata, data);
                            }

                            String[] replies = theForum.getReplies(selectedThread);
                            System.out.println(replies[replies.length - 1]);

                            System.out.println("Post Reply? y/(n): ");
                            String yn = scanner.nextLine();
                            if (yn.length() > 0 && String.valueOf(yn.charAt(0)).equalsIgnoreCase("y"))
                                theForum.getConn().commit();
                            else
                                theForum.getConn().rollback();
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
            System.out.println("0) Go Back");
            for (int i = 0; i < users.length; i++) {
                System.out.println(String.valueOf(1 + i) + ") " + users[i]);
            }
            int selectedUser = -1;
            try {
                selectedUser = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                selectedUser = -1;
            }
            while (selectedUser < 0 || selectedUser > users.length) {
                System.out.println("Invalid input. Please enter a number between 0 and " + (users.length));
                try {
                    selectedUser = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    selectedUser = -1;
                }
            }
            if (selectedUser == 0) {
                return null;
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
            System.out.println("0) Go Back");
            for (int i = 0; i < groups.length; i++) {
                System.out.println(String.valueOf(1 + i) + ") " + groups[i]);
            }
            int selectedGroup = -1;
            try {
                selectedGroup = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                selectedGroup = -1;
            }
            while (selectedGroup < 0 || selectedGroup > groups.length) {
                System.out.println("Invalid input. Please enter a number between 0 and " + (groups.length));
                try {
                    selectedGroup = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    selectedGroup = -1;
                }
            }
            if (selectedGroup == 0) {
                return null;
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
            System.out.println("0) Go Back");
            for (HashMap.Entry<Integer, String> t : threads.entrySet()) {
                System.out.println(String.valueOf(t.getKey()) + ") " + t.getValue());
            }
            int selectedThread = -1;
            try {
                selectedThread = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                selectedThread = -1;
            }
            while (!threads.keySet().contains(selectedThread) && selectedThread != 0) {
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


