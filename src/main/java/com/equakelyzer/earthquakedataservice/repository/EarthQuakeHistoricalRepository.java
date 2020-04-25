package com.equakelyzer.earthquakedataservice.repository;

import com.equakelyzer.earthquakedataservice.repository.document.EarthQuakeHistorical;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EarthQuakeHistoricalRepository extends MongoRepository<EarthQuakeHistorical, String> {
    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCityAndEarthQuakeLocalDateTimeBetween(String city, LocalDateTime from, LocalDateTime to);
    EarthQuakeHistorical findFirstByOrderByEarthQuakeLocalDateTimeDesc();
}
