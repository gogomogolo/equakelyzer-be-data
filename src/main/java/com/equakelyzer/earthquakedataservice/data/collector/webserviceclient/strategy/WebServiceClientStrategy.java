package com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakePhysicalDto;

import java.util.List;

public interface WebServiceClientStrategy {
    List<EarthQuakePhysicalDto> fetch();
}
