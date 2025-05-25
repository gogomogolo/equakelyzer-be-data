# Earthquake Data Service for Turkey

## 1. Project Overview

The Earthquake Data Service for Turkey is a Spring Boot application designed to collect, store, analyze, and provide alerts for earthquake data, primarily focusing on events occurring in and around Turkey. It fetches data from the USGS Earthquake Catalog API, persists it in a PostgreSQL database, and exposes RESTful endpoints for querying and statistical analysis. The service also includes an alerting mechanism for significant earthquakes based on configurable criteria.

## 2. Features

*   **Data Collection:** Periodically fetches earthquake data from the USGS API for a predefined geographic region corresponding to Turkey.
*   **Data Storage:** Stores detailed earthquake information in a PostgreSQL database.
*   **REST APIs:** Provides endpoints for:
    *   Fetching the N most recent earthquakes.
    *   Querying historical earthquakes by city/location, date range, and minimum magnitude, with support for pagination and sorting.
    *   Retrieving summary statistics, including earthquake counts and the largest magnitude events for the last 24 hours and 7 days.
*   **Alerting System:** Logs alerts to the application console for newly detected significant earthquakes based on configurable magnitude and depth thresholds.

## 3. Prerequisites

*   **Java:** JDK 8 or higher.
*   **Gradle:** Version 6.x or compatible (the project uses Gradle wrapper).
*   **PostgreSQL:** A running PostgreSQL server instance.

## 4. Setup and Configuration

### 4.1. Database Setup

1.  Ensure PostgreSQL is installed and running.
2.  Create a database for the application. For example:
    ```sql
    CREATE DATABASE equakelyzer_db;
    ```
3.  Create a user with privileges to connect to and modify this database. For example:
    ```sql
    CREATE USER db_user WITH PASSWORD 'db_password';
    GRANT ALL PRIVILEGES ON DATABASE equakelyzer_db TO db_user;
    ```
    **Note:** Replace `'db_password'` with a secure password.

### 4.2. Application Configuration

Configuration is managed in the `src/main/resources/application.properties` file. Key properties to review and update:

*   **Database Connection:**
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/equakelyzer_db
    spring.datasource.username=db_user
    spring.datasource.password=db_password
    spring.datasource.driver-class-name=org.postgresql.Driver
    ```
    *   Update `spring.datasource.url` if your PostgreSQL server is on a different host or port, or if your database name is different.
    *   Update `spring.datasource.username` and `spring.datasource.password` to match the user you created.

*   **JPA Configuration:**
    ```properties
    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    ```
    *   `spring.jpa.hibernate.ddl-auto=update` allows Hibernate to attempt to update the schema based on your entities. For production, consider more robust schema management tools like Flyway or Liquibase.

*   **USGS API Configuration:**
    ```properties
    usgs.api.baseurl=https://earthquake.usgs.gov/fdsnws/event/1/query
    usgs.api.format=geojson
    usgs.api.region.turkey.minlatitude=35.8
    usgs.api.region.turkey.maxlatitude=42.2
    usgs.api.region.turkey.minlongitude=25.4
    usgs.api.region.turkey.maxlongitude=44.8
    usgs.api.minmagnitude=2.5 # Minimum magnitude for fetching data from USGS
    usgs.api.retry.maxattempts=3
    usgs.api.retry.delayms=5000
    ```
    *   These define the default parameters for fetching data from USGS. The geographic coordinates are set for Turkey.

*   **Significant Earthquake Alerting:**
    ```properties
    alerting.significant.minMagnitude=4.0
    alerting.significant.maxDepth=100.0 # Earthquakes shallower than 100km (depth is in km)
    ```
    *   `alerting.significant.minMagnitude`: Minimum magnitude for an earthquake to be considered significant for alerting.
    *   `alerting.significant.maxDepth`: Maximum depth (in kilometers) for an earthquake to be considered significant (shallower events are often more damaging).

*   **Monitoring Schedule (Data Refresh Interval):**
    ```properties
    monitoring.schedule.fixedRate=300000 # Interval in milliseconds (e.g., 300000 = 5 minutes)
    monitoring.schedule.initialDelay=10000 # Delay in milliseconds before the first run (e.g., 10000 = 10 seconds)
    ```

### 4.3. Building the Application

The project uses Gradle wrapper.
To build the application, navigate to the project root directory and run:
```bash
./gradlew build
```
On Windows, use:
```bash
gradlew.bat build
```

### 4.4. Running the Application

You can run the application using the Gradle wrapper:
```bash
./gradlew bootRun
```
Alternatively, you can run the packaged JAR file (after building):
```bash
java -jar build/libs/earthquakedataservice-0.0.1-SNAPSHOT.jar
```
The application will start, and the data collection service will begin fetching data based on the configured schedule.

## 5. API Endpoints

The following are the main REST API endpoints provided by the service. The base path for these endpoints is `/earthquakehistory`.

*   **Get N most recent earthquakes:**
    *   `GET /earthquakehistory/recent`
    *   **Query Parameters:**
        *   `count` (optional, integer, default: 10): Number of recent earthquakes to retrieve. Capped at 100.
    *   **Example:** `GET /earthquakehistory/recent?count=5`

*   **Get earthquakes by city/location, date range, and magnitude:**
    *   `GET /earthquakehistory/cities/{city}`
    *   **Path Variable:**
        *   `{city}`: A string representing the city or location. This performs a case-insensitive search for the provided text within the earthquake's `locationDescription` field (e.g., "Ankara", "Istanbul", "Aegean Sea").
    *   **Query Parameters:**
        *   `from` (required, ISO DateTime string, e.g., `2023-01-01T00:00:00`): Start of the date range.
        *   `to` (required, ISO DateTime string, e.g., `2023-01-31T23:59:59`): End of the date range.
        *   `minMagnitude` (optional, double, default: 0.0): Minimum magnitude of earthquakes to include.
        *   `page` (optional, integer, default: 0): Page number for pagination.
        *   `size` (optional, integer, default: 20): Number of records per page.
        *   `sort` (optional, string, e.g., `eventDate,desc`): Property and direction for sorting.
    *   **Example:** `GET /earthquakehistory/cities/Izmir?from=2023-03-01T00:00:00&to=2023-03-10T23:59:59&minMagnitude=3.0&page=0&size=10`

*   **Get summary earthquake statistics:**
    *   `GET /earthquakehistory/summary-stats`
    *   Provides counts of earthquakes in the last 24 hours and 7 days, and details of the largest magnitude event in each of those periods.
    *   **Example:** `GET /earthquakehistory/summary-stats`

## 6. Alerting System

The application includes an alerting system for newly detected significant earthquakes.
*   **Criteria:** An earthquake is considered significant if its magnitude is greater than or equal to `alerting.significant.minMagnitude` AND its depth is less than or equal to `alerting.significant.maxDepth`. Depth check is skipped if depth information is not available for an event.
*   **Notification:** Alerts are currently logged as `WARN` messages to the application's standard log output (e.g., console or configured log files). The log message includes details of the significant event.
*   **Configuration:** The criteria for significance can be configured in `src/main/resources/application.properties` (see section 4.2).

## 7. Data Sources

*   **Primary Data Source:** [USGS Earthquake Catalog API](https://earthquake.usgs.gov/fdsnws/event/1/) - Used for fetching earthquake data for the region of Turkey.
*   **Previous Data Sources:** Implementations for Bogazici Observatory crawler and Twitter data collection might exist in the codebase but are currently disabled or have been effectively replaced by the USGS integration for the primary `EarthQuakeHistory` data flow.

## 8. MongoDB Data (Legacy Note)

The codebase may contain entities (e.g., `EarthQuakeHistorical`) and repositories (e.g., `EarthQuakeHistoricalRepository`) related to MongoDB. This appears to be part of an older or separate data handling path, possibly for different data granularity (city, district, town) or from different sources.

*   The endpoint `GET /earthquakehistory/cities` (without a city path variable, expecting `from` and `to` parameters) is related to this MongoDB data path and currently returns an `EarthQuakeStory` object, which is not fully implemented.
*   The primary data flow for new features (USGS data, recent events, statistics, alerts) uses PostgreSQL and the `EarthQuakeHistory` entity. The MongoDB path is considered legacy or separate unless specified otherwise.

This README provides a guide to understanding, setting up, and using the Earthquake Data Service.
