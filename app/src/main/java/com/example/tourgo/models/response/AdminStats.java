package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Dashboard KPI counts as returned by {@code GET /api/admin/stats}. Every value
 * is computed live by the backend from the underlying tables.
 */
public class AdminStats {
    @SerializedName("users")
    private int users;

    @SerializedName("businesses")
    private int businesses;

    @SerializedName("listings")
    private int listings;

    @SerializedName("pending_listings")
    private int pendingListings;

    @SerializedName("pending_businesses")
    private int pendingBusinesses;

    @SerializedName("reports")
    private int reports;

    @SerializedName("flagged_users")
    private int flaggedUsers;

    public int getUsers() { return users; }
    public int getBusinesses() { return businesses; }
    public int getListings() { return listings; }
    public int getPendingListings() { return pendingListings; }
    public int getPendingBusinesses() { return pendingBusinesses; }
    public int getReports() { return reports; }
    public int getFlaggedUsers() { return flaggedUsers; }

    /** Total items awaiting moderation (pending listings + open reports). */
    public int getQueueTotal() { return pendingListings + reports; }
}
