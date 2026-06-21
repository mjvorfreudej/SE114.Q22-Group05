package com.example.tourgo.ui.admin;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.remote.service.AdminService;
import com.example.tourgo.ui.admin.AdminMockData.AdminUser;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Admin › Users directory — fetched from the backend; suspend/activate via the API. */
public class AdminUsersFragment extends Fragment {

    // Users are loaded live from the server, never from mock data.
    private final List<AdminUser> all = new ArrayList<>();
    private String filter = "all";
    private String query = "";

    private AdminUserAdapter adapter;
    private LinearLayout tabs;
    private ProgressBar progress;
    private TextView empty, title;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        title = v.findViewById(R.id.admUsersTitle);
        tabs = v.findViewById(R.id.admTopTabs);
        progress = v.findViewById(R.id.admUsersProgress);
        empty = v.findViewById(R.id.admUsersEmpty);

        RecyclerView rv = v.findViewById(R.id.admUsersList);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminUserAdapter(this::openUser);
        rv.setAdapter(adapter);

        EditText search = v.findViewById(R.id.admUsersSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = s.toString();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        refresh();
        loadUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        setLoading(true);
        AdminService.getUsers(requireContext(), new DataCallback<List<AdminAccount>>() {
            @Override
            public void onSuccess(List<AdminAccount> data) {
                if (!isAdded()) return;
                setLoading(false);
                all.clear();
                if (data != null) {
                    for (AdminAccount dto : data) all.add(AdminUser.fromServer(dto));
                }
                refresh();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                setLoading(false);
                all.clear();
                refresh();
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int count(String status) {
        int n = 0;
        for (AdminUser u : all) if (u.status != null && u.status.equals(status)) n++;
        return n;
    }

    private void refresh() {
        title.setText(getString(R.string.adm_users_title, String.valueOf(all.size())));

        List<AdminTabBar.Tab> tabList = new ArrayList<>();
        tabList.add(new AdminTabBar.Tab("all", getString(R.string.adm_tab_all), all.size()));
        tabList.add(new AdminTabBar.Tab("active", getString(R.string.adm_tab_active), count("active")));
        tabList.add(new AdminTabBar.Tab("flagged", getString(R.string.adm_tab_flagged), count("flagged")));
        tabList.add(new AdminTabBar.Tab("suspended", getString(R.string.adm_tab_suspended), count("suspended")));
        AdminTabBar.build(tabs, tabList, filter, id -> {
            filter = id;
            applyFilter();
        });
        applyFilter();
    }

    private void applyFilter() {
        String q = query.trim().toLowerCase(Locale.getDefault());
        List<AdminUser> visible = new ArrayList<>();
        for (AdminUser u : all) {
            boolean statusOk = filter.equals("all") || (u.status != null && u.status.equals(filter));
            boolean queryOk = q.isEmpty()
                    || u.name.toLowerCase(Locale.getDefault()).contains(q)
                    || u.email.toLowerCase(Locale.getDefault()).contains(q);
            if (statusOk && queryOk) visible.add(u);
        }
        adapter.setItems(visible);
        if (empty != null) {
            boolean showEmpty = visible.isEmpty() && progress.getVisibility() != View.VISIBLE;
            empty.setVisibility(showEmpty ? View.VISIBLE : View.GONE);
        }
    }

    private void setLoading(boolean loading) {
        if (progress != null) progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading && empty != null) empty.setVisibility(View.GONE);
    }

    private void openUser(AdminUser u) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_admin_user, null);
        dialog.setContentView(sheet);

        AdminUi.avatar(sheet.findViewById(R.id.admUserSheetAvatar), u.name);
        ((TextView) sheet.findViewById(R.id.admUserSheetName)).setText(u.name);
        ((TextView) sheet.findViewById(R.id.admUserSheetEmail)).setText(u.email);
        ((TextView) sheet.findViewById(R.id.admUserSheetLoc)).setText(u.loc);

        ((TextView) sheet.findViewById(R.id.admStatBookingsV)).setText(String.valueOf(u.bookings));
        ((TextView) sheet.findViewById(R.id.admStatTierV)).setText(u.tier);
        TextView reportsV = sheet.findViewById(R.id.admStatReportsV);
        reportsV.setText(String.valueOf(u.reported));

        boolean hasReports = u.reported > 0;
        if (hasReports) {
            sheet.findViewById(R.id.admStatReportsBox).setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_red_100)));
            reportsV.setTextColor(ContextCompat.getColor(requireContext(), R.color.adm_red_700));
        }

        ((TextView) sheet.findViewById(R.id.admNavBookingTrailing))
                .setText(getString(R.string.adm_n_stays, u.bookings));
        ((TextView) sheet.findViewById(R.id.admNavReviewsTrailing))
                .setText(getString(R.string.adm_reviews_count, 12));

        TextView reportsTrailing = sheet.findViewById(R.id.admNavReportsTrailing);
        reportsTrailing.setText(hasReports ? getString(R.string.adm_n_open, u.reported) : getString(R.string.adm_none));
        if (hasReports) {
            int red = ContextCompat.getColor(requireContext(), R.color.adm_red_700);
            reportsTrailing.setTextColor(red);
            ((TextView) sheet.findViewById(R.id.admNavReportsLabel)).setTextColor(red);
            ((ImageView) sheet.findViewById(R.id.admNavReportsIcon)).setImageTintList(ColorStateList.valueOf(red));
        }

        boolean suspended = "suspended".equals(u.status);
        MaterialButton suspendBtn = sheet.findViewById(R.id.admBtnSuspend);
        if (suspended) {
            suspendBtn.setText(R.string.adm_reactivate);
            suspendBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.adm_gray_900)));
            suspendBtn.setIconResource(R.drawable.ic_check_circle);
        }

        sheet.findViewById(R.id.admUserSheetClose).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.admBtnContact).setOnClickListener(view ->
                Toast.makeText(requireContext(), R.string.adm_toast_contacted, Toast.LENGTH_SHORT).show());
        suspendBtn.setOnClickListener(view -> {
            dialog.dismiss();
            confirmToggle(u, suspended);
        });

        dialog.show();
    }

    private void confirmToggle(AdminUser u, boolean currentlySuspended) {
        boolean suspend = !currentlySuspended; // active/flagged -> suspend, suspended -> reactivate
        AdminUi.confirm(requireContext(),
                getString(suspend ? R.string.adm_user_suspend_title : R.string.adm_user_reactivate_title),
                getString(suspend ? R.string.adm_user_suspend_msg : R.string.adm_user_reactivate_msg, u.name),
                getString(suspend ? R.string.adm_suspend : R.string.adm_reactivate),
                suspend,
                () -> performToggle(u, suspend));
    }

    /** Call the backend to suspend/activate, then update the row's status indicator. */
    private void performToggle(AdminUser u, boolean suspend) {
        DataCallback<Void> cb = new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded()) return;
                u.status = suspend ? "suspended" : "active";
                Toast.makeText(requireContext(),
                        getString(suspend ? R.string.adm_toast_user_suspended : R.string.adm_toast_user_reactivated, u.name),
                        Toast.LENGTH_SHORT).show();
                refresh();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        };

        if (u.serverId == null) {
            // No backend id (shouldn't happen for server data) — update locally.
            cb.onSuccess(null);
        } else if (suspend) {
            AdminService.suspendUser(requireContext(), u.serverId, cb);
        } else {
            AdminService.activateUser(requireContext(), u.serverId, cb);
        }
    }
}
