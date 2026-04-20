package com.example.tourgo.models;

import org.json.JSONObject;

public class Booking {
    private String id;
    private String userId;
    private String tourId;
    private String hotelId;
    private String bookingType;
    private String checkInDate;
    private String checkOutDate;
    private int guests;
    private double totalPrice;
    private String status;
    private String createdAt;

    public Booking() {}

    public Booking(String userId, String tourId, String hotelId, String bookingType,
                   String checkInDate, String checkOutDate, int guests, double totalPrice) {
        this.userId = userId;
        this.tourId = tourId;
        this.hotelId = hotelId;
        this.bookingType = bookingType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guests = guests;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
    }

    public static Booking fromJson(JSONObject json) {
        Booking b = new Booking();
        b.id = json.optString("id", null);
        b.userId = json.optString("user_id", null);
        b.tourId = json.optString("tour_id", null);
        b.hotelId = json.optString("hotel_id", null);
        b.bookingType = json.optString("booking_type", "TOUR");
        b.checkInDate = json.optString("check_in_date", null);
        b.checkOutDate = json.optString("check_out_date", null);
        b.guests = json.optInt("guests", 1);
        b.totalPrice = json.optDouble("total_price", 0);
        b.status = json.optString("status", "PENDING");
        b.createdAt = json.optString("created_at", null);
        return b;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            if (tourId != null) json.put("tour_id", tourId);
            if (hotelId != null) json.put("hotel_id", hotelId);
            json.put("booking_type", bookingType);
            if (checkInDate != null) json.put("check_in_date", checkInDate);
            if (checkOutDate != null) json.put("check_out_date", checkOutDate);
            json.put("guests", guests);
            json.put("total_price", totalPrice);
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

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public String getCheckInDate() { return checkInDate; }
    public void setCheckInDate(String checkInDate) { this.checkInDate = checkInDate; }

    public String getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(String checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
