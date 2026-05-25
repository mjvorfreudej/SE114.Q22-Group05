package com.example.tourgo.models.response;

import android.content.Context;
import com.example.tourgo.data.local.SessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private List<String> imageUrls;
    private String destination;
    private String region;
    private String duration;
    private String status;
    private String ownerId;
    private float rating;
    private int reviewCount;
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

    public Tour() {
        this.imageUrls = new ArrayList<>();
    }

    public Tour(int imageResId, String name, String location, String priceString, double rating, String duration) {
        this.id = UUID.randomUUID().toString();
        this.imageResId = imageResId;
        this.name = name;
        this.location = location;
        this.rating = (float) rating;
        this.duration = duration;
        this.imageUrls = new ArrayList<>();
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

    public static Tour fromJson(JSONObject json) {
        Tour t = new Tour();
        t.id = json.optString("id", null);
        t.name = json.optString("name", "");
        t.description = json.optString("description", "");
        t.price = json.optDouble("price", 0);
        t.destination = json.optString("destination", "");
        t.region = json.optString("region", "");
        t.duration = json.optString("duration", "");
        t.status = json.optString("status", "APPROVED");
        t.ownerId = json.optString("owner_id", null);
        t.rating = (float) json.optDouble("rating", 0);
        t.reviewCount = json.optInt("review_count", 0);
        t.createdAt = json.optString("created_at", null);

        t.imageUrls = new ArrayList<>();
        JSONArray images = json.optJSONArray("tour_images");
        if (images != null) {
            for (int i = 0; i < images.length(); i++) {
                JSONObject img = images.optJSONObject(i);
                if (img != null) {
                    String url = img.optString("image_url", "");
                    if (!url.isEmpty()) {
                        t.imageUrls.add(url);
                    }
                }
            }
        }

        t.location = t.destination;
        return t;
    }

    public static List<Tour> fromJsonArray(JSONArray array) {
        List<Tour> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) {
                    list.add(Tour.fromJson(obj));
                }
            }
        }
        return list;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

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
}
