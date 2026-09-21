package za.co.crime.validation;

import za.co.crime.model.CrimeRecord;

public class CrimeValidator {

    public void validate(CrimeRecord record) {

        if (record.getYear() == null
                || record.getYear().isBlank()) {

            throw new IllegalArgumentException(
                    "Year is missing"
            );
        }

        if (record.getStation() == null
                || record.getStation().isBlank()) {

            throw new IllegalArgumentException(
                    "Police station is missing"
            );
        }

        validateCoordinate(
                record.getLatitude(),
                -90,
                90,
                "latitude"
        );

        validateCoordinate(
                record.getLongitude(),
                -180,
                180,
                "longitude"
        );
    }

    private void validateCoordinate(
            Double value,
            double minimum,
            double maximum,
            String field
    ) {

        if (value == null) {
            return;
        }

        if (value < minimum || value > maximum) {

            throw new IllegalArgumentException(
                    field
                            + " is outside valid geographic range: "
                            + value
            );
        }
    }
}