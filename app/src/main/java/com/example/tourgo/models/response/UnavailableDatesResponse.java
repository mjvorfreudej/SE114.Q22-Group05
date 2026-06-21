package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Sold-out / unavailable nights for a hotel, as returned by
 * {@code GET /api/hotels/{id}/unavailable-dates}. Each entry in
 * {@link #unavailableDates} is a {@code yyyy-MM-dd} night the hotel has no
 * rooms left for, so the booking date-picker can disable it.
 */
public class UnavailableDatesResponse {
    @SerializedName("hotel_id")
    private String hotelId;

    @SerializedName("total_rooms")
    private int totalRooms;

    @SerializedName("from")
    private String from;

    @SerializedName("to")
    private String to;

    @SerializedName("unavailable_dates")
    private List<String> unavailableDates;

    public String getHotelId() { return hotelId; }
    public int getTotalRooms() { return totalRooms; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public List<String> getUnavailableDates() { return unavailableDates; }
}
