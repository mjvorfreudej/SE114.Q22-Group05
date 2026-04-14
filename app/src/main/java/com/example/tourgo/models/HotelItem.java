package com.example.tourgo.models;

public class HotelItem {
    private int imageResId;
    private String name;
    private String price;
    private double rating;
    private boolean favorite;

    public HotelItem(int imageResId, String name, String price, double rating, boolean favorite) {
        this.imageResId = imageResId;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.favorite = favorite;
    }

    public int getImageResId() {
        return imageResId;
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
}
