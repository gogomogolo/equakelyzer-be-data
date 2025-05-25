package com.equakelyzer.earthquakedataservice.service;

import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignificantEarthquakeAlertingServiceTest {

    // To capture log messages, we need to mock the SLF4J Logger.
    // This is a bit more involved than typical mocking.
    // One common way is to temporarily replace the logger instance.
    @Mock
    private Logger mockLogger; // Mock the specific logger instance

    private SignificantEarthquakeAlertingService alertingService;

    private static final double TEST_MIN_MAGNITUDE = 4.0;
    private static final double TEST_MAX_DEPTH = 100.0;

    // Keep the original logger to restore it later
    private static Logger originalLogger = null;


    @BeforeEach
    void setUp() throws Exception {
        // Instantiate the service with test threshold values
        alertingService = new SignificantEarthquakeAlertingService(TEST_MIN_MAGNITUDE, TEST_MAX_DEPTH);

        // Replace the static final LOGGER in SignificantEarthquakeAlertingService with our mock
        // This is tricky because it's static final.
        // Using reflection to set the logger. This is generally discouraged for production code access
        // but can be acceptable in tests when other DI mechanisms are not available for static loggers.
        Field loggerField = SignificantEarthquakeAlertingService.class.getDeclaredField("LOGGER");
        loggerField.setAccessible(true);

        // Store the original logger and remove the final modifier to change it
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);

        // It's better to get the original logger from the class if it's already initialized
        // However, if it's `LoggerFactory.getLogger(SignificantEarthquakeAlertingService.class)`,
        // we can't easily store and restore *that exact instance* unless we control LoggerFactory.
        // For this test, we'll just ensure our mock is used.

        originalLogger = (Logger) loggerField.get(null); // Store original (though it might be a different instance if class was already loaded)
        loggerField.set(null, mockLogger);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore the original logger if needed, though in typical test environments,
        // this might not be strictly necessary as each test run might get a fresh classloader or context.
        // However, it's good practice if tests run in a shared environment.
        if (originalLogger != null) {
            Field loggerField = SignificantEarthquakeAlertingService.class.getDeclaredField("LOGGER");
            loggerField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL); // Should already be non-final
            loggerField.set(null, originalLogger); // Restore original
        }
    }


    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    @Test
    void checkAndAlertForSignificantEarthquakes_alertsForSignificantEvents() {
        EarthQuakeHistory significantShallow = new EarthQuakeHistory(LocalDateTime.now(), 30.0, -120.0, 20.0, 4.5, "Significant Shallow", "USGS", "ev1", "mw", 0, "url", "reviewed", 0, "us");
        EarthQuakeHistory significantDeep = new EarthQuakeHistory(LocalDateTime.now(), 31.0, -121.0, 150.0, 5.0, "Significant Deep", "USGS", "ev2", "mw", 0, "url", "reviewed", 0, "us"); // Too deep
        EarthQuakeHistory significantNullDepth = new EarthQuakeHistory(LocalDateTime.now(), 30.5, -120.5, null, 4.2, "Significant Null Depth", "USGS", "ev_null_depth", "mw", 0, "url", "reviewed", 0, "us");
        EarthQuakeHistory notSignificantMagnitude = new EarthQuakeHistory(LocalDateTime.now(), 32.0, -122.0, 10.0, 3.5, "Not Significant Mag", "USGS", "ev3", "ml", 0, "url", "reviewed", 0, "us");
        EarthQuakeHistory notSignificantBoundary = new EarthQuakeHistory(LocalDateTime.now(), 33.0, -123.0, 99.9, 3.9, "Not Significant Boundary Mag", "USGS", "ev4", "ml", 0, "url", "reviewed", 0, "us");

        List<EarthQuakeHistory> earthquakes = Arrays.asList(significantShallow, significantDeep, significantNullDepth, notSignificantMagnitude, notSignificantBoundary);

        alertingService.checkAndAlertForSignificantEarthquakes(earthquakes);

        // Verify logger.warn was called for significantShallow and significantNullDepth
        // The exact message format can be tricky to assert, so checking for key details or number of calls.
        verify(mockLogger, times(2)).warn(stringArgumentCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), any(), any());

        List<String> capturedLogs = stringArgumentCaptor.getAllValues();
        // Check that the log for significantShallow contains its magnitude or location
        assertTrue(capturedLogs.stream().anyMatch(log -> log.contains("SIGNIFICANT EARTHQUAKE DETECTED") && log.contains("Significant Shallow")));
        // Check that the log for significantNullDepth contains its magnitude or location
        assertTrue(capturedLogs.stream().anyMatch(log -> log.contains("SIGNIFICANT EARTHQUAKE DETECTED") && log.contains("Significant Null Depth")));

        // Check that logs for non-significant events were not generated
        assertFalse(capturedLogs.stream().anyMatch(log -> log.contains("Significant Deep"))); // Too deep
        assertFalse(capturedLogs.stream().anyMatch(log -> log.contains("Not Significant Mag")));
        assertFalse(capturedLogs.stream().anyMatch(log -> log.contains("Not Significant Boundary Mag")));
    }

    @Test
    void checkAndAlertForSignificantEarthquakes_noAlertsForNonSignificantEvents() {
        EarthQuakeHistory notSignificantMagnitude = new EarthQuakeHistory(LocalDateTime.now(), 32.0, -122.0, 10.0, 3.5, "Not Sig Mag", "USGS", "ev3", "ml", 0, "url", "reviewed", 0, "us");
        EarthQuakeHistory notSignificantDepth = new EarthQuakeHistory(LocalDateTime.now(), 31.0, -121.0, 150.0, 4.5, "Not Sig Depth", "USGS", "ev2", "mw", 0, "url", "reviewed", 0, "us"); // Too deep
        EarthQuakeHistory notSignificantBoundaryMag = new EarthQuakeHistory(LocalDateTime.now(), 33.0, -123.0, 10.0, 3.99, "Boundary Mag", "USGS", "ev4", "ml", 0, "url", "reviewed", 0, "us");
        EarthQuakeHistory notSignificantBoundaryDepth = new EarthQuakeHistory(LocalDateTime.now(), 34.0, -124.0, 100.1, 4.1, "Boundary Depth", "USGS", "ev5", "mw", 0, "url", "reviewed", 0, "us");


        List<EarthQuakeHistory> earthquakes = Arrays.asList(notSignificantMagnitude, notSignificantDepth, notSignificantBoundaryMag, notSignificantBoundaryDepth);
        alertingService.checkAndAlertForSignificantEarthquakes(earthquakes);

        verify(mockLogger, never()).warn(contains("SIGNIFICANT EARTHQUAKE DETECTED"), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void checkAndAlertForSignificantEarthquakes_handlesEmptyList() {
        alertingService.checkAndAlertForSignificantEarthquakes(Collections.emptyList());
        verifyNoInteractions(mockLogger); // No logging should occur beyond the initial debug
    }

    @Test
    void checkAndAlertForSignificantEarthquakes_handlesNullList() {
        alertingService.checkAndAlertForSignificantEarthquakes(null);
        verifyNoInteractions(mockLogger);
    }

    @Test
    void checkAndAlertForSignificantEarthquakes_magnitudeAtThresholdIsAlerted() {
        EarthQuakeHistory atThresholdMag = new EarthQuakeHistory(LocalDateTime.now(), 30.0, -120.0, 50.0, TEST_MIN_MAGNITUDE, "Threshold Mag", "USGS", "ev_thresh_mag", "mw", 0, "url", "reviewed", 0, "us");
        List<EarthQuakeHistory> earthquakes = Collections.singletonList(atThresholdMag);
        alertingService.checkAndAlertForSignificantEarthquakes(earthquakes);
        verify(mockLogger).warn(contains("SIGNIFICANT EARTHQUAKE DETECTED"), eq(TEST_MIN_MAGNITUDE), any(), eq("Threshold Mag"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void checkAndAlertForSignificantEarthquakes_depthAtThresholdIsAlerted() {
        EarthQuakeHistory atThresholdDepth = new EarthQuakeHistory(LocalDateTime.now(), 30.0, -120.0, TEST_MAX_DEPTH, 4.5, "Threshold Depth", "USGS", "ev_thresh_depth", "mw", 0, "url", "reviewed", 0, "us");
        List<EarthQuakeHistory> earthquakes = Collections.singletonList(atThresholdDepth);
        alertingService.checkAndAlertForSignificantEarthquakes(earthquakes);
        verify(mockLogger).warn(contains("SIGNIFICANT EARTHQUAKE DETECTED"), eq(4.5), any(), eq("Threshold Depth"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void checkAndAlertForSignificantEarthquakes_nullMagnitudeNoAlert() {
        EarthQuakeHistory nullMagnitude = new EarthQuakeHistory(LocalDateTime.now(), 30.0, -120.0, 50.0, null, "Null Mag", "USGS", "ev_null_mag", "mw", 0, "url", "reviewed", 0, "us");
        List<EarthQuakeHistory> earthquakes = Collections.singletonList(nullMagnitude);
        alertingService.checkAndAlertForSignificantEarthquakes(earthquakes);
        verify(mockLogger, never()).warn(contains("SIGNIFICANT EARTHQUAKE DETECTED"), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
