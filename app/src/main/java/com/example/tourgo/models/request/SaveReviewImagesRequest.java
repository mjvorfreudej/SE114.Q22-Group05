package com.example.tourgo.models.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveReviewImagesRequest {
    @SerializedName("review_id")
    private String reviewId;

    @SerializedName("image_urls")
    private List<String> imageUrls;

    public SaveReviewImagesRequest(String reviewId, List<String> imageUrls) {
        this.reviewId = reviewId;
        this.imageUrls = imageUrls;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
