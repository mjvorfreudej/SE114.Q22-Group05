package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

public class BusinessReview {
    private String id;
    private int rating;
    private String name;
    private String avatar;
    private String body;
    private String listing;
    private String category;
    
    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public int getRating() { return rating; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }
    public String getBody() { return body; }
    public String getListing() { return listing; }
    public String getCategory() { return category; }
    public String getCreatedAt() { return createdAt; }
}
