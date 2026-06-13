package com.example.tourgo.models;

import java.io.Serializable;

public class Review implements Serializable {
    private String id;
    private String userName;
    private String userAvatar;
    private float rating;
    private String comment;
    private String date;

    public Review(String userName, float rating, String comment, String date) {
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.date = date;
    }

    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getDate() { return date; }
}
