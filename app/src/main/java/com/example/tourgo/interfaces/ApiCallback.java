package com.example.tourgo.interfaces;

public interface ApiCallback {
    void onSuccess(String responseData);
    /** Kèm mã lỗi để UI map sang R.string.* tương ứng. */
    void onError(ApiErrorCode code, String rawMessage);

    /** Giữ chữ ký cũ cho chỗ nào chưa migrate. Mặc định coi là UNKNOWN. */
    default void onError(String errorMessage) {
        onError(ApiErrorCode.UNKNOWN, errorMessage);
    }
}
