package com.equakelyzer.earthquakedataservice.service;

import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SignificantEarthquakeAlertingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignificantEarthquakeAlertingService.class);

    private final double minMagnitudeAlert;
    private final double maxDepthAlert;

    public SignificantEarthquakeAlertingService(
            @Value("${alerting.significant.minMagnitude:4.0}") double minMagnitudeAlert,
            @Value("${alerting.significant.maxDepth:100.0}") double maxDepthAlert) {
        this.minMagnitudeAlert = minMagnitudeAlert;
        this.maxDepthAlert = maxDepthAlert;
        LOGGER.info("SignificantEarthquakeAlertingService initialized with minMagnitude: {}, maxDepth: {}",
                minMagnitudeAlert, maxDepthAlert);
    }

    /**
     * Checks a list of newly fetched earthquakes and logs an alert for significant ones.
     *
     * @param newlyFetchedEarthquakes A list of EarthQuakeHistory objects that were newly saved.
     */
    public void checkAndAlertForSignificantEarthquakes(List<EarthQuakeHistory> newlyFetchedEarthquakes) {
        if (newlyFetchedEarthquakes == null || newlyFetchedEarthquakes.isEmpty()) {
            return;
        }

        LOGGER.debug("Checking {} newly fetched earthquakes for significance.", newlyFetchedEarthquakes.size());

        for (EarthQuakeHistory earthquake : newlyFetchedEarthquakes) {
            if (earthquake.getMagnitude() != null && earthquake.getMagnitude() >= minMagnitudeAlert &&
                (earthquake.getDepth() == null || earthquake.getDepth() <= maxDepthAlert)) { // Depth check is optional if depth is null

                // Log the significant earthquake event
                LOGGER.warn("SIGNIFICANT EARTHQUAKE DETECTED: Magnitude {} {} at \"{}\" (Depth: {} km, USGS ID: {}). URL: {}, Time: {}, Status: {}, Network: {}",
                        earthquake.getMagnitude(),
                        earthquake.getMagnitudeType() != null ? earthquake.getMagnitudeType() : "N/A",
                        earthquake.getLocationDescription(),
                        earthquake.getDepth() != null ? String.format("%.2f", earthquake.getDepth()) : "N/A",
                        earthquake.getUsgsEventId() != null ? earthquake.getUsgsEventId() : "N/A",
                        earthquake.getUsgsEventUrl() != null ? earthquake.getUsgsEventUrl() : "N/A",
                        earthquake.getEventDate(),
                        earthquake.getStatus() != null ? earthquake.getStatus() : "N/A",
                        earthquake.getNetwork() != null ? earthquake.getNetwork() : "N/A"
                );
                // In a real system, this is where you might trigger other notifications (email, SMS, etc.)
            }
        }
    }
}
