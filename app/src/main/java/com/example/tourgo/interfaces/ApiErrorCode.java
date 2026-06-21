package com.example.tourgo.interfaces;

public enum ApiErrorCode {
    // dùng chung
    NETWORK,
    UNAUTHORIZED,         // 401: token hết hạn / chưa login
    FORBIDDEN,            // 403: không có quyền (RLS Supabase)
    NOT_FOUND,            // 404
    RATE_LIMIT,
    SERVER_ERROR,         // 5xx
    UNKNOWN,

    // auth
    EMAIL_ALREADY_REGISTERED,
    INVALID_CREDENTIALS,
    PASSWORD_TOO_SHORT,
    USER_NOT_FOUND,
    INVALID_TOKEN,
    PASSWORD_SAME_AS_OLD,

    // booking
    ALREADY_BOOKED,
    INVALID_DATE,
    TOUR_FULL,
    BOOKING_ERROR,

    // favorite
    ALREADY_FAVORITED
}
