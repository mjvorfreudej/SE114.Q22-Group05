package com.example.tourgo.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.tourgo.R;

public final class ToastHelper {

    private ToastHelper() {}

    public static void showSuccess(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_check_circle, R.color.adm_green_700);
    }

    public static void showError(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_alert_circle, R.color.adm_red_500);
    }

    public static void showInfo(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_help_circle, R.color.adm_blue_500);
    }

    private static void showCustomToast(Context context, String message, int iconRes, int colorRes) {
        try {
            Context appContext = context.getApplicationContext();
            LayoutInflater inflater = LayoutInflater.from(appContext);
            View layout = inflater.inflate(R.layout.layout_custom_toast, null);

            ImageView icon = layout.findViewById(R.id.toastIcon);
            TextView text = layout.findViewById(R.id.toastMessage);

            icon.setImageResource(iconRes);
            icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(appContext, colorRes)));
            text.setText(message);

            Toast toast = new Toast(appContext);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            // Fallback to standard toast on exception
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
