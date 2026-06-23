package com.example.tourgo.models.error;

import android.content.Context;
import android.widget.Toast;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.models.response.ApiResponse;

import retrofit2.Response;

public class ErrorHandler {
    public static ApiError parseError(Response<?> response) {
        if (response == null) {
            return new ApiError(ApiErrorCode.UNKNOWN, "Unknown error occurred");
        }

        int httpCode = response.code();

        if (response.body() instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) response.body();

            if (apiResponse.getSuccess() != null && !apiResponse.getSuccess()) {
                String errorCode = apiResponse.getError();
                String message = apiResponse.getMessage();

                return parseApiError(errorCode, message, httpCode);
            }
        }

        if (response.errorBody() != null) {
            try {
                String errorStr = response.errorBody().string();
                com.google.gson.Gson gson = new com.google.gson.Gson();
                ApiResponse<?> apiResponse = gson.fromJson(errorStr, ApiResponse.class);
                if (apiResponse != null) {
                    String errorCode = apiResponse.getError();
                    String message = apiResponse.getMessage();
                    if (message != null && !message.isEmpty()) {
                        return parseApiError(errorCode, message, httpCode);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!response.isSuccessful()) {
            return parseHttpError(httpCode);
        }

        return new ApiError(
                ApiErrorCode.UNKNOWN,
                "Unknown error occurred",
                httpCode
        );
    }

    public static ApiError parseError(Throwable t) {
        if (t == null) {
            return new ApiError(ApiErrorCode.UNKNOWN, "Unknown error");
        }

        String message = t.getMessage();

        if (message != null) {
            if (message.contains("Unable to resolve host") ||
                    message.contains("Failed to connect")) {
                return new ApiError(
                        ApiErrorCode.NETWORK,
                        "No internet connection"
                );
            }

            if (message.contains("timeout")) {
                return new ApiError(
                        ApiErrorCode.NETWORK,
                        "Connection timeout"
                );
            }
        }

        return new ApiError(
                ApiErrorCode.UNKNOWN,
                message != null ? message : "Unknown error"
        );
    }

    private static ApiError parseHttpError(int httpCode) {
        ApiErrorCode code;
        String message;

        switch (httpCode) {
            case 400:
                code = ApiErrorCode.UNKNOWN;
                message = "Bad request";
                break;
            case 401:
                code = ApiErrorCode.UNAUTHORIZED;
                message = "Unauthorized - Please login again";
                break;
            case 403:
                code = ApiErrorCode.FORBIDDEN;
                message = "Access forbidden";
                break;
            case 404:
                code = ApiErrorCode.NOT_FOUND;
                message = "Resource not found";
                break;
            case 429:
                code = ApiErrorCode.RATE_LIMIT;
                message = "Too many requests";
                break;
            case 500:
            case 502:
            case 503:
                code = ApiErrorCode.SERVER_ERROR;
                message = "Server error";
                break;
            default:
                code = ApiErrorCode.UNKNOWN;
                message = "Error: " + httpCode;
        }

        return new ApiError(code, message, httpCode);
    }

    private static ApiError parseApiError(String errorCode, String message, int httpCode) {
        if (errorCode == null) {
            return new ApiError(
                    ApiErrorCode.UNKNOWN,
                    message != null ? message : "Unknown error",
                    httpCode
            );
        }

        ApiErrorCode code;

        switch (errorCode) {
            case "AUTH_INVALID_CREDENTIALS":
                code = ApiErrorCode.INVALID_CREDENTIALS;
                break;
            case "AUTH_EMAIL_ALREADY_EXISTS":
                code = ApiErrorCode.EMAIL_ALREADY_REGISTERED;
                break;
            case "AUTH_WEAK_PASSWORD":
                code = ApiErrorCode.PASSWORD_TOO_SHORT;
                break;
            case "INVALID_TOKEN":
                code = ApiErrorCode.INVALID_TOKEN;
                break;

            case "UNAUTHORIZED":
                code = ApiErrorCode.UNAUTHORIZED;
                break;
            case "FORBIDDEN":
                code = ApiErrorCode.FORBIDDEN;
                break;
            case "NOT_FOUND":
                code = ApiErrorCode.NOT_FOUND;
                break;
            case "RATE_LIMIT":
                code = ApiErrorCode.RATE_LIMIT;
                break;
            case "SERVER_ERROR":
                code = ApiErrorCode.SERVER_ERROR;
                break;

            case "BOOKING_ERROR":
                code = ApiErrorCode.BOOKING_ERROR;
                break;
            case "ALREADY_BOOKED":
                code = ApiErrorCode.ALREADY_BOOKED;
                break;

            default:
                code = ApiErrorCode.UNKNOWN;
        }

        return new ApiError(
                code,
                message != null ? message : "Error occurred",
                httpCode,
                errorCode
        );
    }

    public static String getUserMessage(Context context, ApiError error) {
        if (error == null) {
            return context.getString(R.string.err_unknown);
        }

        if (error.getMessage() != null && !error.getMessage().isEmpty()) {
            return error.getMessage();
        }

        switch (error.getCode()) {
            case NETWORK:
                return context.getString(R.string.err_no_internet);
            case UNAUTHORIZED:
                return context.getString(R.string.err_unauthorized);
            case FORBIDDEN:
                return "Access forbidden";
            case NOT_FOUND:
                return "Resource not found";
            case INVALID_CREDENTIALS:
                return context.getString(R.string.err_invalid_credentials);
            case EMAIL_ALREADY_REGISTERED:
                return context.getString(R.string.err_email_registered);
            case PASSWORD_TOO_SHORT:
                return context.getString(R.string.err_password_too_short);
            case SERVER_ERROR:
                return "Server error, please try again";
            default:
                return context.getString(R.string.err_unknown);
        }
    }

    public static void showError(Context context, ApiError error,
                                 com.google.android.material.textfield.TextInputLayout passwordField) {
        if (error == null) {
            Toast.makeText(context,
                    context.getString(R.string.err_unknown),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        switch (error.getCode()) {
            case INVALID_CREDENTIALS:
                if (passwordField != null) {
                    passwordField.setError(
                            context.getString(R.string.err_invalid_credentials)
                    );
                } else {
                    Toast.makeText(context,
                            context.getString(R.string.err_invalid_credentials),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case NETWORK:
                Toast.makeText(context,
                        context.getString(R.string.err_no_internet),
                        Toast.LENGTH_SHORT).show();
                break;

            case UNAUTHORIZED:
                Toast.makeText(context,
                        context.getString(R.string.err_unauthorized),
                        Toast.LENGTH_SHORT).show();
                break;

            case SERVER_ERROR:
                Toast.makeText(context,
                        context.getString(R.string.err_server),
                        Toast.LENGTH_SHORT).show();
                break;

            case RATE_LIMIT:
                Toast.makeText(context,
                        context.getString(R.string.err_rate_limit),
                        Toast.LENGTH_SHORT).show();
                break;

            case EMAIL_ALREADY_REGISTERED:
                Toast.makeText(context,
                        context.getString(R.string.err_email_registered),
                        Toast.LENGTH_SHORT).show();
                break;

            default:
                String message = getUserMessage(context, error);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showError(Context context, ApiError error) {
        showError(context, error, null);
    }
}