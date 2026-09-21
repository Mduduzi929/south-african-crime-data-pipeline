package za.co.crime.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public void initialize(Connection connection) throws SQLException {

        // Enable foreign key enforcement in SQLite
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        String sql = """
            CREATE TABLE IF NOT EXISTS dim_year (
                year_id INTEGER PRIMARY KEY AUTOINCREMENT,
                year_label TEXT NOT NULL UNIQUE
            );

            CREATE TABLE IF NOT EXISTS dim_location (
                location_id INTEGER PRIMARY KEY AUTOINCREMENT,
                station TEXT NOT NULL,
                municipality TEXT,
                district TEXT,
                longitude REAL,
                latitude REAL,
                UNIQUE(station, municipality, district)
            );

            CREATE TABLE IF NOT EXISTS dim_crime (
                crime_id INTEGER PRIMARY KEY AUTOINCREMENT,
                crime_code TEXT NOT NULL UNIQUE
            );

            CREATE TABLE IF NOT EXISTS fact_crime (
                fact_id INTEGER PRIMARY KEY AUTOINCREMENT,
                year_id INTEGER NOT NULL,
                location_id INTEGER NOT NULL,
                crime_id INTEGER NOT NULL,
                incident_count INTEGER,
                source_row_number INTEGER NOT NULL,
                source_status TEXT NOT NULL,

                UNIQUE(
                    year_id,
                    location_id,
                    crime_id,
                    source_row_number
                ),

                FOREIGN KEY (year_id)
                    REFERENCES dim_year(year_id),

                FOREIGN KEY (location_id)
                    REFERENCES dim_location(location_id),

                FOREIGN KEY (crime_id)
                    REFERENCES dim_crime(crime_id)
            );

            CREATE TABLE IF NOT EXISTS data_quality_issue (
                issue_id INTEGER PRIMARY KEY AUTOINCREMENT,
                source_row_number INTEGER NOT NULL,
                field_name TEXT NOT NULL,
                issue_type TEXT NOT NULL,
                original_value TEXT
            );

            CREATE INDEX IF NOT EXISTS idx_fact_year
                ON fact_crime(year_id);

            CREATE INDEX IF NOT EXISTS idx_fact_location
                ON fact_crime(location_id);

            CREATE INDEX IF NOT EXISTS idx_fact_crime
                ON fact_crime(crime_id);
            """;

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}