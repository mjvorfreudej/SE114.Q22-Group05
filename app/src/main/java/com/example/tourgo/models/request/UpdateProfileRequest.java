package com.example.tourgo.models.request;

public class UpdateProfileRequest {
    private String name;
    private String phone;
    private String avatar;

    public UpdateProfileRequest(String name, String phone, String avatar) {
        this.name = name;
        this.phone = phone;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
