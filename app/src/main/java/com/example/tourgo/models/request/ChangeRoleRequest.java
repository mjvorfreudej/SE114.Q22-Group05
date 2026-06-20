package com.example.tourgo.models.request;

/** Body for PUT /api/admin/team/{userId}/role — role is OWNER / ADMIN / MODERATOR. */
public class ChangeRoleRequest {
    private final String role;

    public ChangeRoleRequest(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
