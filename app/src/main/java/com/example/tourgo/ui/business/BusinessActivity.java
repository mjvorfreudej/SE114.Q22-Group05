package com.example.tourgo.ui.business;

import android.content.Intent;
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

/**
 * Business Console host. Mirrors {@link com.example.tourgo.ui.admin.AdminActivity}'s
 * pattern and visual style: a floating white pill bottom nav with 5 tabs
 * (Home · Listings · Calendar · Reviews · Profile). Like the user/admin apps, only the
 * selected tab shows its label on a light-gray pill and expands; the rest are icon-only.
 *
 * Recreated 1:1 from the TourGo design-system prototype (ui_kits/business/index.html).
 * The Reports surface lives inside the Reviews tab, per the prototype's two-tab Reviews
 * screen. The marketplace has no business REST API in this app yet, so the screens are
 * wired to {@link BusinessMockData} with optimistic toasts — exactly as the HTML behaves.
 */
public class BusinessActivity extends AppCompatActivity {

    public static final int TAB_HOME = 0, TAB_LISTINGS = 1, TAB_CALENDAR = 2, TAB_REVIEWS = 3, TAB_PROFILE = 4;

    private int currentTab = -1;
    private ImageView[] icons;
    private TextView[] labels;
    private View[] tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business);

        tabs = new View[]{
                findViewById(R.id.bizTabHome), findViewById(R.id.bizTabListings),
                findViewById(R.id.bizTabCalendar), findViewById(R.id.bizTabReviews),
                findViewById(R.id.bizTabProfile)};
        icons = new ImageView[]{
                findViewById(R.id.bizIconHome), findViewById(R.id.bizIconListings),
                findViewById(R.id.bizIconCalendar), findViewById(R.id.bizIconReviews),
                findViewById(R.id.bizIconProfile)};
        labels = new TextView[]{
                findViewById(R.id.bizLabelHome), findViewById(R.id.bizLabelListings),
                findViewById(R.id.bizLabelCalendar), findViewById(R.id.bizLabelReviews),
                findViewById(R.id.bizLabelProfile)};

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnClickListener(v -> selectTab(idx));
        }

        applyInsets();

        int start = savedInstanceState != null
                ? savedInstanceState.getInt("business_tab", TAB_HOME) : TAB_HOME;
        currentTab = -1;
        selectTab(start);
    }

    private void applyInsets() {
        View nav = findViewById(R.id.bizBottomNav);
        ViewGroup.MarginLayoutParams navLp = (ViewGroup.MarginLayoutParams) nav.getLayoutParams();
        final int baseNavBottomMargin = navLp.bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(nav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = baseNavBottomMargin + bars.bottom;
            v.setLayoutParams(lp);
            return insets;
        });
        View container = findViewById(R.id.biz_fragment_container);
        final int baseTop = container.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), baseTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    /** Allow a fragment (e.g. Home quick actions) to jump to another tab. */
    public void goToTab(int tab) {
        selectTab(tab);
    }

    /** Opens the 5-step Add Listing flow (full-screen). */
    public void openAddListing() {
        startActivity(new Intent(this, AddListingActivity.class));
    }

    private void selectTab(int tab) {
        if (tab == currentTab) return;
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
            case TAB_LISTINGS: f = new BusinessListingsFragment(); break;
            case TAB_CALENDAR: f = new BusinessCalendarFragment(); break;
            case TAB_REVIEWS:  f = new BusinessReviewsFragment(); break;
            case TAB_PROFILE:  f = new BusinessProfileFragment(); break;
            case TAB_HOME:
            default:           f = new BusinessHomeFragment(); break;
        }

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        t.replace(R.id.biz_fragment_container, f);
        t.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("business_tab", currentTab);
    }
}
