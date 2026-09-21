package za.co.crime.validation;

import org.junit.jupiter.api.Test;
import za.co.crime.model.CrimeRecord;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CrimeValidatorTest {

    private final CrimeValidator validator = new CrimeValidator();

    @Test
    void validRecordShouldPassValidation() {
        CrimeRecord record = new CrimeRecord(
                "2025",
                "Johannesburg Central",
                "City of Johannesburg",
                "Gauteng",
                28.0473,
                -26.2041,
                new HashMap<>()
        );

        assertDoesNotThrow(() -> validator.validate(record));
    }

    @Test
    void missingYearShouldFailValidation() {
        CrimeRecord record = new CrimeRecord(
                null,
                "Johannesburg Central",
                "City of Johannesburg",
                "Gauteng",
                28.0473,
                -26.2041,
                new HashMap<>()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(record)
        );
    }

    @Test
    void missingStationShouldFailValidation() {
        CrimeRecord record = new CrimeRecord(
                "2025",
                null,
                "City of Johannesburg",
                "Gauteng",
                28.0473,
                -26.2041,
                new HashMap<>()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(record)
        );
    }

    @Test
    void invalidLatitudeShouldFailValidation() {
        CrimeRecord record = new CrimeRecord(
                "2025",
                "Johannesburg Central",
                "City of Johannesburg",
                "Gauteng",
                28.0473,
                100.0,
                new HashMap<>()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(record)
        );
    }

    @Test
    void invalidLongitudeShouldFailValidation() {
        CrimeRecord record = new CrimeRecord(
                "2025",
                "Johannesburg Central",
                "City of Johannesburg",
                "Gauteng",
                200.0,
                -26.2041,
                new HashMap<>()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(record)
        );
    }
}