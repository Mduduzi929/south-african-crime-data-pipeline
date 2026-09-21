package za.co.crime.extraction;

import org.junit.jupiter.api.Test;
import za.co.crime.model.CrimeRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvCrimeReaderTest {

    @Test
    void shouldReadCrimeRecordsFromCsv() throws Exception {

        String csv = """
                year,station,loc_mn,dc_mn,longitude,latitude,murder,robbery
                2025,Test Station,Test Municipality,Test District,28.0473,-26.2041,10,20
                """;

        Path tempFile = Files.createTempFile("crime-test-", ".csv");
        Files.writeString(tempFile, csv);

        CsvCrimeReader reader = new CsvCrimeReader();

        List<CrimeRecord> records = reader.read(tempFile);

        assertEquals(1, records.size());

        CrimeRecord record = records.get(0);

        assertEquals("2025", record.getYear());
        assertEquals("Test Station", record.getStation());
        assertEquals("Test Municipality", record.getMunicipality());
        assertEquals("Test District", record.getDistrict());

        assertEquals(28.0473, record.getLongitude());
        assertEquals(-26.2041, record.getLatitude());

        assertEquals(10, record.getCrimeCounts().get("murder"));
        assertEquals(20, record.getCrimeCounts().get("robbery"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void missingRequiredColumnShouldFail() throws Exception {

        String csv = """
                year,station,loc_mn,longitude,latitude,murder
                2025,Test Station,Municipality,28.0473,-26.2041,10
                """;

        Path tempFile = Files.createTempFile("crime-test-", ".csv");
        Files.writeString(tempFile, csv);

        CsvCrimeReader reader = new CsvCrimeReader();

        assertThrows(
                IllegalArgumentException.class,
                () -> reader.read(tempFile)
        );

        Files.deleteIfExists(tempFile);
    }
}