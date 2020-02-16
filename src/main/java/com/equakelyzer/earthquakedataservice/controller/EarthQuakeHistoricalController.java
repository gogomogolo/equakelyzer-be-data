package com.equakelyzer.earthquakedataservice.controller;

import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoricalRepository;
import com.equakelyzer.earthquakedataservice.repository.document.EarthQuakeHistorical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class EarthQuakeHistoricalController {
    private EarthQuakeHistoricalRepository earthQuakeHistoricalRepository;

    @Autowired
    EarthQuakeHistoricalController(EarthQuakeHistoricalRepository earthQuakeHistoricalRepository){
        this.earthQuakeHistoricalRepository = earthQuakeHistoricalRepository;
    }

    @GetMapping("/earthquakehistory/{city}")
    public List<EarthQuakeHistorical> earthQuakeHistoricalsByCityAndDateRange(
            @PathVariable(value = "city") String city,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ){
        if (from == null && to == null){
            return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCity(city);
        }

        return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCityAndEarthQuakeLocalDateTimeBetween(city, from, to);
    }

    @GetMapping("/earthquakehistory/{city}/{district}")
    public List<EarthQuakeHistorical> earthQuakeHistoricalsByCityAndDistrict(
            @PathVariable(value = "city") String city,
            @PathVariable(value = "district") String district,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  LocalDateTime to
    ){
        if (from == null && to == null){
            return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCityAndDistrict(city, district);
        }

        return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCityAndDistrictAndEarthQuakeLocalDateTimeBetween(city, district, from, to);
    }

    @GetMapping("/earthquakehistory/{city}/{district}/{town}")
    public List<EarthQuakeHistorical> earthQuakeHistoricalsByCityAndDistrictAndTown(
            @PathVariable(value = "city") String city,
            @PathVariable(value = "district") String district,
            @PathVariable(value = "town") String town,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ){
        if (from == null && to == null){
            return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCityAndDistrictAndTown(city, district, town);
        }

        return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCityAndDistrictAndTownAndEarthQuakeLocalDateTimeBetween(city, district, town, from, to);
    }
}
