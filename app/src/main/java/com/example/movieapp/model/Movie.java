package com.example.movieapp.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Movie implements Serializable {
    private String id;
    private String title;
    private String description;
    private int year;
    private String thumbnail;
    private boolean isSeries;
    private List<String> typeId;
    private List<String> typeName;
    private String videoUrl; // Dành cho phim lẻ
    private Date createdTime;

    public Movie() {
    }

    public Movie(String id, String title, String description, int year, String thumbnail,
                 boolean isSeries, List<String> typeId, List<String> typeName, String videoUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.year = year;
        this.thumbnail = thumbnail;
        this.isSeries = isSeries;
        this.typeId = typeId;
        this.typeName = typeName;
        this.videoUrl = videoUrl;
        this.createdTime = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter và Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public boolean isSeries() { return isSeries; }
    public void setSeries(boolean series) { isSeries = series; }

    public List<String> getTypeId() { return typeId; }
    public void setTypeId(List<String> typeId) { this.typeId = typeId; }

    public List<String> getTypeName() { return typeName; }
    public void setTypeName(List<String> typeName) { this.typeName = typeName; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
