package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

public class UploadImageResponse {
    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("fileName")
    private String fileName;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
