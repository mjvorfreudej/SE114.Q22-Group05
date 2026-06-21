package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Server shape of a notification row (GET /api/notifications). This is the raw
 * transport model; {@code NotificationService} maps it to the UI's
 * {@code NotificationItem} (icon key → drawable, created_at → relative "when" +
 * date group).
 */
public class NotificationDto {
    private String id;
    private String role;       // TRAVELER | BUSINESS | ADMIN
    private String category;   // category key, role-specific
    private String title;
    private String body;
    private String icon;       // logical icon key (see NotificationService.iconFor)
    private boolean read;

    @SerializedName("created_at")
    private String createdAt;  // ISO-8601 timestamp

    public String getId() { return id; }
    public String getRole() { return role; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getIcon() { return icon; }
    public boolean isRead() { return read; }
    public String getCreatedAt() { return createdAt; }
}
