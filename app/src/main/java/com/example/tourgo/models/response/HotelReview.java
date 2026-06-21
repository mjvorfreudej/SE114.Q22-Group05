package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HotelReview implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("hotel_id")
    private String hotelId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("review_text")
    private String reviewText;

    @SerializedName("stars")
    private int stars;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("users")
    private ReviewUser user;

    @SerializedName("hotel_review_images")
    private List<ReviewImage> images;

    public static class ReviewUser {
        @SerializedName("name")
        private String name;

        @SerializedName("avatar")
        private String avatar;

        public String getName() {
            return name;
        }

        public String getAvatar() {
            return avatar;
        }
    }

    public static class ReviewImage {
        @SerializedName("image_url")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public Review toReview() {
        String userName = user != null ? user.getName() : "User";
        String userAvatar = user != null ? user.getAvatar() : "";
        List<String> imageUrls = new ArrayList<>();

        if (images != null) {
            for (ReviewImage img : images) {
                if (img.getImageUrl() != null) {
                    imageUrls.add(img.getImageUrl());
                }
            }
        }

        Review review = new Review(userName, userAvatar, reviewText, (float) stars, createdAt, imageUrls);
        review.setId(id);
        review.setUserId(userId);
        review.setHotelId(hotelId);
        return review;
    }

    public String getId() {
        return id;
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getUserId() {
        return userId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public int getStars() {
        return stars;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ReviewUser getUser() {
        return user;
    }

    public List<ReviewImage> getImages() {
        return images;
    }
}
