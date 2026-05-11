package com.example.tourgo.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comment implements Serializable {
    private String userName;
    private String userAvatar;
    private String content;
    private float rating;
    private String date;

    private String id;
    private String hotelId;
    private String userId;
    private List<String> imageUrls;

    public Comment(String userName, String userAvatar, String content, float rating, String date) {
        this(userName, userAvatar, content, rating, date, new ArrayList<>());
    }

    public Comment(String userName, String userAvatar, String content, float rating, String date, List<String> imageUrls) {
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.imageUrls = imageUrls;
    }

    public static Comment fromHotelReviewJson(JSONObject json) {
        JSONObject user = json.optJSONObject("users");

        String userName = "User";
        String avatarUrl = "";

        if (user != null) {
            userName = user.optString("name", "User");
            avatarUrl = user.optString("avatar", "");
        }

        Comment comment = new Comment(
                userName,
                avatarUrl,
                json.optString("review_text", ""),
                (float) json.optInt("stars", 0),
                json.optString("created_at", ""),
                new ArrayList<>()
        );

        comment.id = json.optString("id", "");
        comment.hotelId = json.optString("hotel_id", "");
        comment.userId = json.optString("user_id", "");
        comment.imageUrls = parseImageUrls(json.optJSONArray("hotel_review_images"));

        return comment;
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

    public static List<Comment> fromHotelReviewJsonArray(JSONArray array) {
        List<Comment> comments = new ArrayList<>();

        if (array == null) return comments;

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) {
                comments.add(fromHotelReviewJson(obj));
            }
        }

        return comments;
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
}
