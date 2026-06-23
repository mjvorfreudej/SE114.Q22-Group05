package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.List;

public class Booking {
    private String id;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("tour_id")
    private String tourId;
    @SerializedName("hotel_id")
    private String hotelId;
    @SerializedName("booking_date")
    private String bookingDate;
    private String status;

    @SerializedName("check_in")
    private String checkIn;

    @SerializedName("check_out")
    private String checkOut;

    @SerializedName("num_guests")
    private Integer numGuests;

    @SerializedName("num_rooms")
    private Integer numRooms;

    @SerializedName("users")
    private GuestInfo guestInfo;

    @SerializedName("hotels")
    private HotelInfo hotelInfo;

    @SerializedName("tours")
    private TourInfo tourInfo;

    @SerializedName("guests")
    private String guests;

    @SerializedName("payments")
    private List<PaymentInfo> payments;

    public static class PaymentInfo {
        @SerializedName("transaction_code")
        private String transactionCode;
        public String getTransactionCode() { return transactionCode; }
    }

    public static class GuestInfo {
        private String name;
        private String phone;
        public String getName() { return name != null ? name : "Khách"; }
        public String getPhone() { return phone != null ? phone : ""; }
    }

    public static class HotelInfo {
        private String name;
        public String getName() { return name; }
    }

    public static class TourInfo {
        private String name;
        public String getName() { return name; }
    }

    public GuestInfo getGuestInfo() { return guestInfo; }
    public HotelInfo getHotelInfo() { return hotelInfo; }
    public TourInfo getTourInfo() { return tourInfo; }
    public List<PaymentInfo> getPayments() { return payments; }

    public String getTransactionCode() {
        if (payments != null && !payments.isEmpty() && payments.get(0).getTransactionCode() != null) {
            return payments.get(0).getTransactionCode();
        }
        return null;
    }

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

    public String getCheckIn() { return checkIn; }
    public void setCheckIn(String checkIn) { this.checkIn = checkIn; }

    public String getCheckOut() { return checkOut; }
    public void setCheckOut(String checkOut) { this.checkOut = checkOut; }

    public Integer getNumGuests() { return numGuests; }
    public void setNumGuests(Integer numGuests) { this.numGuests = numGuests; }

    public Integer getNumRooms() { return numRooms; }
    public void setNumRooms(Integer numRooms) { this.numRooms = numRooms; }

    public String getGuests() {
        return guests;
    }

    public void setGuests(String Guests) {
        this.guests = Guests;
    }
}
