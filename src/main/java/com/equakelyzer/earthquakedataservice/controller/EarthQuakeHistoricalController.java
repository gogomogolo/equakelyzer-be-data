package com.equakelyzer.earthquakedataservice.controller;

import com.equakelyzer.earthquakedataservice.controller.resource.EarthQuakeStory;
import com.equakelyzer.earthquakedataservice.repository.EarthQuakeHistoricalRepository;
import com.equakelyzer.earthquakedataservice.repository.document.EarthQuakeHistorical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController("/earthquakehistory")
public class EarthQuakeHistoricalController {
    private EarthQuakeHistoricalRepository earthQuakeHistoricalRepository;

    @Autowired
    EarthQuakeHistoricalController(EarthQuakeHistoricalRepository earthQuakeHistoricalRepository){
        this.earthQuakeHistoricalRepository = earthQuakeHistoricalRepository;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/cities")
    public EarthQuakeStory earthQuakeStoryByDateRange(
            @RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {

    }

    @RequestMapping(method = RequestMethod.GET, path = "/cities/{city}")
    public List<EarthQuakeHistorical> earthQuakeHistoricalsByCityAndDateRange(
            @PathVariable(value = "city") String city,
            @RequestParam(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ){
        return earthQuakeHistoricalRepository.findEarthQuakeHistoricalsByCityAndEarthQuakeLocalDateTimeBetween(city, from, to);
    }
}
