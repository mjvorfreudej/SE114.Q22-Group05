package com.example.tourgo.ui.business;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_business);

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
        MaterialButton btnCancelBiz = findViewById(R.id.btnCancelBiz);

        btnSubmitBiz.setOnClickListener(v -> submitRegistration());
        btnCancelBiz.setOnClickListener(v -> finish());
    }

    private void submitRegistration() {
        String name = etBizName.getText().toString().trim();
        String owner = etBizOwner.getText().toString().trim();
        String taxCode = etBizTaxCode.getText().toString().trim();
        String phone = etBizPhone.getText().toString().trim();
        String email = etBizEmail.getText().toString().trim();
        String address = etBizAddress.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            tilBizName.setError("Tên doanh nghiệp không được để trống");
            isValid = false;
        } else {
            tilBizName.setError(null);
        }

        if (owner.isEmpty()) {
            tilBizOwner.setError("Người đại diện không được để trống");
            isValid = false;
        } else {
            tilBizOwner.setError(null);
        }

        if (taxCode.isEmpty()) {
            tilBizTaxCode.setError("Mã số thuế không được để trống");
            isValid = false;
        } else {
            tilBizTaxCode.setError(null);
        }

        if (phone.isEmpty()) {
            tilBizPhone.setError("Số điện thoại không được để trống");
            isValid = false;
        } else {
            tilBizPhone.setError(null);
        }

        if (email.isEmpty()) {
            tilBizEmail.setError("Email không được để trống");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilBizEmail.setError("Email không hợp lệ");
            isValid = false;
        } else {
            tilBizEmail.setError(null);
        }

        if (address.isEmpty()) {
            tilBizAddress.setError("Địa chỉ không được để trống");
            isValid = false;
        } else {
            tilBizAddress.setError(null);
        }

        if (!isValid) return;

        btnSubmitBiz.setEnabled(false);
        btnSubmitBiz.setText("Đang gửi đăng ký...");

        BusinessRequest request = new BusinessRequest(name, owner, taxCode, address, phone, email);

        RetrofitClient.getInstance(this)
                .getUserApi()
                .registerBusiness(request)
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getSuccess()) {
                            Toast.makeText(RegisterBusinessActivity.this, "Đăng ký thành công! Đang chờ phê duyệt và email xác nhận.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            btnSubmitBiz.setEnabled(true);
                            btnSubmitBiz.setText("Gửi đơn đăng ký");
                            com.example.tourgo.models.error.ApiError error = com.example.tourgo.models.error.ErrorHandler.parseError(response);
                            String message = com.example.tourgo.models.error.ErrorHandler.getUserMessage(RegisterBusinessActivity.this, error);
                            Toast.makeText(RegisterBusinessActivity.this, "Đăng ký thất bại: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                        btnSubmitBiz.setEnabled(true);
                        btnSubmitBiz.setText("Gửi đơn đăng ký");
                        Toast.makeText(RegisterBusinessActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
