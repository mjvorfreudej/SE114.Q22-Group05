package com.example.tourgo.ui.auth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.ApiCallback;
import com.example.tourgo.models.error.ApiError;
import com.example.tourgo.models.error.ErrorHandler;
import com.example.tourgo.models.request.ResetPasswordRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.AuthData;
import com.example.tourgo.remote.RetrofitClient;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordDialog extends DialogFragment {

    private EditText etEmail;
    private TextInputLayout tilEmail;
    private Button btnReset;
    private ProgressBar progressBar;
    private TextView tvSuccess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

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

        setupListeners();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void setupListeners() {
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tilEmail.setError(null);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.setError(getString(R.string.err_email_invalid));
                return;
            }
            sendRecoveryEmail(email);
        });
    }

    private void sendRecoveryEmail(String email) {
        setLoading(true);
        ResetPasswordRequest request = new ResetPasswordRequest(email);
        RetrofitClient.getInstance(requireContext())
                .getAuthApi()
                .resetPasswrod(request)
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        requireActivity().runOnUiThread(() -> {
                            setLoading(false);
                            if (response.isSuccessful() && response.body() != null){
                                ApiResponse<?> apiResponse = response.body();
                                if (apiResponse.getSuccess() != null && apiResponse.getSuccess()){
                                    showSuccess();
                                } else {
                                    ApiError error = ErrorHandler.parseError(response);
                                    ErrorHandler.showError(requireContext(), error, tilEmail);
                                }
                            } else {
                                ApiError error = ErrorHandler.parseError(response);
                                ErrorHandler.showError(requireContext(), error, tilEmail);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            setLoading(false);
                            ApiError error = ErrorHandler.parseError(t);
                            ErrorHandler.showError(requireContext(), error, tilEmail);
                        });
                    }
                });
    }

    private void setLoading(boolean loading) {
        btnReset.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        
        etEmail.setFocusable(!loading);
        etEmail.setFocusableInTouchMode(!loading);
        etEmail.setCursorVisible(!loading);
        
        if (loading) tilEmail.setError(null);
    }

    private void showSuccess() {
        tilEmail.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        tvSuccess.setVisibility(View.VISIBLE);
        tvSuccess.setText(getString(R.string.msg_recovery_sent));
        tvSuccess.postDelayed(() -> { if (isAdded()) dismiss(); }, 4000);
    }
}
