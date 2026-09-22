WTC-N945TSSQ











# South African Crime Data Pipeline

A Java-based data engineering pipeline that extracts South African crime statistics from a CSV dataset, validates and transforms the data, and loads it into a structured SQLite data warehouse for analysis.

## Overview

This project processes South African Police Service (SAPS) crime statistics covering multiple years and police stations across South Africa.

The pipeline follows a traditional ETL architecture:

```text
Raw CSV Dataset
      │
      ▼
   Extract
      │
      ▼
   Validate
      │
      ▼
  Transform
      │
      ▼
    Load
      │
      ▼
 SQLite Data Warehouse

The goal is to create a clean, structured and queryable representation of the source crime data that can later be used for SQL analysis, reporting and data visualisation.

Features
Reads large CSV datasets using Apache Commons CSV
Validates required columns and geographic coordinates
Handles missing crime values
Detects negative source values
Normalizes station, municipality and district names
Transforms wide CSV crime data into individual crime events
Uses a dimensional data warehouse structure
Loads data into SQLite
Uses transactions during database loading
Uses prepared statements to safely insert data
Uses database constraints to maintain data integrity
Includes automated unit tests using JUnit 5
Technology Stack
Technology	Purpose
Java 17	Application and ETL logic
Maven	Build and dependency management
Apache Commons CSV	CSV extraction
SQLite	Local data warehouse
JDBC	Database connectivity
JUnit 5	Automated testing
Git	Version control
Project Structure
south-african-crime-data-pipeline/
│
├── data/
│   └── raw/
│       └── sapacr-2005-2026-v1.4.csv
│
├── database/
│   └── crime.db
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── za/
│   │           └── co/
│   │               └── crime/
│   │                   ├── Main.java
│   │                   │
│   │                   ├── database/
│   │                   │   ├── DatabaseConnection.java
│   │                   │   └── SchemaInitializer.java
│   │                   │
│   │                   ├── extraction/
│   │                   │   └── CsvCrimeReader.java
│   │                   │
│   │                   ├── loading/
│   │                   │   └── CrimeRepository.java
│   │                   │
│   │                   ├── model/
│   │                   │   ├── CrimeRecord.java
│   │                   │   ├── CrimeEvent.java
│   │                   │   └── DataQualityIssue.java
│   │                   │
│   │                   ├── transformation/
│   │                   │   └── CrimeTransformer.java
│   │                   │
│   │                   └── validation/
│   │                       └── CrimeValidator.java
│   │
│   └── test/
│       └── java/
│           └── za/
│               └── co/
│                   └── crime/
│                       ├── extraction/
│                       ├── transformation/
│                       └── validation/
│
├── pom.xml
└── README.md
Data Source

The project uses the SAPS Annual Crime Records dataset provided through DataFirst.

The raw dataset contains crime statistics by:

Year
Police station
Municipality
District
Longitude
Latitude
Crime category

The dataset contains historical records spanning multiple years.

The raw CSV file is kept in:

data/raw/
ETL Pipeline
1. Extract

CsvCrimeReader reads the raw CSV file using Apache Commons CSV.

The reader:

Loads the CSV header
Checks that required columns exist
Reads each row
Converts numeric values into Java numeric types
Represents missing numeric values as null
Creates CrimeRecord objects

Required source columns include:

year
station
loc_mn
dc_mn
longitude
latitude

Crime columns are dynamically read from the remaining CSV columns.

2. Validate

CrimeValidator checks the integrity of extracted records.

Examples of validation include:

Year must be present
Police station must be present
Latitude must be between -90 and 90
Longitude must be between -180 and 180

Invalid records are rejected rather than silently being loaded into the warehouse.

3. Transform

CrimeTransformer converts the source representation into a more suitable structure for the warehouse.

The original CSV contains many crime columns in a wide format.

For example:

year | station | murder | robbery | burglary

is transformed into individual crime events:

year | station | crime_type | incident_count
2025 | station A | murder     | 10
2025 | station A | robbery    | 20
2025 | station A | burglary   | 15

The transformation process also:

Normalizes location names
Detects negative crime values
Handles missing crime values
Records the source row number
Records the source data status

Negative source values are not treated as valid crime counts. They are converted to null and marked as:

NEGATIVE_SOURCE_VALUE

Missing values are marked as:

MISSING_SOURCE_VALUE

Valid values are marked as:

VALID
Data Warehouse

The transformed data is loaded into a dimensional warehouse structure.

dim_year
    │
    │
    ▼
fact_crime ◄──── dim_location
    │
    │
    ▼
dim_crime
dim_year

Stores unique years from the source dataset.

year_id
year_label
dim_location

Stores unique police station locations.

location_id
station
municipality
district
longitude
latitude
dim_crime

Stores unique crime categories.

crime_id
crime_code
fact_crime

Contains the measurable crime observations.

fact_id
year_id
location_id
crime_id
incident_count
source_row_number
source_status
data_quality_issue

Provides a structure for recording data-quality problems.

issue_id
source_row_number
field_name
issue_type
original_value
Database

SQLite is used as the project's data warehouse.

The database is created automatically by the application at:

database/crime.db

The database does not require a separate database server.

This makes the project easy to run locally and allows the complete warehouse to remain inside the project directory.

Running the Project
Requirements

Make sure the following are installed:

Java 17 or later
Maven
Git

Verify Java:

java -version

Verify Maven:

mvn -version
Build the Project

From the project root:

mvn clean compile
Run Tests

Run the complete test suite:

mvn clean test

The tests cover:

CSV extraction
Required column validation
Record validation
Geographic coordinate validation
Crime transformation
Negative crime values
Missing crime values
Run the ETL Pipeline

Run:

mvn exec:java "-Dexec.mainClass=za.co.crime.Main"

The application will:

Read the CSV dataset
Validate the records
Transform the crime data
Connect to SQLite
Create the warehouse schema
Load the transformed data
Create database/crime.db

Expected output:

================================
 SOUTH AFRICAN CRIME ETL
================================

[1] Extracting CSV...

[2] Transforming data...

[3] Connecting to SQLite...

[4] Creating warehouse schema...

[5] Loading data...

================================
 ETL COMPLETED SUCCESSFULLY
================================
Data Quality

Data quality is treated as part of the pipeline rather than something handled after loading.

The pipeline identifies issues such as:

Missing values
Negative source values
Invalid coordinates
Missing required fields

Instead of silently changing questionable source data, the transformation process records the status of the affected data.

This preserves information about the original source while preventing invalid values from being treated as valid crime counts.

Testing

The project uses JUnit 5 for automated testing.

Testing currently covers the major ETL stages:

CSV Reader
    │
    ├── Valid CSV
    └── Missing required columns 


Validator
    │
    ├── Valid records
    ├── Missing year
    ├── Missing station
    ├── Invalid latitude
    └── Invalid longitude

Transformer
    │
    ├── Record → CrimeEvent
    ├── Negative values
    └── Missing values

Run the tests with:

mvn test
Future Improvements

Planned improvements include:

Optimize database loading and dimension lookups
Expand data-quality reporting
Add more integration tests
Add SQL analysis queries
Add automated data profiling
Add crime trend analysis
Add geographic analysis
Add a dashboard for visualizing crime statistics
Add automated pipeline execution
Add CI/CD using GitHub Actions
Project Goals

This project demonstrates practical data engineering concepts including:

ETL pipelines
Data extraction
Data validation
Data transformation
Data quality management
Relational database design
Dimensional modelling
JDBC
SQL
Automated testing
Maven
Clean Java architecture

The project is designed to demonstrate how raw public data can be transformed into a structured analytical dataset suitable for further analysis and visualization.