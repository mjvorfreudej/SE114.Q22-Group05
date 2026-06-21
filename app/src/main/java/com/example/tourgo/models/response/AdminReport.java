package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * A user report awaiting moderation, as returned by
 * {@code GET /api/admin/reports} (only rows with {@code status = 'open'}).
 */
public class AdminReport {
    @SerializedName("id")
    private String id;

    /** "Toxic language" | "Spam / promotion" | "Fake review" | "Harassment" | ... */
    @SerializedName("type")
    private String type;

    @SerializedName("reporter")
    private String reporter;

    @SerializedName("target")
    private String target;

    @SerializedName("context")
    private String context;

    @SerializedName("body")
    private String body;

    @SerializedName("reasoning")
    private String reasoning;

    /** "low" | "mid" | "high" | "critical" */
    @SerializedName("severity")
    private String severity;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getType() { return type != null ? type : ""; }
    public String getReporter() { return reporter != null ? reporter : ""; }
    public String getTarget() { return target != null ? target : ""; }
    public String getContext() { return context != null ? context : ""; }
    public String getBody() { return body != null ? body : ""; }
    public String getReasoning() { return reasoning != null ? reasoning : ""; }
    public String getSeverity() { return severity != null ? severity : "mid"; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
}
