package com.equakelyzer.earthquakedataservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@EnableMongoRepositories
@EnableScheduling
@SpringBootApplication
public class EarthquakedataserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EarthquakedataserviceApplication.class, args);
    }

}
