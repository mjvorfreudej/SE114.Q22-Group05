package com.example.tourgo.models.request;

/** Body for POST /api/admin/team/invite — promotes an existing user to admin. */
public class InviteAdminRequest {
    private final String email;

    public InviteAdminRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
