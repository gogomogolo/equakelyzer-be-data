package com.equakelyzer.earthquakedataservice.data.collector.crawler.strategy.adapter;

import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class BogaziciObservatoryResultsAdapter {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    public static EarthQuakePhysicalDto adapt(String line){
        String[] structuredEarthQuakeData = line.split("\\s+");

        LocalDateTime earthQuakeLocalDateTime = LocalDateTime.parse(
                String.format("%s %s", structuredEarthQuakeData[0], structuredEarthQuakeData[1]),
                DATETIME_FORMATTER);

        double latitude = Double.parseDouble(structuredEarthQuakeData[2]);
        double longitude =  Double.parseDouble(structuredEarthQuakeData[3]);
        double depth =  Double.parseDouble(structuredEarthQuakeData[4]);
        double force = structuredEarthQuakeData[7].contentEquals("-.-") ?
                Double.parseDouble(structuredEarthQuakeData[6]):
                Double.parseDouble(structuredEarthQuakeData[7]);
        String location = String.join(" ",
                Arrays.copyOfRange(structuredEarthQuakeData, 8, structuredEarthQuakeData.length-1));

        return new EarthQuakePhysicalDto(
                earthQuakeLocalDateTime,
                latitude,
                longitude,
                depth,
                force,
                location);
    }
}
