package com.equakelyzer.earthquakedataservice.configuration;

import com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.USGSEarthquakeWebServiceStrategy;
import com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.WebServiceClientStrategy;
import com.equakelyzer.earthquakedataservice.data.refresher.DataRefresher;
import com.equakelyzer.earthquakedataservice.data.refresher.WebServiceDataRefresher;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoryRepository;
import com.equakelyzer.earthquakedataservice.service.SignificantEarthquakeAlertingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EarthQuakeMonitoringServiceConfiguration {

    @Value("${usgs.api.baseurl}")
    private String usgsApiBaseUrl;

    @Value("${usgs.api.format}")
    private String usgsApiFormat;

    @Value("${usgs.api.region.turkey.minlatitude}")
    private double turkeyMinLatitude;

    @Value("${usgs.api.region.turkey.maxlatitude}")
    private double turkeyMaxLatitude;

    @Value("${usgs.api.region.turkey.minlongitude}")
    private double turkeyMinLongitude;

    @Value("${usgs.api.region.turkey.maxlongitude}")
    private double turkeyMaxLongitude;

    @Value("${usgs.api.minmagnitude}")
    private double minMagnitude;

    @Value("${usgs.api.retry.maxattempts:3}")
    private int maxRetryAttempts;

    @Value("${usgs.api.retry.delayms:5000}")
    private long retryDelayMs;


    @Bean
    public WebServiceClientStrategy usgsEarthquakeWebServiceStrategy() {
        return new USGSEarthquakeWebServiceStrategy(
                usgsApiBaseUrl,
                usgsApiFormat,
                turkeyMinLatitude,
                turkeyMaxLatitude,
                turkeyMinLongitude,
                turkeyMaxLongitude,
                minMagnitude,
                maxRetryAttempts,
                retryDelayMs
        );
    }

    @Bean
    public DataRefresher webServiceDataRefresher(WebServiceClientStrategy usgsEarthquakeWebServiceStrategy,
                                                 EarthQuakeHistoryRepository earthQuakeHistoryRepository,
                                                 SignificantEarthquakeAlertingService significantEarthquakeAlertingService) {
        return new WebServiceDataRefresher(usgsEarthquakeWebServiceStrategy, earthQuakeHistoryRepository, significantEarthquakeAlertingService);
    }

    @Bean
    public List<DataRefresher> dataRefreshers(DataRefresher webServiceDataRefresher
                                              // BogaziciObservatoryRefresher bogaziciObservatoryRefresher // Disabled for now
                                              // Add other refreshers here if any in the future
                                              ){
        List<DataRefresher> dataRefreshers = new ArrayList<>();
        dataRefreshers.add(webServiceDataRefresher);
        // dataRefreshers.add(bogaziciObservatoryRefresher);
        return dataRefreshers;
    }
}
