package com.equakelyzer.earthquakedataservice.service;

import com.equakelyzer.earthquakedataservice.data.refresher.DataRefresher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EarthQuakeMonitoringService {
    private List<DataRefresher> dataRefreshers;

    @Autowired
    EarthQuakeMonitoringService(List<DataRefresher> dataRefreshers){
        this.dataRefreshers = dataRefreshers;
    }

    @Scheduled(fixedRate = 300_000)
    public void serve(){
        dataRefreshers.forEach(DataRefresher::refresh);
    }
}
