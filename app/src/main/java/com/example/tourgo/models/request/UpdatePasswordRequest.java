package com.example.tourgo.models.request;

public class UpdatePasswordRequest {
    private String password;

    public UpdatePasswordRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
