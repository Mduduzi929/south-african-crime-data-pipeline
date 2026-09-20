package za.co.crime.extraction;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import za.co.crime.model.CrimeRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvCrimeReader {

    private static final List<String> REQUIRED_COLUMNS = List.of(
            "year",
            "station",
            "loc_mn",
            "dc_mn",
            "longitude",
            "latitude"
    );

    public List<CrimeRecord> read(Path path) throws IOException {

        List<CrimeRecord> records = new ArrayList<>();

        try (
                Reader reader = Files.newBufferedReader(path);

                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {

            validateHeaders(parser.getHeaderNames());

            for (CSVRecord row : parser) {

                CrimeRecord record = parseRow(row);

                records.add(record);
            }
        }

        return records;
    }

    private CrimeRecord parseRow(CSVRecord row) {

        Map<String, Integer> crimeCounts =
                new LinkedHashMap<>();

        for (String column : row.toMap().keySet()) {

            if (REQUIRED_COLUMNS.contains(column)) {
                continue;
            }

            String value = row.get(column);

            crimeCounts.put(
                    column,
                    parseInteger(value)
            );
        }

        return new CrimeRecord(

                clean(row.get("year")),

                clean(row.get("station")),

                clean(row.get("loc_mn")),

                clean(row.get("dc_mn")),

                parseDouble(row.get("longitude")),

                parseDouble(row.get("latitude")),

                crimeCounts
        );
    }

    private void validateHeaders(List<String> headers) {

        for (String required : REQUIRED_COLUMNS) {

            if (!headers.contains(required)) {

                throw new IllegalArgumentException(
                        "Missing required CSV column: " + required
                );
            }
        }
    }

    private String clean(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private Integer parseInteger(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.equalsIgnoreCase("NA")
                || value.equalsIgnoreCase("NaN")) {

            return null;
        }

        return Integer.parseInt(value.trim());
    }

    private Double parseDouble(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        if (value.equalsIgnoreCase("NA")
                || value.equalsIgnoreCase("NaN")) {

            return null;
        }

        return Double.parseDouble(value.trim());
    }
}