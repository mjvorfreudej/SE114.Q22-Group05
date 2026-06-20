package com.example.tourgo.models.response;

import android.content.Context;
import com.example.tourgo.data.local.SessionManager;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Tour implements Serializable {
    private String id;
    private String name;
    private String description;
    private double price;

    @SerializedName("tour_images")
    private List<TourImage> tourImages;

    private String destination;
    private String region;
    private String duration;
    private String status;
    private Object amenities;

    @SerializedName("owner_id")
    private String ownerId;

    private float rating;

    @SerializedName("review_count")
    private int reviewCount;

    @SerializedName("created_at")
    private String createdAt;

    private boolean isFavorite;

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    private int imageResId;
    private String location;

    // Inner class for tour_images array items
    public static class TourImage implements Serializable {
        private String id;

        @SerializedName("tour_id")
        private String tourId;

        @SerializedName("image_url")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public Tour() {
    }

    public Tour(int imageResId, String name, String location, String priceString, double rating, String duration) {
        this.id = UUID.randomUUID().toString();
        this.imageResId = imageResId;
        this.name = name;
        this.location = location;
        this.rating = (float) rating;
        this.duration = duration;
    }

    public String formatPrice(Context context, double amount) {
        SessionManager session = new SessionManager(context);
        String currency = session.getCurrency(); // "VND" hoặc "USD"
        
        if ("VND".equals(currency)) {
            return String.format(Locale.getDefault(), "%,.0f₫", amount);
        } else {
            return String.format(Locale.getDefault(), "$ %,.0f", amount);
        }
    }

    public String getPriceString(Context context) {
        return formatPrice(context, price);
    }

    // Fallback for cases without context
    public String getPriceString() {
        return String.format(Locale.getDefault(), "%,.0f₫", price);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getImageUrls() {
        List<String> urls = new ArrayList<>();
        if (tourImages != null) {
            for (TourImage img : tourImages) {
                if (img.getImageUrl() != null && !img.getImageUrl().isEmpty()) {
                    urls.add(img.getImageUrl());
                }
            }
        }
        return urls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.tourImages = new ArrayList<>();
        if (imageUrls != null) {
            for (String url : imageUrls) {
                TourImage img = new TourImage();
                img.imageUrl = url;
                this.tourImages.add(img);
            }
        }
    }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getImageResId() { return imageResId; }
    public String getLocation() { return location != null ? location : destination; }

    public String getAmenities() {
        if (amenities == null) {
            return null;
        }
        if (amenities instanceof String) {
            return (String) amenities;
        }
        if (amenities instanceof List) {
            List<?> list = (List<?>) amenities;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(list.get(i));
            }
            return sb.toString();
        }
        return amenities.toString();
    }
    public void setAmenities(Object amenities) { this.amenities = amenities; }

    /** Helper to convert Tour to Hotel model for UI consistency in Recently Viewed lists. */
    public Hotel toHotel() {
        Hotel h = new Hotel();
        h.setId(this.id);
        h.setName(this.name);
        h.setAddress(this.destination);
        h.setDescription(this.description);
        h.setPricePerNight(this.price);
        h.setRating(this.rating);
        h.setReviewCount(this.reviewCount);
        h.setFavorite(this.isFavorite);
        
        List<Hotel.HotelImage> hotelImages = new ArrayList<>();
        if (this.tourImages != null) {
            for (TourImage ti : this.tourImages) {
                Hotel.HotelImage hi = new Hotel.HotelImage();
                hi.setImageUrl(ti.getImageUrl());
                hotelImages.add(hi);
            }
        }
        h.setHotelImages(hotelImages);
        return h;
    }
}
