package com.example.tourgo.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.tourgo.models.response.BusinessAccount;
import com.example.tourgo.remote.service.AdminService;
import com.example.tourgo.ui.admin.AdminMockData.BizAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Admin › Pending businesses — fetched from the backend; approve via the API. */
public class AdminBusinessesFragment extends Fragment {

    // Pending businesses are loaded live from the server, never from mock data.
    private final List<BizAccount> all = new ArrayList<>();
    private String query = "";

    private BusinessAdapter adapter;
    private LinearLayout tabs, empty;
    private RecyclerView rv;
    private ProgressBar progress;
    private TextView title;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_businesses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        title = v.findViewById(R.id.admBizTitle);
        rv = v.findViewById(R.id.admBizList);
        empty = v.findViewById(R.id.admBizEmpty);
        tabs = v.findViewById(R.id.admTopTabs);
        progress = v.findViewById(R.id.admBizProgress);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BusinessAdapter(this::onAction);
        rv.setAdapter(adapter);

        EditText search = v.findViewById(R.id.admBizSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                query = s.toString();
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        buildTabs();
        loadBusinesses();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBusinesses();
    }

    private void loadBusinesses() {
        setLoading(true);
        AdminService.getPendingBusinesses(requireContext(), new DataCallback<List<BusinessAccount>>() {
            @Override
            public void onSuccess(List<BusinessAccount> data) {
                if (!isAdded()) return;
                setLoading(false);
                all.clear();
                if (data != null) {
                    for (BusinessAccount dto : data) all.add(BizAccount.fromServer(dto));
                }
                buildTabs();
                applyFilter();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                setLoading(false);
                all.clear();
                buildTabs();
                applyFilter();
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildTabs() {
        title.setText(getString(R.string.adm_biz_title, all.size()));
        List<AdminTabBar.Tab> tabList = new ArrayList<>();
        tabList.add(new AdminTabBar.Tab("pending", getString(R.string.adm_status_pending), all.size()));
        AdminTabBar.build(tabs, tabList, "pending", id -> applyFilter());
    }

    private void applyFilter() {
        String q = query.trim().toLowerCase(Locale.getDefault());
        List<BizAccount> visible = new ArrayList<>();
        for (BizAccount b : all) {
            boolean queryOk = q.isEmpty()
                    || b.name.toLowerCase(Locale.getDefault()).contains(q)
                    || b.owner.toLowerCase(Locale.getDefault()).contains(q);
            if (queryOk) visible.add(b);
        }
        adapter.setItems(visible);
        boolean isEmpty = visible.isEmpty() && progress.getVisibility() != View.VISIBLE;
        empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rv.setVisibility(visible.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            empty.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
        }
    }

    private void onAction(BizAccount biz, String action) {
        if (BusinessAdapter.ACTION_APPROVE.equals(action)) {
            approve(biz);
        } else if (BusinessAdapter.ACTION_REJECT.equals(action)) {
            reject(biz);
        }
    }

    private void approve(BizAccount biz) {
        if (biz.serverId == null) return;
        AdminService.approveBusiness(requireContext(), biz.serverId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        getString(R.string.adm_toast_biz_approved, biz.name), Toast.LENGTH_SHORT).show();
                all.remove(biz);
                buildTabs();
                applyFilter();
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        msg != null ? msg : getString(R.string.err_unknown), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reject(BizAccount biz) {
        if (biz.serverId == null) return;
        AdminService.rejectBusiness(requireContext(), biz.serverId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Đã từ chối đơn đăng ký của " + biz.name, Toast.LENGTH_SHORT).show();
                all.remove(biz);
                buildTabs();
                applyFilter();
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
