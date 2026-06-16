package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * An admin-team member (a user with {@code role = 'admin'}) as returned by
 * {@code GET /api/admin/team}.
 */
public class AdminTeamMember {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public String getId() { return id; }
    public String getName() { return name != null ? name : ""; }
    public String getEmail() { return email != null ? email : ""; }
    public String getRole() { return role != null ? role : "Admin"; }
}
