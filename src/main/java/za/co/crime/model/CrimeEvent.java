package za.co.crime.model;

public class CrimeEvent {

    private final String year;
    private final String station;
    private final String municipality;
    private final String district;

    private final Double longitude;
    private final Double latitude;

    private final String crimeType;
    private final Integer incidentCount;

    private final int sourceRowNumber;
    private final String sourceStatus;

    public CrimeEvent(
            String year,
            String station,
            String municipality,
            String district,
            Double longitude,
            Double latitude,
            String crimeType,
            Integer incidentCount,
            int sourceRowNumber,
            String sourceStatus
    ) {

        this.year = year;
        this.station = station;
        this.municipality = municipality;
        this.district = district;
        this.longitude = longitude;
        this.latitude = latitude;
        this.crimeType = crimeType;
        this.incidentCount = incidentCount;
        this.sourceRowNumber = sourceRowNumber;
        this.sourceStatus = sourceStatus;
    }

    public String getYear() {
        return year;
    }

    public String getStation() {
        return station;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getDistrict() {
        return district;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public Integer getIncidentCount() {
        return incidentCount;
    }

    public int getSourceRowNumber() {
        return sourceRowNumber;
    }

    public String getSourceStatus() {
        return sourceStatus;
    }
}