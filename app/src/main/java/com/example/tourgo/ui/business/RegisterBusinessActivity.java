package com.example.tourgo.ui.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tourgo.utils.ToastHelper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tourgo.R;
import com.example.tourgo.models.request.BusinessRequest;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterBusinessActivity extends AppCompatActivity {

    private TextInputEditText etBizName, etBizOwner, etBizTaxCode, etBizPhone, etBizEmail, etBizAddress;
    private TextInputLayout tilBizName, tilBizOwner, tilBizTaxCode, tilBizPhone, tilBizEmail, tilBizAddress;
    private MaterialButton btnSubmitBiz;
    private ProgressBar pbRegisterLoading;
    private TextView tvAlreadyRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_business);

        // Bind Views
        etBizName = findViewById(R.id.etBizName);
        etBizOwner = findViewById(R.id.etBizOwner);
        etBizTaxCode = findViewById(R.id.etBizTaxCode);
        etBizPhone = findViewById(R.id.etBizPhone);
        etBizEmail = findViewById(R.id.etBizEmail);
        etBizAddress = findViewById(R.id.etBizAddress);

        tilBizName = findViewById(R.id.tilBizName);
        tilBizOwner = findViewById(R.id.tilBizOwner);
        tilBizTaxCode = findViewById(R.id.tilBizTaxCode);
        tilBizPhone = findViewById(R.id.tilBizPhone);
        tilBizEmail = findViewById(R.id.tilBizEmail);
        tilBizAddress = findViewById(R.id.tilBizAddress);

        btnSubmitBiz = findViewById(R.id.btnSubmitBiz);
        pbRegisterLoading = findViewById(R.id.pbRegisterLoading);
        tvAlreadyRegistered = findViewById(R.id.tvAlreadyRegistered);
        MaterialButton btnCancelBiz = findViewById(R.id.btnCancelBiz);

        btnSubmitBiz.setOnClickListener(v -> submitRegistration());
        btnCancelBiz.setOnClickListener(v -> finish());
        
        tvAlreadyRegistered.setOnClickListener(v -> {
            startActivity(new Intent(this, BusinessRegistrationDetailActivity.class));
            finish();
        });

        checkExistingRegistration();
    }

    private void checkExistingRegistration() {
        RetrofitClient.getInstance(this)
                .getUserApi()
                .getBusinesses()
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            BusinessAccount biz = response.body().getData();
                            String status = biz.getStatus();
                            
                            if (status != null && !status.isEmpty()) {
                                tvAlreadyRegistered.setVisibility(View.VISIBLE);
                                prefillFields(biz);
                                
                                // Nếu đang chờ duyệt thì không cho gửi thêm đơn
                                if (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("not approved yet")) {
                                    btnSubmitBiz.setEnabled(false);
                                    btnSubmitBiz.setAlpha(0.5f);
                                    btnSubmitBiz.setText("Đang chờ duyệt...");
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                    }
                });
    }

    private void prefillFields(BusinessAccount biz) {
        etBizName.setText(biz.getName());
        etBizOwner.setText(biz.getOwner());
        etBizTaxCode.setText(biz.getTaxCode());
        etBizPhone.setText(biz.getPhone());
        etBizEmail.setText(biz.getEmail());
        etBizAddress.setText(biz.getAddress());
    }

    private void submitRegistration() {
        String name = etBizName.getText().toString().trim();
        String owner = etBizOwner.getText().toString().trim();
        String taxCode = etBizTaxCode.getText().toString().trim();
        String phone = etBizPhone.getText().toString().trim();
        String email = etBizEmail.getText().toString().trim();
        String address = etBizAddress.getText().toString().trim();

        if (!validateFields(name, owner, taxCode, phone, email, address)) return;

        setLoading(true);

        BusinessRequest request = new BusinessRequest(name, owner, taxCode, address, phone, email);

        RetrofitClient.getInstance(this)
                .getUserApi()
                .registerBusiness(request)
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                            ToastHelper.showSuccess(RegisterBusinessActivity.this, "Đăng ký đối tác thành công! Vui lòng chờ phản hồi qua email.");
                            finish();
                        } else {
                            com.example.tourgo.models.error.ApiError error = com.example.tourgo.models.error.ErrorHandler.parseError(response);
                            String message = com.example.tourgo.models.error.ErrorHandler.getUserMessage(RegisterBusinessActivity.this, error);
                            ToastHelper.showError(RegisterBusinessActivity.this, "Đăng ký thất bại: " + message);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                        setLoading(false);
                        ToastHelper.showError(RegisterBusinessActivity.this, "Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            btnSubmitBiz.setClickable(false);
            btnSubmitBiz.setText("");
            pbRegisterLoading.setVisibility(View.VISIBLE);
        } else {
            btnSubmitBiz.setClickable(true);
            btnSubmitBiz.setText(R.string.biz_reg_submit);
            pbRegisterLoading.setVisibility(View.GONE);
        }
    }

    private boolean validateFields(String name, String owner, String taxCode, String phone, String email, String address) {
        boolean isValid = true;

        if (name.isEmpty()) {
            tilBizName.setError("Tên doanh nghiệp không được để trống");
            isValid = false;
        } else tilBizName.setError(null);

        if (owner.isEmpty()) {
            tilBizOwner.setError("Người đại diện không được để trống");
            isValid = false;
        } else tilBizOwner.setError(null);

        if (taxCode.isEmpty()) {
            tilBizTaxCode.setError("Mã số thuế không được để trống");
            isValid = false;
        } else tilBizTaxCode.setError(null);

        if (phone.isEmpty()) {
            tilBizPhone.setError("Số điện thoại không được để trống");
            isValid = false;
        } else tilBizPhone.setError(null);

        if (email.isEmpty()) {
            tilBizEmail.setError("Email không được để trống");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilBizEmail.setError("Email không hợp lệ");
            isValid = false;
        } else tilBizEmail.setError(null);

        if (address.isEmpty()) {
            tilBizAddress.setError("Địa chỉ không được để trống");
            isValid = false;
        } else tilBizAddress.setError(null);

        return isValid;
    }
}
