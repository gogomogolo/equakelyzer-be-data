package com.equakelyzer.earthquakedataservice.repository.state;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class EarthQuakeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime eventDate;
    private Double latitude;
    private Double longitude;
    private Double depth;
    private Double magnitude; // Renamed from 'force'
    private String locationDescription;
    private String source; // To indicate the data source, e.g., "USGS"

    // New fields propagated from EarthQuakePhysicalDto
    private String usgsEventId;       // from 'code'
    private String magnitudeType;     // from 'magType'
    private Integer tsunamiAlert;     // from 'tsunami' (0 or 1)
    private String usgsEventUrl;      // from 'url'
    private String status;            // from 'status' (e.g., "automatic", "reviewed")
    private Integer significance;     // from 'sig'
    private String network;           // from 'net'


    public EarthQuakeHistory() {
    }

    public EarthQuakeHistory(LocalDateTime eventDate, Double latitude, Double longitude, Double depth,
                             Double magnitude, String locationDescription, String source,
                             String usgsEventId, String magnitudeType, Integer tsunamiAlert,
                             String usgsEventUrl, String status, Integer significance, String network) {
        this.eventDate = eventDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
        this.magnitude = magnitude;
        this.locationDescription = locationDescription;
        this.source = source;
        this.usgsEventId = usgsEventId;
        this.magnitudeType = magnitudeType;
        this.tsunamiAlert = tsunamiAlert;
        this.usgsEventUrl = usgsEventUrl;
        this.status = status;
        this.significance = significance;
        this.network = network;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDepth() {
        return depth;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }

    public Double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Double magnitude) {
        this.magnitude = magnitude;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUsgsEventId() {
        return usgsEventId;
    }

    public void setUsgsEventId(String usgsEventId) {
        this.usgsEventId = usgsEventId;
    }

    public String getMagnitudeType() {
        return magnitudeType;
    }

    public void setMagnitudeType(String magnitudeType) {
        this.magnitudeType = magnitudeType;
    }

    public Integer getTsunamiAlert() {
        return tsunamiAlert;
    }

    public void setTsunamiAlert(Integer tsunamiAlert) {
        this.tsunamiAlert = tsunamiAlert;
    }

    public String getUsgsEventUrl() {
        return usgsEventUrl;
    }

    public void setUsgsEventUrl(String usgsEventUrl) {
        this.usgsEventUrl = usgsEventUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSignificance() {
        return significance;
    }

    public void setSignificance(Integer significance) {
        this.significance = significance;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarthQuakeHistory that = (EarthQuakeHistory) o;
        // Using a combination of fields that should uniquely identify an earthquake event from a source
        // Added usgsEventId to the uniqueness check.
        return Objects.equals(usgsEventId, that.usgsEventId) && // Primary check for USGS data
               Objects.equals(eventDate, that.eventDate) &&
               Objects.equals(latitude, that.latitude) &&
               Objects.equals(longitude, that.longitude) &&
               Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        // Added usgsEventId to the hash.
        return Objects.hash(usgsEventId, eventDate, latitude, longitude, source);
    }

    @Override
    public String toString() {
        return "EarthQuakeHistory{" +
                "id=" + id +
                ", eventDate=" + eventDate +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", depth=" + depth +
                ", magnitude=" + magnitude +
                ", locationDescription='" + locationDescription + '\'' +
                ", source='" + source + '\'' +
                ", usgsEventId='" + usgsEventId + '\'' +
                ", magnitudeType='" + magnitudeType + '\'' +
                ", tsunamiAlert=" + tsunamiAlert +
                ", usgsEventUrl='" + usgsEventUrl + '\'' +
                ", status='" + status + '\'' +
                ", significance=" + significance +
                ", network='" + network + '\'' +
                '}';
    }
}
