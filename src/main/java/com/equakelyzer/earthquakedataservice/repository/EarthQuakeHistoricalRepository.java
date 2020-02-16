package com.equakelyzer.earthquakedataservice.repository;

import com.equakelyzer.earthquakedataservice.repository.document.EarthQuakeHistorical;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EarthQuakeHistoricalRepository extends MongoRepository<EarthQuakeHistorical, String> {
    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCity(String city);
    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCityAndDistrict(String city, String district);
    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCityAndDistrictAndTown(String city, String district, String town);


    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCityAndEarthQuakeLocalDateTimeBetween(String city, LocalDateTime from, LocalDateTime to);
    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCityAndDistrictAndEarthQuakeLocalDateTimeBetween(String city, String district, LocalDateTime from, LocalDateTime to);
    List<EarthQuakeHistorical> findEarthQuakeHistoricalsByCityAndDistrictAndTownAndEarthQuakeLocalDateTimeBetween(String city, String district, String town, LocalDateTime from, LocalDateTime to);

    EarthQuakeHistorical findFirstByOrderByEarthQuakeLocalDateTimeDesc();

}
