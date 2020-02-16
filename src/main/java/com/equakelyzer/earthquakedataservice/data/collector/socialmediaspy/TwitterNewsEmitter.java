package com.equakelyzer.earthquakedataservice.data.collector.socialmediaspy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollector;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollectorType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "twitterNewsEmitter")
public class TwitterNewsEmitter implements DataCollector {

    @Override
    public List<EarthQuakeData> getDataByDataCollectorType(DataCollectorType dataCollectorType) {
        return null;
    }
}
