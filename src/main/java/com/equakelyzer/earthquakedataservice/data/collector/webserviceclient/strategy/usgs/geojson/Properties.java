package com.equakelyzer.earthquakedataservice.data.collector.webserviceclient.strategy.usgs.geojson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Properties {
    @JsonProperty("mag")
    private Double mag;

    @JsonProperty("place")
    private String place;

    @JsonProperty("time")
    private Long time;

    @JsonProperty("url")
    private String url;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("title")
    private String title;

    @JsonProperty("tsunami")
    private Integer tsunami;

    @JsonProperty("sig")
    private Integer sig;

    @JsonProperty("code")
    private String code;

    @JsonProperty("ids")
    private String ids;

    @JsonProperty("type")
    private String type;

    @JsonProperty("types")
    private String types;

    @JsonProperty("magType")
    private String magType;

    @JsonProperty("net")
    private String net;


    // Getters and setters
    public Double getMag() {
        return mag;
    }

    public void setMag(Double mag) {
        this.mag = mag;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTsunami() {
        return tsunami;
    }

    public void setTsunami(Integer tsunami) {
        this.tsunami = tsunami;
    }

    public Integer getSig() {
        return sig;
    }

    public void setSig(Integer sig) {
        this.sig = sig;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getMagType() {
        return magType;
    }

    public void setMagType(String magType) {
        this.magType = magType;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }
}
