package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * A business/partner account as returned by the admin backend
 * ({@code GET /api/admin/businesses/pending}). The client never derives this
 * from the database — it only renders what the Node.js server sends.
 */
public class BusinessAccount {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("owner")
    private String owner;

    @SerializedName("listings")
    private int listings;

    @SerializedName("bookings")
    private int bookings;

    /** "pending" | "active" | "suspended" */
    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getOwner() { return owner; }
    public int getListings() { return listings; }
    public int getBookings() { return bookings; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
