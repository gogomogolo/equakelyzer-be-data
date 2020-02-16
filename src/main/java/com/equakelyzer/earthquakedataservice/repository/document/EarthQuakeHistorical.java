package com.equakelyzer.earthquakedataservice.repository.document;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document
@CompoundIndexes({
        @CompoundIndex(name = "city_district", def = "{'city': 1, 'district': 1}"),
        @CompoundIndex(name = "city_district_town", def = "{'city': 1, 'district': 1, 'town': 1}")
})
@Getter
@Setter

public class EarthQuakeHistorical {
    @MongoId
    private String id;
    private LocalDateTime earthQuakeLocalDateTime;
    private double latitude;
    private double longitude;
    private double depth;
    private double force;
    @Indexed
    private String city;
    private String district;
    private String town;
}
