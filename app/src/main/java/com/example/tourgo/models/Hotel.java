package com.example.tourgo.models;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private String id;
    private String name;
    private String address;
    private String description;
    private double pricePerNight;
    private List<String> imageUrls;
    private float rating;
    private int reviewCount;
    
    private int imageResId;
    private String priceString;
    private String amenities;

    public Hotel() {
        this.imageUrls = new ArrayList<>();
    }

    public Hotel(int imageResId, String name, String address, String priceString, double rating, String amenities) {
        this.imageResId = imageResId;
        this.name = name;
        this.address = address;
        this.priceString = priceString;
        this.rating = (float) rating;
        this.amenities = amenities;
    }

    public Hotel(String id, String name, String address, String description, double pricePerNight, List<String> imageUrls, float rating, int reviewCount) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.imageUrls = imageUrls;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public int getImageResId() { return imageResId; }
    public String getPriceString() { return priceString; }
    public String getAmenities() { return amenities; }
}