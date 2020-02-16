package com.equakelyzer.earthquakedataservice.data.refresher.strategy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;
import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;
import com.equakelyzer.earthquakedataservice.data.collector.DataCollector;
import com.equakelyzer.earthquakedataservice.data.collector.crawler.WebCrawlerType;
import com.equakelyzer.earthquakedataservice.data.refresher.DataRefresher;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoricalRepository;
import com.equakelyzer.earthquakedataservice.repository.document.EarthQuakeHistorical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BogaziciObservatoryRefresher implements DataRefresher {

    private DataCollector dataCollector;
    private EarthQuakeHistoricalRepository earthQuakeHistoricalRepository;

    @Autowired
    public BogaziciObservatoryRefresher(@Qualifier("webCrawler") DataCollector dataCollector,
                                        EarthQuakeHistoricalRepository earthQuakeHistoricalRepository){
        this.dataCollector = dataCollector;
        this.earthQuakeHistoricalRepository = earthQuakeHistoricalRepository;
    }

    @Override
    public void refresh() {
         List<EarthQuakeData> earthQuakes = dataCollector.
                 getDataByDataCollectorType(WebCrawlerType.BOGAZICI_OBSERVATORY);

         EarthQuakeHistorical earthQuakeHistorical = earthQuakeHistoricalRepository.findFirstByOrderByEarthQuakeLocalDateTimeDesc();

         List<EarthQuakeHistorical> earthQuakeHistoricals = new ArrayList<>();
         earthQuakes.forEach(earthQuakeData -> {
             EarthQuakePhysicalDto earthQuakePhysicalDto = (EarthQuakePhysicalDto) earthQuakeData;

             if (earthQuakeHistorical == null){
                 earthQuakeHistoricals.add(createEarthQuakeHistorical(earthQuakePhysicalDto));
             } else if (earthQuakePhysicalDto.earthQuakeLocalDateTime().isAfter(earthQuakeHistorical.getEarthQuakeLocalDateTime())){
                 earthQuakeHistoricals.add(createEarthQuakeHistorical(earthQuakePhysicalDto));
             }
         });
         earthQuakeHistoricalRepository.saveAll(earthQuakeHistoricals);
    }

    private EarthQuakeHistorical createEarthQuakeHistorical(EarthQuakePhysicalDto earthQuakePhysicalDto){
        EarthQuakeHistorical newEarthQuakeHistorical = new EarthQuakeHistorical();

        newEarthQuakeHistorical.setEarthQuakeLocalDateTime(earthQuakePhysicalDto.earthQuakeLocalDateTime());
        newEarthQuakeHistorical.setDepth(earthQuakePhysicalDto.depth());
        newEarthQuakeHistorical.setForce(earthQuakePhysicalDto.force());
        newEarthQuakeHistorical.setLongitude(earthQuakePhysicalDto.longitude());
        newEarthQuakeHistorical.setLatitude(earthQuakePhysicalDto.latitude());
        newEarthQuakeHistorical.setCity("");
        newEarthQuakeHistorical.setDistrict("");
        newEarthQuakeHistorical.setTown("");

        String location = earthQuakePhysicalDto.location();

        String cityDistrictTownRegex = "(.*)\\-(.*) \\((\\w*)\\)";
        String cityDistrictRegex = "(.*) \\((\\w*)\\)";
        String cityTownRegex = "(.*)\\-\\((\\w*)\\)";

        Pattern pattern = Pattern.compile(cityDistrictTownRegex);
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()){
            newEarthQuakeHistorical.setCity(matcher.group(3));
            newEarthQuakeHistorical.setDistrict(matcher.group(2));
            newEarthQuakeHistorical.setTown(matcher.group(1));
        } else{
            pattern = Pattern.compile(cityDistrictRegex);
            matcher = pattern.matcher(location);

            if (matcher.find()) {
                newEarthQuakeHistorical.setCity(matcher.group(2));
                newEarthQuakeHistorical.setDistrict(matcher.group(1));
            } else {
                pattern = Pattern.compile(cityTownRegex);
                matcher = pattern.matcher(location);

                if (matcher.find()){
                    newEarthQuakeHistorical.setCity(matcher.group(2));
                    newEarthQuakeHistorical.setTown(matcher.group(1));
                } else {
                    newEarthQuakeHistorical.setCity(location);
                }
            }
        }

        return newEarthQuakeHistorical;
    }
}
