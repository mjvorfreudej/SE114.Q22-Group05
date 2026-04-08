package com.example.tourgo.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String email;
    private String phone;
    private String name;
    private String avatar;
    private String role; // "USER" or "ADMIN"
    private List<String> favoriteTourIds;

    public User() {
        this.favoriteTourIds = new ArrayList<>();
    }

    public User(String id, String email, String phone, String name, String avatar, String role) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.avatar = avatar;
        this.role = role;
        this.favoriteTourIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getFavoriteTourIds() { return favoriteTourIds; }
    public void setFavoriteTourIds(List<String> favoriteTourIds) { this.favoriteTourIds = favoriteTourIds; }
}
