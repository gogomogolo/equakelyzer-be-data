package com.equakelyzer.earthquakedataservice.controller.resource;

import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory; // Assuming full object is sometimes useful
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in JSON response
public class EarthquakeSummaryStatsDto {

    private long countLast24Hours;
    private long countLast7Days;
    private EarthQuakeHistory largestMagnitudeEventLast24Hours; // Or just Double for magnitude
    private EarthQuakeHistory largestMagnitudeEventLast7Days;   // Or just Double for magnitude

    // Constructors
    public EarthquakeSummaryStatsDto() {
    }

    public EarthquakeSummaryStatsDto(long countLast24Hours, long countLast7Days,
                                     EarthQuakeHistory largestMagnitudeEventLast24Hours,
                                     EarthQuakeHistory largestMagnitudeEventLast7Days) {
        this.countLast24Hours = countLast24Hours;
        this.countLast7Days = countLast7Days;
        this.largestMagnitudeEventLast24Hours = largestMagnitudeEventLast24Hours;
        this.largestMagnitudeEventLast7Days = largestMagnitudeEventLast7Days;
    }

    // Getters and Setters
    public long getCountLast24Hours() {
        return countLast24Hours;
    }

    public void setCountLast24Hours(long countLast24Hours) {
        this.countLast24Hours = countLast24Hours;
    }

    public long getCountLast7Days() {
        return countLast7Days;
    }

    public void setCountLast7Days(long countLast7Days) {
        this.countLast7Days = countLast7Days;
    }

    public EarthQuakeHistory getLargestMagnitudeEventLast24Hours() {
        return largestMagnitudeEventLast24Hours;
    }

    public void setLargestMagnitudeEventLast24Hours(EarthQuakeHistory largestMagnitudeEventLast24Hours) {
        this.largestMagnitudeEventLast24Hours = largestMagnitudeEventLast24Hours;
    }

    public EarthQuakeHistory getLargestMagnitudeEventLast7Days() {
        return largestMagnitudeEventLast7Days;
    }

    public void setLargestMagnitudeEventLast7Days(EarthQuakeHistory largestMagnitudeEventLast7Days) {
        this.largestMagnitudeEventLast7Days = largestMagnitudeEventLast7Days;
    }

    // toString() for logging (optional)
    @Override
    public String toString() {
        return "EarthquakeSummaryStatsDto{" +
               "countLast24Hours=" + countLast24Hours +
               ", countLast7Days=" + countLast7Days +
               ", largestMagnitudeEventLast24Hours=" + (largestMagnitudeEventLast24Hours != null ? largestMagnitudeEventLast24Hours.getMagnitude() : "null") +
               ", largestMagnitudeEventLast7Days=" + (largestMagnitudeEventLast7Days != null ? largestMagnitudeEventLast7Days.getMagnitude() : "null") +
               '}';
    }
}
