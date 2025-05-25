package com.equakelyzer.earthquakedataservice.service;

import com.equakelyzer.earthquakedataservice.data.refresher.DataRefresher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EarthQuakeMonitoringService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EarthQuakeMonitoringService.class);
    private final List<DataRefresher> dataRefreshers;

    @Autowired
    EarthQuakeMonitoringService(List<DataRefresher> dataRefreshers) {
        this.dataRefreshers = dataRefreshers;
    }

    @Scheduled(fixedRateString = "${monitoring.schedule.fixedRate:300000}", initialDelayString = "${monitoring.schedule.initialDelay:10000}")
    public void serve() {
        if (dataRefreshers == null || dataRefreshers.isEmpty()) {
            LOGGER.warn("No data refreshers configured. Data monitoring will not run.");
            return;
        }
        LOGGER.info("Starting scheduled earthquake data refresh cycle for {} refreshers.", dataRefreshers.size());
        for (DataRefresher dataRefresher : dataRefreshers) {
            try {
                LOGGER.info("Executing refresher: {}", dataRefresher.getClass().getSimpleName());
                dataRefresher.refresh();
                LOGGER.info("Successfully executed refresher: {}", dataRefresher.getClass().getSimpleName());
            } catch (Exception e) {
                // Log the error and continue with the next refresher
                LOGGER.error("Error during data refresh with {}: {}",
                        dataRefresher.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        LOGGER.info("Finished scheduled earthquake data refresh cycle.");
    }
}
