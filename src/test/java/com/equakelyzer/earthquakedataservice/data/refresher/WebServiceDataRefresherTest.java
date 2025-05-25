package com.equakelyzer.earthquakedataservice.data.refresher;

import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;
import com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.WebServiceClientStrategy;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoryRepository;
import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import com.equakelyzer.earthquakedataservice.service.SignificantEarthquakeAlertingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebServiceDataRefresherTest {

    @Mock
    private WebServiceClientStrategy mockWebServiceClientStrategy;

    @Mock
    private EarthQuakeHistoryRepository mockEarthQuakeHistoryRepository;

    @Mock
    private SignificantEarthquakeAlertingService mockSignificantEarthquakeAlertingService;

    @InjectMocks
    private WebServiceDataRefresher webServiceDataRefresher;

    private EarthQuakePhysicalDto dto1, dto2, dto3;
    private EarthQuakeHistory history1, history2, history3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        dto1 = new EarthQuakePhysicalDto(now.minusHours(1), 34.0, -118.0, 10.0, 5.0, "LA Area", "event1", "mw", 0, "url1", "reviewed", 500, "us");
        history1 = new EarthQuakeHistory(now.minusHours(1), 34.0, -118.0, 10.0, 5.0, "LA Area", "USGS", "event1", "mw", 0, "url1", "reviewed", 500, "us");

        dto2 = new EarthQuakePhysicalDto(now.minusHours(2), 36.0, -120.0, 5.0, 4.5, "SF Bay Area", "event2", "ml", 1, "url2", "reviewed", 400, "ci");
        history2 = new EarthQuakeHistory(now.minusHours(2), 36.0, -120.0, 5.0, 4.5, "SF Bay Area", "USGS", "event2", "ml", 1, "url2", "reviewed", 400, "ci");

        dto3 = new EarthQuakePhysicalDto(now.minusHours(3), 38.0, -122.0, 15.0, 3.0, "North Bay", "event3", "md", 0, "url3", "automatic", 300, "nc");
        history3 = new EarthQuakeHistory(now.minusHours(3), 38.0, -122.0, 15.0, 3.0, "North Bay", "USGS", "event3", "md", 0, "url3", "automatic", 300, "nc");
    }

    @Test
    void refresh_savesNewEventsAndAlerts() {
        List<EarthQuakePhysicalDto> fetchedDtos = Arrays.asList(dto1, dto2, dto3);
        when(mockWebServiceClientStrategy.fetch()).thenReturn(fetchedDtos);

        // dto1 is new, dto2 already exists, dto3 is new
        when(mockEarthQuakeHistoryRepository.findByUsgsEventId("event1")).thenReturn(Optional.empty());
        when(mockEarthQuakeHistoryRepository.save(any(EarthQuakeHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved entity

        when(mockEarthQuakeHistoryRepository.findByUsgsEventId("event2")).thenReturn(Optional.of(history2));
        // No save for history2

        when(mockEarthQuakeHistoryRepository.findByUsgsEventId("event3")).thenReturn(Optional.empty());
        // .save for history3 will be covered by the general thenAnswer for save

        webServiceDataRefresher.refresh();

        // Verify save was called for new events (history1 and history3)
        ArgumentCaptor<EarthQuakeHistory> historyCaptor = ArgumentCaptor.forClass(EarthQuakeHistory.class);
        verify(mockEarthQuakeHistoryRepository, times(2)).save(historyCaptor.capture());
        List<EarthQuakeHistory> savedHistories = historyCaptor.getAllValues();
        assertEquals(2, savedHistories.size());
        assertTrue(savedHistories.stream().anyMatch(h -> "event1".equals(h.getUsgsEventId())));
        assertTrue(savedHistories.stream().anyMatch(h -> "event3".equals(h.getUsgsEventId())));

        // Verify findByUsgsEventId was called for all
        verify(mockEarthQuakeHistoryRepository).findByUsgsEventId("event1");
        verify(mockEarthQuakeHistoryRepository).findByUsgsEventId("event2");
        verify(mockEarthQuakeHistoryRepository).findByUsgsEventId("event3");

        // Verify alerting service was called with the newly saved events
        ArgumentCaptor<List<EarthQuakeHistory>> alertListCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockSignificantEarthquakeAlertingService).checkAndAlertForSignificantEarthquakes(alertListCaptor.capture());
        List<EarthQuakeHistory> alertedHistories = alertListCaptor.getValue();
        assertEquals(2, alertedHistories.size());
        assertTrue(alertedHistories.stream().anyMatch(h -> "event1".equals(h.getUsgsEventId())));
        assertTrue(alertedHistories.stream().anyMatch(h -> "event3".equals(h.getUsgsEventId())));
    }

    @Test
    void refresh_handlesNoFetchedEvents() {
        when(mockWebServiceClientStrategy.fetch()).thenReturn(Collections.emptyList());

        webServiceDataRefresher.refresh();

        verify(mockEarthQuakeHistoryRepository, never()).save(any());
        verify(mockSignificantEarthquakeAlertingService, never()).checkAndAlertForSignificantEarthquakes(any());
    }

    @Test
    void refresh_handlesAllEventsAreDuplicates() {
        List<EarthQuakePhysicalDto> fetchedDtos = Arrays.asList(dto1, dto2);
        when(mockWebServiceClientStrategy.fetch()).thenReturn(fetchedDtos);

        when(mockEarthQuakeHistoryRepository.findByUsgsEventId("event1")).thenReturn(Optional.of(history1));
        when(mockEarthQuakeHistoryRepository.findByUsgsEventId("event2")).thenReturn(Optional.of(history2));

        webServiceDataRefresher.refresh();

        verify(mockEarthQuakeHistoryRepository, never()).save(any());
        // Alerting service should still be called, but with an empty list if nothing was newly saved.
        // Or, if the implementation only calls it when newlySavedEarthquakes is not empty, then never().
        // Current implementation calls if newlySavedEarthquakes is not empty.
        verify(mockSignificantEarthquakeAlertingService, never()).checkAndAlertForSignificantEarthquakes(argThat(List::isEmpty));
        // More precisely, if the list is truly empty, it won't be called.
        // Let's refine: if newlySavedEarthquakes list is empty, the check inside refresh() prevents the call.
         ArgumentCaptor<List<EarthQuakeHistory>> alertListCaptor = ArgumentCaptor.forClass(List.class);
         // The service method `checkAndAlertForSignificantEarthquakes` is called only if `newlySavedEarthquakes` is not empty.
         // So, in this case where all are duplicates, it should not be called.
         verify(mockSignificantEarthquakeAlertingService, never()).checkAndAlertForSignificantEarthquakes(any());
    }


    @Test
    void refresh_usesFallbackQueryWhenUsgsIdIsBlank() {
        // Create a DTO with a blank usgsEventId to test the fallback
        EarthQuakePhysicalDto dtoWithBlankId = new EarthQuakePhysicalDto(
                LocalDateTime.now(), 39.0, -121.0, 20.0, 4.0, "Location With Blank ID",
                "", "mw", 0, "url_blank", "reviewed", 350, "bk"
        );
        List<EarthQuakePhysicalDto> fetchedDtos = Collections.singletonList(dtoWithBlankId);
        when(mockWebServiceClientStrategy.fetch()).thenReturn(fetchedDtos);

        // Simulate that the fallback query also finds nothing (it's a new event)
        when(mockEarthQuakeHistoryRepository.findByEventDateAndLatitudeAndLongitudeAndMagnitudeAndSource(
                any(LocalDateTime.class), anyDouble(), anyDouble(), anyDouble(), eq("USGS")
        )).thenReturn(Optional.empty());

        when(mockEarthQuakeHistoryRepository.save(any(EarthQuakeHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        webServiceDataRefresher.refresh();

        // Verify findByUsgsEventId is NOT called with blank string if logic prevents it, or it's called and returns empty.
        // The current logic in WebServiceDataRefresher for history.getUsgsEventId().isBlank() will prevent findByUsgsEventId.
        verify(mockEarthQuakeHistoryRepository, never()).findByUsgsEventId(""); // Should not be called with blank

        // Verify the fallback query was used
        verify(mockEarthQuakeHistoryRepository).findByEventDateAndLatitudeAndLongitudeAndMagnitudeAndSource(
                eq(dtoWithBlankId.earthQuakeLocalDateTime()),
                eq(dtoWithBlankId.latitude()),
                eq(dtoWithBlankId.longitude()),
                eq(dtoWithBlankId.magnitude()),
                eq("USGS")
        );

        // Verify save was called
        verify(mockEarthQuakeHistoryRepository, times(1)).save(any(EarthQuakeHistory.class));

        // Verify alerting service was called with the newly saved event
        ArgumentCaptor<List<EarthQuakeHistory>> alertListCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockSignificantEarthquakeAlertingService).checkAndAlertForSignificantEarthquakes(alertListCaptor.capture());
        assertEquals(1, alertListCaptor.getValue().size());
        assertEquals("", alertListCaptor.getValue().get(0).getUsgsEventId()); // History object will have blank usgsEventId
    }
}
