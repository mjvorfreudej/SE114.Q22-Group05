package com.example.tourgo.models.request;

import com.google.gson.annotations.SerializedName;

public class UpdateReviewRequest {
    @SerializedName("review_text")
    private String reviewText;

    @SerializedName("stars")
    private int stars;

    public UpdateReviewRequest(String reviewText, int stars) {
        this.reviewText = reviewText;
        this.stars = stars;
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
