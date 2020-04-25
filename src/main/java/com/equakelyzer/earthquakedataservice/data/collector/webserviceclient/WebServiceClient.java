package com.equakelyzer.earthquakedataservice.data.collector.webserviceclient;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollector;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollectorType;

import java.util.List;

public class WebServiceClient implements DataCollector {
    @Override
    public List<EarthQuakeData> getDataByDataCollectorType(DataCollectorType dataCollectorType) {
        return null;
    }
}
