package com.example.tourgo.models;

import java.util.ArrayList;
import java.util.List;

public class Tour {
    private String id;
    private String name;
    private String description;
    private double price;
    private List<String> imageUrls;
    private String destination;
    private String region; // "North", "Central", "South"
    private String duration;
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private String ownerId;
    private float rating;
    private int reviewCount;

    private int imageResId;
    private String location;
    private String priceString;

    public Tour() {
        this.imageUrls = new ArrayList<>();
    }

    public Tour(int imageResId, String name, String location, String priceString, double rating, String duration) {
        this.imageResId = imageResId;
        this.name = name;
        this.location = location;
        this.priceString = priceString;
        this.rating = (float) rating;
        this.duration = duration;
    }

    public int getImageResId() { return imageResId; }
    public String getLocation() { return location; }
    public String getPriceString() { return priceString; }

    public Tour(String id, String name, String description, double price, List<String> imageUrls, String destination, String region, String duration, String status, String ownerId, float rating, int reviewCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls;
        this.destination = destination;
        this.region = region;
        this.duration = duration;
        this.status = status;
        this.ownerId = ownerId;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
