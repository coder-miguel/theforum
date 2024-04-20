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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;
import java.sql.ResultSet;

public class Method {
    private static final String CONNECTION_PROPERTIES = "connection.properties";
    private static String username, password;
    private static String connectionUrl;
    private static Connection conn;

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
                + "databaseName=testforTola;"
                + "user=sa;"
                + "password=" + prop.getProperty("saPassword") + ";"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=15;";
        Scanner myObj = new Scanner(System.in);
        String inpDeptName, inpName;
        float inpCred;
        System.out.println("Type 'New' if you are new or 'Existing' if you alreay have an account : ");
        if (myObj.nextLine().toLowerCase().equals("new")) {
            System.out.println("Create a usernmame: ");
            while (checkifValidUsername(myObj.nextLine())){
                System.out.println("Try again, Invaild input. ");
            }
            System.out.println("Valid username. ");
            System.out.println("Create a password: ");
            while (checkifValidPassword(myObj.nextLine())) {
                System.out.println("Try again, Invaild password. ");
            }
            System.out.println("Valid password. ");
            addnewUser(username,password);
        } else if (myObj.nextLine().toLowerCase().equals("existing")) {

        } else {
            System.out.println("Try again, Invaild input. ");
            System.out.println("Type 'New' if you are new or 'Existing' if you alreay have an account : ");
        }

        // Asks the user to pick between these options
        myObj.close();
    }

    // High level methods for now until the interface is made
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
            e.printStackTrace();
        }
    }

    public void createGroup(String name, String owner_name) {

    }

    public void joinGroup(String username, String group_name) {

    }

    public void postThread(String username, String title) {

    }

    public void seeAvailableThreads(int thread_id, String group_name) {

    }

    public void seegroupMemebers(String group_name) {

    }

    public static boolean checkifValidUsername(String username_i) {
    String inputsql = "SELECT username FROM ForumUser WHERE username = ?;";
    try(
    Connection connection = DriverManager.getConnection(connectionUrl);
    PreparedStatement prepstest = connection.prepareStatement(inputsql, Statement.RETURN_GENERATED_KEYS);)
    {
        prepstest.setString(1, username_i);
        connection.setAutoCommit(false);
        boolean resultSetNotEmpty = prepstest.executeQuery().next();
        if (resultSetNotEmpty){
             
        }
        else{
            username = username_i;
        }
        return resultSetNotEmpty;
    }catch(
    SQLException e)
    {
        e.printStackTrace();
        return false;
    }
  }

  public static boolean checkifValidPassword(String password_i) {
    if (password_i.length() < 3 || password_i.length() > 16){
        return true;
    } else {
        password = password_i;
        return false;
    }
  }
}
