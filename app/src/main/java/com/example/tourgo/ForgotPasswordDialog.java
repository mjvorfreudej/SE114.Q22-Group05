package com.example.tourgo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordDialog extends DialogFragment {

    private EditText etEmail;
    private TextInputLayout tilEmail;
    private Button btnReset;
    private ProgressBar progressBar;
    private TextView tvSuccess;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        View view = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_forgot_password, null);

        etEmail     = view.findViewById(R.id.etFPassEmail);
        tilEmail    = view.findViewById(R.id.tilFPassEmail);
        btnReset    = view.findViewById(R.id.btnFPassReset);
        progressBar = view.findViewById(R.id.progressBarFPass);
        tvSuccess   = view.findViewById(R.id.tvFPassSuccess);

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    tilEmail.setError(null);
                } else {
                    tilEmail.setError("Email không hợp lệ");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError("Email không hợp lệ");
                return;
            }
            sendRecoveryEmail(email);
        });

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void sendRecoveryEmail(String email) {
        setLoading(true);

        SupabaseClient.resetPassword(email, new AuthCallback() {
            @Override
            public void onSuccess(String responseData) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    showSuccess();
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    tilEmail.setError(errorMessage);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnReset.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        tilEmail.setEnabled(!loading);
    }

    private void showSuccess() {
        tilEmail.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.VISIBLE);
        tvSuccess.setText("Email khôi phục đã được gửi!\nVui lòng kiểm tra hộp thư và nhấn vào liên kết.");

        tvSuccess.postDelayed(() -> {
            if (isAdded()) dismiss();
        }, 3000);
    }
}