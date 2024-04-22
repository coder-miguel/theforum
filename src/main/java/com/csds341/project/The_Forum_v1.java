/**
 * Project: CSDS 341 Project Group 6
 * Database: The Forum (Methods)
 * Version: 1.0
 * Authors:
 *  - Tola Oshomoji (tdo18)
 * User Manual:
 * This program provides uses a command-line interface to interact with the Forum database.
 * It allows users to perform various operations such as creating a new user, logging in, creating/joining groups, 
 * creating threads, posting replies, and viewing available threads and group members.
 * 
 * Usage:
 * 1. When prompted, type 'New' if you are a new user or 'Existing' if you already have an account.
 * 2. If you're a new user, you'll be prompted to create a username and password. Ensure your username is unique and your password is between 3 and 16 characters long.
 * 3. If you're an existing user, enter your username and password to log in.
 * 4. After logging in, you'll be presented with a menu of options to choose from:
 *    - Create a new group
 *    - Join a group
 *    - Create a thread
 *    - Post a thread
 *    - Post a reply
 *    - See available threads in the user's group
 *    - See group members
 * 5. Follow the on-screen prompts to perform your desired operation.
 * 6. At any time, type the corresponding option number to select an action from the menu.
 * 
 * Sql Files Needed to be run before java code:
 * 1.theforum.sql
 * 2.storedprocedure1.sql
 * 3.storedprocedure2.sql
 * 4.trigger.sql
 * 
 * Run the Main.java file once before running this forum
 * That file is where the sql files above will be able to read and executed
 */
package com.csds341.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.sql.ResultSet;

public class The_Forum_v1 {
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    private static String username, password, group_name;
    private static int reply_id;
    private static String connectionUrl;
    private static boolean go2 = true;
    private static boolean go = true;
    private static Connection connection;

    public static void main(String[] args) {
        Properties prop = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES)) {
            prop.load(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionUrl = "jdbc:sqlserver://" + prop.getProperty("databaseServer") + "\\" + prop.getProperty("networkId")
                + ";"
                + "databaseName=theforum;"
                + "user=sa;"
                + "password=" + prop.getProperty("saPassword") + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=15;";
        // This is the beginning of the user interface
        Scanner myObj = new Scanner(System.in);
        System.out.println("Welcome to The Forum. ");
        System.out.println("Type 'New' if you are new or 'Existing' if you alreay have an account : ");
        while (go) {
            String tracker = myObj.nextLine();
            if (tracker.toLowerCase().equals("new")) {
                System.out.println("Create a usernmame: ");
                while (checkifValidUsername(myObj.nextLine())) {
                    System.out.println("Try again, Invaild input. ");
                }
                System.out.println("Valid username. ");
                System.out.println("Create a password: ");
                while (checkifValidPassword(myObj.nextLine())) {
                    System.out.println("Try again, Invaild password. ");
                }
                System.out.println("Valid password. ");
                addnewUser(username, password);
                go = false;
            } else if (tracker.toLowerCase().equals("existing")) {
                System.out.println("Enter your username: ");
                while (!checkifValidUsername(myObj.nextLine())) {
                    System.out.println("Try again, Invaild username. ");
                }
                System.out.println("Enter your password: ");
                while (!checkifValidPasswordE(myObj.nextLine())) {
                    System.out.println("Try again, Invaild password. ");
                }
                System.out.println("Successful Login");
                go = false;
            } else {
                System.out.println("Try again, Invaild input. ");
                System.out.println("Type 'New' if you are new or 'Existing' if you alreay have an account : ");
            }
        }

        // This is the main menu for the user to select from
        delay(1000);
        System.out.println("Enter a number that corresponds to your choice of action: ");
        System.out.println("1. Create a new group");
        System.out.println("2. Join a group");
        System.out.println("3. Create a thread");
        System.out.println("4. Post a thread");
        System.out.println("5. Post a reply");
        System.out.println("6. See available threads in the user's group");
        System.out.println("7. See group members");
        int option = myObj.nextInt();
        myObj.nextLine();
        switch (option) {
            case 1:
                System.out.println("Enter the name of the group: ");
                while (checkifValidGroup(myObj.nextLine())) {
                    System.out.println("Try again, Group Name already exists.");
                }
                System.out.println("valid group name.");
                createGroup(group_name, username);
                break;
            case 2:
                System.out.println("Available Groups in The Forum: ");
                allavailableGroups();
                System.out.println("Enter the name of the group: ");
                while (!checkifValidGroup(myObj.nextLine())) {
                    System.out.println("Try again, group doesn't exists.");
                }
                joinGroup(username, group_name);
                break;
            case 3:
                System.out.println("Enter the title of the thread:");
                createThread(myObj.nextLine());
                break;
            case 4:
                while (go2) {
                    System.out.println("Enter the ID of your thread: ");
                    int thread_id = myObj.nextInt();
                    myObj.nextLine();
                    System.out.println("Enter the name of the group you want to add your thread to : ");
                    checkpostThread(thread_id, myObj.nextLine());
                }
                break;
            case 5:
                System.out.println("Enter the thread id: ");
                while (!checkthreadIDforReply(myObj.nextInt())) {
                    System.out.println("Try again, thread id doesn't exists.");
                }
                myObj.nextLine();
                System.out.println("Enter Content: ");
                String content = myObj.nextLine();
                System.out.println("Do you want to add an attachment? (Y/N)");
                String track4 = myObj.nextLine();
                if (track4.toLowerCase().equals("y") || track4.toLowerCase().equals("yes")) {
                    postReply(content);
                    System.out.println("Name of attachment: ");
                    String aname = myObj.nextLine();
                    System.out.println("Enter the information of the attachment:");
                    String ainfo = myObj.nextLine();
                    System.out.println("Enter the attachment in varbinary form:");
                    byte[] abinary = myObj.nextLine().getBytes();
                    createAttachment(aname, ainfo, abinary);
                } else {
                    System.out.println("No attachment added.");
                    postReply(content);
                }
                break;
            case 6:
                System.out.println("Enter the name of the group: ");
                String track = myObj.nextLine();
                while (!checkifInGroup(track)) {
                    System.out.println("Try again, you are not in this group.");
                    track = myObj.nextLine();
                }
                seeAvailableThreads(track);
                break;
            case 7:
                System.out.println("Enter the name of the group: ");
                String track2 = myObj.nextLine();
                while (!checkifInGroup(track2)) {
                    System.out.println("Try again, you are not in this group.");
                    track2 = myObj.nextLine();
                }
                seeGroupMembers(track2);
                break;
            default:
                System.out.println("Invalid input.");
                break;
        }

        myObj.close();
    }

    // Method that creates an attachment to a reply
    public static void createAttachment(String aname, String ainfo, byte[] abinary) {
        String inputsql = "INSERT INTO Attachment (reply_id, name, metadata, data) VALUES (?, ?, ?, ?);";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setInt(1, reply_id);
            prepstest.setString(2, aname);
            prepstest.setString(3, ainfo);
            prepstest.setBytes(4, abinary);
            connection.setAutoCommit(false);
            prepstest.executeUpdate();
            connection.commit();
            System.out.println("Attachment has been added to the Thread.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    // A method that posts a reply to a thread
    public static void postReply(String content) {
        ResultSet resultSet = null;
        String inputsql = "INSERT INTO Reply (thread_id, username, content, date_created) VALUES (?, ?, ?, GETDATE());";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setInt(1, reply_id);
            prepstest.setString(2, username);
            prepstest.setString(3, content);
            connection.setAutoCommit(false);
            prepstest.executeUpdate();
            resultSet = prepstest.getGeneratedKeys();
            while (resultSet.next()) {
                reply_id = resultSet.getInt(1);
            }
            connection.commit();
            System.out.println("Reply has been added to the thread.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    // A method that checks if the thread id is valid for a reply
    public static boolean checkthreadIDforReply(int thread_id_i) {
        reply_id = thread_id_i;
        String inputsql = "select * from (Select thread_id from ThreadGroup where group_name in (select group_name from usergroup where username = ? )) as I where I.thread_id = ?;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, username);
            prepstest.setInt(2, thread_id_i);
            connection.setAutoCommit(false);
            boolean resultSetNotEmpty = prepstest.executeQuery().next();
            return resultSetNotEmpty;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // A method that displays the group members for a given group and user
    public static void seeGroupMembers(String group_name) {
        int x = 1;
        String inputsql = "SELECT username FROM UserGroup WHERE group_name = ?;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, group_name);
            connection.setAutoCommit(false);
            ResultSet rs = prepstest.executeQuery();
            System.out.println("Group Members:");
            while (rs.next()) {
                System.out.println("" + x + " ) " + rs.getString("username"));
                x = x + 1;
            }
            delay(600);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // A method that checks if a user is in a group
    public static boolean checkifInGroup(String group_name) {
        String inputsql = "SELECT username FROM UserGroup WHERE username = ? and group_name = ?;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, username);
            prepstest.setString(2, group_name);
            connection.setAutoCommit(false);
            boolean resultSetNotEmpty = prepstest.executeQuery().next();
            return resultSetNotEmpty;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // A method that checks if a thread is valid for a group
    public static boolean checkpostThread(int thread_id, String group_name) {
        String inputsql = "SELECT id FROM Thread WHERE id = ? and username in (Select username from UserGroup where username = ? and group_name = ?);";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setInt(1, thread_id);
            prepstest.setString(2, username);
            prepstest.setString(3, group_name);
            connection.setAutoCommit(false);
            boolean resultSetNotEmpty = prepstest.executeQuery().next();
            if (resultSetNotEmpty) {
                System.out.println("Thread ID: " + thread_id + " is valid.");
                addThreadToGroup(thread_id, group_name);
                go2 = false;
            } else {
                System.out
                        .println("Thread ID: " + thread_id + " is invalid, or you are not in the group: " + group_name);
            }
            return resultSetNotEmpty;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // A method that adds a thread to a group
    public static void addThreadToGroup(int thread_id, String group_name) {
        String inputsql = "INSERT INTO ThreadGroup (thread_id, group_name) VALUES (?, ?);";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setInt(1, thread_id);
            prepstest.setString(2, group_name);
            connection.setAutoCommit(false);
            prepstest.executeUpdate();
            connection.commit();
            System.out.println("Thread ID: " + thread_id + " has been added to the group: " + group_name);
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("This thread is already in the group.");
            e.printStackTrace();
        }
    }

    // A method that creates a thread
    public static void createThread(String title) {
        String inputsql = "INSERT INTO Thread values (?, ?, GETDATE());";
        ResultSet resultSet = null;
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, username);
            prepstest.setString(2, title);
            connection.setAutoCommit(false);
            prepstest.executeUpdate();
            resultSet = prepstest.getGeneratedKeys();
            while (resultSet.next()) {
                System.out.println("Your Thread ID is:  " +
                        resultSet.getString(1));
            }
            connection.commit();
            System.out.println("Thread Title: " + title + " has been added to the database.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    // A method that adds a new user to the database
    public static void addnewUser(String username, String password) {
        String inputsql = "INSERT INTO ForumUser (username, password, date_created) VALUES (?, ?, GETDATE());";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, username);
            prepstest.setString(2, password);
            connection.setAutoCommit(false);
            prepstest.executeUpdate();
            connection.commit();
            System.out.println("Username: " + username + " Password: " + password + " has been added to the database.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    // A method that was created to delay the program so the menu doesn't appear too
    // quickly
    public static void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // A method that displays all available groups
    public static void allavailableGroups() {
        String inputsql = "SELECT name FROM ForumGroup;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            connection.setAutoCommit(false);
            ResultSet rs = prepstest.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // A method that checks if a group is valid
    public static boolean checkifValidGroup(String group_name_i) {
        if (group_name_i.length() < 1) {
            return false;
        }
        group_name = group_name_i;
        String inputsql = "SELECT name FROM ForumGroup WHERE name = ?;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, group_name_i);
            connection.setAutoCommit(false);
            boolean resultSetNotEmpty = prepstest.executeQuery().next();
            return resultSetNotEmpty;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // A method that creates a group uses a stored procedure to accomplish this
    public static void createGroup(String name, String owner_name) {
        String calledStoredProc = "{call dbo.insertCreateGroup(?,?)}";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                CallableStatement prepstest = connection.prepareCall(calledStoredProc);) {
            prepstest.setString(1, name);
            prepstest.setString(2, owner_name);
            connection.setAutoCommit(false);
            prepstest.execute();
            connection.commit();
            System.out
                    .println("Group Name: " + name + " Owner Name: " + owner_name + " has been added to the database.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    // A method that inserts a user into a group using a stored procedure
    public static void joinGroup(String username, String group_name) {
        String calledStoredProc = "{call dbo.insertUserGroup(?,?)}";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                CallableStatement prepstest = connection.prepareCall(calledStoredProc);) {
            prepstest.setString(1, username);
            prepstest.setString(2, group_name);
            connection.setAutoCommit(false);
            prepstest.execute();
            connection.commit();
            System.out.println(
                    "Username: " + username + " Group Name: " + group_name + " has been added to the database.");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("You are already in this group. ");
            delay(200);
        }
    }

    // A method that displays all available threads in a group for a given user
    public static void seeAvailableThreads(String group_name) {
        Boolean noThread = true;
        String inputsql = "SELECT * FROM Thread WHERE id in (Select thread_id from ThreadGroup where group_name = ?);";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, group_name);
            connection.setAutoCommit(false);
            ResultSet rs = prepstest.executeQuery();
            while (rs.next()) {
                noThread = false;
                System.out.println("Thread ID: " + rs.getString("id") + " Username: " + rs.getString("username")
                        + " Title: " + rs.getString("title") + " Date Created: " + rs.getString("date_created"));
            }
            if (noThread) {
                System.out.println("There is currently no threads for you to reply to.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // A method that checks if a username is valid
    public static boolean checkifValidUsername(String username_i) {
        username = username_i;
        String inputsql = "SELECT username FROM ForumUser WHERE username = ?;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, username_i);
            connection.setAutoCommit(false);
            boolean resultSetNotEmpty = prepstest.executeQuery().next();
            return resultSetNotEmpty;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // A method that checks if a password is valid for a new user
    public static boolean checkifValidPassword(String password_i) {
        if (password_i.length() < 3 || password_i.length() > 16) {
            return true;
        } else {
            password = password_i;
            return false;
        }
    }

    // A method that checks if a password is valid for an existing user
    public static boolean checkifValidPasswordE(String password_i) {
        password = password_i;
        String inputsql = "SELECT * FROM ForumUser WHERE username = ? and password = ?;";
        try (Connection connection = DriverManager.getConnection(connectionUrl);
                PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);) {
            prepstest.setString(1, username);
            prepstest.setString(2, password);
            connection.setAutoCommit(false);
            boolean resultSetNotEmpty = prepstest.executeQuery().next();
            return resultSetNotEmpty;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
