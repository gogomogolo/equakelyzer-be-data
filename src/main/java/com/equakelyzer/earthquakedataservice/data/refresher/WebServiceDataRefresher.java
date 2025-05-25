package com.equakelyzer.earthquakedataservice.data.refresher;

import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;
import com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.WebServiceClientStrategy;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoryRepository;
import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import com.equakelyzer.earthquakedataservice.service.SignificantEarthquakeAlertingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WebServiceDataRefresher implements DataRefresher {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceDataRefresher.class);
    private static final String USGS_SOURCE_IDENTIFIER = "USGS";

    private final WebServiceClientStrategy webServiceClientStrategy;
    private final EarthQuakeHistoryRepository earthQuakeHistoryRepository;
    private final SignificantEarthquakeAlertingService significantEarthquakeAlertingService;

    public WebServiceDataRefresher(WebServiceClientStrategy webServiceClientStrategy,
                                   EarthQuakeHistoryRepository earthQuakeHistoryRepository,
                                   SignificantEarthquakeAlertingService significantEarthquakeAlertingService) {
        this.webServiceClientStrategy = webServiceClientStrategy;
        this.earthQuakeHistoryRepository = earthQuakeHistoryRepository;
        this.significantEarthquakeAlertingService = significantEarthquakeAlertingService;
    }

    @Override
    public void refresh() {
        LOGGER.info("Starting data refresh using strategy: {}", webServiceClientStrategy.getClass().getSimpleName());
        List<EarthQuakeHistory> newlySavedEarthquakes = new ArrayList<>();
        try {
            List<EarthQuakePhysicalDto> earthQuakeDtoList = webServiceClientStrategy.fetch();

            if (earthQuakeDtoList == null || earthQuakeDtoList.isEmpty()) {
                LOGGER.info("No data fetched from {}.", webServiceClientStrategy.getClass().getSimpleName());
                return;
            }

            LOGGER.info("Fetched {} records from {}. Processing and saving...", earthQuakeDtoList.size(), webServiceClientStrategy.getClass().getSimpleName());
            int newRecordsSavedCount = 0;
            int recordsSkippedCount = 0;

            for (EarthQuakePhysicalDto dto : earthQuakeDtoList) {
                EarthQuakeHistory history = convertToHistory(dto);
                Optional<EarthQuakeHistory> existingRecord;

                if (USGS_SOURCE_IDENTIFIER.equals(history.getSource()) && history.getUsgsEventId() != null && !history.getUsgsEventId().isBlank()) {
                    existingRecord = earthQuakeHistoryRepository.findByUsgsEventId(history.getUsgsEventId());
                } else {
                    existingRecord = earthQuakeHistoryRepository.findByEventDateAndLatitudeAndLongitudeAndMagnitudeAndSource(
                            history.getEventDate(), history.getLatitude(), history.getLongitude(), history.getMagnitude(), history.getSource());
                }

                if (existingRecord.isPresent()) {
                    LOGGER.debug("Record already exists (based on query for {}), skipping: {}",
                            (USGS_SOURCE_IDENTIFIER.equals(history.getSource()) && history.getUsgsEventId() != null) ? "usgsEventId=" + history.getUsgsEventId() : "event details",
                            history);
                    recordsSkippedCount++;
                } else {
                    try {
                        EarthQuakeHistory savedEntity = earthQuakeHistoryRepository.save(history);
                        newlySavedEarthquakes.add(savedEntity); // Add to list for alerting
                        newRecordsSavedCount++;
                        LOGGER.debug("Saved new earthquake record: {}", savedEntity);
                    } catch (DataIntegrityViolationException e) {
                        LOGGER.warn("Data integrity violation while saving record (possibly duplicate if check failed or not specific enough): {}. Error: {}", history, e.getMessage());
                        recordsSkippedCount++;
                    } catch (Exception e) {
                        LOGGER.error("Error saving earthquake record: {}. Error: {}", history, e.getMessage(), e);
                        recordsSkippedCount++; // Also skip on other save errors
                    }
                }
            }
            LOGGER.info("Data refresh completed for {}. New records saved: {}. Records skipped (already existing or error): {}.",
                    webServiceClientStrategy.getClass().getSimpleName(), newRecordsSavedCount, recordsSkippedCount);

            // After processing all fetched data, check for significant earthquakes among the newly saved ones
            if (!newlySavedEarthquakes.isEmpty()) {
                significantEarthquakeAlertingService.checkAndAlertForSignificantEarthquakes(newlySavedEarthquakes);
            }

        } catch (Exception e) {
            LOGGER.error("Critical error during data refresh with strategy {}: {}",
                    webServiceClientStrategy.getClass().getSimpleName(), e.getMessage(), e);
        }
    }
    
    private EarthQuakeHistory convertToHistory(EarthQuakePhysicalDto dto) {
        return new EarthQuakeHistory(
                dto.earthQuakeLocalDateTime(),
                dto.latitude(),
                dto.longitude(),
                dto.depth(),
                dto.magnitude(), // This is the magnitude from USGS
                dto.location(),
                USGS_SOURCE_IDENTIFIER,
                dto.usgsEventId(),
                dto.magnitudeType(),
                dto.tsunamiAlert(),
                dto.usgsEventUrl(),
                dto.status(),
                dto.significance(),
                dto.network()
        );
    }
}
