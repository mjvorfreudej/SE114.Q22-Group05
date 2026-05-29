package com.example.tourgo.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminMockData.BizAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Admin › Active Businesses directory — search, status tabs, kebab actions w/ confirm. */
public class AdminBusinessesFragment extends Fragment {

    private final List<BizAccount> all = AdminMockData.businesses();
    private String filter = "all";
    private String query = "";

    private BusinessAdapter adapter;
    private LinearLayout tabs, empty;
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_businesses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ((TextView) v.findViewById(R.id.admBizTitle)).setText(getString(R.string.adm_biz_title, all.size()));

        rv = v.findViewById(R.id.admBizList);
        empty = v.findViewById(R.id.admBizEmpty);
        tabs = v.findViewById(R.id.admTopTabs);

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

        refresh();
    }

    private int countActive() {
        int n = 0;
        for (BizAccount b : all) if (!b.suspended) n++;
        return n;
    }

    private void refresh() {
        List<AdminTabBar.Tab> tabList = new ArrayList<>();
        tabList.add(new AdminTabBar.Tab("all", getString(R.string.adm_tab_all), all.size()));
        tabList.add(new AdminTabBar.Tab("active", getString(R.string.adm_tab_active), countActive()));
        tabList.add(new AdminTabBar.Tab("suspended", getString(R.string.adm_tab_suspended), all.size() - countActive()));
        AdminTabBar.build(tabs, tabList, filter, id -> {
            filter = id;
            applyFilter();
        });
        applyFilter();
    }

    private void applyFilter() {
        String q = query.trim().toLowerCase(Locale.getDefault());
        List<BizAccount> visible = new ArrayList<>();
        for (BizAccount b : all) {
            boolean statusOk = filter.equals("all")
                    || (filter.equals("active") && !b.suspended)
                    || (filter.equals("suspended") && b.suspended);
            boolean queryOk = q.isEmpty()
                    || b.name.toLowerCase(Locale.getDefault()).contains(q)
                    || b.owner.toLowerCase(Locale.getDefault()).contains(q);
            if (statusOk && queryOk) visible.add(b);
        }
        adapter.setItems(visible);
        boolean isEmpty = visible.isEmpty();
        empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void onAction(BizAccount biz, String action) {
        boolean suspend = BusinessAdapter.ACTION_SUSPEND.equals(action);
        AdminUi.confirm(requireContext(),
                getString(suspend ? R.string.adm_biz_suspend_title : R.string.adm_biz_reactivate_title),
                getString(suspend ? R.string.adm_biz_suspend_msg : R.string.adm_biz_reactivate_msg, biz.name),
                getString(suspend ? R.string.adm_suspend : R.string.adm_reactivate),
                suspend,
                () -> {
                    biz.suspended = suspend;
                    Toast.makeText(requireContext(),
                            getString(suspend ? R.string.adm_toast_biz_suspended : R.string.adm_toast_biz_reactivated, biz.name),
                            Toast.LENGTH_SHORT).show();
                    refresh();
                });
    }
}
