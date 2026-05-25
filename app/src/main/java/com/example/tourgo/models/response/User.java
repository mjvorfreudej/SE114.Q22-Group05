package com.example.tourgo.models.response;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String email;
    private String phone;
    private String name;
    private String avatar;
    private String role; // "USER" or "ADMIN"

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getRole() {
        return role;
    }
}