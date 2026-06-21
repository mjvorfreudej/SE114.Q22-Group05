package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

public class Favorite {
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("tour_id")
    private String tourId;
    @SerializedName("hotel_id")
    private String hotelId;
    @SerializedName("created_at")
    private String createdAt;

    public Favorite() {}

    public Favorite(String userId, String tourId, String hotelId) {
        this.userId = userId;
        this.tourId = tourId;
        this.hotelId = hotelId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
