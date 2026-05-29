package com.example.tourgo.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.TourService;
import com.example.tourgo.ui.admin.AdminMockData.PendingListing;
import com.example.tourgo.ui.admin.AdminMockData.UserReport;
import com.example.tourgo.utils.ImageLoader;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

/** Admin › Moderation — pending tours (from the backend) & user reports. */
public class AdminModerationFragment extends Fragment {

    // Pending tours are fetched live from the server (GET /api/tours/pending).
    private final List<PendingListing> listings = new ArrayList<>();
    private final List<UserReport> reports = AdminMockData.userReports();

    private PendingListingAdapter listingAdapter;
    private ReportAdapter reportAdapter;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView empty;
    private String tab = "pending";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_moderation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rv = v.findViewById(R.id.admModList);
        progress = v.findViewById(R.id.admModProgress);
        empty = v.findViewById(R.id.admModEmpty);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        listingAdapter = new PendingListingAdapter(this::openListing);
        reportAdapter = new ReportAdapter(this::openReport);
        listingAdapter.setItems(listings);
        reportAdapter.setItems(reports);

        LinearLayout tabs = v.findViewById(R.id.admTopTabs);
        java.util.List<AdminTabBar.Tab> tabList = new java.util.ArrayList<>();
        tabList.add(new AdminTabBar.Tab("pending", getString(R.string.adm_tab_pending_listings), 17));
        tabList.add(new AdminTabBar.Tab("reports", getString(R.string.adm_tab_user_reports), 6));
        AdminTabBar.build(tabs, tabList, tab, id -> {
            tab = id;
            showTab();
        });

        showTab();
        loadPendingTours();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh so newly submitted / externally approved tours stay in sync.
        loadPendingTours();
    }

    private void showTab() {
        rv.setAdapter("pending".equals(tab) ? listingAdapter : reportAdapter);
        updateEmptyState();
    }

    private void loadPendingTours() {
        if (!"pending".equals(tab)) return;
        setLoading(true);
        TourService.getPendingTours(requireContext(), new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> tours) {
                if (!isAdded()) return;
                setLoading(false);
                listings.clear();
                if (tours != null) {
                    for (Tour tour : tours) {
                        listings.add(PendingListing.fromTour(requireContext(), tour));
                    }
                }
                listingAdapter.setItems(listings);
                updateEmptyState();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                setLoading(false);
                listings.clear();
                listingAdapter.setItems(listings);
                updateEmptyState();
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && empty != null) empty.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        if (empty == null) return;
        boolean showEmpty = "pending".equals(tab) && listings.isEmpty()
                && progress != null && progress.getVisibility() != View.VISIBLE;
        empty.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
    }

    // ── Listing review sheet ─────────────────────────────────────────────────
    private void openListing(PendingListing it) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_admin_listing, null);
        dialog.setContentView(sheet);
        expandFull(dialog, sheet);

        ImageView photo = sheet.findViewById(R.id.admListingPhoto);
        if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
            ImageLoader.loadThumbnail(photo, it.imageUrl);
        } else {
            photo.setImageResource(it.photoRes);
        }
        AdminUi.catChip(requireContext(), sheet.findViewById(R.id.admListingCat),
                sheet.findViewById(R.id.admListingCatDot), sheet.findViewById(R.id.admListingCatLabel), it.cat);
        ((TextView) sheet.findViewById(R.id.admListingName)).setText(it.name);
        ((TextView) sheet.findViewById(R.id.admListingCity)).setText(it.city);
        String unit = "hotel".equals(it.cat) ? " / night" : " / person";
        String priceLabel = it.priceText != null ? it.priceText + unit : "$" + it.price + unit;
        ((TextView) sheet.findViewById(R.id.admListingPrice)).setText(priceLabel);
        ((TextView) sheet.findViewById(R.id.admListingSubmitted)).setText(getString(R.string.adm_submitted, it.date));
        ((TextView) sheet.findViewById(R.id.admListingDesc)).setText(it.desc);
        ((TextView) sheet.findViewById(R.id.admListingBusiness)).setText(it.business);

        // Status history
        if (!it.history.isEmpty()) {
            sheet.findViewById(R.id.admListingHistorySection).setVisibility(View.VISIBLE);
            LinearLayout hist = sheet.findViewById(R.id.admListingHistory);
            LayoutInflater inf = LayoutInflater.from(requireContext());
            for (String[] h : it.history) {
                View row = inf.inflate(R.layout.item_admin_history, hist, false);
                ((TextView) row.findViewById(R.id.admHistoryAt)).setText(h[0]);
                ((TextView) row.findViewById(R.id.admHistoryNote)).setText(h[1]);
                hist.addView(row);
            }
        }

        sheet.findViewById(R.id.admListingClose).setOnClickListener(view -> dialog.dismiss());

        sheet.findViewById(R.id.admBtnApprove).setOnClickListener(view -> approve(it, dialog));
        sheet.findViewById(R.id.admBtnReject).setOnClickListener(view ->
                AdminUi.confirm(requireContext(),
                        getString(R.string.adm_reject_listing_title),
                        getString(R.string.adm_reject_listing_msg),
                        getString(R.string.adm_reject), true,
                        () -> { dialog.dismiss(); toast(R.string.adm_toast_listing_rejected); }));
        sheet.findViewById(R.id.admBtnRevision).setOnClickListener(view ->
                AdminUi.confirm(requireContext(),
                        getString(R.string.adm_request_revision_title),
                        getString(R.string.adm_request_revision_msg),
                        getString(R.string.adm_send_request), false,
                        () -> { dialog.dismiss(); toast(R.string.adm_toast_revision_requested); }));

        dialog.show();
    }

    // ── Report review sheet ──────────────────────────────────────────────────
    private void openReport(UserReport r) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_admin_report, null);
        dialog.setContentView(sheet);

        ((TextView) sheet.findViewById(R.id.admReportKindTitle)).setText(r.kind);
        ((TextView) sheet.findViewById(R.id.admReportMeta)).setText(getString(R.string.adm_reported_by, r.reporter, r.when));
        AdminUi.avatar(sheet.findViewById(R.id.admReportAvatar), r.target);
        ((TextView) sheet.findViewById(R.id.admReportTargetName)).setText(r.target);
        ((TextView) sheet.findViewById(R.id.admReportContext)).setText(r.context);
        ((TextView) sheet.findViewById(R.id.admReportBody)).setText("\"" + r.body + "\"");
        ((TextView) sheet.findViewById(R.id.admReportReasoning)).setText(r.reasoning);

        sheet.findViewById(R.id.admReportClose).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.admBtnContactBoth).setOnClickListener(view -> {
            dialog.dismiss();
            toast(R.string.adm_toast_contacted);
        });
        sheet.findViewById(R.id.admBtnDismiss).setOnClickListener(view -> {
            dialog.dismiss();
            toast(R.string.adm_toast_report_dismissed);
        });
        sheet.findViewById(R.id.admBtnApproveReport).setOnClickListener(view -> {
            dialog.dismiss();
            toast(R.string.adm_toast_report_approved);
        });

        dialog.show();
    }

    private void expandFull(BottomSheetDialog dialog, View sheet) {
        sheet.post(() -> {
            View parent = (View) sheet.getParent();
            if (parent == null) return;
            ViewGroup.LayoutParams lp = parent.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.setLayoutParams(lp);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
    }

    /**
     * Approve a pending tour via the backend (PUT /api/tours/{id}/approve). On
     * success the server flips its status to APPROVED — it then disappears from
     * this list and shows up on the User Home screen.
     */
    private void approve(PendingListing it, BottomSheetDialog dialog) {
        if (it.serverId == null) {
            // Legacy mock entry with no backend id — optimistic toast only.
            dialog.dismiss();
            toast(R.string.adm_toast_listing_approved);
            return;
        }
        TourService.approveTour(requireContext(), it.serverId, new DataCallback<Tour>() {
            @Override
            public void onSuccess(Tour tour) {
                if (!isAdded()) return;
                dialog.dismiss();
                listings.remove(it);
                listingAdapter.setItems(listings);
                updateEmptyState();
                toast(R.string.adm_toast_listing_approved);
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toast(int res) {
        Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show();
    }
}
