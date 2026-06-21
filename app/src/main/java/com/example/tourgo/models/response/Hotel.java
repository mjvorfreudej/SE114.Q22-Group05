package com.example.tourgo.models.response;

import android.content.Context;
import com.example.tourgo.data.local.SessionManager;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Hotel implements Serializable {
    private String id;
    private String name;
    private String address;
    private String description;
    @SerializedName("price_per_night")
    private double pricePerNight;
    private String amenities;
    private float rating;
    @SerializedName("review_count")
    private int reviewCount;
    @SerializedName("created_at")
    private String createdAt;
    private boolean isFavorite;
    private int imageResId;
    @SerializedName("hotel_images")
    private List<HotelImage> hotelImages;
    private double latitude;
    private double longitude;
    private String status;

    public static class HotelImage implements Serializable {
        private String id;
        @SerializedName("hotel_id")
        private String hotelId;
        @SerializedName("image_url")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public Hotel() {
    }

    public Hotel(int imageResId, String name, String address, double pricePerNight, double rating, String amenities) {
        this.id = UUID.randomUUID().toString();
        this.imageResId = imageResId;
        this.name = name;
        this.address = address;
        this.pricePerNight = pricePerNight;
        this.rating = (float) rating;
        this.amenities = amenities;
    }

    public String formatPrice(Context context, double amount) {
        SessionManager session = new SessionManager(context);
        String currency = session.getCurrency();
        
        // Sử dụng định dạng số theo Locale hiện tại (ngôn ngữ hệ thống)
        if ("USD".equals(currency)) {
            // Định dạng: $ 1,234,567
            return String.format(Locale.getDefault(), "$ %,.0f", amount);
        } else {
            // Định dạng: 1.234.567₫ (Mặc định VND)
            return String.format(Locale.getDefault(), "%,.0f₫", amount);
        }
    }

    public String getPriceString(Context context) {
        return formatPrice(context, pricePerNight);
    }

    public String formatPrice(double amount) {
        return String.format(Locale.getDefault(), "%,.0f₫", amount);
    }

    public List<String> getImageUrls() {
        List<String> urls = new ArrayList<>();
        if (hotelImages != null) {
            for (HotelImage img : hotelImages) {
                if (img.getImageUrl() != null && !img.getImageUrl().isEmpty()) {
                    urls.add(img.getImageUrl());
                }
            }
        }
        return urls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.hotelImages = new ArrayList<>();
        if (imageUrls != null) {
            for (String url : imageUrls) {
                HotelImage img = new HotelImage();
                img.imageUrl = url;
                this.hotelImages.add(img);
            }
        }
    }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public int getImageResId() { return imageResId; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public boolean hasCoordinates() {
        return !(latitude == 0 && longitude == 0);
    }

    public List<HotelImage> getHotelImages() {
        return hotelImages;
    }

    public void setHotelImages(List<HotelImage> hotelImages) {
        this.hotelImages = hotelImages;
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
