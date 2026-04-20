package com.example.tourgo.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
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


    private int imageResId;
    private String priceString;

    public Hotel() {
        this.imageUrls = new ArrayList<>();
    }


    public Hotel(int imageResId, String name, String address, String priceString, double rating, String amenities) {
        this.imageResId = imageResId;
        this.name = name;
        this.address = address;
        this.priceString = priceString;
        this.rating = (float) rating;
        this.amenities = amenities;
        this.imageUrls = new ArrayList<>();
    }


    public Hotel(String id, String name, String address, String description, double pricePerNight,
                 List<String> imageUrls, float rating, int reviewCount) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.description = description;
        this.pricePerNight = pricePerNight;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.rating = rating;
        this.reviewCount = reviewCount;
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
        h.createdAt = json.optString("created_at", null);


        h.imageUrls = new ArrayList<>();
        JSONArray images = json.optJSONArray("hotel_images");
        if (images != null) {
            for (int i = 0; i < images.length(); i++) {
                JSONObject img = images.optJSONObject(i);
                if (img != null) {
                    String url = img.optString("image_url", "");
                    if (!url.isEmpty()) {
                        h.imageUrls.add(url);
                    }
                }
            }
        }


        h.priceString = String.format("%,.0f₫ / đêm", h.pricePerNight);

        return h;
    }


    public static List<Hotel> fromJsonArray(JSONArray array) {
        List<Hotel> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) {
                    list.add(Hotel.fromJson(obj));
                }
            }
        }
        return list;
    }


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
    public String getPriceString() { return priceString != null ? priceString : String.format("%,.0f₫ / đêm", pricePerNight); }
}