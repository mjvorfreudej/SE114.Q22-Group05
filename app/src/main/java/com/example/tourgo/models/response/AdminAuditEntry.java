package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * One moderation action from the audit log, as returned by
 * {@code GET /api/admin/audit-log}.
 */
public class AdminAuditEntry {
    @SerializedName("id")
    private String id;

    /** Human-readable action line, e.g. "Suspended user guest_jay_88". */
    @SerializedName("action")
    private String action;

    @SerializedName("actor_name")
    private String actorName;

    /** approve | suspend | reject | revision | role | policy — drives the icon. */
    @SerializedName("kind")
    private String kind;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getAction() { return action != null ? action : ""; }
    public String getActorName() { return actorName != null ? actorName : ""; }
    public String getKind() { return kind != null ? kind : "policy"; }
    public String getCreatedAt() { return createdAt; }
}
