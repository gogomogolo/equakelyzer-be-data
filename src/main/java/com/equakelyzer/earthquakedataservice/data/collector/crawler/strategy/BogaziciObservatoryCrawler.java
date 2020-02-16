package com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;
import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;
import com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy.adapter.BogaziciObservatoryResultsAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BogaziciObservatoryCrawler implements WebCrawlerStrategy {
    private static final String BOGAZICI_OBSERVATORY_URL = "http://www.koeri.boun.edu.tr/scripts/lst0.asp";
    private static final String EARTH_QUAKE_DATA_TAG_HTML = "pre";

    private static final String NEW_LINE_REGEX = "\\r?\\n";

    @Override
    public List<EarthQuakeData> crawl() {
        List<EarthQuakeData> earthQuakeData = new ArrayList<>();

        try {
            Document document = Jsoup.connect(BOGAZICI_OBSERVATORY_URL).get();
            Elements elements = document.getElementsByTag(EARTH_QUAKE_DATA_TAG_HTML);

            List<String> urlContent = Arrays.asList(elements.text().split(NEW_LINE_REGEX));
            List<String> earthQuakes = urlContent.subList(6, urlContent.size());

            earthQuakes.forEach(line -> {
                EarthQuakePhysicalDto earthQuakePhysicalDto = BogaziciObservatoryResultsAdapter.adapt(line);
                earthQuakeData.add(earthQuakePhysicalDto);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return earthQuakeData;
    }
}
