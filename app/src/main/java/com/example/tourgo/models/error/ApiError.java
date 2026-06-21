package com.example.tourgo.models.error;

import com.example.tourgo.interfaces.ApiErrorCode;

public class ApiError {
    private ApiErrorCode code;
    private String message;
    private int httpCode;
    private String rawError;

    public ApiError(ApiErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiError(ApiErrorCode code, String message, int httpCode) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode;
    }

    public ApiError(ApiErrorCode code, String message, int httpCode, String rawError) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode;
        this.rawError = rawError;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getRawError() {
        return rawError;
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", httpCode=" + httpCode +
                '}';
    }
}
