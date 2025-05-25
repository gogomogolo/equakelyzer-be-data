package com.equakelyzer.earthquakedataservice.service;

import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoryRepository;
import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EarthQuakeQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EarthQuakeQueryService.class);

    private final EarthQuakeHistoryRepository earthQuakeHistoryRepository;

    @Autowired
    public EarthQuakeQueryService(EarthQuakeHistoryRepository earthQuakeHistoryRepository) {
        this.earthQuakeHistoryRepository = earthQuakeHistoryRepository;
    }

    /**
     * Retrieves the N most recent earthquakes.
     *
     * @param count The number of recent earthquakes to retrieve.
     * @return A list of EarthQuakeHistory objects.
     */
    public List<EarthQuakeHistory> getMostRecentEarthquakes(int count) {
        LOGGER.info("Requesting the {} most recent earthquakes.", count);
        // Using the findFirstNByOrderByEventDateDesc method from the repository
        // Spring Data JPA interprets "findFirstN" as limiting the result size,
        // so we directly pass the count to the method if it's defined like:
        // List<EarthQuakeHistory> findFirstNByOrderByEventDateDesc(int n);
        // Or, if using Pageable for more flexibility:
        // List<EarthQuakeHistory> findByOrderByEventDateDesc(Pageable pageable);
        // The repository method was defined as findFirstNByOrderByEventDateDesc(Pageable pageable)
        // So we need to use PageRequest.of(0, count) for the first page with 'count' elements.
        return earthQuakeHistoryRepository.findFirstNByOrderByEventDateDesc(PageRequest.of(0, count));
    }

    /**
     * Retrieves earthquakes by location, date range, and minimum magnitude, with pagination.
     *
     * @param location     The location string to search for (e.g., city).
     * @param fromDate     The start of the date range for the earthquake event.
     * @param toDate       The end of the date range for the earthquake event.
     * @param minMagnitude The minimum magnitude of the earthquake.
     * @param pageable     Pagination information.
     * @return A Page of EarthQuakeHistory objects matching the criteria.
     */
    public org.springframework.data.domain.Page<EarthQuakeHistory> getEarthquakesByLocationDateAndMagnitude(
            String location, LocalDateTime fromDate, LocalDateTime toDate, Double minMagnitude,
            org.springframework.data.domain.Pageable pageable) {

        LOGGER.info("Requesting earthquakes for location '{}', from {} to {}, minMagnitude {}, page {}",
                location, fromDate, toDate, minMagnitude, pageable);

        return earthQuakeHistoryRepository.findByLocationDescriptionContainingIgnoreCaseAndEventDateBetweenAndMagnitudeGreaterThanEqual(
                location, fromDate, toDate, minMagnitude, pageable
        );
    }

    /**
     * Counts the number of earthquakes that occurred after a specified date.
     *
     * @param dateTime The date and time after which to count earthquakes.
     * @return The total count of earthquakes.
     */
    public long countEarthquakesSince(LocalDateTime dateTime) {
        LOGGER.debug("Counting earthquakes since {}", dateTime);
        return earthQuakeHistoryRepository.countByEventDateAfter(dateTime);
    }

    /**
     * Finds the earthquake with the largest magnitude that occurred after a specified date.
     *
     * @param dateTime The date and time after which to find the earthquake.
     * @return An Optional containing the EarthQuakeHistory object with the largest magnitude,
     *         or empty if no earthquakes are found after the given date.
     */
    public java.util.Optional<EarthQuakeHistory> getLargestMagnitudeEarthquakeSince(LocalDateTime dateTime) {
        LOGGER.debug("Finding largest magnitude earthquake since {}", dateTime);
        return earthQuakeHistoryRepository.findFirstByEventDateAfterOrderByMagnitudeDesc(dateTime);
    }

    /**
     * Compiles summary statistics for earthquakes (counts and largest magnitude events
     * for the last 24 hours and last 7 days).
     *
     * @return EarthquakeSummaryStatsDto containing the compiled statistics.
     */
    public com.equakelyzer.earthquakedataservice.controller.resource.EarthquakeSummaryStatsDto getEarthquakeSummaryStats() {
        LOGGER.info("Compiling earthquake summary statistics.");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        long count24Hours = countEarthquakesSince(yesterday);
        long count7Days = countEarthquakesSince(sevenDaysAgo); // This will include the last 24 hours count as well

        // To get count for *only* the period between 7 days ago and 1 day ago,
        // you might need a different query or subtract: count7Days - count24Hours.
        // For this implementation, countLast7Days means all events in the last 7 days.

        java.util.Optional<EarthQuakeHistory> largestMag24HoursOpt = getLargestMagnitudeEarthquakeSince(yesterday);
        java.util.Optional<EarthQuakeHistory> largestMag7DaysOpt = getLargestMagnitudeEarthquakeSince(sevenDaysAgo);

        return new com.equakelyzer.earthquakedataservice.controller.resource.EarthquakeSummaryStatsDto(
                count24Hours,
                count7Days,
                largestMag24HoursOpt.orElse(null),
                largestMag7DaysOpt.orElse(null)
        );
    }
}
