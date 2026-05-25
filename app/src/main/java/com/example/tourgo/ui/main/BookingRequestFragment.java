package com.example.tourgo.ui.main;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import com.example.tourgo.R;
import com.example.tourgo.models.response.Hotel;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.data.local.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingRequestFragment extends Fragment {

    private int guestCount = 4;
    private int bedCount = 2;
    private ImageButton btnMinusGuest, btnMinusBed;
    private TextView tvGuestCount, tvBedCount, tvCheckInDate, tvCheckOutDate, tvSummary;
    
    private Calendar startDate;
    private Calendar endDate;
    private Calendar calendarDisplay; 
    private SessionManager session;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat summaryFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

            BookingConfirmFragment nextFragment = new BookingConfirmFragment();
            nextFragment.setArguments(args);
            host.showStep(nextFragment);
        });

        updateCounterUI();
        updateDateUI();
    }

    private void showDateSelectionDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_select_date, null);
        dialog.setContentView(view);

        calendarDisplay = (Calendar) startDate.clone();
        calendarDisplay.set(Calendar.DAY_OF_MONTH, 1);

        TextView tvMonthYear = view.findViewById(R.id.tvMonthYear);
        TableLayout tableCalendar = view.findViewById(R.id.tableCalendar);

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
                    } else {
                        tv.setTextColor(Color.BLACK);
                        highlightDate(tv, currentCellDate);
                        tv.setOnClickListener(v -> {
                            if (startDate != null && endDate == null) {
                                if (currentCellDate.after(startDate)) {
                                    endDate = (Calendar) currentCellDate.clone();
                                } else {
                                    startDate = (Calendar) currentCellDate.clone();
                                    endDate = null;
                                }
                            } else {
                                startDate = (Calendar) currentCellDate.clone();
                                endDate = null;
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
        if (startDate != null && isSameDay(date, startDate)) {
            tv.setBackgroundResource(endDate != null ? R.drawable.bg_date_selected_left : R.drawable.bg_circle_blue);
            tv.setTextColor(Color.WHITE);
        } else if (endDate != null && isSameDay(date, endDate)) {
            tv.setBackgroundResource(R.drawable.bg_date_selected_right);
            tv.setTextColor(Color.WHITE);
        } else if (startDate != null && endDate != null && date.after(startDate) && date.before(endDate)) {
            tv.setBackgroundColor(Color.parseColor("#E8F0FE"));
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
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
