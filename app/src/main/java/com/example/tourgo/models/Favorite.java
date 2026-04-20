package com.example.tourgo.models;

import org.json.JSONObject;

public class Favorite {
    private String id;
    private String userId;
    private String tourId;
    private String hotelId;
    private String createdAt;

    public Favorite() {}

    public Favorite(String userId, String tourId, String hotelId) {
        this.userId = userId;
        this.tourId = tourId;
        this.hotelId = hotelId;
    }

    public static Favorite fromJson(JSONObject json) {
        Favorite f = new Favorite();
        f.id = json.optString("id", null);
        f.userId = json.optString("user_id", null);
        f.tourId = json.optString("tour_id", null);
        f.hotelId = json.optString("hotel_id", null);
        f.createdAt = json.optString("created_at", null);
        return f;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            if (tourId != null) json.put("tour_id", tourId);
            if (hotelId != null) json.put("hotel_id", hotelId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
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
