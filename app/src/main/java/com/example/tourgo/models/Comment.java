package com.example.tourgo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comment implements Serializable {
    private String userName;
    private String userAvatar;
    private String content;
    private float rating;
    private String date;
    private List<Integer> images; // List of image resource IDs

    public Comment(String userName, String userAvatar, String content, float rating, String date) {
        this(userName, userAvatar, content, rating, date, new ArrayList<>());
    }

    public Comment(String userName, String userAvatar, String content, float rating, String date, List<Integer> images) {
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.images = images;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public String getContent() {
        return content;
    }

    public float getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public List<Integer> getImages() {
        return images;
    }

    public void setImages(List<Integer> images) {
        this.images = images;
    }
    
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
}
