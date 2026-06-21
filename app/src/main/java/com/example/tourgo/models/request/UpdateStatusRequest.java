package com.example.tourgo.models.request;

public class UpdateStatusRequest {
    private String status;

    public UpdateStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
