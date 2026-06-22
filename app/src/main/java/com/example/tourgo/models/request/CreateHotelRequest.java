package com.example.tourgo.models.request;

import java.util.List;

public class CreateHotelRequest {
    private String name;
    private String description;
    private double price;
    private String city;
    private String address;
    private List<String> amenities;
    private String status;

    @com.google.gson.annotations.SerializedName("open_from")
    private String openFrom;
    @com.google.gson.annotations.SerializedName("open_until")
    private String openUntil;
    @com.google.gson.annotations.SerializedName("blocked_dates")
    private List<String> blockedDates;

    public CreateHotelRequest(String name, String description, double price, String city, String address, List<String> amenities, String status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.city = city;
        this.address = address;
        this.amenities = amenities;
        this.status = status;
    }

    public CreateHotelRequest(String name, String description, double price, String city, String address, List<String> amenities, String status,
                              String openFrom, String openUntil, List<String> blockedDates) {
        this(name, description, price, city, address, amenities, status);
        this.openFrom = openFrom;
        this.openUntil = openUntil;
        this.blockedDates = blockedDates;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @com.google.gson.annotations.SerializedName("total_rooms")
    private int totalRooms;

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
}
