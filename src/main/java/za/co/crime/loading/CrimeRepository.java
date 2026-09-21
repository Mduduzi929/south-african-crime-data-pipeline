package za.co.crime.loading;

import za.co.crime.model.CrimeEvent;

import java.sql.*;
import java.util.List;

public class CrimeRepository {

    private final Connection connection;

    public CrimeRepository(Connection connection) {
        this.connection = connection;
    }

    public void load(List<CrimeEvent> events) throws SQLException {

        boolean previousAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            String yearSql = """
                    INSERT INTO dim_year(year_label)
                    VALUES (?)
                    ON CONFLICT(year_label) DO NOTHING
                    """;

            String locationSql = """
                    INSERT INTO dim_location
                    (station, municipality, district, longitude, latitude)
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT(station, municipality, district) DO NOTHING
                    """;

            String crimeSql = """
                    INSERT INTO dim_crime(crime_code)
                    VALUES (?)
                    ON CONFLICT(crime_code) DO NOTHING
                    """;

            String factSql = """
                    INSERT INTO fact_crime
                    (
                        year_id,
                        location_id,
                        crime_id,
                        incident_count,
                        source_row_number,
                        source_status
                    )
                    VALUES (?, ?, ?, ?, ?, ?)

                    ON CONFLICT(
                        year_id,
                        location_id,
                        crime_id,
                        source_row_number
                    )
                    DO UPDATE SET
                        incident_count = excluded.incident_count,
                        source_status = excluded.source_status
                    """;

            try (
                    PreparedStatement yearStatement =
                            connection.prepareStatement(yearSql);

                    PreparedStatement locationStatement =
                            connection.prepareStatement(locationSql);

                    PreparedStatement crimeStatement =
                            connection.prepareStatement(crimeSql);

                    PreparedStatement factStatement =
                            connection.prepareStatement(factSql)
            ) {

                int count = 0;

                for (CrimeEvent event : events) {

                    // -------------------------
                    // YEAR
                    // -------------------------

                    insertYear(yearStatement, event.getYear());

                    int yearId =
                            findYearId(event.getYear());


                    // -------------------------
                    // LOCATION
                    // -------------------------

                    insertLocation(locationStatement, event);

                    int locationId =
                            findLocationId(event);


                    // -------------------------
                    // CRIME TYPE
                    // -------------------------

                    insertCrime(
                            crimeStatement,
                            event.getCrimeType()
                    );

                    int crimeId =
                            findCrimeId(event.getCrimeType());


                    // -------------------------
                    // FACT TABLE
                    // -------------------------

                    factStatement.setInt(1, yearId);
                    factStatement.setInt(2, locationId);
                    factStatement.setInt(3, crimeId);

                    if (event.getIncidentCount() == null) {
                        factStatement.setNull(
                                4,
                                Types.INTEGER
                        );
                    } else {
                        factStatement.setInt(
                                4,
                                event.getIncidentCount()
                        );
                    }

                    factStatement.setInt(
                            5,
                            event.getSourceRowNumber()
                    );

                    factStatement.setString(
                            6,
                            event.getSourceStatus()
                    );

                    factStatement.addBatch();

                    count++;

                    if (count % 1000 == 0) {
                        factStatement.executeBatch();

                        System.out.println(
                                "Loaded rows: " + count
                        );
                    }
                }

                factStatement.executeBatch();
            }

            connection.commit();

        } catch (SQLException | RuntimeException e) {

            connection.rollback();

            throw e;

        } finally {

            connection.setAutoCommit(
                    previousAutoCommit
            );
        }
    }


    private void insertYear(
            PreparedStatement statement,
            String year
    ) throws SQLException {

        statement.setString(1, year);

        statement.executeUpdate();
    }


    private void insertLocation(
            PreparedStatement statement,
            CrimeEvent event
    ) throws SQLException {

        statement.setString(
                1,
                event.getStation()
        );

        if (event.getMunicipality() == null) {
            statement.setNull(2, Types.VARCHAR);
        } else {
            statement.setString(
                    2,
                    event.getMunicipality()
            );
        }

        if (event.getDistrict() == null) {
            statement.setNull(3, Types.VARCHAR);
        } else {
            statement.setString(
                    3,
                    event.getDistrict()
            );
        }

        if (event.getLongitude() == null) {
            statement.setNull(4, Types.DOUBLE);
        } else {
            statement.setDouble(
                    4,
                    event.getLongitude()
            );
        }

        if (event.getLatitude() == null) {
            statement.setNull(5, Types.DOUBLE);
        } else {
            statement.setDouble(
                    5,
                    event.getLatitude()
            );
        }

        statement.executeUpdate();
    }


    private void insertCrime(
            PreparedStatement statement,
            String crimeType
    ) throws SQLException {

        statement.setString(
                1,
                crimeType
        );

        statement.executeUpdate();
    }


    private int findYearId(
            String year
    ) throws SQLException {

        String sql = """
                SELECT year_id
                FROM dim_year
                WHERE year_label = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, year);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {
                    throw new SQLException(
                            "Year not found: " + year
                    );
                }

                return result.getInt("year_id");
            }
        }
    }


    private int findLocationId(
            CrimeEvent event
    ) throws SQLException {

        /*
         * SQLite needs explicit NULL handling.
         *
         * "=" does not match NULL.
         *
         * Therefore we use:
         *
         * column = ?
         * OR (column IS NULL AND ? IS NULL)
         */

        String sql = """
                SELECT location_id
                FROM dim_location
                WHERE station = ?

                  AND (
                        municipality = ?
                        OR (
                            municipality IS NULL
                            AND ? IS NULL
                        )
                  )

                  AND (
                        district = ?
                        OR (
                            district IS NULL
                            AND ? IS NULL
                        )
                  )
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    event.getStation()
            );

            statement.setString(
                    2,
                    event.getMunicipality()
            );

            statement.setString(
                    3,
                    event.getMunicipality()
            );

            statement.setString(
                    4,
                    event.getDistrict()
            );

            statement.setString(
                    5,
                    event.getDistrict()
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {
                    throw new SQLException(
                            "Location not found: "
                                    + event.getStation()
                    );
                }

                return result.getInt(
                        "location_id"
                );
            }
        }
    }


    private int findCrimeId(
            String crimeType
    ) throws SQLException {

        String sql = """
                SELECT crime_id
                FROM dim_crime
                WHERE crime_code = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    crimeType
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {
                    throw new SQLException(
                            "Crime type not found: "
                                    + crimeType
                    );
                }

                return result.getInt(
                        "crime_id"
                );
            }
        }
    }
}