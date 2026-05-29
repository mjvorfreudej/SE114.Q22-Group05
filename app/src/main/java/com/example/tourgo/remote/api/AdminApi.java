package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Admin-console endpoints on the Node.js backend. The Android client only ever
 * talks to these REST routes via Retrofit — never to the database directly.
 */
public interface AdminApi {

    // ── Businesses ───────────────────────────────────────────────────────────

    /** Fetch business/partner accounts awaiting approval. */
    @GET("api/admin/businesses/pending")
    Call<ApiResponse<List<BusinessAccount>>> getPendingBusinesses();

    /** Approve a pending business. */
    @PUT("api/admin/businesses/{id}/approve")
    Call<ApiResponse<Void>> approveBusiness(@Path("id") String businessId);

    // ── Users ────────────────────────────────────────────────────────────────

    /** Fetch all user accounts. */
    @GET("api/admin/users")
    Call<ApiResponse<List<AdminAccount>>> getUsers();

    /** Suspend / lock a violating user. */
    @PUT("api/admin/users/{id}/suspend")
    Call<ApiResponse<Void>> suspendUser(@Path("id") String userId);

    /** Re-activate / unlock a user. */
    @PUT("api/admin/users/{id}/activate")
    Call<ApiResponse<Void>> activateUser(@Path("id") String userId);
}
