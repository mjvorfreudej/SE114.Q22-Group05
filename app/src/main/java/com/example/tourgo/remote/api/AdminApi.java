package com.example.tourgo.remote.api;

import com.example.tourgo.models.request.ChangeRoleRequest;
import com.example.tourgo.models.request.InviteAdminRequest;
import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.AdminActivityItem;
import com.example.tourgo.models.response.AdminAuditEntry;
import com.example.tourgo.models.response.AdminReport;
import com.example.tourgo.models.response.AdminStats;
import com.example.tourgo.models.response.AdminTeamMember;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuditExport;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.models.response.ModerationPolicy;
import com.example.tourgo.models.response.NotificationPrefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // ── Team management ──────────────────────────────────────────────────────────

    /** Promote an existing user (by email) to admin. */
    @POST("api/admin/team/invite")
    Call<ApiResponse<AdminTeamMember>> inviteAdmin(@Body InviteAdminRequest request);

    /** Change an admin's display role (OWNER / ADMIN / MODERATOR). */
    @PUT("api/admin/team/{userId}/role")
    Call<ApiResponse<Void>> changeAdminRole(@Path("userId") String userId, @Body ChangeRoleRequest request);

    /** Remove an admin (demote back to a regular user). */
    @DELETE("api/admin/team/{userId}")
    Call<ApiResponse<Void>> removeAdmin(@Path("userId") String userId);

    // ── Audit log pagination + export ─────────────────────────────────────────────

    /** Page of audit entries older than {@code before} (ISO created_at cursor). */
    @GET("api/admin/audit-log")
    Call<ApiResponse<List<AdminAuditEntry>>> getAuditLog(@Query("before") String before, @Query("limit") int limit);

    /** The audit log rendered as CSV (returned in the JSON envelope). */
    @GET("api/admin/audit-log/export")
    Call<ApiResponse<AuditExport>> exportAuditLog();

    // ── Settings: moderation policy + notification preferences ─────────────────────

    @GET("api/admin/settings/moderation")
    Call<ApiResponse<ModerationPolicy>> getModerationPolicy();

    @PUT("api/admin/settings/moderation")
    Call<ApiResponse<Void>> updateModerationPolicy(@Body ModerationPolicy policy);

    @GET("api/admin/settings/notifications")
    Call<ApiResponse<NotificationPrefs>> getNotificationPrefs();

    @PUT("api/admin/settings/notifications")
    Call<ApiResponse<Void>> updateNotificationPrefs(@Body NotificationPrefs prefs);
}
