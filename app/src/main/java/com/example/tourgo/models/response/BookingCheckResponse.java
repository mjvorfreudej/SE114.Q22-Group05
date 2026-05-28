package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

public class BookingCheckResponse {
    @SerializedName("hasBooked")
    private Boolean hasBooked;

    public Boolean getHasBooked() {
        return hasBooked;
    }

    public void setHasBooked(Boolean hasBooked) {
        this.hasBooked = hasBooked;
    }
}
