package com.example.tourgo.ui.business;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminTabBar;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.remote.api.ReviewApi;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessReview;
import com.example.tourgo.ui.business.BusinessMockData.RatingBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Business › Reviews & Reports — rating summary, review list with replies, reports queue. */
public class BusinessReviewsFragment extends Fragment {

    private static final int STAR_GOLD = Color.parseColor("#FDB022");

    private View reviewsContent, reportsContent;
    private LinearLayout reviewsList, reportsList;
    private View rootView;
    private List<BusinessReview> mReviews = new ArrayList<>();
    private String mActiveTab = "reviews";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        rootView = v;

        reviewsContent = v.findViewById(R.id.bizReviewsContent);
        reportsContent = v.findViewById(R.id.bizReportsContent);
        reviewsList = v.findViewById(R.id.bizReviewsList);
        reportsList = v.findViewById(R.id.bizReportsList);

        updateTabs();
        loadReviews();
    }

    private void updateTabs() {
        if (!isAdded() || rootView == null) return;
        List<AdminTabBar.Tab> tabs = Arrays.asList(
                new AdminTabBar.Tab("reviews", getString(R.string.biz_tab_reviews), mReviews.size()),
                new AdminTabBar.Tab("reports", getString(R.string.biz_tab_reports), 0)
        );
        AdminTabBar.build(rootView.findViewById(R.id.bizReviewTabs), tabs, mActiveTab, id -> {
            mActiveTab = id;
            boolean reviews = "reviews".equals(id);
            BizUi.show(reviewsContent, reviews);
            BizUi.show(reportsContent, !reviews);
        });
    }

    private void loadReviews() {
        ReviewApi api = RetrofitClient.getInstance(requireContext()).getReviewApi();
        api.getBusinessReviews().enqueue(new Callback<ApiResponse<List<BusinessReview>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BusinessReview>>> call, Response<ApiResponse<List<BusinessReview>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mReviews = response.body().getData();
                } else {
                    mReviews.clear();
                }
                updateTabs();
                rebuildReviewsUi();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BusinessReview>>> call, Throwable t) {
                mReviews.clear();
                updateTabs();
                rebuildReviewsUi();
            }
        });
    }

    private void rebuildReviewsUi() {
        if (!isAdded() || rootView == null) return;

        buildSummary(rootView);

        if (reviewsList != null) {
            reviewsList.removeAllViews();
            buildReviews(reviewsList);
        }

        if (reportsList != null) {
            reportsList.removeAllViews();
        }
    }

    // ── Summary card ────────────────────────────────────────────────────────────
    private void buildSummary(View v) {
        int totalReviews = mReviews.size();
        int sumRatings = 0;
        int[] starCounts = new int[5]; // index 0=5star, 1=4star, 2=3star, 3=2star, 4=1star
        for (BusinessReview r : mReviews) {
            int rating = Math.max(1, Math.min(5, r.getRating()));
            sumRatings += rating;
            starCounts[5 - rating]++;
        }

        double overallVal = totalReviews > 0 ? (double) sumRatings / totalReviews : 0.0;
        String overallStr = String.format(Locale.getDefault(), "%.1f", overallVal);

        TextView sumRatingTv = v.findViewById(R.id.bizSummaryRating);
        if (sumRatingTv != null) {
            sumRatingTv.setText(overallStr);
        }

        LinearLayout sumStarsContainer = v.findViewById(R.id.bizSummaryStars);
        if (sumStarsContainer != null) {
            sumStarsContainer.removeAllViews();
            addStars(sumStarsContainer, (int) Math.round(overallVal), 11);
        }

        TextView sumCountTv = v.findViewById(R.id.bizSummaryCount);
        if (sumCountTv != null) {
            sumCountTv.setText(getString(R.string.biz_reviews_count, totalReviews));
        }

        LinearLayout bars = v.findViewById(R.id.bizRatingBars);
        if (bars != null) {
            bars.removeAllViews();
            int maxCount = 0;
            for (int count : starCounts) {
                if (count > maxCount) maxCount = count;
            }
            for (int i = 0; i < 5; i++) {
                int stars = 5 - i;
                int count = starCounts[i];
                float pct = maxCount > 0 ? (float) count / maxCount : 0f;
                bars.addView(buildBar(new RatingBar(stars, count, pct)));
            }
        }
    }

    private View buildBar(RatingBar rb) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, BizUi.dp(requireContext(), 2), 0, BizUi.dp(requireContext(), 2));

        TextView n = new TextView(requireContext());
        n.setText(String.valueOf(rb.stars));
        n.setTextColor(color(R.color.adm_gray_500));
        n.setTextSize(11f);
        row.addView(n, new LinearLayout.LayoutParams(BizUi.dp(requireContext(), 12),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView star = new ImageView(requireContext());
        star.setImageResource(R.drawable.ic_star);
        star.setColorFilter(STAR_GOLD, PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                BizUi.dp(requireContext(), 9), BizUi.dp(requireContext(), 9));
        slp.setMargins(0, 0, BizUi.dp(requireContext(), 8), 0);
        row.addView(star, slp);

        // Track with proportional fill
        LinearLayout track = new LinearLayout(requireContext());
        track.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable tg = new GradientDrawable();
        tg.setShape(GradientDrawable.RECTANGLE);
        tg.setCornerRadius(BizUi.dp(requireContext(), 999));
        tg.setColor(color(R.color.adm_gray_100));
        track.setBackground(tg);
        track.setClipToOutline(true);
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(0,
                BizUi.dp(requireContext(), 5), 1f);
        trackLp.setMargins(0, 0, BizUi.dp(requireContext(), 8), 0);

        View fill = new View(requireContext());
        GradientDrawable fg = new GradientDrawable();
        fg.setShape(GradientDrawable.RECTANGLE);
        fg.setCornerRadius(BizUi.dp(requireContext(), 999));
        fg.setColor(STAR_GOLD);
        fill.setBackground(fg);
        track.addView(fill, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, rb.pct));
        View rest = new View(requireContext());
        track.addView(rest, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f - rb.pct));
        row.addView(track, trackLp);

        TextView count = new TextView(requireContext());
        count.setText(String.valueOf(rb.count));
        count.setTextColor(color(R.color.adm_gray_500));
        count.setTextSize(11f);
        count.setGravity(Gravity.END);
        row.addView(count, new LinearLayout.LayoutParams(BizUi.dp(requireContext(), 18),
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return row;
    }

    // ── Reviews list ────────────────────────────────────────────────────────────
    private void buildReviews(LinearLayout list) {
        LayoutInflater inf = LayoutInflater.from(requireContext());
        for (BusinessReview r : mReviews) {
            View card = inf.inflate(R.layout.item_biz_review, list, false);
            AdminUi.avatar(card.findViewById(R.id.bizReviewAvatar), r.getName());
            ((TextView) card.findViewById(R.id.bizReviewName)).setText(r.getName());
            
            String timeStr = r.getCreatedAt();
            if (timeStr != null && timeStr.length() >= 10) {
                timeStr = timeStr.substring(0, 10);
            } else {
                timeStr = "";
            }
            String listingName = r.getListing() != null ? r.getListing() : "Dịch vụ";
            
            ((TextView) card.findViewById(R.id.bizReviewMeta)).setText(listingName + " · " + timeStr);
            ((TextView) card.findViewById(R.id.bizReviewBody)).setText(r.getBody());
            addStars(card.findViewById(R.id.bizReviewStars), r.getRating(), 11);

            View replyBox = card.findViewById(R.id.bizReviewReplyBox);
            View replyBtn = card.findViewById(R.id.bizReviewReplyBtn);
            
            replyBox.setVisibility(View.GONE);
            replyBtn.setVisibility(View.VISIBLE);
            replyBtn.setOnClickListener(view -> openReply(r));
            
            list.addView(card);
        }
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    private void openReply(BusinessReview r) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_biz_reply, null, false);
        AdminUi.avatar(sheet.findViewById(R.id.bizReplyAvatar), r.getName());
        ((TextView) sheet.findViewById(R.id.bizReplyName)).setText(r.getName());
        ((TextView) sheet.findViewById(R.id.bizReplyBody)).setText(r.getBody());
        addStars(sheet.findViewById(R.id.bizReplyStars), r.getRating(), 11);

        sheet.findViewById(R.id.bizReplyClose).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizReplyCancel).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizReplyPost).setOnClickListener(view -> {
            dialog.dismiss();
            toast(getString(R.string.biz_toast_reply));
        });
        dialog.setContentView(sheet);
        dialog.show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void addStars(LinearLayout container, int rating, int sizeDp) {
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(requireContext());
            star.setImageResource(R.drawable.ic_star);
            star.setColorFilter(i <= rating ? STAR_GOLD
                    : color(R.color.adm_gray_300), PorterDuff.Mode.SRC_IN);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    BizUi.dp(requireContext(), sizeDp), BizUi.dp(requireContext(), sizeDp));
            lp.setMargins(BizUi.dp(requireContext(), 1), 0, 0, 0);
            container.addView(star, lp);
        }
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
