package za.co.crime.model;

public record DataQualityIssue(
        int sourceRowNumber,
        String fieldName,
        String issueType,
        String originalValue
) {
}