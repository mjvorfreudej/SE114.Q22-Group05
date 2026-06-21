package com.example.tourgo.models.request;

import com.google.gson.annotations.SerializedName;

public class SaveReviewImagesRequest {
    @SerializedName("review_id")
    private String reviewId;

    @SerializedName("image_url")
    private String imageUrl;

    public SaveReviewImagesRequest(String reviewId, String imageUrl) {
        this.reviewId = reviewId;
        this.imageUrl = imageUrl;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
