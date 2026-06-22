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
import com.example.tourgo.data.local.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import com.example.tourgo.remote.service.BookingService;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import android.content.Intent;
import com.example.tourgo.models.response.ChatRoom;
import com.example.tourgo.remote.service.ChatService;
import com.example.tourgo.ui.chat.ChatActivity;
import java.util.Map;
import android.widget.Toast;

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
    private final Map<Integer, Booking> mIdToBookingMap = new HashMap<>();

    private TextView todayDateText;
    private TextView blockDatesBtn;
    private int mSelectedDay;
    private String mMonthYearKey;
    private Set<String> mBlockedDays = new HashSet<>();
    private SessionManager mSessionManager;

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

        todayDateText = v.findViewById(R.id.bizCalTodayText);
        blockDatesBtn = v.findViewById(R.id.bizBlockDatesBtn);

        toggleMonth.setOnClickListener(view -> setView(true));
        toggleTimeline.setOnClickListener(view -> setView(false));

        // Compute current month parameters
        Calendar calendar = Calendar.getInstance();
        mTodayDay = calendar.get(Calendar.DAY_OF_MONTH);
        mSelectedDay = mTodayDay;
        mSessionManager = new SessionManager(requireContext());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0-based
        mMonthYearKey = year + "_" + month;
        mBlockedDays = mSessionManager.getBlockedDates(mMonthYearKey);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        if (monthTitleText != null) {
            monthTitleText.setText(sdf.format(calendar.getTime()));
        }

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        mStartBlank = dayOfWeek - 1;
        mDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (blockDatesBtn != null) {
            blockDatesBtn.setOnClickListener(view -> {
                String dayStr = String.valueOf(mSelectedDay);
                if (mBlockedDays.contains(dayStr)) {
                    mBlockedDays.remove(dayStr);
                } else {
                    mBlockedDays.add(dayStr);
                }
                mSessionManager.saveBlockedDates(mMonthYearKey, mBlockedDays);
                updateDateLabel();
                rebuildCalendar();
            });
        }

        updateDateLabel();
        buildLegend(v.findViewById(R.id.bizCalLegend));
        buildWeekdays(v.findViewById(R.id.bizWeekdays));

        setView(true);
        loadBookings();
    }

    private void updateDateLabel() {
        if (todayDateText == null) return;
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH) + 1; // 1-based

        if (mSelectedDay == mTodayDay) {
            todayDateText.setText("Hôm nay · " + mTodayDay + "/" + currentMonth);
        } else {
            todayDateText.setText("Ngày " + mSelectedDay + "/" + currentMonth);
        }

        if (blockDatesBtn != null) {
            if (mBlockedDays.contains(String.valueOf(mSelectedDay))) {
                blockDatesBtn.setText("Bỏ chặn");
            } else {
                blockDatesBtn.setText(R.string.biz_block_dates);
            }
        }
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
        mIdToBookingMap.clear();
        for (Booking b : bookings) {
            int id = b.getId() != null ? b.getId().hashCode() : 0;
            mIdToBookingMap.put(id, b);
            
            String guest = "Khách";
            String phone = "";
            if (b.getGuestInfo() != null) {
                guest = b.getGuestInfo().getName();
                phone = b.getGuestInfo().getPhone();
            }
            
            String room = "Dịch vụ";
            if (b.getHotelInfo() != null && b.getHotelInfo().getName() != null) {
                int roomNum = Math.abs(id % 20) + 1;
                room = b.getHotelInfo().getName() + " · Phòng " + roomNum;
            } else if (b.getTourInfo() != null && b.getTourInfo().getName() != null) {
                room = b.getTourInfo().getName();
            }
            
            int day = mTodayDay;
            String inTime = "12:00";
            String outTime = "12:00";
            
            String dateStr = b.getBookingDate();
            if (dateStr != null && dateStr.length() >= 10) {
                try {
                    int bYear = Integer.parseInt(dateStr.substring(0, 4));
                    int bMonth = Integer.parseInt(dateStr.substring(5, 7));
                    
                    Calendar displayedCal = Calendar.getInstance();
                    int dispYear = displayedCal.get(Calendar.YEAR);
                    int dispMonth = displayedCal.get(Calendar.MONTH) + 1; // 1-based
                    
                    if (bYear != dispYear || bMonth != dispMonth) {
                        continue;
                    }
                    
                    day = Integer.parseInt(dateStr.substring(8, 10));
                    if (dateStr.length() >= 16) {
                        inTime = dateStr.substring(11, 16);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                continue;
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
        boolean selected = day == mSelectedDay;
        int bg, fg, dot;
        boolean border = false;
        switch (occ) {
            case "blocked": bg = R.color.adm_gray_200;  fg = R.color.adm_gray_600;  dot = R.color.adm_gray_400;  break;
            case "partial": bg = R.color.adm_amber_100; fg = R.color.adm_amber_700; dot = R.color.adm_amber_500; break;
            case "full":    bg = R.color.adm_red_100;   fg = R.color.adm_red_700;   dot = R.color.adm_red_500;   break;
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
        
        if (selected) {
            g.setStroke(BizUi.dp(requireContext(), 2), color(R.color.adm_gray_900));
        } else if (today) {
            g.setStroke(BizUi.dp(requireContext(), 1.5f), color(R.color.adm_gray_400));
        } else if (border) {
            g.setStroke(BizUi.dp(requireContext(), 1), color(R.color.adm_gray_200));
        }
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

        cell.setOnClickListener(view -> {
            mSelectedDay = day;
            updateDateLabel();
            rebuildCalendar();
        });

        return cell;
    }

    // ── Today's bookings ──────────────────────────────────────────────────────
    private void buildTodayBookings(LinearLayout list) {
        LayoutInflater inf = LayoutInflater.from(requireContext());
        for (CalBooking b : mBookings) {
            if (b.day != mSelectedDay) continue;
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
    private void dialContact(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Không có số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể thực hiện cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }

    private void contactCustomer(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Không có ID khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(requireContext(), "Đang mở cuộc trò chuyện...", Toast.LENGTH_SHORT).show();
        
        ChatService.getOrCreateRoomForBusiness(requireContext(), userId, new DataCallback<ChatRoom>() {
            @Override
            public void onSuccess(ChatRoom chatRoom) {
                if (isAdded()) {
                    Intent intent = new Intent(requireContext(), ChatActivity.class);
                    intent.putExtra("chat_room", chatRoom);
                    startActivity(intent);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), 
                        "Không thể mở cuộc trò chuyện: " + (msg != null ? msg : "Lỗi kết nối"), 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateStatus(String bookingId, String newStatus, BottomSheetDialog dialog) {
        if (bookingId == null) return;

        android.app.ProgressDialog pd = new android.app.ProgressDialog(requireContext());
        pd.setMessage("Đang cập nhật...");
        pd.show();

        BookingService.updateBusinessBookingStatus(requireContext(), bookingId, newStatus, new DataCallback<Booking>() {
            @Override
            public void onSuccess(Booking updated) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        pd.dismiss();
                        dialog.dismiss();
                        Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        loadBookings(); // Reload to refresh calendar
                    });
                }
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        pd.dismiss();
                        Toast.makeText(requireContext(), "Lỗi: " + rawMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void openBooking(CalBooking b) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_biz_booking, null, false);

        Booking realBooking = mIdToBookingMap.get(b.id);

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

        // Dynamic guests count
        int guestCount = (realBooking != null && realBooking.getNumGuests() != null)
                ? realBooking.getNumGuests() : 2;
        addSummary(inf, summary, getString(R.string.biz_detail_guests), guestCount + " khách");

        // Dynamic nights count
        int nightsCount = 1;
        if (realBooking != null && realBooking.getCheckIn() != null && realBooking.getCheckOut() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                java.util.Date dIn = sdf.parse(realBooking.getCheckIn());
                java.util.Date dOut = sdf.parse(realBooking.getCheckOut());
                long diff = dOut.getTime() - dIn.getTime();
                nightsCount = (int) (diff / (24 * 60 * 60 * 1000));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        boolean isHotel = (realBooking != null && realBooking.getHotelId() != null);
        if (isHotel) {
            addSummary(inf, summary, getString(R.string.biz_detail_nights), nightsCount + " đêm");
        } else {
            addSummary(inf, summary, "Kiểu dịch vụ", "Chuyến đi (Tour)");
        }
        addSummary(inf, summary, getString(R.string.biz_detail_total), "Đã thanh toán");

        MaterialButton leftBtn = sheet.findViewById(R.id.bizDetailContact);
        MaterialButton rightBtn = sheet.findViewById(R.id.bizDetailCheckinBtn);

        String currentStatus = (realBooking != null && realBooking.getStatus() != null) ? realBooking.getStatus().toUpperCase() : "PENDING";

        // Reset layout params first
        LinearLayout.LayoutParams leftLp = (LinearLayout.LayoutParams) leftBtn.getLayoutParams();
        leftLp.weight = 1.0f;
        leftLp.setMarginEnd(BizUi.dp(requireContext(), 5));
        leftBtn.setLayoutParams(leftLp);
        leftBtn.setVisibility(View.VISIBLE);
        rightBtn.setVisibility(View.VISIBLE);

        if ("PENDING".equals(currentStatus)) {
            // Left: Reject
            leftBtn.setText("Từ chối");
            leftBtn.setIconResource(R.drawable.ic_close);
            leftBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.adm_red_700));
            leftBtn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_red_700)));
            leftBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_red_200)));
            leftBtn.setOnClickListener(v -> {
                if (realBooking != null) {
                    updateStatus(realBooking.getId(), "CANCELLED", dialog);
                }
            });

            // Right: Confirm
            rightBtn.setText("Xác nhận");
            rightBtn.setIconResource(R.drawable.ic_check);
            rightBtn.setOnClickListener(v -> {
                if (realBooking != null) {
                    updateStatus(realBooking.getId(), "CONFIRMED", dialog);
                }
            });
        } else if ("CONFIRMED".equals(currentStatus)) {
            // Left: Contact
            leftBtn.setText(R.string.biz_detail_contact);
            leftBtn.setIconResource(R.drawable.ic_phone);
            leftBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.adm_gray_900));
            leftBtn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_gray_900)));
            leftBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_gray_300)));
            leftBtn.setOnClickListener(v -> contactCustomer(realBooking != null ? realBooking.getUserId() : null));

            // Right: Check-in (Hotel) or Complete (Tour)
            if (isHotel) {
                rightBtn.setText("Nhận phòng");
                rightBtn.setIconResource(R.drawable.ic_check);
                rightBtn.setOnClickListener(v -> {
                    if (realBooking != null) {
                        updateStatus(realBooking.getId(), "CHECKED-IN", dialog);
                    }
                });
            } else {
                rightBtn.setText("Hoàn thành");
                rightBtn.setIconResource(R.drawable.ic_check);
                rightBtn.setOnClickListener(v -> {
                    if (realBooking != null) {
                        updateStatus(realBooking.getId(), "COMPLETED", dialog);
                    }
                });
            }
        } else if ("CHECKED-IN".equals(currentStatus)) {
            // Left: Contact
            leftBtn.setText(R.string.biz_detail_contact);
            leftBtn.setIconResource(R.drawable.ic_phone);
            leftBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.adm_gray_900));
            leftBtn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_gray_900)));
            leftBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_gray_300)));
            leftBtn.setOnClickListener(v -> contactCustomer(realBooking != null ? realBooking.getUserId() : null));

            // Right: Trả phòng (Hotel)
            rightBtn.setText("Trả phòng");
            rightBtn.setIconResource(R.drawable.ic_check);
            rightBtn.setOnClickListener(v -> {
                if (realBooking != null) {
                    updateStatus(realBooking.getId(), "COMPLETED", dialog);
                }
            });
        } else {
            // COMPLETED or CANCELLED: Hide right button, Left is full width Contact
            rightBtn.setVisibility(View.GONE);
            leftLp.weight = 2.0f;
            leftLp.setMarginEnd(0);
            leftBtn.setLayoutParams(leftLp);

            leftBtn.setText(R.string.biz_detail_contact);
            leftBtn.setIconResource(R.drawable.ic_phone);
            leftBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.adm_gray_900));
            leftBtn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_gray_900)));
            leftBtn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.adm_gray_300)));
            leftBtn.setOnClickListener(v -> contactCustomer(realBooking != null ? realBooking.getUserId() : null));
        }

        sheet.findViewById(R.id.bizSheetClose).setOnClickListener(view -> dialog.dismiss());

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
        if (mBlockedDays.contains(String.valueOf(day))) {
            return "blocked";
        }
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
