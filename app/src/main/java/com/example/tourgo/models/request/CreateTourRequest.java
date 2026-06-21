package com.example.tourgo.models.request;

/**
 * JSON body sent to the backend {@code POST /api/tours} when a provider submits
 * a new tour. The server validates it, sets it in the database (with
 * {@code status = "PENDING"}) and returns the created tour. Null fields are
 * omitted by Gson so the server can apply its own defaults.
 */
public class CreateTourRequest {
    private String name;
    private String description;
    private double price;
    private String destination;
    private String region;
    private String duration;
    private String status;

    @com.google.gson.annotations.SerializedName("open_from")
    private String openFrom;
    @com.google.gson.annotations.SerializedName("open_until")
    private String openUntil;
    @com.google.gson.annotations.SerializedName("blocked_dates")
    private java.util.List<String> blockedDates;

    private java.util.List<String> amenities;

    public void setAmenities(java.util.List<String> amenities) {
        this.amenities = amenities;
    }

    public CreateTourRequest(String name, String description, double price,
                             String destination, String region, String duration, String status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.destination = destination;
        this.region = region;
        this.duration = duration;
        this.status = status;
    }

    public CreateTourRequest(String name, String description, double price,
                             String destination, String region, String duration, String status,
                             String openFrom, String openUntil, java.util.List<String> blockedDates) {
        this(name, description, price, destination, region, duration, status);
        this.openFrom = openFrom;
        this.openUntil = openUntil;
        this.blockedDates = blockedDates;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
}
