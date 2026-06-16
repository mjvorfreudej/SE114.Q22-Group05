package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * One entry of the Admin home "recent activity" feed, as returned by
 * {@code GET /api/admin/activity}. The backend merges recent businesses, tours
 * and reports into a single time-ordered list.
 */
public class AdminActivityItem {
    /** "business" | "tour" | "report" | "user" — drives the icon/colour. */
    @SerializedName("kind")
    private String kind;

    /** Bold lead of the title line (e.g. the entity name). */
    @SerializedName("bold")
    private String bold;

    /** Remainder of the title line (e.g. " submitted a new listing"). */
    @SerializedName("rest")
    private String rest;

    @SerializedName("meta")
    private String meta;

    @SerializedName("created_at")
    private String createdAt;

    public String getKind() { return kind != null ? kind : ""; }
    public String getBold() { return bold != null ? bold : ""; }
    public String getRest() { return rest != null ? rest : ""; }
    public String getMeta() { return meta != null ? meta : ""; }
    public String getCreatedAt() { return createdAt; }
}
