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
import com.example.tourgo.ui.business.BusinessMockData.RatingBar;
import com.example.tourgo.ui.business.BusinessMockData.Report;
import com.example.tourgo.ui.business.BusinessMockData.Review;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Arrays;
import java.util.List;

/** Business › Reviews & Reports — rating summary, review list with replies, reports queue. */
public class BusinessReviewsFragment extends Fragment {

    private static final int STAR_GOLD = Color.parseColor("#FDB022");

    private View reviewsContent, reportsContent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        reviewsContent = v.findViewById(R.id.bizReviewsContent);
        reportsContent = v.findViewById(R.id.bizReportsContent);

        List<AdminTabBar.Tab> tabs = Arrays.asList(
                new AdminTabBar.Tab("reviews", getString(R.string.biz_tab_reviews), BusinessMockData.TOTAL_REVIEWS),
                new AdminTabBar.Tab("reports", getString(R.string.biz_tab_reports), BusinessMockData.reports().size())
        );
        AdminTabBar.build(v.findViewById(R.id.bizReviewTabs), tabs, "reviews", id -> {
            boolean reviews = "reviews".equals(id);
            BizUi.show(reviewsContent, reviews);
            BizUi.show(reportsContent, !reviews);
        });

        buildSummary(v);
        buildReviews(v.findViewById(R.id.bizReviewsList));
        buildReports(v.findViewById(R.id.bizReportsList));
    }

    // ── Summary card ────────────────────────────────────────────────────────────
    private void buildSummary(View v) {
        ((TextView) v.findViewById(R.id.bizSummaryRating)).setText(BusinessMockData.OVERALL_RATING);
        addStars(v.findViewById(R.id.bizSummaryStars), 5, 11);
        ((TextView) v.findViewById(R.id.bizSummaryCount))
                .setText(getString(R.string.biz_reviews_count, BusinessMockData.TOTAL_REVIEWS));

        LinearLayout bars = v.findViewById(R.id.bizRatingBars);
        for (RatingBar rb : BusinessMockData.ratingDistribution()) bars.addView(buildBar(rb));
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
        for (Review r : BusinessMockData.reviews()) {
            View card = inf.inflate(R.layout.item_biz_review, list, false);
            AdminUi.avatar(card.findViewById(R.id.bizReviewAvatar), r.name);
            ((TextView) card.findViewById(R.id.bizReviewName)).setText(r.name);
            ((TextView) card.findViewById(R.id.bizReviewMeta)).setText(r.listing + " · " + r.when);
            ((TextView) card.findViewById(R.id.bizReviewBody)).setText(r.body);
            addStars(card.findViewById(R.id.bizReviewStars), r.rating, 11);

            View replyBox = card.findViewById(R.id.bizReviewReplyBox);
            View replyBtn = card.findViewById(R.id.bizReviewReplyBtn);
            if (r.replied) {
                replyBox.setVisibility(View.VISIBLE);
                ((TextView) card.findViewById(R.id.bizReviewReplyText)).setText(r.replyText);
            } else {
                replyBtn.setVisibility(View.VISIBLE);
                replyBtn.setOnClickListener(view -> openReply(r));
            }
            list.addView(card);
        }
    }

    // ── Reports list ────────────────────────────────────────────────────────────
    private void buildReports(LinearLayout list) {
        LayoutInflater inf = LayoutInflater.from(requireContext());
        for (Report r : BusinessMockData.reports()) {
            View card = inf.inflate(R.layout.item_biz_report, list, false);
            ((TextView) card.findViewById(R.id.bizReportKind)).setText(r.kind);
            ((TextView) card.findViewById(R.id.bizReportWhen)).setText(r.when);
            String snippet = r.body.length() > 90 ? r.body.substring(0, 90) + "…" : r.body;
            ((TextView) card.findViewById(R.id.bizReportBody)).setText("\"" + snippet + "\"");
            ((TextView) card.findViewById(R.id.bizReportBy)).setText(byUser(r.targetUser));
            card.setOnClickListener(view -> openReport(r));
            list.addView(card);
        }
    }

    private CharSequence byUser(String user) {
        SpannableStringBuilder sb = new SpannableStringBuilder("by ");
        int start = sb.length();
        sb.append(user);
        sb.setSpan(new StyleSpan(Typeface.BOLD), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(color(R.color.adm_gray_900)), start, sb.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    private void openReply(Review r) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_biz_reply, null, false);
        AdminUi.avatar(sheet.findViewById(R.id.bizReplyAvatar), r.name);
        ((TextView) sheet.findViewById(R.id.bizReplyName)).setText(r.name);
        ((TextView) sheet.findViewById(R.id.bizReplyBody)).setText(r.body);
        addStars(sheet.findViewById(R.id.bizReplyStars), r.rating, 11);

        sheet.findViewById(R.id.bizReplyClose).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizReplyCancel).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizReplyPost).setOnClickListener(view -> {
            dialog.dismiss();
            toast(getString(R.string.biz_toast_reply));
        });
        dialog.setContentView(sheet);
        dialog.show();
    }

    private void openReport(Report r) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_biz_report, null, false);
        ((TextView) sheet.findViewById(R.id.bizReportSheetKind)).setText(r.kind);
        ((TextView) sheet.findViewById(R.id.bizReportSheetReporter))
                .setText(getString(R.string.biz_reported_by, r.reporter, r.when));
        ((TextView) sheet.findViewById(R.id.bizReportSheetBody)).setText("\"" + r.body + "\"");
        ((TextView) sheet.findViewById(R.id.bizReportSheetReasoning)).setText(r.reasoning);

        sheet.findViewById(R.id.bizReportClose).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizReportDismiss).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizReportApprove).setOnClickListener(view -> {
            dialog.dismiss();
            toast(getString(R.string.biz_toast_banned));
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
