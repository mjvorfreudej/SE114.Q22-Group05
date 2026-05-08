package com.example.tourgo.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        this.imageResId = imageResId;
        this.name = name;
        this.address = address;
        this.pricePerNight = pricePerNight;
        this.rating = (float) rating;
        this.amenities = amenities;
        this.imageUrls = new ArrayList<>();
    }

    public String getCurrencySymbol() {
        return Locale.getDefault().getLanguage().equals("vi") ? "₫" : "VND";
    }

    public String formatPrice(double amount) {
        // Sử dụng Locale.getDefault() để tự động định dạng dấu phân cách theo ngôn ngữ
        if (Locale.getDefault().getLanguage().equals("vi")) {
            return String.format(Locale.getDefault(), "%,.0f₫", amount);
        } else {
            return String.format(Locale.getDefault(), "VND %,.0f", amount);
        }
    }

    public String getPriceString() {
        // Phương thức này nên được thay thế bằng cách sử dụng String Resource trong Adapter để hỗ trợ đa ngôn ngữ tốt hơn cho chuỗi " / đêm"
        return formatPrice(pricePerNight);
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
                if (!url.isEmpty()) {
                    h.imageUrls.add(url);
                }
            }
        }

        JSONArray joinedImages = json.optJSONArray("hotel_images");
        if (joinedImages != null) {
            for (int i = 0; i < joinedImages.length(); i++) {
                JSONObject imgObj = joinedImages.optJSONObject(i);
                if (imgObj != null) {
                    String url = imgObj.optString("url", imgObj.optString("image_url", ""));
                    if (!url.isEmpty() && !h.imageUrls.contains(url)) {
                        h.imageUrls.add(url);
                    }
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
                if (obj != null) {
                    list.add(fromJson(obj));
                }
            }
        }
        return list;
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
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getImageResId() { return imageResId; }
}
