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
    private List<Integer> images; // List of image resource IDs

    public Comment(String userName, String userAvatar, String content, float rating, String date) {
        this(userName, userAvatar, content, rating, date, new ArrayList<>());
    }

    public Comment(String userName, String userAvatar, String content, float rating, String date, List<Integer> images) {
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.rating = rating;
        this.date = date;
        this.images = images;
    }

    public static Comment fromHotelReviewJson(JSONObject json) {
        JSONObject profile = json.optJSONObject("profiles");

        String userName = "User";
        String avatarUrl = "";

        if(profile != null) {
            userName = profile.optString("name", "User");
            avatarUrl = profile.optString("avatar_url", "");
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

        return comment;
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

    public List<Integer> getImages() {
        return images;
    }

    public void setImages(List<Integer> images) {
        this.images = images;
    }
    
    public boolean hasImages() {
        return images != null && !images.isEmpty();
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
