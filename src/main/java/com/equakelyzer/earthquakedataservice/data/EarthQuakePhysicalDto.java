package com.equakelyzer.earthquakedataservice.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public class EarthQuakePhysicalDto implements EarthQuakeData{
    private final LocalDateTime earthQuakeLocalDateTime;
    private final double latitude;
    private final double longitude;
    private final double depth;
    private final double force;
    private final String location;

    @Override
    public EarthQuakePhysicalDto earthQuakeData() {
        return this;
    }
}
