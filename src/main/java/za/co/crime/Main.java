package za.co.crime;

import za.co.crime.database.DatabaseConnection;
import za.co.crime.database.SchemaInitializer;
import za.co.crime.extraction.CsvCrimeReader;
import za.co.crime.loading.CrimeRepository;
import za.co.crime.model.CrimeEvent;
import za.co.crime.model.CrimeRecord;
import za.co.crime.transformation.CrimeTransformer;
import za.co.crime.validation.CrimeValidator;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String csvPath = System.getenv().getOrDefault(
                "CRIME_CSV",
                "data/raw/sapacr-2005-2026-v1.4.csv"
        );

        try {

            System.out.println("================================");
            System.out.println(" SOUTH AFRICAN CRIME ETL");
            System.out.println("================================");


            // =========================================
            // 1. EXTRACT
            // =========================================

            System.out.println("\n[1] Extracting CSV...");

            CsvCrimeReader reader =
                    new CsvCrimeReader();

            List<CrimeRecord> rawRecords =
                    reader.read(Path.of(csvPath));

            System.out.println(
                    "Rows extracted: "
                            + rawRecords.size()
            );


            // =========================================
            // 2. TRANSFORM
            // =========================================

            System.out.println(
                    "\n[2] Transforming data..."
            );

            CrimeValidator validator =
                    new CrimeValidator();

            CrimeTransformer transformer =
                    new CrimeTransformer();

            List<CrimeEvent> events =
                    transformer.transform(
                            rawRecords,
                            validator
                    );

            System.out.println(
                    "Crime events created: "
                            + events.size()
            );


            // =========================================
            // 3. CONNECT TO SQLITE
            // =========================================

            System.out.println(
                    "\n[3] Connecting to SQLite..."
            );

            try (
                    Connection connection =
                            DatabaseConnection
                                    .getConnection()
            ) {

                System.out.println(
                        "SQLite connected successfully."
                );


                // =========================================
                // 4. CREATE SCHEMA
                // =========================================

                System.out.println(
                        "\n[4] Creating warehouse schema..."
                );

                SchemaInitializer schema =
                        new SchemaInitializer();

                schema.initialize(connection);

                System.out.println(
                        "Schema ready."
                );


                // =========================================
                // 5. LOAD
                // =========================================

                System.out.println(
                        "\n[5] Loading data..."
                );

                CrimeRepository repository =
                        new CrimeRepository(connection);

                repository.load(events);
            }


            // =========================================
            // COMPLETE
            // =========================================

            System.out.println(
                    "\n================================"
            );

            System.out.println(
                    " ETL COMPLETED SUCCESSFULLY"
            );

            System.out.println(
                    "================================"
            );

            System.out.println(
                    "\nDatabase created at:"
            );

            System.out.println(
                    "database/crime.db"
            );


        } catch (Exception e) {

            System.err.println(
                    "\nETL FAILED:"
            );

            System.err.println(
                    e.getMessage()
            );

            e.printStackTrace();

            System.exit(1);
        }
    }
}