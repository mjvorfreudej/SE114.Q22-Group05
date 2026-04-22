package com.example.tourgo.utils;

import android.content.Context;
import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;

public final class ApiErrorMapper {
    private ApiErrorMapper() {
    }

    public static int messageResOf(ApiErrorCode code) {
        switch (code) {
            case NETWORK:
                return R.string.err_network;
            case EMAIL_ALREADY_REGISTERED:
                return R.string.err_email_registered;
            case INVALID_CREDENTIALS:
                return R.string.err_invalid_credentials;
            case PASSWORD_TOO_SHORT:
                return R.string.err_password_too_short;
            case USER_NOT_FOUND:
                return R.string.err_user_not_found;
            case RATE_LIMIT:
                return R.string.err_rate_limit;
            case INVALID_TOKEN:
                return R.string.err_invalid_token;
            case PASSWORD_SAME_AS_OLD:
                return R.string.err_password_same_as_old;
            default:
                return R.string.err_unknown;
        }
    }

    public static String messageOf(Context ctx, ApiErrorCode code) {
        return ctx.getString(messageResOf(code));
    }
}
