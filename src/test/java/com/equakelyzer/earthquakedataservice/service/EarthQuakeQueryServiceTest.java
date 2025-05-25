package com.equakelyzer.earthquakedataservice.service;

import com.equakelyzer.earthquakedataservice.controller.resource.EarthquakeSummaryStatsDto;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoryRepository;
import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EarthQuakeQueryServiceTest {

    @Mock
    private EarthQuakeHistoryRepository earthQuakeHistoryRepository;

    @InjectMocks
    private EarthQuakeQueryService earthQuakeQueryService;

    private EarthQuakeHistory sampleEarthquake1;
    private EarthQuakeHistory sampleEarthquake2;

    @BeforeEach
    void setUp() {
        sampleEarthquake1 = new EarthQuakeHistory(LocalDateTime.now().minusHours(1), 34.0, -118.0, 10.0, 5.5, "Near LA", "USGS", "event1", "mw", 0, "url1", "reviewed", 500, "us");
        sampleEarthquake2 = new EarthQuakeHistory(LocalDateTime.now().minusHours(2), 36.0, -120.0, 5.0, 4.5, "Central CA", "USGS", "event2", "ml", 0, "url2", "reviewed", 400, "us");
    }

    @Test
    void getMostRecentEarthquakes_returnsCorrectData() {
        List<EarthQuakeHistory> expectedEarthquakes = Arrays.asList(sampleEarthquake1, sampleEarthquake2);
        Pageable pageable = PageRequest.of(0, 2);
        when(earthQuakeHistoryRepository.findFirstNByOrderByEventDateDesc(pageable)).thenReturn(expectedEarthquakes);

        List<EarthQuakeHistory> actualEarthquakes = earthQuakeQueryService.getMostRecentEarthquakes(2);

        assertEquals(2, actualEarthquakes.size());
        assertEquals(expectedEarthquakes, actualEarthquakes);
        verify(earthQuakeHistoryRepository).findFirstNByOrderByEventDateDesc(pageable);
    }

    @Test
    void getEarthquakesByLocationDateAndMagnitude_returnsCorrectData() {
        String location = "CA";
        LocalDateTime fromDate = LocalDateTime.now().minusDays(1);
        LocalDateTime toDate = LocalDateTime.now();
        Double minMagnitude = 4.0;
        Pageable pageable = PageRequest.of(0, 10);
        List<EarthQuakeHistory> content = Collections.singletonList(sampleEarthquake1);
        Page<EarthQuakeHistory> expectedPage = new PageImpl<>(content, pageable, content.size());

        when(earthQuakeHistoryRepository.findByLocationDescriptionContainingIgnoreCaseAndEventDateBetweenAndMagnitudeGreaterThanEqual(
                location, fromDate, toDate, minMagnitude, pageable
        )).thenReturn(expectedPage);

        Page<EarthQuakeHistory> actualPage = earthQuakeQueryService.getEarthquakesByLocationDateAndMagnitude(
                location, fromDate, toDate, minMagnitude, pageable
        );

        assertEquals(expectedPage, actualPage);
        assertFalse(actualPage.isEmpty());
        assertEquals(1, actualPage.getTotalElements());
        verify(earthQuakeHistoryRepository).findByLocationDescriptionContainingIgnoreCaseAndEventDateBetweenAndMagnitudeGreaterThanEqual(
                location, fromDate, toDate, minMagnitude, pageable
        );
    }

    @Test
    void getEarthquakeSummaryStats_calculatesCorrectly() {
        LocalDateTime now = LocalDateTime.now(); // Fixed point in time for consistent calculations
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // Mock repository calls
        when(earthQuakeHistoryRepository.countByEventDateAfter(yesterday)).thenReturn(5L);
        when(earthQuakeHistoryRepository.countByEventDateAfter(sevenDaysAgo)).thenReturn(20L);
        when(earthQuakeHistoryRepository.findFirstByEventDateAfterOrderByMagnitudeDesc(yesterday))
                .thenReturn(Optional.of(sampleEarthquake1)); // Mag 5.5
        when(earthQuakeHistoryRepository.findFirstByEventDateAfterOrderByMagnitudeDesc(sevenDaysAgo))
                .thenReturn(Optional.of(sampleEarthquake1)); // Assume same for simplicity, could be different

        EarthquakeSummaryStatsDto stats = earthQuakeQueryService.getEarthquakeSummaryStats();

        assertNotNull(stats);
        assertEquals(5L, stats.getCountLast24Hours());
        assertEquals(20L, stats.getCountLast7Days());

        assertNotNull(stats.getLargestMagnitudeEventLast24Hours());
        assertEquals(sampleEarthquake1.getMagnitude(), stats.getLargestMagnitudeEventLast24Hours().getMagnitude());
        assertEquals(sampleEarthquake1.getUsgsEventId(), stats.getLargestMagnitudeEventLast24Hours().getUsgsEventId());

        assertNotNull(stats.getLargestMagnitudeEventLast7Days());
        assertEquals(sampleEarthquake1.getMagnitude(), stats.getLargestMagnitudeEventLast7Days().getMagnitude());
        assertEquals(sampleEarthquake1.getUsgsEventId(), stats.getLargestMagnitudeEventLast7Days().getUsgsEventId());

        verify(earthQuakeHistoryRepository).countByEventDateAfter(yesterday);
        verify(earthQuakeHistoryRepository).countByEventDateAfter(sevenDaysAgo);
        verify(earthQuakeHistoryRepository).findFirstByEventDateAfterOrderByMagnitudeDesc(yesterday);
        verify(earthQuakeHistoryRepository).findFirstByEventDateAfterOrderByMagnitudeDesc(sevenDaysAgo);
    }

    @Test
    void getEarthquakeSummaryStats_handlesNoEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        when(earthQuakeHistoryRepository.countByEventDateAfter(any(LocalDateTime.class))).thenReturn(0L);
        when(earthQuakeHistoryRepository.findFirstByEventDateAfterOrderByMagnitudeDesc(any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        EarthquakeSummaryStatsDto stats = earthQuakeQueryService.getEarthquakeSummaryStats();

        assertNotNull(stats);
        assertEquals(0L, stats.getCountLast24Hours());
        assertEquals(0L, stats.getCountLast7Days());
        assertNull(stats.getLargestMagnitudeEventLast24Hours());
        assertNull(stats.getLargestMagnitudeEventLast7Days());
    }
}
