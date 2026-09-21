package za.co.crime.transformation;

import org.junit.jupiter.api.Test;
import za.co.crime.model.CrimeEvent;
import za.co.crime.model.CrimeRecord;
import za.co.crime.validation.CrimeValidator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CrimeTransformerTest {

    private final CrimeTransformer transformer =
            new CrimeTransformer();

    private final CrimeValidator validator =
            new CrimeValidator();

    @Test
    void shouldTransformCrimeRecordIntoCrimeEvents() {

        Map<String, Integer> crimes = new LinkedHashMap<>();
        crimes.put("murder", 10);
        crimes.put("robbery", 20);

        CrimeRecord record = new CrimeRecord(
                "2025",
                " TEST STATION ",
                " TEST MUNICIPALITY ",
                " TEST DISTRICT ",
                28.0473,
                -26.2041,
                crimes
        );

        List<CrimeEvent> events =
                transformer.transform(List.of(record), validator);

        assertEquals(2, events.size());

        CrimeEvent first = events.get(0);

        assertEquals("2025", first.getYear());
        assertEquals("test station", first.getStation());
        assertEquals("test municipality", first.getMunicipality());
        assertEquals("test district", first.getDistrict());
        assertEquals("murder", first.getCrimeType());
        assertEquals(10, first.getIncidentCount());
        assertEquals("VALID", first.getSourceStatus());
    }

    @Test
    void negativeCrimeValueShouldBecomeNull() {

        Map<String, Integer> crimes = new LinkedHashMap<>();
        crimes.put("murder", -5);

        CrimeRecord record = new CrimeRecord(
                "2025",
                "Test Station",
                "Municipality",
                "District",
                28.0473,
                -26.2041,
                crimes
        );

        List<CrimeEvent> events =
                transformer.transform(List.of(record), validator);

        CrimeEvent event = events.get(0);

        assertNull(event.getIncidentCount());
        assertEquals(
                "NEGATIVE_SOURCE_VALUE",
                event.getSourceStatus()
        );
    }

    @Test
    void missingCrimeValueShouldBeMarked() {

        Map<String, Integer> crimes = new LinkedHashMap<>();
        crimes.put("sexual_offences", null);

        CrimeRecord record = new CrimeRecord(
                "2025",
                "Test Station",
                "Municipality",
                "District",
                28.0473,
                -26.2041,
                crimes
        );

        List<CrimeEvent> events =
                transformer.transform(List.of(record), validator);

        CrimeEvent event = events.get(0);

        assertNull(event.getIncidentCount());
        assertEquals(
                "MISSING_SOURCE_VALUE",
                event.getSourceStatus()
        );
    }
}