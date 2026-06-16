package com.example.tourgo.ui.business;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tourgo.R;
import com.example.tourgo.ui.admin.AdminUi;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Business → Add Listing — the 5-step listing creation flow from the design-system
 * prototype (BizAddListing). One activity drives a stepper + a swappable content area;
 * the form fields are illustrative (mock submission), with the key interactions wired:
 * category select, amenity toggles, seasonal rates toggle, and the terms checkbox that
 * gates Publish. Back from step 1 shows a discard confirm.
 */
public class AddListingActivity extends AppCompatActivity {

    private static final int TOTAL = 5;
    private static final int[] LABELS = {
            R.string.biz_step_basic,
            R.string.biz_step_location,
            R.string.biz_step_pricing,
            R.string.biz_step_calendar,
            R.string.biz_step_review
    };

    private int step = 1;
    private String kind = "hotel";
    private boolean agreed = false;

    // Data for listing form
    private String listingName = "";
    private String listingDesc = "";
    private String listingAddress = "";
    private String listingCity = "";
    private int listingCapacity = 4;
    private double listingPrice = 0.0;
    private java.util.List<String> selectedAmenities = new java.util.ArrayList<>();

    private LinearLayout stepperRow, stepperLabels;
    private FrameLayout content;
    private TextView stepLabel;
    private MaterialButton backBtn, nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_listing);
        applyInsets();

        stepperRow = findViewById(R.id.bizStepperRow);
        stepperLabels = findViewById(R.id.bizStepperLabels);
        content = findViewById(R.id.bizStepContent);
        stepLabel = findViewById(R.id.bizAddStepLabel);
        backBtn = findViewById(R.id.bizAddBackBtn);
        nextBtn = findViewById(R.id.bizAddNextBtn);

        findViewById(R.id.bizAddBack).setOnClickListener(v -> goBack());
        findViewById(R.id.bizAddSaveDraft).setOnClickListener(v ->
                toast(getString(R.string.biz_toast_draft)));

        backBtn.setOnClickListener(v -> goBack());
        nextBtn.setOnClickListener(v -> goNext());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goBack();
            }
        });

        render();
    }

    private void applyInsets() {
        View appbar = findViewById(R.id.bizAddAppbar);
        final int baseTop = appbar.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(appbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    baseTop + bars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        View nav = findViewById(R.id.bizAddNav);
        final int baseBottom = nav.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(nav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    baseBottom + bars.bottom
            );
            return insets;
        });
    }

    private void goBack() {
        saveCurrentStepData();

        if (step > 1) {
            step--;
            render();
        } else {
            AdminUi.confirm(
                    this,
                    getString(R.string.biz_discard_title),
                    getString(R.string.biz_discard_msg),
                    getString(R.string.biz_discard),
                    true,
                    this::finish
            );
        }
    }

    private void goNext() {
        saveCurrentStepData();

        if (step < TOTAL) {
            step++;
            render();
        } else {
            publishListing();
        }
    }

    private void saveCurrentStepData() {
        switch (step) {
            case 1:
                EditText etName = content.findViewById(R.id.bizName);
                EditText etDesc = content.findViewById(R.id.bizDesc);

                if (etName != null) {
                    listingName = etName.getText().toString().trim();
                }

                if (etDesc != null) {
                    listingDesc = etDesc.getText().toString().trim();
                }
                break;

            case 2:
                EditText etAddr = content.findViewById(R.id.bizAddress);
                EditText etCity = content.findViewById(R.id.bizCity);

                if (etAddr != null) {
                    listingAddress = etAddr.getText().toString().trim();
                }

                if (etCity != null) {
                    listingCity = etCity.getText().toString().trim();
                }
                break;

            case 3:
                EditText etPrice = content.findViewById(R.id.bizPrice);

                if (etPrice != null) {
                    try {
                        listingPrice = Double.parseDouble(etPrice.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        listingPrice = 0.0;
                    }
                }
                break;
        }
    }

    private void publishListing() {
        if (!agreed) return;

        setLoading(true);

        if ("tour".equals(kind)) {
            com.example.tourgo.models.request.CreateTourRequest request =
                    new com.example.tourgo.models.request.CreateTourRequest(
                            listingName,
                            listingDesc,
                            listingPrice,
                            listingAddress,
                            listingCity,
                            "3 days",
                            "PENDING"
                    );

            com.example.tourgo.remote.RetrofitClient.getInstance(this)
                    .getTourApi()
                    .createTour(request)
                    .enqueue(new retrofit2.Callback<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Tour>>() {
                        @Override
                        public void onResponse(
                                retrofit2.Call<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Tour>> call,
                                retrofit2.Response<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Tour>> response
                        ) {
                            handleResponse(response.isSuccessful());
                        }

                        @Override
                        public void onFailure(
                                retrofit2.Call<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Tour>> call,
                                Throwable t
                        ) {
                            handleResponse(false);
                        }
                    });
        } else {
            com.example.tourgo.models.request.CreateHotelRequest request =
                    new com.example.tourgo.models.request.CreateHotelRequest(
                            listingName,
                            listingDesc,
                            listingPrice,
                            listingCity,
                            listingAddress,
                            selectedAmenities,
                            "PENDING"
                    );

            com.example.tourgo.remote.RetrofitClient.getInstance(this)
                    .getHotelApi()
                    .createHotel(request)
                    .enqueue(new retrofit2.Callback<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Hotel>>() {
                        @Override
                        public void onResponse(
                                retrofit2.Call<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Hotel>> call,
                                retrofit2.Response<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Hotel>> response
                        ) {
                            handleResponse(response.isSuccessful());
                        }

                        @Override
                        public void onFailure(
                                retrofit2.Call<com.example.tourgo.models.response.ApiResponse<com.example.tourgo.models.response.Hotel>> call,
                                Throwable t
                        ) {
                            handleResponse(false);
                        }
                    });
        }
    }

    private void setLoading(boolean loading) {
        nextBtn.setEnabled(!loading);
        nextBtn.setAlpha(loading ? 0.5f : 1.0f);
        nextBtn.setText(
                loading
                        ? "Publishing..."
                        : (step < TOTAL ? getString(R.string.biz_continue) : getString(R.string.biz_publish))
        );
    }

    private void handleResponse(boolean success) {
        runOnUiThread(() -> {
            setLoading(false);

            if (success) {
                toast(getString(R.string.biz_toast_published));
                finish();
            } else {
                toast("Failed to publish listing. Please try again.");
            }
        });
    }

    // ── Render ──────────────────────────────────────────────────────────────────
    private void render() {
        stepLabel.setText(getString(R.string.biz_step_of, step, TOTAL));
        buildStepper();
        buildButtons();
        swapContent();
    }

    private void buildButtons() {
        backBtn.setText(step > 1 ? R.string.biz_back : R.string.biz_cancel);

        if (step < TOTAL) {
            nextBtn.setText(R.string.biz_continue);
            nextBtn.setIconResource(R.drawable.ic_chevron_right_24);
            nextBtn.setEnabled(true);
            nextBtn.setAlpha(1f);
        } else {
            nextBtn.setText(R.string.biz_publish);
            nextBtn.setIconResource(R.drawable.ic_rocket);
            nextBtn.setEnabled(agreed);
            nextBtn.setAlpha(agreed ? 1f : 0.4f);
        }
    }

    private void buildStepper() {
        stepperRow.removeAllViews();
        stepperLabels.setVisibility(View.GONE);

        for (int n = 1; n <= TOTAL; n++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);

            col.addView(buildCircle(n));

            TextView label = new TextView(this);
            label.setText(LABELS[n - 1]);
            label.setGravity(Gravity.CENTER);
            label.setTextSize(10f);
            label.setTextColor(color(n <= step ? R.color.adm_gray_900 : R.color.adm_gray_400));
            label.setTypeface(label.getTypeface(), n == step ? Typeface.BOLD : Typeface.NORMAL);

            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            labelLp.topMargin = dp(6);
            col.addView(label, labelLp);

            stepperRow.addView(
                    col,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            if (n < TOTAL) {
                View line = new View(this);
                GradientDrawable g = new GradientDrawable();
                g.setShape(GradientDrawable.RECTANGLE);
                g.setCornerRadius(dp(999));
                g.setColor(color(n < step ? R.color.adm_gray_900 : R.color.adm_gray_100));
                line.setBackground(g);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(2), 1f);
                lp.setMargins(dp(2), 0, dp(2), 0);
                lp.topMargin = dp(10);
                lp.gravity = Gravity.TOP;
                stepperRow.addView(line, lp);
            }
        }
    }

    private View buildCircle(int n) {
        FrameLayout wrap = new FrameLayout(this);
        int size = dp(22);

        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);

        if (n < step) {
            g.setColor(color(R.color.adm_gray_900));
        } else if (n == step) {
            g.setColor(color(R.color.white));
            g.setStroke(dp(2), color(R.color.adm_gray_900));
        } else {
            g.setColor(color(R.color.adm_gray_100));
        }

        wrap.setBackground(g);

        if (n < step) {
            ImageView check = new ImageView(this);
            check.setImageResource(R.drawable.ic_check);
            check.setColorFilter(color(R.color.white));

            FrameLayout.LayoutParams clp = new FrameLayout.LayoutParams(
                    dp(12),
                    dp(12),
                    Gravity.CENTER
            );
            wrap.addView(check, clp);
        } else {
            TextView num = new TextView(this);
            num.setText(String.valueOf(n));
            num.setTextSize(10f);
            num.setTypeface(num.getTypeface(), Typeface.BOLD);
            num.setTextColor(color(n == step ? R.color.adm_gray_900 : R.color.adm_gray_400));
            num.setGravity(Gravity.CENTER);

            wrap.addView(
                    num,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    )
            );
        }

        wrap.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        return wrap;
    }

    private void swapContent() {
        content.removeAllViews();
        LayoutInflater inf = LayoutInflater.from(this);

        switch (step) {
            case 2:
                inf.inflate(R.layout.step_biz_location, content, true);
                bindLocation();
                break;

            case 3:
                inf.inflate(R.layout.step_biz_pricing, content, true);
                bindPricing();
                break;

            case 4:
                inf.inflate(R.layout.step_biz_availability, content, true);
                bindAvailability();
                break;

            case 5:
                inf.inflate(R.layout.step_biz_review, content, true);
                bindReview();
                break;

            case 1:
            default:
                inf.inflate(R.layout.step_biz_basic, content, true);
                bindBasic();
                break;
        }
    }

    // ── Step 1 ──────────────────────────────────────────────────────────────────
    private void bindBasic() {
        View hotel = content.findViewById(R.id.bizCatHotel);
        View tour = content.findViewById(R.id.bizCatTour);

        EditText etName = content.findViewById(R.id.bizName);
        EditText etDesc = content.findViewById(R.id.bizDesc);

        if (etName != null) {
            etName.setText(listingName);
        }

        if (etDesc != null) {
            etDesc.setText(listingDesc);
        }

        content.findViewById(R.id.bizCatHotelIconWrap)
                .setBackgroundTintList(android.content.res.ColorStateList.valueOf(color(R.color.adm_amber_100)));

        content.findViewById(R.id.bizCatTourIconWrap)
                .setBackgroundTintList(android.content.res.ColorStateList.valueOf(color(R.color.adm_teal_100)));

        hotel.setOnClickListener(v -> {
            kind = "hotel";
            styleCatCards(hotel, tour);
        });

        tour.setOnClickListener(v -> {
            kind = "tour";
            styleCatCards(hotel, tour);
        });

        styleCatCards(hotel, tour);

        ((TextView) content.findViewById(R.id.bizPhotosLabel))
                .setText(getString(R.string.biz_photos, 3));

        content.findViewById(R.id.bizPhotoUpload).setOnClickListener(v ->
                toast(getString(R.string.biz_upload)));
    }

    private void styleCatCards(View hotel, View tour) {
        selectCard(hotel, "hotel".equals(kind), R.color.adm_amber_500);
        selectCard(tour, "tour".equals(kind), R.color.adm_teal_500);
    }

    private void selectCard(View card, boolean selected, @ColorRes int accent) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(dp(12));
        g.setColor(color(selected ? R.color.white : R.color.adm_gray_50));
        g.setStroke(dp(selected ? 2 : 1), color(selected ? accent : R.color.adm_gray_200));
        card.setBackground(g);
    }

    // ── Step 2 ──────────────────────────────────────────────────────────────────
    private void bindLocation() {
        EditText etAddr = content.findViewById(R.id.bizAddress);
        EditText etCity = content.findViewById(R.id.bizCity);

        if (etAddr != null) {
            etAddr.setText(listingAddress);
        }

        if (etCity != null) {
            etCity.setText(listingCity);
        }

        TextView tvCapacity = content.findViewById(R.id.bizCapacityText);

        if (tvCapacity != null) {
            tvCapacity.setText(String.valueOf(listingCapacity));
        }

        View btnMinus = content.findViewById(R.id.bizCapacityMinus);
        View btnPlus = content.findViewById(R.id.bizCapacityPlus);

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (listingCapacity > 1) {
                    listingCapacity--;

                    if (tvCapacity != null) {
                        tvCapacity.setText(String.valueOf(listingCapacity));
                    }
                }
            });
        }

        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                listingCapacity++;

                if (tvCapacity != null) {
                    tvCapacity.setText(String.valueOf(listingCapacity));
                }
            });
        }

        GridLayout grid = content.findViewById(R.id.bizAmenities);

        String[] labels = {
                getString(R.string.biz_am_wifi),
                getString(R.string.biz_am_pool),
                getString(R.string.biz_am_parking),
                getString(R.string.biz_am_breakfast),
                getString(R.string.biz_am_workspace),
                getString(R.string.biz_am_ac)
        };

        int[] icons = {
                R.drawable.ic_wifi,
                R.drawable.ic_pool,
                R.drawable.ic_garage,
                R.drawable.ic_tag,
                R.drawable.ic_workplace,
                R.drawable.ic_settings
        };

        boolean[] on = {true, true, false, true, false, true};

        for (int i = 0; i < labels.length; i++) {
            grid.addView(buildAmenityChip(labels[i], icons[i], on[i]));
        }
    }

    private View buildAmenityChip(String label, int iconRes, boolean initialOn) {
        final boolean[] state = {selectedAmenities.contains(label) || initialOn};

        if (state[0] && !selectedAmenities.contains(label)) {
            selectedAmenities.add(label);
        }

        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setPadding(dp(12), dp(8), dp(12), dp(8));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);

        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(12), dp(12));
        ilp.setMargins(0, 0, dp(6), 0);
        chip.addView(icon, ilp);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(11.5f);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        chip.addView(tv);

        Runnable apply = () -> {
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.RECTANGLE);
            g.setCornerRadius(dp(999));
            g.setColor(color(state[0] ? R.color.adm_gray_900 : R.color.adm_gray_100));
            chip.setBackground(g);

            int fg = color(state[0] ? R.color.white : R.color.adm_gray_600);
            tv.setTextColor(fg);
            icon.setColorFilter(fg);
        };

        apply.run();

        chip.setOnClickListener(v -> {
            state[0] = !state[0];

            if (state[0]) {
                if (!selectedAmenities.contains(label)) {
                    selectedAmenities.add(label);
                }
            } else {
                selectedAmenities.remove(label);
            }

            apply.run();
        });

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.setMargins(0, 0, dp(6), dp(6));
        chip.setLayoutParams(lp);

        return chip;
    }

    // ── Step 3 ──────────────────────────────────────────────────────────────────
    private void bindPricing() {
        ((TextView) content.findViewById(R.id.bizPriceLabel)).setText(
                "hotel".equals(kind)
                        ? R.string.biz_base_price_night
                        : R.string.biz_base_price_person
        );

        EditText etPrice = content.findViewById(R.id.bizPrice);

        if (etPrice != null && listingPrice > 0) {
            etPrice.setText(String.valueOf(listingPrice));
        }

        season(
                content.findViewById(R.id.bizSeasonPeak),
                R.string.biz_season_peak,
                R.string.biz_season_peak_range,
                R.string.biz_season_peak_delta,
                R.color.adm_red_100,
                R.color.adm_red_700
        );

        season(
                content.findViewById(R.id.bizSeasonMid),
                R.string.biz_season_mid,
                R.string.biz_season_mid_range,
                R.string.biz_season_mid_delta,
                R.color.adm_gray_200,
                R.color.adm_gray_700
        );

        season(
                content.findViewById(R.id.bizSeasonOff),
                R.string.biz_season_off,
                R.string.biz_season_off_range,
                R.string.biz_season_off_delta,
                R.color.adm_green_100,
                R.color.adm_green_700
        );

        fee(content.findViewById(R.id.bizFeeCleaning), R.string.biz_fee_cleaning, R.string.biz_fee_cleaning_value);
        fee(content.findViewById(R.id.bizFeeLate), R.string.biz_fee_late, R.string.biz_fee_late_value);
        fee(content.findViewById(R.id.bizFeeExtra), R.string.biz_fee_extra_guest, R.string.biz_fee_extra_guest_value);

        MaterialSwitch toggle = content.findViewById(R.id.bizSeasonalToggle);
        View rows = content.findViewById(R.id.bizSeasonRows);

        toggle.setOnCheckedChangeListener((b, checked) -> BizUi.show(rows, checked));
    }

    private void season(
            View row,
            int labelRes,
            int rangeRes,
            int deltaRes,
            @ColorRes int chipBg,
            @ColorRes int chipFg
    ) {
        ((TextView) row.findViewById(R.id.bizSeasonLabel)).setText(labelRes);
        ((TextView) row.findViewById(R.id.bizSeasonRange)).setText(rangeRes);

        TextView delta = row.findViewById(R.id.bizSeasonDelta);
        delta.setText(deltaRes);
        delta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color(chipBg)));
        delta.setTextColor(color(chipFg));
    }

    private void fee(View row, int labelRes, int valueRes) {
        ((TextView) row.findViewById(R.id.bizFeeLabel)).setText(labelRes);
        ((TextView) row.findViewById(R.id.bizFeeValue)).setText(valueRes);
    }

    // ── Step 4 ──────────────────────────────────────────────────────────────────
    private void bindAvailability() {
        LinearLayout weekdays = content.findViewById(R.id.bizMiniWeekdays);

        for (String d : new String[]{"S", "M", "T", "W", "T", "F", "S"}) {
            TextView tv = new TextView(this);
            tv.setText(d);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(9.5f);
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            tv.setTextColor(color(R.color.adm_gray_400));

            weekdays.addView(
                    tv,
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                    )
            );
        }

        java.util.Set<Integer> blocked =
                new java.util.HashSet<>(java.util.Arrays.asList(7, 8, 21, 22));

        java.util.Set<Integer> selected =
                new java.util.HashSet<>(java.util.Arrays.asList(13, 14, 15, 16));

        LinearLayout grid = content.findViewById(R.id.bizMiniGrid);
        int gap = dp(2);
        int idx = 0;

        for (int w = 0; w < 5; w++) {
            LinearLayout week = new LinearLayout(this);
            week.setOrientation(LinearLayout.HORIZONTAL);

            grid.addView(
                    week,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    )
            );

            for (int c = 0; c < 7; c++, idx++) {
                int day = idx - 2;
                boolean valid = day > 0 && day <= 31;

                View cell = buildMiniCell(
                        valid ? day : 0,
                        valid && blocked.contains(day),
                        valid && selected.contains(day)
                );

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(30), 1f);
                lp.setMargins(gap, gap, gap, gap);
                week.addView(cell, lp);
            }
        }

        grid.post(() -> {
            for (int w2 = 0; w2 < grid.getChildCount(); w2++) {
                LinearLayout weekRow = (LinearLayout) grid.getChildAt(w2);

                for (int c2 = 0; c2 < weekRow.getChildCount(); c2++) {
                    View cell = weekRow.getChildAt(c2);
                    int cellWidth = cell.getWidth();

                    if (cellWidth > 0) {
                        ViewGroup.LayoutParams lp = cell.getLayoutParams();
                        lp.height = cellWidth;
                        cell.setLayoutParams(lp);
                    }
                }
            }
        });

        LinearLayout legend = content.findViewById(R.id.bizMiniLegend);
        miniLegend(legend, R.color.adm_gray_900, R.string.biz_legend_selected, false);
        miniLegend(legend, R.color.adm_red_500, R.string.biz_legend_blocked, false);
        miniLegend(legend, android.R.color.transparent, R.string.biz_legend_open, true);
    }

    private View buildMiniCell(int day, boolean blocked, boolean selected) {
        TextView cell = new TextView(this);
        cell.setGravity(Gravity.CENTER);
        cell.setTextSize(11f);

        if (day == 0) {
            cell.setText("");
            return cell;
        }

        cell.setText(String.valueOf(day));

        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(dp(6));

        if (selected) {
            g.setColor(color(R.color.adm_gray_900));
            cell.setTextColor(color(R.color.white));
            cell.setTypeface(cell.getTypeface(), Typeface.BOLD);
        } else if (blocked) {
            g.setColor(color(R.color.adm_red_100));
            cell.setTextColor(color(R.color.adm_red_700));
        } else {
            g.setColor(0x00000000);
            cell.setTextColor(color(R.color.adm_gray_700));
        }

        cell.setBackground(g);

        int m = dp(1);
        cell.setPadding(m, m, m, m);

        return cell;
    }

    private void miniLegend(
            LinearLayout legend,
            @ColorRes int dotColor,
            int labelRes,
            boolean outline
    ) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, dp(12), 0);
        item.setLayoutParams(lp);

        View dot = new View(this);

        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(dp(3));
        g.setColor(color(dotColor));

        if (outline) {
            g.setStroke(dp(1), color(R.color.adm_gray_300));
        }

        dot.setBackground(g);

        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(10), dp(10));
        dlp.setMargins(0, 0, dp(4), 0);
        item.addView(dot, dlp);

        TextView label = new TextView(this);
        label.setText(labelRes);
        label.setTextSize(10f);
        label.setTextColor(color(R.color.adm_gray_500));
        item.addView(label);

        legend.addView(item);
    }

    // ── Step 5 ──────────────────────────────────────────────────────────────────
    private void bindReview() {
        AdminUi.catChip(
                this,
                content.findViewById(R.id.bizPreviewCatChip),
                content.findViewById(R.id.bizPreviewCatDot),
                (TextView) content.findViewById(R.id.bizPreviewCatLabel),
                kind
        );

        String displayName = listingName.isEmpty() ? "The Grand Orchid Resort" : listingName;
        String displayCity = listingCity.isEmpty() ? "Bangkok" : listingCity;
        String displayAddress = listingAddress.isEmpty() ? "Thailand" : listingAddress;
        double displayPrice = listingPrice > 0 ? listingPrice : 199;

        ((TextView) content.findViewById(R.id.bizPreviewName)).setText(displayName);
        ((TextView) content.findViewById(R.id.bizPreviewLoc)).setText(displayCity + ", " + displayAddress);

        String unit = getString("hotel".equals(kind) ? R.string.biz_per_night : R.string.biz_per_person);
        ((TextView) content.findViewById(R.id.bizPreviewPrice)).setText("$" + displayPrice + " " + unit);

        LinearLayout summary = content.findViewById(R.id.bizReviewSummary);
        LayoutInflater inf = LayoutInflater.from(this);

        addSummary(
                inf,
                summary,
                getString(R.string.biz_summary_photos),
                getString(R.string.biz_summary_photos_value)
        );

        addSummary(
                inf,
                summary,
                getString(R.string.biz_summary_amenities),
                selectedAmenities.isEmpty()
                        ? getString(R.string.biz_summary_amenities_value)
                        : String.valueOf(selectedAmenities.size())
        );

        addSummary(
                inf,
                summary,
                getString(R.string.biz_summary_capacity),
                String.valueOf(listingCapacity)
        );

        addSummary(
                inf,
                summary,
                getString(R.string.biz_summary_pricing),
                displayPrice + " " + unit
        );

        View agreeRow = content.findViewById(R.id.bizAgreeRow);
        View box = content.findViewById(R.id.bizAgreeBox);
        View check = content.findViewById(R.id.bizAgreeCheck);

        applyAgree(box, check);

        agreeRow.setOnClickListener(v -> {
            agreed = !agreed;
            applyAgree(box, check);
            buildButtons();
        });
    }

    private void applyAgree(View box, View check) {
        box.setBackgroundResource(agreed ? R.drawable.bg_biz_check_on : R.drawable.bg_biz_check_off);
        check.setVisibility(agreed ? View.VISIBLE : View.INVISIBLE);
    }

    private void addSummary(LayoutInflater inf, LinearLayout parent, String label, String value) {
        View row = inf.inflate(R.layout.item_biz_summary, parent, false);
        ((TextView) row.findViewById(R.id.bizSummaryLabel)).setText(label);
        ((TextView) row.findViewById(R.id.bizSummaryValue)).setText(value);
        parent.addView(row);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }
}