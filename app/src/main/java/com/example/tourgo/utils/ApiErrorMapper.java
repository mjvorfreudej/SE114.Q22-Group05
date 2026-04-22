package com.example.tourgo.utils;

import android.content.Context;
import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;

public final class ApiErrorMapper {
    private ApiErrorMapper() {
    }

    public static int messageResOf(ApiErrorCode code) {
        switch (code) {
            case NETWORK:                  return R.string.err_network;
            case UNAUTHORIZED:              return R.string.err_unauthorized;
            case FORBIDDEN:                 return R.string.err_forbidden;
            case NOT_FOUND:                 return R.string.err_not_found;
            case RATE_LIMIT:                return R.string.err_rate_limit;
            case SERVER_ERROR:              return R.string.err_server;

            case EMAIL_ALREADY_REGISTERED:  return R.string.err_email_registered;
            case INVALID_CREDENTIALS:       return R.string.err_invalid_credentials;
            case PASSWORD_TOO_SHORT:        return R.string.err_password_too_short;
            case USER_NOT_FOUND:            return R.string.err_user_not_found;
            case INVALID_TOKEN:             return R.string.err_invalid_token;
            case PASSWORD_SAME_AS_OLD:      return R.string.err_password_same_as_old;

            case ALREADY_BOOKED:            return R.string.err_already_booked;
            case INVALID_DATE:              return R.string.err_invalid_date;
            case TOUR_FULL:                 return R.string.err_tour_full;
            case ALREADY_FAVORITED:         return R.string.err_already_favorited;
            case BOOKING_ERROR:             return R.string.booking_error;

            default:                        return R.string.err_unknown;
        }
    }

    public static String messageOf(Context ctx, ApiErrorCode code) {
        return ctx.getString(messageResOf(code));
    }
}
