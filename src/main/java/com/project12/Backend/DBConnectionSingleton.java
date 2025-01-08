/*
 * Utility class for managing database connections and operations.
 * This class provides methods to read from, write to, and update a MySQL database.
 * It uses a singleton pattern to ensure only one instance of the database connection exists.
 */
package com.project12.Backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBConnectionSingleton {

    // Database connection details
    private final String url = "jdbc:mysql://localhost:3306/GTFS";
    private final String user = "root";
    private final String password = "";

    // Singleton instance of the database connection
    private static final DBConnectionSingleton dbConnection = new DBConnectionSingleton();

    /**
     * Private constructor to prevent instantiation.
     * Initializes the DBConnection instance.
     */
    public DBConnectionSingleton() {}

    /**
     * Executes a query to read data from the database.
     *
     * @param query The SQL query to execute.
     * @return A list of strings, where each string represents a row of the result set.
     */
    public List<String> readFromDB(String query) {
        List<String> res = new ArrayList<>();
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish a database connection
            Connection connection = DriverManager.getConnection(url, user, password);

            // Create a statement for executing the query
            Statement statement = connection.createStatement();

            // Execute the query
            ResultSet rs = statement.executeQuery(query);

            // Get the metadata of the result set
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();

            // Iterate over the result set and construct rows of data
            while (rs.next()) {
                String row = "";
                for (int i = 1; i <= columnCount; i++) {
                    row += rs.getString(i) + " ";
                }
                res.add(row);
            }

            // Close the connection
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Returns the singleton instance of the DBConnection class.
     *
     * @return The singleton instance of DBConnection.
     */
    public static DBConnectionSingleton getDbConnection() {
        return dbConnection;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
