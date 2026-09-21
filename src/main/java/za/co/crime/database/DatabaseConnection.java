package za.co.crime.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:database/crime.db";

        return DriverManager.getConnection(url);
    }
}