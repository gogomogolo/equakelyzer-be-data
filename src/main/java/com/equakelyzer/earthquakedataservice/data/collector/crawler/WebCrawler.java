package com.equakelyzer.earthquakedataservice.data.collector.crawler;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollector;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollectorType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(value = "webCrawler")
public class WebCrawler implements DataCollector {
    @Override
    public List<EarthQuakeData> getDataByDataCollectorType(DataCollectorType dataCollectorType) {
        WebCrawlerType webCrawlerType = (WebCrawlerType)dataCollectorType;
        return webCrawlerType.webCrawlerStrategy().crawl();
    }
}
