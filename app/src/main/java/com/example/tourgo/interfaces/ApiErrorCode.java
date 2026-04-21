package com.example.tourgo.interfaces;

public enum AuthErrorCode {
    NETWORK,                 // lỗi mạng, timeout, unable to resolve host
    EMAIL_ALREADY_REGISTERED,
    INVALID_CREDENTIALS,
    PASSWORD_TOO_SHORT,
    USER_NOT_FOUND,
    RATE_LIMIT,
    INVALID_TOKEN,           // reset token hết hạn / không hợp lệ
    PASSWORD_SAME_AS_OLD,
    UNKNOWN
}
