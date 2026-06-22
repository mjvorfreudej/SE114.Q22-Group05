package com.example.tourgo.ui.business;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.R;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusinessRegistrationDetailActivity extends AppCompatActivity {

    private static final String TAG = "BusinessDetail";
    private TextView tvDetailStatus, tvRejectionReason, tvDetailBizName, tvDetailOwner, tvDetailTaxCode, tvDetailPhone, tvDetailEmail, tvDetailAddress;
    private View dividerStatus, layoutRejection;
    private MaterialCardView cvStatus;
    private MaterialButton btnReRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_registration_detail);

        // Đẩy header xuống dưới thanh trạng thái / camera (tránh bị khuất).
        View root = findViewById(R.id.registrationDetailRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, bars.top, 0, 0);
                return insets;
            });
        }

        // Khởi tạo Views
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvRejectionReason = findViewById(R.id.tvRejectionReason);
        tvDetailBizName = findViewById(R.id.tvDetailBizName);
        tvDetailOwner = findViewById(R.id.tvDetailOwner);
        tvDetailTaxCode = findViewById(R.id.tvDetailTaxCode);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        dividerStatus = findViewById(R.id.dividerStatus);
        layoutRejection = findViewById(R.id.layoutRejection);
        cvStatus = findViewById(R.id.cvStatus);
        btnReRegister = findViewById(R.id.btnReRegister);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        btnReRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterBusinessActivity.class));
            finish();
        });

        loadBusinessDetail();
    }

    private void loadBusinessDetail() {
        RetrofitClient.getInstance(this)
                .getUserApi()
                .getBusinesses()
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        ApiResponse<BusinessAccount> body = response.body();

                        // Nếu request thất bại (mã 4xx/5xx), parse từ errorBody
                        if (!response.isSuccessful() && response.errorBody() != null) {
                            try {
                                String errorJson = response.errorBody().string();
                                Log.d(TAG, "Dữ liệu lỗi từ Server: " + errorJson);
                                Type type = new TypeToken<ApiResponse<BusinessAccount>>(){}.getType();
                                body = new Gson().fromJson(errorJson, type);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi giải mã error body", e);
                            }
                        }

                        if (body != null) {
                            // Binding dữ liệu: Ưu tiên data nhận được, nếu thiếu thì hiện "-"
                            BusinessAccount biz = body.getData();
                            displayDetail(biz, body.getError());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                        Log.e(TAG, "Lỗi kết nối API", t);
                    }
                });
    }

    private void displayDetail(BusinessAccount biz, String errorCode) {
        if (biz != null) {
            // Binding dữ liệu một cách an toàn (tránh null)
            tvDetailBizName.setText(nonNull(biz.getName()));
            tvDetailOwner.setText(nonNull(biz.getOwner()));
            tvDetailTaxCode.setText(nonNull(biz.getTaxCode()));
            tvDetailPhone.setText(nonNull(biz.getPhone()));
            tvDetailEmail.setText(nonNull(biz.getEmail()));
            tvDetailAddress.setText(nonNull(biz.getAddress()));
        } else {
            setEmptyFields();
        }

        // Xác định trạng thái để hiển thị UI (màu sắc, icon)
        String status = (biz != null && biz.getStatus() != null) ? biz.getStatus() : "pending";
        
        // Nếu Server báo BUSINESS_NOT_APPROVED thì mặc định là đang chờ duyệt
        if ("BUSINESS_NOT_APPROVED".equals(errorCode)) {
            status = "pending";
        }

        updateStatusUI(status, biz);
    }

    private String nonNull(String value) {
        return (value == null || value.isEmpty()) ? "-" : value;
    }

    private void setEmptyFields() {
        tvDetailBizName.setText("-");
        tvDetailOwner.setText("-");
        tvDetailTaxCode.setText("-");
        tvDetailPhone.setText("-");
        tvDetailEmail.setText("-");
        tvDetailAddress.setText("-");
    }

    private void updateStatusUI(String status, BusinessAccount biz) {
        if (status == null) status = "pending";

        switch (status.toLowerCase()) {
            case "not approved yet":
            case "pending":
                tvDetailStatus.setText(R.string.profile_registration_status_pending);
                tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.black));
                cvStatus.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.adm_amber_50)));
                cvStatus.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.adm_amber_200)));
                
                dividerStatus.setVisibility(View.GONE);
                layoutRejection.setVisibility(View.GONE);
                btnReRegister.setVisibility(View.GONE);
                break;
                
            case "rejected":
                tvDetailStatus.setText(R.string.biz_reg_status_rejected);
                tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.adm_red_700));
                cvStatus.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.adm_red_50)));
                cvStatus.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.adm_red_200)));
                
                dividerStatus.setVisibility(View.VISIBLE);
                layoutRejection.setVisibility(View.VISIBLE);
                tvRejectionReason.setText((biz != null && biz.getRejectionReason() != null) ? biz.getRejectionReason() : getString(R.string.biz_reg_no_reason));
                btnReRegister.setVisibility(View.VISIBLE);
                break;

            case "active":
                tvDetailStatus.setText(R.string.biz_reg_status_active);
                tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.adm_green_700));
                cvStatus.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.adm_teal_50)));
                cvStatus.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.adm_teal_200)));

                dividerStatus.setVisibility(View.GONE);
                layoutRejection.setVisibility(View.GONE);
                btnReRegister.setVisibility(View.GONE);
                break;
        }
    }
}
