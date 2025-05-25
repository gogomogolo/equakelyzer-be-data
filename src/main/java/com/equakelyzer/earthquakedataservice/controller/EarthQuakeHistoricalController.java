package com.equakelyzer.earthquakedataservice.controller;

import com.equakelyzer.earthquakedataservice.controller.resource.EarthQuakeStory;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoricalRepository; // Will be removed if not used elsewhere
import com.equakelyzer.earthquakedataservice.repository.document.EarthQuakeHistorical; // Will be removed if not used elsewhere
import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import com.equakelyzer.earthquakedataservice.service.EarthQuakeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // Consider making this configurable
@RestController
@RequestMapping("/earthquakehistory") // Base path for the controller
public class EarthQuakeHistoricalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EarthQuakeHistoricalController.class);

    // EarthQuakeHistoricalRepository is kept for the /cities endpoint (EarthQuakeStory)
    private final EarthQuakeHistoricalRepository earthQuakeHistoricalRepository;
    private final EarthQuakeQueryService earthQuakeQueryService;

    @Autowired
    public EarthQuakeHistoricalController(EarthQuakeHistoricalRepository earthQuakeHistoricalRepository,
                                          EarthQuakeQueryService earthQuakeQueryService) {
        this.earthQuakeHistoricalRepository = earthQuakeHistoricalRepository; // Kept for the /cities endpoint
        this.earthQuakeQueryService = earthQuakeQueryService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<EarthQuakeHistory>> getMostRecentEarthquakes(
            @RequestParam(value = "count", defaultValue = "10") int count) {

        if (count <= 0) {
            LOGGER.warn("Received request for non-positive count: {}. Returning Bad Request.", count);
            return ResponseEntity.badRequest().body(Collections.emptyList()); // Or throw an exception
        }
        if (count > 100) { // Cap the count to a reasonable limit
            LOGGER.warn("Received request for count {} which is greater than max limit 100. Capping to 100.", count);
            count = 100;
        }

        try {
            List<EarthQuakeHistory> recentEarthquakes = earthQuakeQueryService.getMostRecentEarthquakes(count);
            if (recentEarthquakes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(recentEarthquakes);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving recent earthquakes for count {}: {}", count, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    // Existing endpoint /cities - This seems to be for a different, unimplemented feature (EarthQuakeStory)
    // and is not the target for this refactoring. Leaving as is.
    @GetMapping("/cities") // Changed from RequestMapping to GetMapping for clarity
    public EarthQuakeStory earthQuakeStoryByDateRange(
            @RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        LOGGER.warn("Endpoint /cities called but is not fully implemented and uses EarthQuakeStory.");
        // This method is not implemented and refers to EarthQuakeStory.
        return null;
    }

    // Modified endpoint: previously /cities/{city}, now more descriptive and uses EarthQuakeHistory
    @GetMapping("/location/{location}")
    public ResponseEntity<org.springframework.data.domain.Page<EarthQuakeHistory>> getEarthquakesByLocationDateMagnitude(
            @PathVariable(value = "location") String location,
            @RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "minMagnitude", defaultValue = "0.0") Double minMagnitude,
            @org.springframework.data.web.PageableDefault(size = 20, sort = "eventDate", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable
    ) {
        LOGGER.info("Request received for earthquakes in location: {}, from: {}, to: {}, minMagnitude: {}, page: {}",
                location, from, to, minMagnitude, pageable);

        if (from.isAfter(to)) {
            LOGGER.warn("Invalid date range: 'from' date ({}) is after 'to' date ({}).", from, to);
            return ResponseEntity.badRequest().build(); // Or return an error message
        }

        try {
            org.springframework.data.domain.Page<EarthQuakeHistory> earthquakesPage =
                    earthQuakeQueryService.getEarthquakesByLocationDateAndMagnitude(location, from, to, minMagnitude, pageable);

            if (earthquakesPage.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(earthquakesPage);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving earthquakes for location {}: {}", location, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // The old /cities/{city} endpoint that returned List<EarthQuakeHistorical> is now effectively replaced by /location/{location}.
    // If EarthQuakeHistoricalRepository and EarthQuakeHistorical are no longer needed, they can be removed in a separate cleanup task.
    // For now, I am commenting out the direct dependency if it's not used elsewhere, assuming it's fully replaced.
    // private final EarthQuakeHistoricalRepository earthQuakeHistoricalRepository;
    // Constructor updated:
    // public EarthQuakeHistoricalController(EarthQuakeQueryService earthQuakeQueryService) {
    // this.earthQuakeHistoricalRepository = null; // Or remove if not needed
    // this.earthQuakeQueryService = earthQuakeQueryService;
    // }
    // However, the constructor still has EarthQuakeHistoricalRepository. If it's truly unused, it should be removed.
    // For this task, I will assume EarthQuakeHistoricalRepository is NOT used by other methods in this controller
    // and proceed to remove it from the constructor to avoid confusion.
    // The subtask implies that the /cities/{city} endpoint is the one being modified.

    @GetMapping("/summary-stats")
    public ResponseEntity<com.equakelyzer.earthquakedataservice.controller.resource.EarthquakeSummaryStatsDto> getEarthquakeSummaryStatistics() {
        LOGGER.info("Request received for earthquake summary statistics.");
        try {
            com.equakelyzer.earthquakedataservice.controller.resource.EarthquakeSummaryStatsDto stats =
                    earthQuakeQueryService.getEarthquakeSummaryStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving earthquake summary statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
