package com.equakelyzer.earthquakedataservice.data.collector;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;

import java.util.List;

public interface DataCollector {
    List<EarthQuakeData> getDataByDataCollectorType(DataCollectorType dataCollectorType);
}
