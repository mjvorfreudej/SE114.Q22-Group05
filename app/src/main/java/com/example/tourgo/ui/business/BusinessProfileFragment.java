package com.example.tourgo.ui.business;

import android.content.Intent;
import android.os.Bundle;
import com.example.tourgo.ui.chat.ChatListActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.FavoriteRepository;
import com.example.tourgo.data.repository.HotelRepository;
import com.example.tourgo.data.repository.TourRepository;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.models.response.BusinessReview;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.auth.LoginActivity;
import com.example.tourgo.utils.LocaleHelper;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Business › Profile — account header, mini stats, settings rows, logout. */
public class BusinessProfileFragment extends Fragment {

    private TextView mAvatar;
    private TextView mOwnerName;
    private TextView mOrgName;
    private TextView mStatusText;
    private TextView mListingsVal;
    private TextView mBookingsVal;
    private TextView mRatingVal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mAvatar = v.findViewById(R.id.bizProfileAvatar);
        mOwnerName = v.findViewById(R.id.bizProfileOwnerName);
        mOrgName = v.findViewById(R.id.bizProfileOrgName);
        mStatusText = v.findViewById(R.id.bizProfileStatusText);
        mListingsVal = v.findViewById(R.id.bizProfileListingsVal);
        mBookingsVal = v.findViewById(R.id.bizProfileBookingsVal);
        mRatingVal = v.findViewById(R.id.bizProfileRatingVal);

        // Fallback initials avatar while loading
        AdminUi.avatar(mAvatar, getString(R.string.biz_owner_name));

        LayoutInflater inf = LayoutInflater.from(requireContext());
        LinearLayout business = v.findViewById(R.id.bizBusinessRows);
        addRow(inf, business, R.drawable.ic_building, R.string.biz_row_details, 0);
        addRow(inf, business, R.drawable.ic_dollar, R.string.biz_row_payouts, 0);
        addRow(inf, business, R.drawable.ic_tag, R.string.biz_row_tax, 0);
        addRow(inf, business, R.drawable.ic_users, R.string.biz_row_team, 0);

        LinearLayout prefs = v.findViewById(R.id.bizPrefRows);
        addRow(inf, prefs, R.drawable.ic_bell_20, R.string.biz_row_notifications, 0);
        View msgRow = addRow(inf, prefs, R.drawable.ic_message_circle, R.string.profile_messages, 0);
        if (msgRow != null) {
            msgRow.setOnClickListener(view -> startActivity(new Intent(requireContext(), ChatListActivity.class)));
        }
        View langRow = addRow(inf, prefs, R.drawable.ic_globe_24, R.string.biz_row_language, R.string.biz_row_language_value);
        if (langRow != null) {
            langRow.setOnClickListener(view -> showLanguageDialog());
        }
        addRow(inf, prefs, R.drawable.ic_lock_24, R.string.biz_row_security, 0);
        addRow(inf, prefs, R.drawable.ic_help_circle, R.string.biz_row_help, 0);

        v.findViewById(R.id.bizLogout).setOnClickListener(view ->
                AdminUi.confirm(requireContext(),
                        getString(R.string.profile_logout_title),
                        getString(R.string.profile_logout_confirm),
                        getString(R.string.profile_logout_title), true, this::logout));

        loadBusinessProfile();
    }

    private void loadBusinessProfile() {
        if (!isAdded()) return;

        RetrofitClient.getInstance(requireContext())
                .getUserApi()
                .getBusinesses()
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            BusinessAccount biz = response.body().getData();
                            bindProfileData(biz);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                        // Keep placeholders on failure
                    }
                });

        RetrofitClient.getInstance(requireContext())
                .getReviewApi()
                .getBusinessReviews()
                .enqueue(new Callback<ApiResponse<List<BusinessReview>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<BusinessReview>>> call, Response<ApiResponse<List<BusinessReview>>> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<BusinessReview> reviews = response.body().getData();
                            updateAverageRating(reviews);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<BusinessReview>>> call, Throwable t) {
                        // Keep default rating on failure
                    }
                });
    }

    private void bindProfileData(BusinessAccount biz) {
        if (biz == null) return;

        if (mOwnerName != null) mOwnerName.setText(biz.getOwner());
        if (mOrgName != null) mOrgName.setText(biz.getName());
        if (mAvatar != null) AdminUi.avatar(mAvatar, biz.getOwner());
        if (mListingsVal != null) mListingsVal.setText(String.valueOf(biz.getListings()));
        if (mBookingsVal != null) mBookingsVal.setText(String.valueOf(biz.getBookings()));

        if (mStatusText != null && biz.getStatus() != null) {
            if ("active".equals(biz.getStatus())) {
                mStatusText.setText(R.string.biz_verified_business);
            } else {
                mStatusText.setText(biz.getStatus().toUpperCase(Locale.getDefault()));
            }
        }
    }

    private void updateAverageRating(List<BusinessReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            if (mRatingVal != null) mRatingVal.setText("0.0");
            return;
        }

        int sum = 0;
        for (BusinessReview r : reviews) {
            sum += r.getRating();
        }
        double avg = (double) sum / reviews.size();
        if (mRatingVal != null) {
            mRatingVal.setText(String.format(Locale.getDefault(), "%.1f", avg));
        }
    }

    private void showLanguageDialog() {
        if (!isAdded()) return;
        String[] languages = {"Tiếng Việt", "English"};
        final String[] tags = {"vi", "en"};
        String currentTag = LocaleHelper.getCurrentLanguageTag();
        int checkedItem = "en".equals(currentTag) ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.biz_row_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    LocaleHelper.setAppLocale(tags[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private View addRow(LayoutInflater inf, LinearLayout parent, int iconRes, int labelRes, int trailingRes) {
        View row = inf.inflate(R.layout.item_biz_profile_row, parent, false);
        ((ImageView) row.findViewById(R.id.bizRowIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.bizRowLabel)).setText(labelRes);
        if (trailingRes != 0) {
            TextView trailing = row.findViewById(R.id.bizRowTrailing);
            trailing.setText(trailingRes);
            trailing.setVisibility(View.VISIBLE);
        }
        parent.addView(row);
        return row;
    }

    private void logout() {
        new SessionManager(requireContext()).clear();
        UserRepository.getInstance().clearCache();
        FavoriteRepository.getInstance().clearCache();
        HotelRepository.getInstance().clearCache();
        TourRepository.getInstance().clearCache();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
