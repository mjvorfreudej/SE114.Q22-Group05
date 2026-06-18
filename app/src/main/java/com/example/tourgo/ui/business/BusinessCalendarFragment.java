package com.example.tourgo.ui.business;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.tourgo.models.response.Booking;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.ui.admin.AdminUi;
import com.example.tourgo.ui.business.BusinessMockData.CalBooking;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.tourgo.remote.api.BookingApi;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Business › Calendar — monthly occupancy grid, today's bookings, timeline view, detail sheet. */
public class BusinessCalendarFragment extends Fragment {

    private View monthView, timelineView;
    private View toggleMonth, toggleTimeline;
    private ImageView toggleMonthIcon, toggleTimelineIcon;
    private TextView monthTitleText;
    private LinearLayout calGrid, todayBookingsList, timelineRows;

    private int mTodayDay;
    private int mStartBlank;
    private int mDays;
    private List<CalBooking> mBookings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        monthView = v.findViewById(R.id.bizMonthView);
        timelineView = v.findViewById(R.id.bizTimelineView);
        toggleMonth = v.findViewById(R.id.bizToggleMonth);
        toggleTimeline = v.findViewById(R.id.bizToggleTimeline);
        toggleMonthIcon = v.findViewById(R.id.bizToggleMonthIcon);
        toggleTimelineIcon = v.findViewById(R.id.bizToggleTimelineIcon);
        monthTitleText = v.findViewById(R.id.bizCalendarMonthText);
        calGrid = v.findViewById(R.id.bizCalGrid);
        todayBookingsList = v.findViewById(R.id.bizTodayBookings);
        timelineRows = v.findViewById(R.id.bizTimelineRows);

        toggleMonth.setOnClickListener(view -> setView(true));
        toggleTimeline.setOnClickListener(view -> setView(false));

        // Compute current month parameters
        Calendar calendar = Calendar.getInstance();
        mTodayDay = calendar.get(Calendar.DAY_OF_MONTH);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        if (monthTitleText != null) {
            monthTitleText.setText(sdf.format(calendar.getTime()));
        }

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        mStartBlank = dayOfWeek - 1;
        mDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        buildLegend(v.findViewById(R.id.bizCalLegend));
        buildWeekdays(v.findViewById(R.id.bizWeekdays));

        setView(true);
        loadBookings();
    }

    private void loadBookings() {
        BookingApi api = RetrofitClient.getInstance(requireContext()).getBookingApi();
        api.getBusinessBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mapBookings(response.body().getData());
                } else {
                    mBookings.clear();
                }
                rebuildCalendar();
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                mBookings.clear();
                rebuildCalendar();
            }
        });
    }

    private void mapBookings(List<Booking> bookings) {
        mBookings.clear();
        for (Booking b : bookings) {
            int id = b.getId() != null ? b.getId().hashCode() : 0;
            
            String guest = "Khách";
            String phone = "";
            if (b.getGuestInfo() != null) {
                guest = b.getGuestInfo().getName();
                phone = b.getGuestInfo().getPhone();
            }
            
            String room = "Dịch vụ";
            if (b.getHotelInfo() != null && b.getHotelInfo().getName() != null) {
                room = b.getHotelInfo().getName();
            } else if (b.getTourInfo() != null && b.getTourInfo().getName() != null) {
                room = b.getTourInfo().getName();
            }
            
            int day = mTodayDay;
            String inTime = "12:00";
            String outTime = "12:00";
            
            String dateStr = b.getBookingDate();
            if (dateStr != null && dateStr.length() >= 10) {
                try {
                    day = Integer.parseInt(dateStr.substring(8, 10));
                    if (dateStr.length() >= 16) {
                        inTime = dateStr.substring(11, 16);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            String status = b.getStatus() != null ? b.getStatus().toLowerCase() : "pending";
            mBookings.add(new CalBooking(id, guest, room, inTime, outTime, day, status, phone));
        }
    }

    private void rebuildCalendar() {
        if (!isAdded()) return;

        if (calGrid != null) {
            calGrid.removeAllViews();
            buildGrid(calGrid);
        }

        if (todayBookingsList != null) {
            todayBookingsList.removeAllViews();
            buildTodayBookings(todayBookingsList);
        }

        if (timelineRows != null) {
            timelineRows.removeAllViews();
            buildTimeline(timelineRows);
        }
    }

    private void setView(boolean month) {
        BizUi.show(monthView, month);
        BizUi.show(timelineView, !month);
        toggleMonth.setBackgroundTintList(ColorStateList.valueOf(color(month ? R.color.white : android.R.color.transparent)));
        toggleTimeline.setBackgroundTintList(ColorStateList.valueOf(color(!month ? R.color.white : android.R.color.transparent)));
        toggleMonthIcon.setImageTintList(ColorStateList.valueOf(color(month ? R.color.adm_gray_900 : R.color.adm_gray_400)));
        toggleTimelineIcon.setImageTintList(ColorStateList.valueOf(color(!month ? R.color.adm_gray_900 : R.color.adm_gray_400)));
    }

    // ── Legend ────────────────────────────────────────────────────────────────
    private void buildLegend(LinearLayout legend) {
        if (legend == null) return;
        legend.removeAllViews();
        addLegend(legend, R.color.adm_green_500, R.string.biz_cal_available);
        addLegend(legend, R.color.adm_amber_500, R.string.biz_cal_partial);
        addLegend(legend, R.color.adm_red_500, R.string.biz_cal_booked);
        addLegend(legend, R.color.adm_gray_400, R.string.biz_cal_blocked);
    }

    private void addLegend(LinearLayout legend, @ColorRes int dotColor, int labelRes) {
        LinearLayout item = new LinearLayout(requireContext());
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, BizUi.dp(requireContext(), 10), 0);
        item.setLayoutParams(lp);

        View dot = new View(requireContext());
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(BizUi.dp(requireContext(), 2));
        g.setColor(color(dotColor));
        dot.setBackground(g);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                BizUi.dp(requireContext(), 8), BizUi.dp(requireContext(), 8));
        dlp.setMargins(0, 0, BizUi.dp(requireContext(), 4), 0);
        item.addView(dot, dlp);

        TextView label = new TextView(requireContext());
        label.setText(labelRes);
        label.setTextColor(color(R.color.adm_gray_600));
        label.setTextSize(10.5f);
        item.addView(label);

        legend.addView(item);
    }

    // ── Weekday header ──────────────────────────────────────────────────────────
    private void buildWeekdays(LinearLayout row) {
        if (row == null) return;
        row.removeAllViews();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            TextView tv = new TextView(requireContext());
            tv.setText(d);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(color(R.color.adm_gray_400));
            tv.setTextSize(10f);
            tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
            row.addView(tv, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    // ── Month grid ──────────────────────────────────────────────────────────────
    private void buildGrid(LinearLayout grid) {
        int total = mStartBlank + mDays;
        int weeks = (int) Math.ceil(total / 7.0);
        int day = 1;
        int cellIndex = 0;
        int cellHeight = BizUi.dp(requireContext(), 40);
        int gap = BizUi.dp(requireContext(), 2);

        for (int w = 0; w < weeks; w++) {
            LinearLayout week = new LinearLayout(requireContext());
            week.setOrientation(LinearLayout.HORIZONTAL);
            grid.addView(week, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            for (int c = 0; c < 7; c++, cellIndex++) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, cellHeight, 1f);
                lp.setMargins(gap, gap, gap, gap);
                if (cellIndex < mStartBlank || day > mDays) {
                    View blank = new View(requireContext());
                    week.addView(blank, lp);
                } else {
                    week.addView(buildDayCell(day), lp);
                    day++;
                }
            }
        }
    }

    private View buildDayCell(int day) {
        String occ = getDynamicOccupancy(day);
        boolean today = day == mTodayDay;
        int bg, fg, dot;
        boolean border = false;
        switch (occ) {
            case "partial": bg = R.color.adm_amber_100; fg = R.color.adm_amber_700; dot = R.color.adm_amber_500; break;
            case "full":    bg = R.color.adm_red_100;   fg = R.color.adm_red_700;   dot = R.color.adm_red_500;   break;
            case "blocked": bg = R.color.adm_gray_200;  fg = R.color.adm_gray_600;  dot = R.color.adm_gray_400;  break;
            case "free":
            default:        bg = R.color.white;         fg = R.color.adm_gray_900;  dot = R.color.adm_green_500; border = true; break;
        }

        LinearLayout cell = new LinearLayout(requireContext());
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setPadding(BizUi.dp(requireContext(), 4), BizUi.dp(requireContext(), 4),
                BizUi.dp(requireContext(), 4), BizUi.dp(requireContext(), 4));
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setCornerRadius(BizUi.dp(requireContext(), 6));
        g.setColor(color(bg));
        if (today) g.setStroke(BizUi.dp(requireContext(), 2), color(R.color.adm_gray_900));
        else if (border) g.setStroke(BizUi.dp(requireContext(), 1), color(R.color.adm_gray_200));
        cell.setBackground(g);

        TextView num = new TextView(requireContext());
        num.setText(String.valueOf(day));
        num.setGravity(Gravity.END);
        num.setTextColor(color(fg));
        num.setTextSize(11f);
        num.setTypeface(num.getTypeface(), today ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        cell.addView(num, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout dotRow = new LinearLayout(requireContext());
        dotRow.setGravity(Gravity.END | Gravity.BOTTOM);
        LinearLayout.LayoutParams dotRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        View dotView = new View(requireContext());
        GradientDrawable dg = new GradientDrawable();
        dg.setShape(GradientDrawable.OVAL);
        dg.setColor(color(dot));
        dotView.setBackground(dg);
        dotRow.addView(dotView, new LinearLayout.LayoutParams(
                BizUi.dp(requireContext(), 5), BizUi.dp(requireContext(), 5)));
        cell.addView(dotRow, dotRowLp);

        return cell;
    }

    // ── Today's bookings ──────────────────────────────────────────────────────
    private void buildTodayBookings(LinearLayout list) {
        LayoutInflater inf = LayoutInflater.from(requireContext());
        for (CalBooking b : mBookings) {
            if (b.day != mTodayDay) continue;
            View row = inf.inflate(R.layout.item_biz_today_booking, list, false);
            AdminUi.avatar(row.findViewById(R.id.bizTodayAvatar), b.guest);
            ((TextView) row.findViewById(R.id.bizTodayGuest)).setText(b.guest);
            ((TextView) row.findViewById(R.id.bizTodaySub)).setText(b.room + " · check-in " + b.in);
            TextView status = row.findViewById(R.id.bizTodayStatus);
            BizUi.status(requireContext(), status, null, status, b.status);
            row.setOnClickListener(view -> openBooking(b));
            list.addView(row);
        }
    }

    // ── Timeline ────────────────────────────────────────────────────────────────
    private void buildTimeline(LinearLayout rows) {
        for (CalBooking b : mBookings) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, BizUi.dp(requireContext(), 6), 0, BizUi.dp(requireContext(), 6));

            TextView room = new TextView(requireContext());
            room.setText(b.room);
            room.setTextColor(color(R.color.adm_gray_900));
            room.setTextSize(11f);
            room.setTypeface(room.getTypeface(), android.graphics.Typeface.BOLD);
            row.addView(room, new LinearLayout.LayoutParams(
                    BizUi.dp(requireContext(), 62), LinearLayout.LayoutParams.WRAP_CONTENT));

            boolean pending = "pending".equals(b.status);
            TextView bar = new TextView(requireContext());
            bar.setText("  " + b.guest + " · " + b.in);
            bar.setTextColor(color(pending ? R.color.adm_amber_700 : R.color.adm_blue_700));
            bar.setTextSize(10f);
            bar.setTypeface(bar.getTypeface(), android.graphics.Typeface.BOLD);
            bar.setGravity(Gravity.CENTER_VERTICAL);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.RECTANGLE);
            g.setCornerRadius(BizUi.dp(requireContext(), 6));
            g.setColor(color(pending ? R.color.adm_amber_100 : R.color.adm_blue_50));
            bar.setBackground(g);
            bar.setPadding(BizUi.dp(requireContext(), 8), BizUi.dp(requireContext(), 6),
                    BizUi.dp(requireContext(), 8), BizUi.dp(requireContext(), 6));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(bar, barLp);
            row.setOnClickListener(view -> openBooking(b));

            rows.addView(row);
        }
    }

    // ── Detail sheet ────────────────────────────────────────────────────────────
    private void openBooking(CalBooking b) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_biz_booking, null, false);

        AdminUi.avatar(sheet.findViewById(R.id.bizDetailAvatar), b.guest);
        ((TextView) sheet.findViewById(R.id.bizDetailGuest)).setText(b.guest);
        ((TextView) sheet.findViewById(R.id.bizDetailRoom)).setText(b.room);
        TextView status = sheet.findViewById(R.id.bizDetailStatus);
        BizUi.status(requireContext(), status, null, status, b.status);
        ((TextView) sheet.findViewById(R.id.bizDetailCheckin)).setText(b.in);
        ((TextView) sheet.findViewById(R.id.bizDetailCheckout)).setText(b.out);

        LinearLayout summary = sheet.findViewById(R.id.bizDetailSummary);
        LayoutInflater inf = LayoutInflater.from(requireContext());
        addSummary(inf, summary, getString(R.string.biz_detail_contact), b.phone);
        addSummary(inf, summary, getString(R.string.biz_detail_guests), "2 adults");
        addSummary(inf, summary, getString(R.string.biz_detail_nights), "1 night");
        addSummary(inf, summary, getString(R.string.biz_detail_total), "Paid");

        sheet.findViewById(R.id.bizSheetClose).setOnClickListener(view -> dialog.dismiss());
        sheet.findViewById(R.id.bizDetailCheckinBtn).setOnClickListener(view -> dialog.dismiss());

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void addSummary(LayoutInflater inf, LinearLayout parent, String label, String value) {
        View row = inf.inflate(R.layout.item_biz_summary, parent, false);
        ((TextView) row.findViewById(R.id.bizSummaryLabel)).setText(label);
        ((TextView) row.findViewById(R.id.bizSummaryValue)).setText(value);
        parent.addView(row);
    }

    private String getDynamicOccupancy(int day) {
        int count = 0;
        for (CalBooking b : mBookings) {
            if (b.day == day) {
                count++;
            }
        }
        if (count == 0) return "free";
        if (count == 1) return "partial";
        return "full";
    }

    private int color(@ColorRes int res) {
        return ContextCompat.getColor(requireContext(), res);
    }
}
