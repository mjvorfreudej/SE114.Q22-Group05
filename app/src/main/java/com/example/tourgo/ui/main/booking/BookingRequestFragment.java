package com.example.tourgo.ui.main.booking;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.HotelService;
import com.example.tourgo.data.local.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BookingRequestFragment extends Fragment {

    private int guestCount = 4;
    private int bedCount = 2;
    private ImageButton btnMinusGuest, btnMinusBed;
    private TextView tvGuestCount, tvBedCount, tvCheckInDate, tvCheckOutDate, tvSummary;
    
    private Calendar startDate;
    private Calendar endDate;
    private Calendar dialogStartDate;
    private Calendar dialogEndDate;
    private Calendar calendarDisplay; 
    private SessionManager session;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat summaryFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    // Nights the hotel is sold out (yyyy-MM-dd), fetched from the backend so the
    // date-picker can disable them. Matches the backend's date-key format.
    private final SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final Set<String> unavailableDates = new HashSet<>();
    // Live references to the open date dialog so we can re-render once the
    // unavailable dates arrive (the fetch may finish after the dialog opens).
    private TableLayout dialogTable;
    private TextView dialogMonthYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        applyTopInset(view);

        session = new SessionManager(requireContext());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.DAY_OF_MONTH, 2);

        tvCheckInDate = view.findViewById(R.id.tvCheckInDate);
        tvCheckOutDate = view.findViewById(R.id.tvCheckOutDate);
        tvSummary = view.findViewById(R.id.tvSummary);
        tvGuestCount = view.findViewById(R.id.tvGuestCount);
        tvBedCount = view.findViewById(R.id.tvBedCount);
        btnMinusGuest = view.findViewById(R.id.btnMinusGuest);
        btnMinusBed = view.findViewById(R.id.btnMinusBed);
        
        view.findViewById(R.id.btnBack).setOnClickListener(v -> requireActivity().finish());
        
        View.OnClickListener dateClick = v -> showDateSelectionDialog();
        view.findViewById(R.id.btnCheckIn).setOnClickListener(dateClick);
        view.findViewById(R.id.btnCheckOut).setOnClickListener(dateClick);

        view.findViewById(R.id.btnAddGuest).setOnClickListener(v -> { guestCount++; updateCounterUI(); });
        btnMinusGuest.setOnClickListener(v -> { if (guestCount > 1) { guestCount--; updateCounterUI(); } });
        
        view.findViewById(R.id.btnAddBed).setOnClickListener(v -> { bedCount++; updateCounterUI(); });
        btnMinusBed.setOnClickListener(v -> { if (bedCount > 1) { bedCount--; updateCounterUI(); } });

        view.findViewById(R.id.btnNextStep).setOnClickListener(v -> {
            if (!(getActivity() instanceof BookingActivity)) return;
            BookingActivity host = (BookingActivity) getActivity();
            Hotel hotel = host.getHotel();
            Tour tour = hotel == null ? host.getTour() : null;
            if (hotel == null && tour == null) return;
            if (startDate == null || endDate == null) return;

            if (isBeforeToday(startDate)) {
                Toast.makeText(requireContext(), R.string.booking_error_checkin_past, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!endDate.after(startDate)) {
                Toast.makeText(requireContext(), R.string.booking_error_checkout_after_checkin, Toast.LENGTH_SHORT).show();
                return;
            }
            if (rangeHasBlockedNight(startDate, endDate)) {
                Toast.makeText(requireContext(), R.string.booking_error_range_sold_out, Toast.LENGTH_SHORT).show();
                return;
            }

            long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
            int nights = (int) (diff / (24 * 60 * 60 * 1000));
            if (nights <= 0) nights = 1;

            double pricePerNight = hotel != null ? hotel.getPricePerNight() : tour.getPrice();
            double roomPrice = pricePerNight * nights;
            double taxes = roomPrice * 0.1;
            
            // Giữ nguyên giá trị phí dịch vụ (không quy đổi tỷ giá)
            double serviceCharge = 50000.0;
            double total = roomPrice + taxes + serviceCharge;

            String checkInOut = summaryFormat.format(startDate.getTime()) + " - " + summaryFormat.format(endDate.getTime());
            String guestInfo = getString(R.string.booking_guest_info_format, guestCount, bedCount);

            Bundle args = new Bundle();
            args.putInt("num_nights", nights);
            args.putDouble("room_price", roomPrice);
            args.putDouble("taxes", taxes);
            args.putDouble("service_charge", serviceCharge);
            args.putDouble("total_price", total);
            args.putString("check_in_out", checkInOut + getString(R.string.booking_nights_format, nights));
            args.putString("guest_info", guestInfo);
            args.putLong("check_in_millis", startDate.getTimeInMillis());
            args.putLong("check_out_millis", endDate.getTimeInMillis());
            args.putInt("guests", guestCount);
            args.putInt("rooms", bedCount);

            BookingConfirmFragment nextFragment = new BookingConfirmFragment();
            nextFragment.setArguments(args);
            host.showStep(nextFragment);
        });

        updateCounterUI();
        updateDateUI();

        loadUnavailableDates();
    }

    /** Fetch the hotel's sold-out nights so the picker can disable them (hotels only). */
    private void loadUnavailableDates() {
        if (!(getActivity() instanceof BookingActivity)) return;
        Hotel hotel = ((BookingActivity) getActivity()).getHotel();
        if (hotel == null || hotel.getId() == null) return;

        HotelService.getUnavailableDates(requireContext(), hotel.getId(), new DataCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> dates) {
                if (!isAdded()) return;
                unavailableDates.clear();
                if (dates != null) unavailableDates.addAll(dates);
                // If the date dialog is already open, re-render to grey out the new dates.
                if (dialogTable != null && dialogMonthYear != null) {
                    renderCalendar(dialogTable, dialogMonthYear);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                // Fail open: if we can't load sold-out dates, leave all dates selectable.
            }
        });
    }

    private void applyTopInset(View root) {
        final int basePaddingTop = root.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void showDateSelectionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_date, null);
        dialog.setContentView(view);

        dialogStartDate = startDate != null ? (Calendar) startDate.clone() : null;
        dialogEndDate = endDate != null ? (Calendar) endDate.clone() : null;

        calendarDisplay = (Calendar) (dialogStartDate != null ? dialogStartDate : Calendar.getInstance()).clone();
        calendarDisplay.set(Calendar.DAY_OF_MONTH, 1);

        TextView tvMonthYear = view.findViewById(R.id.tvMonthYear);
        TableLayout tableCalendar = view.findViewById(R.id.tableCalendar);
        dialogTable = tableCalendar;
        dialogMonthYear = tvMonthYear;
        dialog.setOnDismissListener(d -> { dialogTable = null; dialogMonthYear = null; });

        view.findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            calendarDisplay.add(Calendar.MONTH, -1);
            renderCalendar(tableCalendar, tvMonthYear);
        });

        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            calendarDisplay.add(Calendar.MONTH, 1);
            renderCalendar(tableCalendar, tvMonthYear);
        });

        renderCalendar(tableCalendar, tvMonthYear);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirmDate).setOnClickListener(v -> {
            startDate = dialogStartDate != null ? (Calendar) dialogStartDate.clone() : null;
            endDate = dialogEndDate != null ? (Calendar) dialogEndDate.clone() : null;
            updateDateUI();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void renderCalendar(TableLayout table, TextView tvMonthYear) {
        tvMonthYear.setText(monthYearFormat.format(calendarDisplay.getTime()));
        
        int childCount = table.getChildCount();
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }

        Calendar cal = (Calendar) calendarDisplay.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; 
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar prevMonth = (Calendar) cal.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        int dayCounter = 1;
        int nextMonthDayCounter = 1;

        for (int i = 0; i < 6; i++) { 
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 16, 0, 0);

            for (int j = 0; j < 7; j++) {
                TextView tv = new TextView(requireContext());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                tv.setLayoutParams(lp);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(0, 20, 0, 20);

                final Calendar currentCellDate = (Calendar) calendarDisplay.clone();

                if (i == 0 && j < firstDayOfWeek) {
                    int prevDay = daysInPrevMonth - firstDayOfWeek + j + 1;
                    tv.setText(String.valueOf(prevDay));
                    tv.setTextColor(Color.parseColor("#E0E0E0"));
                } else if (dayCounter > daysInMonth) {
                    tv.setText(String.format(Locale.getDefault(), "%02d", nextMonthDayCounter++));
                    tv.setTextColor(Color.parseColor("#E0E0E0"));
                } else {
                    final int day = dayCounter;
                    tv.setText(String.format(Locale.getDefault(), "%02d", day));
                    tv.setTextColor(Color.BLACK);
                    currentCellDate.set(Calendar.DAY_OF_MONTH, day);

                    if (isBeforeToday(currentCellDate)) {
                        tv.setTextColor(Color.parseColor("#E0E0E0"));
                    } else if (unavailableDates.contains(keyFormat.format(currentCellDate.getTime()))) {
                        // Sold out — show greyed + struck through and make it unselectable.
                        tv.setTextColor(Color.parseColor("#C0C0C0"));
                        tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        tv.setTextColor(Color.BLACK);
                        highlightDate(tv, currentCellDate);
                        tv.setOnClickListener(v -> {
                            if (dialogStartDate != null && dialogEndDate == null) {
                                if (currentCellDate.after(dialogStartDate)) {
                                    // Reject a stay that would cover a sold-out night.
                                    if (rangeHasBlockedNight(dialogStartDate, currentCellDate)) {
                                        Toast.makeText(requireContext(), R.string.booking_error_range_sold_out, Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    dialogEndDate = (Calendar) currentCellDate.clone();
                                } else {
                                    dialogStartDate = (Calendar) currentCellDate.clone();
                                    dialogEndDate = null;
                                }
                            } else {
                                dialogStartDate = (Calendar) currentCellDate.clone();
                                dialogEndDate = null;
                            }
                            renderCalendar(table, tvMonthYear);
                        });
                    }
                    dayCounter++;
                }
                row.addView(tv);
            }
            table.addView(row);
            if (dayCounter > daysInMonth && i >= 4) break;
        }
    }

    private void highlightDate(TextView tv, Calendar date) {
        if (dialogStartDate != null && isSameDay(date, dialogStartDate)) {
            tv.setBackgroundResource(dialogEndDate != null ? R.drawable.bg_date_selected_left : R.drawable.bg_circle_blue);
            tv.setTextColor(Color.WHITE);
        } else if (dialogEndDate != null && isSameDay(date, dialogEndDate)) {
            tv.setBackgroundResource(R.drawable.bg_date_selected_right);
            tv.setTextColor(Color.WHITE);
        } else if (dialogStartDate != null && dialogEndDate != null && date.after(dialogStartDate) && date.before(dialogEndDate)) {
            tv.setBackgroundColor(Color.parseColor("#E8F0FE"));
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * True if any night in the stay [start, end) is sold out. A hotel stay
     * occupies check-in (inclusive) through the night before check-out.
     */
    private boolean rangeHasBlockedNight(Calendar start, Calendar end) {
        if (unavailableDates.isEmpty()) return false;
        Calendar cursor = (Calendar) start.clone();
        while (cursor.before(end)) {
            if (unavailableDates.contains(keyFormat.format(cursor.getTime()))) return true;
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return false;
    }

    private boolean isBeforeToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return date.before(today);
    }

    private void updateDateUI() {
        if (startDate != null) {
            tvCheckInDate.setText(dateFormat.format(startDate.getTime()));
        }
        if (endDate != null) {
            tvCheckOutDate.setText(dateFormat.format(endDate.getTime()));
            long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
            int nights = (int) (diff / (24 * 60 * 60 * 1000));
            if (nights <= 0) nights = 1;
            tvSummary.setText(summaryFormat.format(startDate.getTime()) + " - " + summaryFormat.format(endDate.getTime()) + getString(R.string.booking_nights_format, nights));
        } else {
            tvCheckOutDate.setText("-- --- ----");
            tvSummary.setText(R.string.booking_select_return);
        }
    }

    private void updateCounterUI() {
        tvGuestCount.setText(String.valueOf(guestCount));
        tvBedCount.setText(String.valueOf(bedCount));
        
        btnMinusGuest.setBackgroundResource(guestCount > 1 ? R.drawable.bg_circle_blue : R.drawable.bg_circle_outline);
        btnMinusGuest.setImageTintList(ColorStateList.valueOf(guestCount > 1 ? Color.WHITE : Color.BLACK));

        btnMinusBed.setBackgroundResource(bedCount > 1 ? R.drawable.bg_circle_blue : R.drawable.bg_circle_outline);
        btnMinusBed.setImageTintList(ColorStateList.valueOf(bedCount > 1 ? Color.WHITE : Color.BLACK));
    }
}
