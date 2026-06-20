package com.example.tourgo.ui.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.BusinessListing;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.admin.AdminTabBar;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.business.BusinessMockData.Listing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Business › My Listings — search, status tabs, category filter, cards, bulk actions. */
public class BusinessListingsFragment extends Fragment implements ListingAdapter.Listener {

    private String tab = "all";
    private String cat = "all";

    private ListingAdapter adapter;
    private View empty;
    private TextView emptyMsg;
    private View bulkBar;
    private TextView bulkCount;
    private TextView[] catChips;
    private LinearLayout tabsContainer;
    private final List<Listing> allListings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        empty = v.findViewById(R.id.bizListingsEmpty);
        emptyMsg = v.findViewById(R.id.bizListingsEmptyMsg);
        bulkBar = v.findViewById(R.id.bizBulkBar);
        bulkCount = v.findViewById(R.id.bizBulkCount);
        tabsContainer = v.findViewById(R.id.bizListingTabs);

        RecyclerView rv = v.findViewById(R.id.bizListingsRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ListingAdapter(requireContext(), this);
        rv.setAdapter(adapter);

        v.findViewById(R.id.bizAddListing).setOnClickListener(view -> openAdd());

        // Category chips
        catChips = new TextView[]{
                v.findViewById(R.id.bizChipAll),
                v.findViewById(R.id.bizChipHotels),
                v.findViewById(R.id.bizChipTours)};
        final String[] catIds = {"all", "hotel", "tour"};
        for (int i = 0; i < catChips.length; i++) {
            final String id = catIds[i];
            catChips[i].setOnClickListener(view -> { cat = id; styleChips(); refresh(); });
        }
        styleChips();

        // Bulk bar actions
        v.findViewById(R.id.bizBulkClear).setOnClickListener(view -> adapter.clearSelection());
        v.findViewById(R.id.bizBulkPause).setOnClickListener(view -> {
            adapter.clearSelection();
            toast(getString(R.string.biz_bulk_pause));
        });
        v.findViewById(R.id.bizBulkDelete).setOnClickListener(view -> {
            adapter.clearSelection();
            toast(getString(R.string.biz_toast_deleted));
        });

        loadListings();
    }

    private void loadListings() {
        RetrofitClient.getInstance(requireContext())
                .getUserApi()
                .getMyListings()
                .enqueue(new Callback<ApiResponse<List<BusinessListing>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<BusinessListing>>> call, Response<ApiResponse<List<BusinessListing>>> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            allListings.clear();
                            List<BusinessListing> data = response.body().getData();
                            for (BusinessListing item : data) {
                                allListings.add(new Listing(
                                        item.getId().hashCode(),
                                        item.getName(),
                                        item.getLocation(),
                                        (int) item.getPrice(),
                                        0, // photoIndex
                                        item.getStatus(),
                                        item.getCategory(),
                                        0, // bookings
                                        -1 // rating: -1 means no rating yet
                                ));
                            }
                            rebuildTabs();
                            refresh();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<BusinessListing>>> call, Throwable t) {
                        // fallback empty list
                    }
                });
    }

    private void rebuildTabs() {
        if (tabsContainer == null) return;
        List<AdminTabBar.Tab> tabs = Arrays.asList(
                new AdminTabBar.Tab("all", getString(R.string.biz_tab_all), allListings.size()),
                new AdminTabBar.Tab("active", getString(R.string.biz_tab_active), count(allListings, "active")),
                new AdminTabBar.Tab("inactive", getString(R.string.biz_tab_inactive), count(allListings, "inactive")),
                new AdminTabBar.Tab("draft", getString(R.string.biz_tab_draft), count(allListings, "draft"))
        );
        AdminTabBar.build(tabsContainer, tabs, tab, id -> { tab = id; refresh(); });
    }

    private void openAdd() {
        ((BusinessActivity) requireActivity()).openAddListing();
    }

    private void styleChips() {
        final String[] catIds = {"all", "hotel", "tour"};
        for (int i = 0; i < catChips.length; i++) {
            boolean sel = catIds[i].equals(cat);
            catChips[i].setBackgroundResource(sel
                    ? R.drawable.bg_biz_chip_selected : R.drawable.bg_biz_chip_idle);
        }
    }

    private void refresh() {
        List<Listing> visible = new ArrayList<>();
        for (Listing l : allListings) {
            if (!"all".equals(tab) && !l.status.equals(tab)) continue;
            if (!"all".equals(cat) && !l.cat.equals(cat)) continue;
            visible.add(l);
        }
        adapter.submit(visible);
        boolean isEmpty = visible.isEmpty();
        empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty) {
            emptyMsg.setText("draft".equals(tab)
                    ? R.string.biz_empty_drafts_msg : R.string.biz_empty_listings_msg);
        }
    }

    private int count(List<Listing> data, String status) {
        int n = 0;
        for (Listing l : data) if (l.status.equals(status)) n++;
        return n;
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // ── ListingAdapter.Listener ───────────────────────────────────────────────
    @Override
    public void onEdit(Listing l) {
        openAdd();
    }

    @Override
    public void onDelete(Listing l) {
        AdminUi.confirm(requireContext(),
                getString(R.string.biz_delete_title),
                getString(R.string.biz_delete_msg, l.name),
                getString(R.string.biz_action_delete), true,
                () -> toast(getString(R.string.biz_toast_deleted)));
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            bulkBar.setVisibility(View.VISIBLE);
            bulkCount.setText(getString(R.string.biz_selected_count, selectedCount));
        } else {
            bulkBar.setVisibility(View.GONE);
        }
    }
}
