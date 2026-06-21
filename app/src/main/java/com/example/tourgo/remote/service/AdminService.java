package com.example.tourgo.remote.service;

import android.content.Context;

import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
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
import com.example.tourgo.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin-console data access. Every call goes through Retrofit to the Node.js
 * backend; the client performs no database work itself.
 */
public class AdminService {

    public static void getPendingBusinesses(Context context, DataCallback<List<BusinessAccount>> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .getPendingBusinesses()
                .enqueue(new Callback<ApiResponse<List<BusinessAccount>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<BusinessAccount>>> call, Response<ApiResponse<List<BusinessAccount>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<BusinessAccount>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<BusinessAccount>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getApprovedBusinesses(Context context, DataCallback<List<BusinessAccount>> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .getApprovedBusinesses()
                .enqueue(new Callback<ApiResponse<List<BusinessAccount>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<BusinessAccount>>> call, Response<ApiResponse<List<BusinessAccount>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<BusinessAccount>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<BusinessAccount>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void approveBusiness(Context context, String businessId, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .approveBusiness(businessId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(null);
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void rejectBusiness(Context context, String businessId, DataCallback<Void> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .rejectBusiness(businessId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                                callback.onSuccess(null);
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void getUsers(Context context, DataCallback<List<AdminAccount>> callback) {
        RetrofitClient.getInstance(context)
                .getAdminApi()
                .getUsers()
                .enqueue(new Callback<ApiResponse<List<AdminAccount>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AdminAccount>>> call, Response<ApiResponse<List<AdminAccount>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<AdminAccount>> apiResponse = response.body();
                            if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                                callback.onSuccess(apiResponse.getData());
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                callback.onError(error.getCode(), error.getMessage());
                            }
                        } else {
                            ApiError error = ErrorHandler.parseError(response);
                            callback.onError(error.getCode(), error.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<AdminAccount>>> call, Throwable t) {
                        ApiError error = ErrorHandler.parseError(t);
                        callback.onError(error.getCode(), error.getMessage());
                    }
                });
    }

    public static void suspendUser(Context context, String userId, DataCallback<Void> callback) {
        toggleUser(context, userId, true, callback);
    }

    public static void activateUser(Context context, String userId, DataCallback<Void> callback) {
        toggleUser(context, userId, false, callback);
    }

    private static void toggleUser(Context context, String userId, boolean suspend, DataCallback<Void> callback) {
        Call<ApiResponse<Void>> call = suspend
                ? RetrofitClient.getInstance(context).getAdminApi().suspendUser(userId)
                : RetrofitClient.getInstance(context).getAdminApi().activateUser(userId);
        enqueueVoid(call, callback);
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    public static void getStats(Context context, DataCallback<AdminStats> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getStats(), callback);
    }

    public static void getActivity(Context context, DataCallback<List<AdminActivityItem>> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getActivity(), callback);
    }

    // ── Reports (Moderation) ─────────────────────────────────────────────────────

    public static void getReports(Context context, DataCallback<List<AdminReport>> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getReports(), callback);
    }

    public static void dismissReport(Context context, String reportId, DataCallback<Void> callback) {
        enqueueVoid(RetrofitClient.getInstance(context).getAdminApi().dismissReport(reportId), callback);
    }

    public static void resolveReport(Context context, String reportId, DataCallback<Void> callback) {
        enqueueVoid(RetrofitClient.getInstance(context).getAdminApi().resolveReport(reportId), callback);
    }

    // ── Profile ──────────────────────────────────────────────────────────────────

    public static void getTeam(Context context, DataCallback<List<AdminTeamMember>> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getTeam(), callback);
    }

    public static void getAuditLog(Context context, DataCallback<List<AdminAuditEntry>> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getAuditLog(), callback);
    }

    // ── Team management (invite / change role / remove) ───────────────────────────

    public static void inviteAdmin(Context context, String email, DataCallback<AdminTeamMember> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi()
                .inviteAdmin(new InviteAdminRequest(email)), callback);
    }

    public static void changeAdminRole(Context context, String userId, String role, DataCallback<Void> callback) {
        enqueueVoid(RetrofitClient.getInstance(context).getAdminApi()
                .changeAdminRole(userId, new ChangeRoleRequest(role)), callback);
    }

    public static void removeAdmin(Context context, String userId, DataCallback<Void> callback) {
        enqueueVoid(RetrofitClient.getInstance(context).getAdminApi().removeAdmin(userId), callback);
    }

    // ── Audit log pagination + export ─────────────────────────────────────────────

    public static void getAuditLog(Context context, String before, int limit,
                                   DataCallback<List<AdminAuditEntry>> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getAuditLog(before, limit), callback);
    }

    public static void exportAuditLog(Context context, DataCallback<AuditExport> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().exportAuditLog(), callback);
    }

    // ── Settings: moderation policy + notification preferences ────────────────────

    public static void getModerationPolicy(Context context, DataCallback<ModerationPolicy> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getModerationPolicy(), callback);
    }

    public static void updateModerationPolicy(Context context, ModerationPolicy policy, DataCallback<Void> callback) {
        enqueueVoid(RetrofitClient.getInstance(context).getAdminApi().updateModerationPolicy(policy), callback);
    }

    public static void getNotificationPrefs(Context context, DataCallback<NotificationPrefs> callback) {
        enqueueData(RetrofitClient.getInstance(context).getAdminApi().getNotificationPrefs(), callback);
    }

    public static void updateNotificationPrefs(Context context, NotificationPrefs prefs, DataCallback<Void> callback) {
        enqueueVoid(RetrofitClient.getInstance(context).getAdminApi().updateNotificationPrefs(prefs), callback);
    }

    // ── Shared enqueue helpers ───────────────────────────────────────────────────

    /** Enqueue a call whose success payload is required (non-null data). */
    private static <T> void enqueueData(Call<ApiResponse<T>> call, DataCallback<T> callback) {
        call.enqueue(new Callback<ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<T> apiResponse = response.body();
                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess() && apiResponse.getData() != null) {
                        callback.onSuccess(apiResponse.getData());
                        return;
                    }
                }
                ApiError error = ErrorHandler.parseError(response);
                callback.onError(error.getCode(), error.getMessage());
            }

            @Override
            public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
                ApiError error = ErrorHandler.parseError(t);
                callback.onError(error.getCode(), error.getMessage());
            }
        });
    }

    /** Enqueue a call that only signals success/failure (no payload). */
    private static void enqueueVoid(Call<ApiResponse<Void>> call, DataCallback<Void> callback) {
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        callback.onSuccess(null);
                        return;
                    }
                }
                ApiError error = ErrorHandler.parseError(response);
                callback.onError(error.getCode(), error.getMessage());
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                ApiError error = ErrorHandler.parseError(t);
                callback.onError(error.getCode(), error.getMessage());
            }
        });
    }
}
