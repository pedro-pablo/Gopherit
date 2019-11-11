package com.gopherit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String CONNECTION_STRING = "jdbc:sqlite:bookmarks.db";
    private static final String SQL_BOOKMARK_TABLE = "CREATE TABLE IF NOT EXISTS bookmark (" +
            "   id integer NOT NULL PRIMARY KEY," +
            "   host text NOT NULL," +
            "   port integer NOT NULL," +
            "   selector text," +
            "   type integer NOT NULL," +
            "   name text NOT NULL)";

    private static Connection connection;

    private DatabaseConnection() {
    }

    static Connection getInstance() throws SQLException {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            connection = DriverManager.getConnection(CONNECTION_STRING);
        }
        return connection;
    }

    public static void createTables() throws SQLException {
        Connection connection = getInstance();
        Statement statement = connection.createStatement();
        statement.execute(SQL_BOOKMARK_TABLE);
    }

}
