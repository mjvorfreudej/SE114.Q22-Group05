package com.example.tourgo.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HotelItem implements Serializable {
    private int imageResId;
    private List<Integer> galleryImages;
    private String name;
    private String price;
    private double rating;
    private boolean favorite;
    private String location;
    private String description;
    private List<Comment> comments;

    public HotelItem(int imageResId, String name, String price, double rating, boolean favorite) {
        this(imageResId, name, price, rating, favorite, "", "");
    }

    public HotelItem(int imageResId, String name, String price, double rating, boolean favorite, String location, String description) {
        this.imageResId = imageResId;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.favorite = favorite;
        this.location = location;
        this.description = description;
        this.galleryImages = new ArrayList<>();
        this.galleryImages.add(imageResId);
        this.comments = new ArrayList<>();
    }

    public int getImageResId() {
        return imageResId;
    }

    public List<Integer> getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(List<Integer> galleryImages) {
        this.galleryImages = galleryImages;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
