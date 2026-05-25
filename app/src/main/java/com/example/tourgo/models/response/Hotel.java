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

public class Hotel implements Serializable {
    private String id;
    private String name;
    private String address;
    private String description;
    private double pricePerNight;
    private List<String> imageUrls;
    private String amenities;
    private float rating;
    private int reviewCount;
    private String createdAt;
    private boolean isFavorite;
    private int imageResId;

    public Hotel() {
        this.imageUrls = new ArrayList<>();
    }

    public Hotel(int imageResId, String name, String address, double pricePerNight, double rating, String amenities) {
        this.id = UUID.randomUUID().toString();
        this.imageResId = imageResId;
        this.name = name;
        this.address = address;
        this.pricePerNight = pricePerNight;
        this.rating = (float) rating;
        this.amenities = amenities;
        this.imageUrls = new ArrayList<>();
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

    public static Hotel fromJson(JSONObject json) {
        Hotel h = new Hotel();
        h.id = json.optString("id", null);
        h.name = json.optString("name", "");
        h.address = json.optString("address", "");
        h.description = json.optString("description", "");
        h.pricePerNight = json.optDouble("price_per_night", 0);
        h.amenities = json.optString("amenities", "");
        h.rating = (float) json.optDouble("rating", 0);
        h.reviewCount = json.optInt("review_count", 0);
        h.createdAt = json.optString("createdAt", null);

        JSONArray directImages = json.optJSONArray("image_urls");
        if (directImages != null) {
            for (int i = 0; i < directImages.length(); i++) {
                String url = directImages.optString(i);
                if (!url.isEmpty()) h.imageUrls.add(url);
            }
        }

        JSONArray joinedImages = json.optJSONArray("hotel_images");
        if (joinedImages != null) {
            for (int i = 0; i < joinedImages.length(); i++) {
                JSONObject imgObj = joinedImages.optJSONObject(i);
                if (imgObj != null) {
                    String url = imgObj.optString("url", imgObj.optString("image_url", ""));
                    if (!url.isEmpty() && !h.imageUrls.contains(url)) h.imageUrls.add(url);
                }
            }
        }
        return h;
    }

    public static List<Hotel> fromJsonArray(JSONArray array) {
        List<Hotel> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) list.add(fromJson(obj));
            }
        }
        return list;
    }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPricePerNight() { return pricePerNight; }
    public List<String> getImageUrls() { return imageUrls; }
    public float getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public int getImageResId() { return imageResId; }
}
