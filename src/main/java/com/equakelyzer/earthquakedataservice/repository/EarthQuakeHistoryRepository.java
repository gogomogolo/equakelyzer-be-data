package com.equakelyzer.earthquakedataservice.repository;

import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
// Changed primary key type from Integer to Long to match EarthQuakeHistory.id
public interface EarthQuakeHistoryRepository extends JpaRepository<EarthQuakeHistory, Long> {

    /**
     * Finds an earthquake history record by its USGS Event ID.
     *
     * @param usgsEventId the unique event identifier from USGS
     * @return an Optional containing the EarthQuakeHistory if found, otherwise empty
     */
    Optional<EarthQuakeHistory> findByUsgsEventId(String usgsEventId);

    /**
     * Finds an earthquake history record by its key properties to check for existence,
     * typically used as a fallback or for non-USGS sources.
     *
     * @param eventDate the date and time of the event
     * @param latitude the latitude of the event
     * @param longitude the longitude of the event
     * @param magnitude the magnitude of the event
     * @param source the source of the data (e.g., "USGS")
     * @return an Optional containing the EarthQuakeHistory if found, otherwise empty
     */
    Optional<EarthQuakeHistory> findByEventDateAndLatitudeAndLongitudeAndMagnitudeAndSource(
            LocalDateTime eventDate, Double latitude, Double longitude, Double magnitude, String source);

    /**
     * Finds the top N most recent earthquakes.
     * The method name is interpreted by Spring Data JPA to create the query.
     * "findFirst" limits the result set. "OrderByEventDateDesc" sorts by the eventDate field in descending order.
     *
     * @param pageable Contains the number of results to return (N).
     * @return A list of the N most recent EarthQuakeHistory objects.
     */
    List<EarthQuakeHistory> findFirstNByOrderByEventDateDesc(org.springframework.data.domain.Pageable pageable);

    /**
     * Finds earthquakes by location (case-insensitive, containing), event date range,
     * and minimum magnitude, with pagination.
     *
     * @param location            The location string to search for (e.g., city).
     * @param fromDate            The start of the date range for the earthquake event.
     * @param toDate              The end of the date range for the earthquake event.
     * @param minMagnitude        The minimum magnitude of the earthquake.
     * @param pageable            Pagination information.
     * @return A Page of EarthQuakeHistory objects matching the criteria.
     */
    org.springframework.data.domain.Page<EarthQuakeHistory> findByLocationDescriptionContainingIgnoreCaseAndEventDateBetweenAndMagnitudeGreaterThanEqual(
            String location,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Double minMagnitude,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Counts the number of earthquakes that occurred after a specified date.
     *
     * @param date The date after which to count earthquakes.
     * @return The total count of earthquakes.
     */
    Long countByEventDateAfter(LocalDateTime date);

    /**
     * Finds the earthquake with the largest magnitude that occurred after a specified date.
     *
     * @param date The date after which to find the earthquake.
     * @return An Optional containing the EarthQuakeHistory object with the largest magnitude,
     *         or empty if no earthquakes are found after the given date.
     */
    Optional<EarthQuakeHistory> findFirstByEventDateAfterOrderByMagnitudeDesc(LocalDateTime date);
}
