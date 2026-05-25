package com.example.tourgo.models.response;

import org.json.JSONObject;

public class Booking {
    private String id;
    private String userId;
    private String tourId;
    private String hotelId;
    private String bookingDate;
    private String status;

    public Booking() {}

    public Booking(String userId, String tourId, String hotelId) {
        this.userId = userId;
        this.tourId = tourId;
        this.hotelId = hotelId;
        this.status = "PENDING";
    }

    public static Booking fromJson(JSONObject json) {
        Booking b = new Booking();
        b.id = json.optString("id", null);
        b.userId = json.optString("user_id", null);
        b.tourId = json.optString("tour_id", null);
        b.hotelId = json.optString("hotel_id", null);
        b.bookingDate = json.optString("booking_date", null);
        b.status = json.optString("status", "PENDING");
        return b;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);

            if (tourId != null) {
                json.put("tour_id", tourId);
            }

            if (hotelId != null) {
                json.put("hotel_id", hotelId);
            }

            json.put("status", status != null ? status : "PENDING");
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

    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
