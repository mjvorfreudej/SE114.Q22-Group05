package com.example.tourgo.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.AdminStats;
import com.example.tourgo.remote.service.AdminService;

/**
 * Admin Console host. Mirrors {@link com.example.tourgo.ui.main.home.MainActivity}'s pattern
 * and visual style: a floating white pill bottom nav with 5 tabs
 * (Home · Moderate · Business · Users · Profile). Like the user app, only the selected
 * tab shows its label on a light-gray pill and expands; the rest show icon-only.
 */
public class AdminActivity extends AppCompatActivity {

    public static final int TAB_HOME = 0, TAB_MODERATION = 1, TAB_BUSINESS = 2, TAB_USERS = 3, TAB_PROFILE = 4;

    private int currentTab = -1;
    private ImageView[] icons;
    private TextView[] labels;
    private View[] tabs;
    private TextView moderateBadge;
    private TextView businessBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        tabs = new View[]{
                findViewById(R.id.admTabHome), findViewById(R.id.admTabModerate),
                findViewById(R.id.admTabBusiness), findViewById(R.id.admTabUsers),
                findViewById(R.id.admTabProfile)};
        icons = new ImageView[]{
                findViewById(R.id.admIconHome), findViewById(R.id.admIconModerate),
                findViewById(R.id.admIconBusiness), findViewById(R.id.admIconUsers),
                findViewById(R.id.admIconProfile)};
        labels = new TextView[]{
                findViewById(R.id.admLabelHome), findViewById(R.id.admLabelModerate),
                findViewById(R.id.admLabelBusiness), findViewById(R.id.admLabelUsers),
                findViewById(R.id.admLabelProfile)};
        moderateBadge = findViewById(R.id.admBadgeModerate);
        businessBadge = findViewById(R.id.admBadgeBusiness);

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnClickListener(v -> selectTab(idx));
        }

        applyInsets();

        int start = savedInstanceState != null
                ? savedInstanceState.getInt("admin_tab", TAB_HOME) : TAB_HOME;
        // Force the transaction on first selection.
        currentTab = -1;
        selectTab(start);
    }

    private void applyInsets() {
        // Floating nav: lift the whole pill above the gesture/nav bar via bottom margin
        // (mirrors MainActivity). Content draws under the status bar (fragments pad their
        // own white headers).
        View nav = findViewById(R.id.admBottomNav);
        ViewGroup.MarginLayoutParams navLp = (ViewGroup.MarginLayoutParams) nav.getLayoutParams();
        final int baseNavBottomMargin = navLp.bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(nav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = baseNavBottomMargin + bars.bottom;
            v.setLayoutParams(lp);
            return insets;
        });
        View container = findViewById(R.id.adm_fragment_container);
        final int baseTop = container.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), baseTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    /** Allow a fragment (e.g. Home quick actions) to jump to another tab. */
    public void goToTab(int tab) {
        selectTab(tab, null);
    }

    /**
     * Jump to the Moderation tab and open a specific sub-tab
     * ("pending" | "approved" | "reports"), e.g. from the Home "Review reports" card.
     */
    public void goToModerationTab(String modTab) {
        selectTab(TAB_MODERATION, modTab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNavBadges();
    }

    /**
     * Refresh the bottom-nav badges from live stats: the Moderation badge (pending
     * listings + open reports) and the Business badge (businesses awaiting approval).
     * Called on resume and by the Moderation / Business tabs after an approve/resolve
     * so the counts track the backend instead of a baked-in number. Fragments can
     * trigger it via {@code ((AdminActivity) getActivity()).refreshNavBadges()}.
     */
    public void refreshNavBadges() {
        if (moderateBadge == null && businessBadge == null) return;
        AdminService.getStats(this, new DataCallback<AdminStats>() {
            @Override
            public void onSuccess(AdminStats s) {
                if (s == null) return;
                setBadge(moderateBadge, s.getQueueTotal());
                setBadge(businessBadge, s.getPendingBusinesses());
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                // Leave the last known badge state on failure.
            }
        });
    }

    private void setBadge(TextView badge, int count) {
        if (badge == null) return;
        if (count > 0) {
            badge.setText(count > 99 ? "99+" : String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void selectTab(int tab) {
        selectTab(tab, null);
    }

    private void selectTab(int tab, String modTab) {
        // Re-selecting the same tab is a no-op, unless we need to open a specific
        // Moderation sub-tab (e.g. the Home "Review reports" card → User Reports).
        if (tab == currentTab && modTab == null) return;
        currentTab = tab;

        int active = ContextCompat.getColor(this, R.color.adm_gray_900);
        int idle = ContextCompat.getColor(this, R.color.adm_gray_400);
        for (int i = 0; i < tabs.length; i++) {
            boolean sel = i == tab;
            tabs[i].setBackground(sel
                    ? ContextCompat.getDrawable(this, R.drawable.bg_nav_selected) : null);
            icons[i].setColorFilter(sel ? active : idle);
            labels[i].setTextColor(active);
            labels[i].setVisibility(sel ? View.VISIBLE : View.GONE);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabs[i].getLayoutParams();
            lp.weight = sel ? 1.5f : 1.0f;
            tabs[i].setLayoutParams(lp);
        }

        Fragment f;
        switch (tab) {
            case TAB_MODERATION:
                AdminModerationFragment mod = new AdminModerationFragment();
                if (modTab != null) {
                    Bundle args = new Bundle();
                    args.putString(AdminModerationFragment.ARG_INITIAL_TAB, modTab);
                    mod.setArguments(args);
                }
                f = mod;
                break;
            case TAB_BUSINESS:   f = new AdminBusinessesFragment(); break;
            case TAB_USERS:      f = new AdminUsersFragment(); break;
            case TAB_PROFILE:    f = new AdminProfileFragment(); break;
            case TAB_HOME:
            default:             f = new AdminHomeFragment(); break;
        }

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        t.replace(R.id.adm_fragment_container, f);
        t.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("admin_tab", currentTab);
    }
}
