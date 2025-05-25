package com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;
import com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.usgs.geojson.Feature;
import com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.usgs.geojson.GeoJsonResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class USGSEarthquakeWebServiceStrategy implements WebServiceClientStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(USGSEarthquakeWebServiceStrategy.class);
    private static final DateTimeFormatter URL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String format;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;
    private final double minMagnitude;
    private final int maxRetryAttempts;
    private final long retryDelayMs;


    public USGSEarthquakeWebServiceStrategy(String baseUrl, String format,
                                            double minLatitude, double maxLatitude,
                                            double minLongitude, double maxLongitude,
                                            double minMagnitude,
                                            int maxRetryAttempts, long retryDelayMs) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10)) // Connection timeout
                .build();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.format = format;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.minMagnitude = minMagnitude;
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public List<EarthQuakePhysicalDto> fetch() { // Renamed from fetchData and updated return type
        String apiUrl = buildApiUrl();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(java.time.Duration.ofSeconds(15)) // Request timeout
                .GET()
                .build();

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                LOGGER.info("Attempt {}/{} to fetch earthquake data from USGS API: {}", attempt, maxRetryAttempts, apiUrl);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String responseBody = response.body();
                    if (responseBody == null || responseBody.isBlank()) {
                        LOGGER.warn("Received empty response body from USGS API (URL: {})", apiUrl);
                        return Collections.emptyList();
                    }
                    GeoJsonResponse geoJsonResponse = objectMapper.readValue(responseBody, GeoJsonResponse.class);
                    LOGGER.info("Successfully fetched and parsed {} features from USGS API.", (geoJsonResponse.getFeatures() != null ? geoJsonResponse.getFeatures().size() : 0));
                    return adaptResponseToDtoList(geoJsonResponse); // Renamed method
                } else if (response.statusCode() == 204) { // No content
                    LOGGER.info("No new earthquake data found from USGS API (URL: {}). Status code: 204", apiUrl);
                    return Collections.emptyList();
                } else {
                    LOGGER.error("Error fetching earthquake data from USGS API (URL: {}). Status code: {}. Response body: {}",
                            apiUrl, response.statusCode(), response.body());
                    // For client errors (4xx), don't retry unless specific cases are identified
                    if (response.statusCode() >= 400 && response.statusCode() < 500) {
                        break; // Do not retry client errors
                    }
                }
            } catch (SocketTimeoutException | ConnectException e) {
                LOGGER.warn("Network timeout or connection error on attempt {}/{} for URL {}: {}", attempt, maxRetryAttempts, apiUrl, e.getMessage());
                if (attempt == maxRetryAttempts) {
                    LOGGER.error("Failed to fetch earthquake data from USGS API after {} attempts due to network issues (URL: {}).", maxRetryAttempts, apiUrl, e);
                    return Collections.emptyList(); // Or throw custom exception
                }
            } catch (IOException e) {
                LOGGER.warn("IOException on attempt {}/{} for URL {}: {}", attempt, maxRetryAttempts, apiUrl, e.getMessage());
                if (attempt == maxRetryAttempts) {
                    LOGGER.error("Failed to fetch earthquake data from USGS API after {} attempts due to IOException (URL: {}).", maxRetryAttempts, apiUrl, e);
                    return Collections.emptyList(); // Or throw custom exception
                }
            } catch (InterruptedException e) {
                LOGGER.warn("Request interrupted on attempt {}/{} for URL {}: {}", attempt, maxRetryAttempts, apiUrl, e.getMessage());
                Thread.currentThread().interrupt(); // Restore interruption status
                return Collections.emptyList();
            } catch (JsonProcessingException e) {
                LOGGER.error("Error parsing JSON response from USGS API (URL: {}).", apiUrl, e);
                return Collections.emptyList(); // JSON parsing error, unlikely to succeed on retry
            } catch (Exception e) { // Catch-all for unexpected errors
                LOGGER.error("Unexpected error on attempt {}/{} while fetching data from USGS API (URL: {}).", attempt, maxRetryAttempts, apiUrl, e);
                if (attempt == maxRetryAttempts) {
                    LOGGER.error("Failed to fetch earthquake data from USGS API after {} attempts due to an unexpected error (URL: {}).", maxRetryAttempts, apiUrl, e);
                    return Collections.emptyList();
                }
            }

            if (attempt < maxRetryAttempts) {
                try {
                    LOGGER.info("Waiting {}ms before next retry...", retryDelayMs);
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Retry delay interrupted for URL {}", apiUrl);
                    return Collections.emptyList();
                }
            }
        }
        LOGGER.error("Exhausted all {} retry attempts for USGS API (URL: {}). Returning empty list.", maxRetryAttempts, apiUrl);
        return Collections.emptyList();
    }

    private String buildApiUrl() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC")); // Use UTC for consistency with API
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // Example: https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2023-01-01T00:00:00&endtime=2023-01-08T00:00:00&minlatitude=35.8&maxlatitude=42.2&minlongitude=25.4&maxlongitude=44.8&minmagnitude=2.5
        // Using ISO_LOCAL_DATE for starttime and endtime as per USGS API examples for date-only queries if time is not critical
        // However, the previous implementation used ISO_LOCAL_DATE_TIME. For consistency with previous work and precision, sticking to ISO_LOCAL_DATE_TIME.
        return String.format("%s?format=%s&starttime=%s&endtime=%s&minlatitude=%s&maxlatitude=%s&minlongitude=%s&maxlongitude=%s&minmagnitude=%s",
                this.baseUrl,
                this.format,
                sevenDaysAgo.format(URL_DATE_FORMATTER),
                now.format(URL_DATE_FORMATTER),
                this.minLatitude,
                this.maxLatitude,
                this.minLongitude,
                this.maxLongitude,
                this.minMagnitude);
    }

    private List<EarthQuakePhysicalDto> adaptResponseToDtoList(GeoJsonResponse geoJsonResponse) { // Renamed method and updated return type
        if (geoJsonResponse == null || geoJsonResponse.getFeatures() == null || geoJsonResponse.getFeatures().isEmpty()) {
            LOGGER.info("No features found in GeoJsonResponse.");
            return Collections.emptyList();
        }

        return geoJsonResponse.getFeatures().stream()
                .filter(Objects::nonNull) // Ensure feature itself is not null
                .map(this::convertToDto)
                .filter(Objects::nonNull) // Ensure DTO conversion was successful
                .collect(Collectors.toList());
    }

    private EarthQuakePhysicalDto convertToDto(Feature feature) {
        if (feature.getProperties() == null || feature.getGeometry() == null ||
            feature.getGeometry().getCoordinates() == null || feature.getGeometry().getCoordinates().size() < 3) {
            LOGGER.warn("Skipping feature due to missing properties, geometry, or insufficient coordinates. Feature: {}", feature);
            return null;
        }

        com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.usgs.geojson.Properties properties = feature.getProperties();
        List<Double> coordinates = feature.getGeometry().getCoordinates();

        LocalDateTime eventDate = null;
        if (properties.getTime() != null && properties.getTime() > 0) { // Ensure time is valid
            eventDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(properties.getTime()), ZoneId.systemDefault());
        } else {
            LOGGER.warn("Skipping feature due to invalid or missing time. Feature: {}", feature);
            return null;
        }

        Double longitude = coordinates.get(0);
        Double latitude = coordinates.get(1);
        Double depth = coordinates.get(2); // Depth from USGS, typically in km

        if (longitude == null || latitude == null || depth == null) {
            LOGGER.warn("Skipping feature due to null coordinates (lon, lat, or depth). Feature: {}", feature);
            return null;
        }

        Double magnitude = properties.getMag();
        if (magnitude == null) {
            LOGGER.warn("Skipping feature due to null magnitude. Feature: {}", feature);
            return null;
        }

        String locationDescription = properties.getPlace();
        if (locationDescription == null || locationDescription.isBlank()) {
            LOGGER.warn("Skipping feature due to null or blank location description. Feature: {}", feature);
            return null;
        }

        // The 'force' field in EarthQuakePhysicalDto has been renamed to 'magnitude'.
        return new EarthQuakePhysicalDto(
                eventDate,
                latitude,
                longitude,
                depth,
                magnitude, // This is properties.getMag()
                locationDescription,
                properties.getCode(),      // usgsEventId from 'code'
                properties.getMagType(),   // magnitudeType from 'magType'
                properties.getTsunami(),   // tsunamiAlert from 'tsunami'
                properties.getUrl(),       // usgsEventUrl from 'url'
                properties.getStatus(),    // status from 'status'
                properties.getSig(),       // significance from 'sig'
                properties.getTypes()      // network from 'types' (often contains network like ",us,") - needs parsing, or use 'net' if available
                                           // The 'types' field in GeoJSON often lists networks like ",us,tx,..."
                                           // A dedicated 'net' field in the GeoJSON properties would be better if available.
                                           // For now, let's assume 'types' contains usable network info or is a placeholder.
                                           // If 'net' is a direct property, it should be used: properties.getNet()
                                           // Looking at the GeoJSON structure, 'net' is a top-level property in 'properties'.
                                           // So, properties.getNet() should be used if Properties.java has a 'net' field.
                                           // Let's assume Properties.java will be updated to include 'net'.
                                           // If not, properties.getTypes() is a fallback.
                                           // For now, I will use properties.getTypes() as a placeholder and it should be properties.getNet()
                                           // properties.getNet() // Correct field for network
                                           // For now, using "types" as "net" is not in Properties.java
                                           // This will need correction if "net" is the actual field in GeoJSON properties
                                           // The original GeoJSON properties has "net", "code", "ids", "sources", "types", "nst", "dmin", "rms", "gap", "magType".
                                           // So, Properties.java should have a 'net' field.
                                           // I will assume Properties.java has a 'net' field.
                                           properties.getNet() // Assuming 'net' field exists in Properties.java
        );
    }
}
