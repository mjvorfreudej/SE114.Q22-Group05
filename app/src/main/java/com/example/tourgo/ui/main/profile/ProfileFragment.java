package com.example.tourgo.ui.main.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.admin.AdminActivity;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.ui.business.BusinessActivity;
import com.example.tourgo.ui.business.BusinessRegistrationDetailActivity;
import com.example.tourgo.ui.business.RegisterBusinessActivity;
import com.example.tourgo.ui.main.booking.BookingHistorySection;
import com.example.tourgo.utils.LocaleHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private TextView tvProfileName, tvProfileEmail;
    private ImageView ivProfileAvatar;
    private SessionManager session;
    private View rowBusinessStatus, rowRegisterBusiness;
    private TextView tvBusinessStatusSub;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyTopInset(view);

        session = new SessionManager(requireContext());

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        rowBusinessStatus = view.findViewById(R.id.rowBusinessStatus);
        rowRegisterBusiness = view.findViewById(R.id.rowRegisterBusiness);
        tvBusinessStatusSub = view.findViewById(R.id.tvBusinessStatusSub);

        View btnBack = view.findViewById(R.id.btnProfileBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        setupBookings(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (session.isLoggedIn()) {
            loadUserProfile();
            if (getView() != null) {
                setupSettings(getView());
            }
        }
    }

    private void loadUserProfile() {
        UserRepository.getInstance().getCurrentUser(requireContext(), true, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                if (user != null) {
                    if (tvProfileName != null) tvProfileName.setText(user.getName());
                    if (tvProfileEmail != null) tvProfileEmail.setText(user.getEmail());

                    // Trong hàm loadUserProfile của ProfileFragment
                    if (ivProfileAvatar != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(ProfileFragment.this) // Dùng ProfileFragment.this để gắn với lifecycle của Fragment
                                .load(user.getAvatar())
                                .circleCrop()
                                // THÊM ĐOẠN NÀY ĐỂ BUỘC GLIDE LOAD ẢNH MỚI
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                // ----------------------------------------
                                .placeholder(R.drawable.ic_person_24)
                                .error(R.drawable.ic_person_24) // Load lỗi thì hiện icon mặc định
                                .into(ivProfileAvatar);
                    }

                    session.saveUserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getAvatar());
                    Log.d("DEBUG_AVATAR", "Avatar từ API: " + user.getAvatar());
                    if (getView() != null) {
                        setupSettings(getView());
                    }
                }
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                if (!isAdded()) return;
                if (tvProfileName != null) tvProfileName.setText(session.getShortName());
                if (tvProfileEmail != null) tvProfileEmail.setText(session.getEmail());

                String avatarUrl = session.getAvatar();

                if (ivProfileAvatar != null && avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(ProfileFragment.this)
                            .load(avatarUrl)
                            .circleCrop()
                            .placeholder(R.drawable.ic_person_24)
                            .into(ivProfileAvatar);
                }
            }
        });
    }

    private void applyTopInset(View root) {
        final int basePaddingTop = root.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupBookings(View root) {
        new BookingHistorySection(requireContext()).bind(root);
    }

    private void setupSettings(View root) {
        // Admin Console
        View rowAdmin = root.findViewById(R.id.rowAdminConsole);
        if (rowAdmin != null) {
            rowAdmin.setVisibility(session.isAdmin() ? View.VISIBLE : View.GONE);
            rowAdmin.setOnClickListener(v -> startActivity(new Intent(requireContext(), AdminActivity.class)));
        }

        // Business Console
        View rowBusiness = root.findViewById(R.id.rowBusinessConsole);
        if (rowBusiness != null) {
            rowBusiness.setVisibility(session.isBusiness() ? View.VISIBLE : View.GONE);
            rowBusiness.setOnClickListener(v -> startActivity(new Intent(requireContext(), BusinessActivity.class)));
        }

        // Logic for Business Registration and Status
        if (!session.isAdmin() && !session.isBusiness()) {
            checkBusinessRegistrationStatus();
        } else {
            hideRegistrationRows();
        }

        // Language Selection
        final MaterialButtonToggleGroup toggleLanguage = root.findViewById(R.id.toggleLanguage);
        View rowLanguage = root.findViewById(R.id.rowLanguage);
        if (toggleLanguage != null && toggleLanguage.getTag() == null) {
            toggleLanguage.setTag(true); // Mark as initialized to prevent duplicate listeners
            
            String currentLang = LocaleHelper.getCurrentLanguageTag();
            if ("vi".equals(currentLang)) toggleLanguage.check(R.id.btnLangVi);
            else toggleLanguage.check(R.id.btnLangEn);

            toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    String lang = (checkedId == R.id.btnLangVi) ? "vi" : "en";
                    if (!lang.equals(LocaleHelper.getCurrentLanguageTag())) {
                        Log.d(TAG, "Changing language to: " + lang);
                        LocaleHelper.setAppLocale(lang);
                    }
                }
            });

            // Make the whole row clickable for easier interaction
            if (rowLanguage != null) {
                rowLanguage.setOnClickListener(v -> {
                    int nextId = (toggleLanguage.getCheckedButtonId() == R.id.btnLangVi) ? R.id.btnLangEn : R.id.btnLangVi;
                    toggleLanguage.check(nextId);
                });
            }
        }

        // Currency Selection
        final MaterialButtonToggleGroup toggleCurrency = root.findViewById(R.id.toggleCurrency);
        View rowCurrency = root.findViewById(R.id.rowCurrency);
        if (toggleCurrency != null && toggleCurrency.getTag() == null) {
            toggleCurrency.setTag(true);
            
            String currentCurr = session.getCurrency();
            if ("VND".equals(currentCurr)) toggleCurrency.check(R.id.btnCurrVnd);
            else toggleCurrency.check(R.id.btnCurrUsd);

            toggleCurrency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    String currency = (checkedId == R.id.btnCurrVnd) ? "VND" : "USD";
                    if (!currency.equals(session.getCurrency())) {
                        session.setCurrency(currency);
                        setupBookings(root);
                        Toast.makeText(getContext(), "Currency changed to: " + currency, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if (rowCurrency != null) {
                rowCurrency.setOnClickListener(v -> {
                    int nextId = (toggleCurrency.getCheckedButtonId() == R.id.btnCurrVnd) ? R.id.btnCurrUsd : R.id.btnCurrVnd;
                    toggleCurrency.check(nextId);
                });
            }
        }

        // Other Settings Rows
        View rowPersonalInfo = root.findViewById(R.id.rowPersonalInfo);
        if (rowPersonalInfo != null) {
            rowPersonalInfo.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        }
        setupSimpleRow(root, R.id.rowPaymentMethods, "Payment Methods");
        setupSimpleRow(root, R.id.rowNotifications, "Notifications");
        setupSimpleRow(root, R.id.rowPrivacy, "Privacy & Security");

        View btnEdit = root.findViewById(R.id.btnProfileEdit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        }

        root.findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
    }

    private void setupSimpleRow(View root, int id, final String message) {
        View row = root.findViewById(id);
        if (row != null) {
            row.setOnClickListener(v -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    private void checkBusinessRegistrationStatus() {
        RetrofitClient.getInstance(requireContext())
                .getUserApi()
                .getBusinesses()
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        if (!isAdded()) return;

                        ApiResponse<BusinessAccount> body = response.body();

                        if (!response.isSuccessful() && response.errorBody() != null) {
                            try {
                                String errorJson = response.errorBody().string();
                                Type type = new TypeToken<ApiResponse<BusinessAccount>>(){}.getType();
                                body = new Gson().fromJson(errorJson, type);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
                            }
                        }

                        if (body != null && body.getData() != null) {
                            BusinessAccount biz = body.getData();
                            showBusinessStatus(biz);
                        } else {
                            showRegisterRow();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                        if (!isAdded()) return;
                        showRegisterRow();
                    }
                });
    }

    private void showBusinessStatus(BusinessAccount biz) {
        if (rowRegisterBusiness != null) rowRegisterBusiness.setVisibility(View.GONE);
        if (rowBusinessStatus != null) {
            rowBusinessStatus.setVisibility(View.VISIBLE);
            if (tvBusinessStatusSub != null) {
                String status = biz.getStatus();
                if ("pending".equals(status)) {
                    tvBusinessStatusSub.setText(R.string.profile_registration_status_pending);
                } else if ("rejected".equals(status)) {
                    tvBusinessStatusSub.setText(R.string.profile_registration_status_rejected);
                }
            }
            rowBusinessStatus.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), BusinessRegistrationDetailActivity.class);
                intent.putExtra("business_account", biz);
                startActivity(intent);
            });
        }
    }

    private void showRegisterRow() {
        if (rowBusinessStatus != null) rowBusinessStatus.setVisibility(View.GONE);
        if (rowRegisterBusiness != null) {
            rowRegisterBusiness.setVisibility(View.VISIBLE);
            rowRegisterBusiness.setOnClickListener(v -> startActivity(new Intent(requireContext(), RegisterBusinessActivity.class)));
        }
    }

    private void hideRegistrationRows() {
        if (rowRegisterBusiness != null) rowRegisterBusiness.setVisibility(View.GONE);
        if (rowBusinessStatus != null) rowBusinessStatus.setVisibility(View.GONE);
    }

    private void showLogoutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_logout)
                .setMessage(R.string.profile_logout_confirm)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> {
                    session.clear();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
