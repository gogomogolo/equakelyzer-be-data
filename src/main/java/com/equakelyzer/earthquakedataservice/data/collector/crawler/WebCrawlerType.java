package com.equakelyzer.earthquakedataservice.data.collector.crawler;

import com.equakelyzer.earthquakedataservice.data.collector.DataCollectorType;
import com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy.BogaziciObservatoryCrawler;
import com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy.WebCrawlerStrategy;

public enum WebCrawlerType implements DataCollectorType {
    BOGAZICI_OBSERVATORY("Kandilli", new BogaziciObservatoryCrawler());

    private String message;
    private WebCrawlerStrategy webCrawlerStrategy;


    WebCrawlerType(String message, WebCrawlerStrategy webCrawlerStrategy) {
        this.message = message;
        this.webCrawlerStrategy = webCrawlerStrategy;
    }

    public WebCrawlerStrategy webCrawlerStrategy(){
        return this.webCrawlerStrategy;
    }
}
