package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * A business/partner account as returned by the admin backend
 * ({@code GET /api/admin/businesses/pending}). The client never derives this
 * from the database — it only renders what the Node.js server sends.
 */
public class BusinessAccount implements Serializable {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("name")
    private String name;

    @SerializedName("owner")
    private String owner;

    @SerializedName("tax_code")
    private String taxCode;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("listings")
    private int listings;

    @SerializedName("bookings")
    private int bookings;

    @SerializedName("reviews")
    private int reviews;

    /** "pending" | "active" | "suspended" | "rejected" */
    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("reviewed_at")
    private String reviewedAt;

    @SerializedName("reviewed_by")
    private String reviewedBy;

    @SerializedName("rejection_reason")
    private String rejectionReason;

    public String getId() { return id; }

    public String getUserId() {
        return userId;
    }

    public String getName() { return name; }
    public String getOwner() { return owner; }

    public String getTaxCode() {
        return taxCode;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getListings() { return listings; }
    public int getBookings() { return bookings; }
    public int getReviews() { return reviews; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
