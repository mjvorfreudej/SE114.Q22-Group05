package com.example.tourgo.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.service.UserService;

public class UserRepository {
    private static UserRepository instance;
    private User currentUser;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private UserRepository() {}

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public void getCurrentUser(Context context, boolean forceRefresh, DataCallback<User> callback) {
        if (!forceRefresh && currentUser != null) {
            mainHandler.post(() -> callback.onSuccess(currentUser));
            return;
        }

        UserService.getCurrentUser(context, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                mainHandler.post(() -> callback.onSuccess(user));
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                mainHandler.post(() -> callback.onError(code, message));
            }
        });
    }

    public User getCachedUser() {
        return currentUser;
    }

    public void updateCachedUser(User user) {
        this.currentUser = user;
    }

    public void clearCache() {
        currentUser = null;
    }
}
