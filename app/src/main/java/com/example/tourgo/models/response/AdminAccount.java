package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * A user account with admin-moderation metadata, as returned by
 * {@code GET /api/admin/users}. Fields beyond the core profile (bookings,
 * reported, tier, location) are computed/served by the backend.
 */
public class AdminAccount {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    /** "active" | "flagged" | "suspended" */
    @SerializedName("status")
    private String status;

    @SerializedName("bookings")
    private int bookings;

    @SerializedName("reported")
    private int reported;

    @SerializedName("tier")
    private String tier;

    @SerializedName("location")
    private String location;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public int getBookings() { return bookings; }
    public int getReported() { return reported; }
    public String getTier() { return tier; }
    public String getLocation() { return location; }
    public String getCreatedAt() { return createdAt; }
}
