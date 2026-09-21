package za.co.crime.transformation;

import za.co.crime.model.CrimeEvent;
import za.co.crime.model.CrimeRecord;
import za.co.crime.validation.CrimeValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrimeTransformer {

    public List<CrimeEvent> transform(
            List<CrimeRecord> records,
            CrimeValidator validator
    ) {

        List<CrimeEvent> events = new ArrayList<>();

        int sourceRow = 2;

        for (CrimeRecord record : records) {

            validator.validate(record);

            for (Map.Entry<String, Integer> entry :
                    record.getCrimeCounts().entrySet()) {

                Integer value = entry.getValue();

                String status = "VALID";

                Integer normalizedValue = value;

                /*
                 * Negative values in the source dataset
                 * are not automatically treated as zero.
                 */
                if (value != null && value < 0) {

                    normalizedValue = null;

                    status = "NEGATIVE_SOURCE_VALUE";
                }

                if (value == null) {

                    status = "MISSING_SOURCE_VALUE";
                }

                CrimeEvent event = new CrimeEvent(

                        record.getYear(),

                        normalize(record.getStation()),

                        normalize(record.getMunicipality()),

                        normalize(record.getDistrict()),

                        record.getLongitude(),

                        record.getLatitude(),

                        entry.getKey(),

                        normalizedValue,

                        sourceRow,

                        status
                );

                events.add(event);
            }

            sourceRow++;
        }

        return events;
    }

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toLowerCase();
    }
}