package com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;

import java.util.List;

public interface WebCrawlerStrategy {
    List<EarthQuakeData> crawl();
}
