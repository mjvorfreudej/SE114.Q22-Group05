package com.example.tourgo.models.request;

import com.google.gson.annotations.SerializedName;

public class UpdatePaymentStatusRequest {
    @SerializedName("bookingId")
    private String bookingId;
    private String status;

    public UpdatePaymentStatusRequest(String bookingId, String status) {
        this.bookingId = bookingId;
        this.status = status;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
