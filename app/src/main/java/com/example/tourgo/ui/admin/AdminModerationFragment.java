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
import com.example.tourgo.models.response.AdminReport;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.AdminService;
import com.example.tourgo.remote.service.HotelService;
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

    // Both lists are fetched live from the backend:
    //   pending tours  → GET /api/tours/pending
    //   user reports   → GET /api/admin/reports
    private final List<PendingListing> listings = new ArrayList<>();
    private final List<UserReport> reports = new ArrayList<>();
    private final List<PendingListing> approvedListings = new ArrayList<>();

    private PendingListingAdapter listingAdapter;
    private ReportAdapter reportAdapter;
    private LinearLayout tabsContainer;
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

        tabsContainer = v.findViewById(R.id.admTopTabs);
        buildTabs();

        showTab();
        loadPendingListings();
        loadApprovedListings();
        loadReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh so newly submitted tours / filed reports stay in sync.
        loadPendingListings();
        loadApprovedListings();
        loadReports();
    }

    /** (Re)build the segmented tab strip with the current live counts. */
    private void buildTabs() {
        if (tabsContainer == null) return;
        List<AdminTabBar.Tab> tabList = new ArrayList<>();
        tabList.add(new AdminTabBar.Tab("pending", getString(R.string.adm_tab_pending_listings), listings.size()));
        tabList.add(new AdminTabBar.Tab("approved", getString(R.string.adm_tab_approved_listings), approvedListings.size()));
        tabList.add(new AdminTabBar.Tab("reports", getString(R.string.adm_tab_user_reports), reports.size()));
        AdminTabBar.build(tabsContainer, tabList, tab, id -> {
            tab = id;
            showTab();
        });
    }

    private void showTab() {
        if ("pending".equals(tab)) {
            rv.setAdapter(listingAdapter);
            listingAdapter.setItems(listings);
        } else if ("approved".equals(tab)) {
            rv.setAdapter(listingAdapter);
            listingAdapter.setItems(approvedListings);
        } else {
            rv.setAdapter(reportAdapter);
            reportAdapter.setItems(reports);
        }
        updateEmptyState();
    }

    private void loadPendingListings() {
        if ("pending".equals(tab)) setLoading(true);
        
        // Fetch tours first
        TourService.getPendingTours(requireContext(), new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> tours) {
                if (!isAdded()) return;
                
                // Nest hotel fetch
                HotelService.getPendingHotels(requireContext(), new DataCallback<List<com.example.tourgo.models.response.Hotel>>() {
                    @Override
                    public void onSuccess(List<com.example.tourgo.models.response.Hotel> hotels) {
                        if (!isAdded()) return;
                        if ("pending".equals(tab)) setLoading(false);
                        
                        listings.clear();
                        if (tours != null) {
                            for (Tour t : tours) {
                                listings.add(PendingListing.fromTour(requireContext(), t));
                            }
                        }
                        if (hotels != null) {
                            for (com.example.tourgo.models.response.Hotel h : hotels) {
                                listings.add(PendingListing.fromHotel(requireContext(), h));
                            }
                        }
                        // Sort by date descending
                        listings.sort((a, b) -> b.date.compareTo(a.date));
                        
                        listingAdapter.setItems(listings);
                        buildTabs();
                        updateEmptyState();
                    }

                    @Override
                    public void onError(ApiErrorCode code, String msg) {
                        if (!isAdded()) return;
                        if ("pending".equals(tab)) setLoading(false);
                        
                        // If hotels query fails, still display tours
                        listings.clear();
                        if (tours != null) {
                            for (Tour t : tours) {
                                listings.add(PendingListing.fromTour(requireContext(), t));
                            }
                        }
                        listingAdapter.setItems(listings);
                        buildTabs();
                        updateEmptyState();
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                
                // If tours query fails, try hotels anyway
                HotelService.getPendingHotels(requireContext(), new DataCallback<List<com.example.tourgo.models.response.Hotel>>() {
                    @Override
                    public void onSuccess(List<com.example.tourgo.models.response.Hotel> hotels) {
                        if (!isAdded()) return;
                        if ("pending".equals(tab)) setLoading(false);
                        
                        listings.clear();
                        if (hotels != null) {
                            for (com.example.tourgo.models.response.Hotel h : hotels) {
                                listings.add(PendingListing.fromHotel(requireContext(), h));
                            }
                        }
                        listingAdapter.setItems(listings);
                        buildTabs();
                        updateEmptyState();
                    }

                    @Override
                    public void onError(ApiErrorCode code2, String msg2) {
                        if (!isAdded()) return;
                        if ("pending".equals(tab)) setLoading(false);
                        listings.clear();
                        listingAdapter.setItems(listings);
                        buildTabs();
                        updateEmptyState();
                        Toast.makeText(requireContext(),
                                msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadApprovedListings() {
        if ("approved".equals(tab)) setLoading(true);
        
        TourService.getTours(requireContext(), new DataCallback<List<Tour>>() {
            @Override
            public void onSuccess(List<Tour> tours) {
                if (!isAdded()) return;
                
                HotelService.getHotels(requireContext(), new DataCallback<List<com.example.tourgo.models.response.Hotel>>() {
                    @Override
                    public void onSuccess(List<com.example.tourgo.models.response.Hotel> hotels) {
                        if (!isAdded()) return;
                        if ("approved".equals(tab)) setLoading(false);
                        
                        approvedListings.clear();
                        if (tours != null) {
                            for (Tour t : tours) {
                                // Set status to APPROVED since they are loaded from getTours (approved list)
                                t.setStatus("APPROVED");
                                approvedListings.add(PendingListing.fromTour(requireContext(), t));
                            }
                        }
                        if (hotels != null) {
                            for (com.example.tourgo.models.response.Hotel h : hotels) {
                                // Set status to APPROVED since they are loaded from getHotels (approved list)
                                h.setStatus("APPROVED");
                                approvedListings.add(PendingListing.fromHotel(requireContext(), h));
                            }
                        }
                        // Sort by date descending
                        approvedListings.sort((a, b) -> b.date.compareTo(a.date));
                        
                        if ("approved".equals(tab)) {
                            listingAdapter.setItems(approvedListings);
                        }
                        buildTabs();
                        updateEmptyState();
                    }

                    @Override
                    public void onError(ApiErrorCode code, String msg) {
                        if (!isAdded()) return;
                        if ("approved".equals(tab)) setLoading(false);
                        
                        approvedListings.clear();
                        if (tours != null) {
                            for (Tour t : tours) {
                                t.setStatus("APPROVED");
                                approvedListings.add(PendingListing.fromTour(requireContext(), t));
                            }
                        }
                        if ("approved".equals(tab)) {
                            listingAdapter.setItems(approvedListings);
                        }
                        buildTabs();
                        updateEmptyState();
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                
                HotelService.getHotels(requireContext(), new DataCallback<List<com.example.tourgo.models.response.Hotel>>() {
                    @Override
                    public void onSuccess(List<com.example.tourgo.models.response.Hotel> hotels) {
                        if (!isAdded()) return;
                        if ("approved".equals(tab)) setLoading(false);
                        
                        approvedListings.clear();
                        if (hotels != null) {
                            for (com.example.tourgo.models.response.Hotel h : hotels) {
                                h.setStatus("APPROVED");
                                approvedListings.add(PendingListing.fromHotel(requireContext(), h));
                            }
                        }
                        if ("approved".equals(tab)) {
                            listingAdapter.setItems(approvedListings);
                        }
                        buildTabs();
                        updateEmptyState();
                    }

                    @Override
                    public void onError(ApiErrorCode code2, String msg2) {
                        if (!isAdded()) return;
                        if ("approved".equals(tab)) setLoading(false);
                        approvedListings.clear();
                        if ("approved".equals(tab)) {
                            listingAdapter.setItems(approvedListings);
                        }
                        buildTabs();
                        updateEmptyState();
                        Toast.makeText(requireContext(),
                                msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadReports() {
        if ("reports".equals(tab)) setLoading(true);
        AdminService.getReports(requireContext(), new DataCallback<List<AdminReport>>() {
            @Override
            public void onSuccess(List<AdminReport> data) {
                if (!isAdded()) return;
                if ("reports".equals(tab)) setLoading(false);
                reports.clear();
                if (data != null) {
                    for (AdminReport r : data) reports.add(UserReport.fromServer(r));
                }
                reportAdapter.setItems(reports);
                buildTabs();
                updateEmptyState();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                if ("reports".equals(tab)) setLoading(false);
                reports.clear();
                reportAdapter.setItems(reports);
                buildTabs();
                updateEmptyState();
                // No toast here — an empty reports table is a valid state.
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && empty != null) empty.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        if (empty == null) return;
        boolean listEmpty;
        if ("pending".equals(tab)) {
            listEmpty = listings.isEmpty();
        } else if ("approved".equals(tab)) {
            listEmpty = approvedListings.isEmpty();
        } else {
            listEmpty = reports.isEmpty();
        }
        boolean showEmpty = listEmpty && progress != null && progress.getVisibility() != View.VISIBLE;
        empty.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
    }

    // ── Listing review screen ─────────────────────────────────────────────────
    private void openListing(PendingListing it) {
        android.content.Intent intent = new android.content.Intent(requireContext(), com.example.tourgo.ui.main.detail.DetailActivity.class);
        if ("hotel".equals(it.cat)) {
            intent.putExtra("hotel_object", it.originalHotel);
        } else {
            intent.putExtra("hotel_object", it.originalTour);
        }
        intent.putExtra("is_admin_mode", true);
        intent.putExtra("pending_listing_item", it);
        startActivity(intent);
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
        // "Contact both" has no backend action — it's an out-of-band message.
        sheet.findViewById(R.id.admBtnContactBoth).setOnClickListener(view -> {
            dialog.dismiss();
            toast(R.string.adm_toast_contacted);
        });
        // Dismiss → mark the report dismissed (no action against the target).
        sheet.findViewById(R.id.admBtnDismiss).setOnClickListener(view ->
                actOnReport(r, dialog, false, R.string.adm_toast_report_dismissed));
        // Approve → resolve the report (action taken against the target).
        sheet.findViewById(R.id.admBtnApproveReport).setOnClickListener(view ->
                actOnReport(r, dialog, true, R.string.adm_toast_report_approved));

        dialog.show();
    }

    /**
     * Dismiss or resolve a report via the backend
     * (PUT /api/admin/reports/{id}/dismiss|resolve). On success the row leaves
     * the open-reports list.
     */
    private void actOnReport(UserReport r, BottomSheetDialog dialog, boolean resolve, int toastRes) {
        if (r.serverId == null) {
            dialog.dismiss();
            toast(toastRes);
            return;
        }
        DataCallback<Void> cb = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (!isAdded()) return;
                dialog.dismiss();
                reports.remove(r);
                reportAdapter.setItems(reports);
                buildTabs();
                updateEmptyState();
                refreshHostBadge();
                toast(toastRes);
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        };
        if (resolve) AdminService.resolveReport(requireContext(), r.serverId, cb);
        else AdminService.dismissReport(requireContext(), r.serverId, cb);
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

    private void approve(PendingListing it, BottomSheetDialog dialog) {
        if (it.serverId == null) {
            dialog.dismiss();
            toast(R.string.adm_toast_listing_approved);
            return;
        }

        if ("hotel".equals(it.cat)) {
            HotelService.approveHotel(requireContext(), it.serverId, new DataCallback<com.example.tourgo.models.response.Hotel>() {
                @Override
                public void onSuccess(com.example.tourgo.models.response.Hotel hotel) {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    listings.remove(it);
                    listingAdapter.setItems(listings);
                    updateEmptyState();
                    buildTabs();
                    toast(R.string.adm_toast_listing_approved);
                }

                @Override
                public void onError(ApiErrorCode code, String msg) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(),
                            msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            TourService.approveTour(requireContext(), it.serverId, new DataCallback<Tour>() {
                @Override
                public void onSuccess(Tour tour) {
                    if (!isAdded()) return;
                    dialog.dismiss();
                    listings.remove(it);
                    listingAdapter.setItems(listings);
                    updateEmptyState();
                    buildTabs();
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
    }

    private void reject(PendingListing it, BottomSheetDialog dialog) {
        if (it.serverId == null) {
            dialog.dismiss();
            toast(R.string.adm_toast_listing_rejected);
            return;
        }

        DataCallback<Void> cb = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (!isAdded()) return;
                dialog.dismiss();
                listings.remove(it);
                listingAdapter.setItems(listings);
                updateEmptyState();
                buildTabs();
                refreshHostBadge();
                toast(R.string.adm_toast_listing_rejected);
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        };

        if ("hotel".equals(it.cat)) {
            HotelService.rejectHotel(requireContext(), it.serverId, cb);
        } else {
            TourService.rejectTour(requireContext(), it.serverId, cb);
        }
    }

    /** Nudge the host to recompute its bottom-nav badges after an action. */
    private void refreshHostBadge() {
        if (getActivity() instanceof AdminActivity) {
            ((AdminActivity) getActivity()).refreshNavBadges();
        }
    }

    private void toast(int res) {
        Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show();
    }
}
