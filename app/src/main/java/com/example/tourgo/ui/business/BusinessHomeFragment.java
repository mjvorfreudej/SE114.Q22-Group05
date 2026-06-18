package com.example.tourgo.ui.business;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.notification.NotificationItem;
import com.example.tourgo.ui.notification.NotificationMockData;
import com.example.tourgo.ui.notification.NotificationPopover;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Business › Home dashboard — welcome banner, 4 KPI tiles, quick actions, recent bookings. */
public class BusinessHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        LayoutInflater inf = LayoutInflater.from(requireContext());

        setupBell(v);

        // Find header views
        TextView headerName = v.findViewById(R.id.bizHeaderName);
        TextView welcomeTitle = v.findViewById(R.id.bizWelcomeTitle);
        TextView welcomeSub = v.findViewById(R.id.bizWelcomeSub);

        // KPI tiles (2×2)
        GridLayout grid = v.findViewById(R.id.bizStatGrid);
        final TextView statListingsVal = addStat(inf, grid, 0, R.drawable.ic_building, R.color.adm_blue_700, R.color.adm_blue_50,
                "0", R.string.biz_stat_listings, null);
        final TextView statBookingsVal = addStat(inf, grid, 1, R.drawable.ic_calendar, R.color.adm_teal_ink, R.color.adm_teal_50,
                "0", R.string.biz_stat_bookings, null);
        final TextView statReviewsVal = addStat(inf, grid, 2, R.drawable.ic_message_circle, R.color.adm_orange_ink, R.color.adm_orange_50,
                "0", R.string.biz_stat_reviews, null);
        final TextView statReportsVal = addStat(inf, grid, 3, R.drawable.ic_flag, R.color.adm_red_700, R.color.adm_red_100,
                "0", R.string.biz_stat_reports, null);

        // Fetch real data from server
        RetrofitClient.getInstance(requireContext())
                .getUserApi()
                .getBusinesses()
                .enqueue(new Callback<ApiResponse<BusinessAccount>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<BusinessAccount>> call, Response<ApiResponse<BusinessAccount>> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            BusinessAccount biz = response.body().getData();
                            if (headerName != null) headerName.setText(biz.getName());
                            if (welcomeTitle != null) welcomeTitle.setText("Chào mừng trở lại, " + biz.getOwner());
                            if (welcomeSub != null) welcomeSub.setText("Hôm nay bạn có " + biz.getBookings() + " lượt đặt phòng và " + biz.getReviews() + " đánh giá đang chờ phản hồi.");
                            if (statListingsVal != null) statListingsVal.setText(String.valueOf(biz.getListings()));
                            if (statBookingsVal != null) statBookingsVal.setText(String.valueOf(biz.getBookings()));
                            if (statReviewsVal != null) statReviewsVal.setText(String.valueOf(biz.getReviews()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<BusinessAccount>> call, Throwable t) {
                        // Keep 0s on error
                    }
                });

        // Quick actions
        LinearLayout qa = v.findViewById(R.id.bizQuickActions);
        addQuickAction(inf, qa, R.drawable.ic_plus, R.color.adm_amber_100, R.color.adm_amber_500,
                R.string.biz_qa_add, () -> activity().openAddListing());
        addQuickAction(inf, qa, R.drawable.ic_calendar, R.color.adm_teal_100, R.color.adm_teal_500,
                R.string.biz_qa_calendar, () -> activity().goToTab(BusinessActivity.TAB_CALENDAR));
        addQuickAction(inf, qa, R.drawable.ic_star, R.color.adm_purple_100, R.color.adm_purple_500,
                R.string.biz_qa_reviews, () -> activity().goToTab(BusinessActivity.TAB_REVIEWS));
    }

    private BusinessActivity activity() {
        return (BusinessActivity) requireActivity();
    }

    /**
     * Header bell → Business notification popover (design index.html, role="business").
     * The red count badge shows the initial unread tally; the popover and full center
     * hold their own optimistic state, mirroring the prototype (no notifications API yet).
     */
    private void setupBell(View root) {
        View bell = root.findViewById(R.id.bizBellBtn);
        TextView badge = root.findViewById(R.id.bizBellBadge);

        int unread = 0;
        for (NotificationItem n : NotificationMockData.seed(requireContext(), NotificationMockData.Role.BUSINESS)) {
            if (!n.read) unread++;
        }
        if (unread > 0) {
            badge.setText(unread > 9 ? "9+" : String.valueOf(unread));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }

        bell.setOnClickListener(v -> NotificationPopover.show(v, NotificationMockData.Role.BUSINESS));
    }

    private TextView addStat(LayoutInflater inf, GridLayout grid, int index, int iconRes,
                             @ColorRes int accent, @ColorRes int soft, String value,
                             int labelRes, @Nullable String delta) {
        View tile = inf.inflate(R.layout.item_biz_stat, grid, false);
        tile.findViewById(R.id.bizStatIconWrap)
                .setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView icon = tile.findViewById(R.id.bizStatIcon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color(accent)));
        TextView valTv = tile.findViewById(R.id.bizStatValue);
        valTv.setText(value);
        ((TextView) tile.findViewById(R.id.bizStatLabel)).setText(labelRes);
        TextView d = tile.findViewById(R.id.bizStatDelta);
        if (delta != null) {
            d.setText(delta);
            d.setVisibility(View.VISIBLE);
        }

        int gap = BizUi.dp(requireContext(), 5);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(index % 2, 1f);
        lp.rowSpec = GridLayout.spec(index / 2);
        lp.setMargins(gap, gap, gap, gap);
        tile.setLayoutParams(lp);
        grid.addView(tile);
        return valTv;
    }

    private void addQuickAction(LayoutInflater inf, LinearLayout row, int iconRes,
                                @ColorRes int soft, @ColorRes int accent, int labelRes,
                                Runnable onClick) {
        View tile = inf.inflate(R.layout.item_biz_quick_action, row, false);
        tile.findViewById(R.id.bizQaIconWrap)
                .setBackgroundTintList(ColorStateList.valueOf(color(soft)));
        ImageView icon = tile.findViewById(R.id.bizQaIcon);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(color(accent)));
        ((TextView) tile.findViewById(R.id.bizQaLabel)).setText(labelRes);
        tile.setOnClickListener(view -> onClick.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        int gap = BizUi.dp(requireContext(), 4);
        lp.setMargins(row.getChildCount() == 0 ? 0 : gap, 0,
                gap, 0);
        tile.setLayoutParams(lp);
        row.addView(tile);
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
