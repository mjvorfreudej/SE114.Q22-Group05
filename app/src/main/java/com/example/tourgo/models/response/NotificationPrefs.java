package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Per-admin notification delivery preferences,
 * GET/PUT /api/admin/settings/notifications. ({@code security} is always true.)
 */
public class NotificationPrefs {
    @SerializedName("pending")
    private boolean pending;
    @SerializedName("reported")
    private boolean reported;
    @SerializedName("team")
    private boolean team;
    @SerializedName("sla")
    private boolean sla;
    @SerializedName("digest")
    private boolean digest;
    @SerializedName("weekly")
    private boolean weekly;
    @SerializedName("security")
    private boolean security;

    public NotificationPrefs() {
    }

    public NotificationPrefs(boolean pending, boolean reported, boolean team, boolean sla,
                             boolean digest, boolean weekly, boolean security) {
        this.pending = pending;
        this.reported = reported;
        this.team = team;
        this.sla = sla;
        this.digest = digest;
        this.weekly = weekly;
        this.security = security;
    }

    public boolean isPending() { return pending; }
    public boolean isReported() { return reported; }
    public boolean isTeam() { return team; }
    public boolean isSla() { return sla; }
    public boolean isDigest() { return digest; }
    public boolean isWeekly() { return weekly; }
    public boolean isSecurity() { return security; }
}
