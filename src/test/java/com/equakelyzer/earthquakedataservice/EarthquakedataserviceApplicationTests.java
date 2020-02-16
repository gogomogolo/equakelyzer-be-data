package com.equakelyzer.earthquakedataservice;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;
import com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy.BogaziciObservatoryCrawler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class EarthquakedataserviceApplicationTests {

    @Test
    void contextLoads() {
        BogaziciObservatoryCrawler bogaziciObservatoryCrawler = new BogaziciObservatoryCrawler();

        List<EarthQuakeData> a = bogaziciObservatoryCrawler.crawl();
    }

}
