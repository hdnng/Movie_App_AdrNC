package com.example.movieapp.model;

import java.io.Serializable;

public class Episode implements Serializable {
    private String id;
    private int episodeNumber;
    private String title;
    private String videoUrl;

    public Episode() {}

    public Episode(String id, int episodeNumber, String title, String videoUrl) {
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.videoUrl = videoUrl;
        this.id = id;
    }

    public int getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(int episodeNumber) { this.episodeNumber = episodeNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}

