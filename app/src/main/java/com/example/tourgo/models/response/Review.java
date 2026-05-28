package com.example.tourgo.models.response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Review implements Serializable {
    private String userName;
    private String userAvatar;
    private String content;
    private float rating;
    private String date;

    private String id;
    private String hotelId;
    private String userId;
    private List<String> imageUrls;

    public Review(String userName, String userAvatar, String content, float rating, String date) {
        this(userName, userAvatar, content, rating, date, new ArrayList<>());
    }

    public Review(String userName, String userAvatar, String content, float rating, String date, List<String> imageUrls) {
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.imageUrls = imageUrls;
    }

    public static Review fromHotelReviewJson(JSONObject json) {
        JSONObject user = json.optJSONObject("users");

        String userName = "User";
        String avatarUrl = "";

        if (user != null) {
            userName = user.optString("name", "User");
            avatarUrl = user.optString("avatar", "");
        }

        Review review = new Review(
                userName,
                avatarUrl,
                json.optString("review_text", ""),
                (float) json.optInt("stars", 0),
                json.optString("created_at", ""),
                new ArrayList<>()
        );

        review.id = json.optString("id", "");
        review.hotelId = json.optString("hotel_id", "");
        review.userId = json.optString("user_id", "");
        review.imageUrls = parseImageUrls(json.optJSONArray("hotel_review_images"));

        return review;
    }

    private static List<String> parseImageUrls(JSONArray array) {
        List<String> urls = new ArrayList<>();
        if (array == null) return urls;

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj == null) continue;

            String url = obj.optString("image_url", "");
            if (!url.isEmpty()) urls.add(url);
        }

        return urls;
    }

    public static List<Review> fromHotelReviewJsonArray(JSONArray array) {
        List<Review> reviews = new ArrayList<>();

        if (array == null) return reviews;

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) {
                reviews.add(fromHotelReviewJson(obj));
            }
        }

        return reviews;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public String getContent() {
        return content;
    }

    public float getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
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

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }
}
