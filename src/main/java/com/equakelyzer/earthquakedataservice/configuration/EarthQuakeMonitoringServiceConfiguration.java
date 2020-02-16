package com.equakelyzer.earthquakedataservice.configuration;

import com.equakelyzer.earthquakedataservice.data.refresher.DataRefresher;
import com.equakelyzer.earthquakedataservice.data.refresher.strategy.BogaziciObservatoryRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class EarthQuakeMonitoringServiceConfiguration {
    @Bean
    public List<DataRefresher> dataRefreshers(BogaziciObservatoryRefresher bogaziciObservatoryRefresher){
        List<DataRefresher> dataRefreshers = new ArrayList<>();

        dataRefreshers.add(bogaziciObservatoryRefresher);

        return dataRefreshers;
    }


}
