package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

public class BusinessListing {
    private String id;
    private String name;
    private String location;
    private double price;
    private String status;
    private String category;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getCategory() { return category; }
    public String getCreatedAt() { return createdAt; }

    private int bookings;
    private double rating;

    public int getBookings() { return bookings; }
    public double getRating() { return rating; }
}
