package com.example.tourgo.remote.api;

import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.AdminActivityItem;
import com.example.tourgo.models.response.AdminAuditEntry;
import com.example.tourgo.models.response.AdminReport;
import com.example.tourgo.models.response.AdminStats;
import com.example.tourgo.models.response.AdminTeamMember;
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

    /** Fetch approved business/partner accounts. */
    @GET("api/admin/businesses/approved")
    Call<ApiResponse<List<BusinessAccount>>> getApprovedBusinesses();

    /** Approve a pending business. */
    @PUT("api/admin/businesses/{id}/approve")
    Call<ApiResponse<Void>> approveBusiness(@Path("id") String businessId);

    /** Suspend a pending business. */
    @PUT("api/admin/businesses/{id}/suspend")
    Call<ApiResponse<Void>> suspendBusiness(@Path("id") String businessId);

    /** Reject a pending business. */
    @PUT("api/admin/businesses/{id}/reject")
    Call<ApiResponse<Void>> rejectBusiness(@Path("id") String businessId);

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

    // ── Dashboard ──────────────────────────────────────────────────────────────

    /** Live KPI counts for the Admin home dashboard. */
    @GET("api/admin/stats")
    Call<ApiResponse<AdminStats>> getStats();

    /** Recent activity feed for the Admin home dashboard. */
    @GET("api/admin/activity")
    Call<ApiResponse<List<AdminActivityItem>>> getActivity();

    // ── Reports (Moderation) ─────────────────────────────────────────────────────

    /** Open user reports awaiting moderation. */
    @GET("api/admin/reports")
    Call<ApiResponse<List<AdminReport>>> getReports();

    /** Dismiss a report (no action taken). */
    @PUT("api/admin/reports/{id}/dismiss")
    Call<ApiResponse<Void>> dismissReport(@Path("id") String reportId);

    /** Resolve a report (action taken against the target). */
    @PUT("api/admin/reports/{id}/resolve")
    Call<ApiResponse<Void>> resolveReport(@Path("id") String reportId);

    // ── Profile ──────────────────────────────────────────────────────────────────

    /** Admin team members (users with role = admin). */
    @GET("api/admin/team")
    Call<ApiResponse<List<AdminTeamMember>>> getTeam();

    /** Recent moderation actions for the audit log. */
    @GET("api/admin/audit-log")
    Call<ApiResponse<List<AdminAuditEntry>>> getAuditLog();
}
