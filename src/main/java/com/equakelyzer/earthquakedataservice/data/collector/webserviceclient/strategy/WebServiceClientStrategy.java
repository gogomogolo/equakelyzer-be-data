package com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy;

import com.equakelyzer.earthquakedataservice.data.EarthQuakeData;

import java.util.List;

public interface WebServiceClientStrategy {
    List<EarthQuakeData> fetch();
}
