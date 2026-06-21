package com.example.tourgo.models.request;

import com.google.gson.annotations.SerializedName;

public class CreateReviewRequest {
    @SerializedName("hotel_id")
    private String hotelId;

    @SerializedName("tour_id")
    private String tourId;

    @SerializedName("review_text")
    private String reviewText;

    @SerializedName("stars")
    private int stars;

    public CreateReviewRequest(String hotelId, String tourId, String reviewText, int stars) {
        this.hotelId = hotelId;
        this.tourId = tourId;
        this.reviewText = reviewText;
        this.stars = stars;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getTourId() {
        return tourId;
    }

    public void setTourId(String tourId) {
        this.tourId = tourId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
