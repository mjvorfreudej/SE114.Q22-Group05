package com.example.tourgo.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tour {
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


    private int imageResId;
    private String location;
    private String priceString;

    public Tour() {
        this.imageUrls = new ArrayList<>();
    }


    public Tour(int imageResId, String name, String location, String priceString, double rating, String duration) {
        this.imageResId = imageResId;
        this.name = name;
        this.location = location;
        this.priceString = priceString;
        this.rating = (float) rating;
        this.duration = duration;
        this.imageUrls = new ArrayList<>();
    }


    public Tour(String id, String name, String description, double price, List<String> imageUrls,
                String destination, String region, String duration, String status, String ownerId,
                float rating, int reviewCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.destination = destination;
        this.region = region;
        this.duration = duration;
        this.status = status;
        this.ownerId = ownerId;
        this.rating = rating;
        this.reviewCount = reviewCount;
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
        t.priceString = String.format("%,.0f₫", t.price);

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
    public String getPriceString() { return priceString != null ? priceString : String.format("%,.0f₫", price); }
}
