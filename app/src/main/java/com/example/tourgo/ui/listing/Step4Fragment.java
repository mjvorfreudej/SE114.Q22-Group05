package com.example.tourgo.ui.listing;

import android.app.DatePickerDialog;
import java.util.List;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tourgo.databinding.FragmentStep4Binding;
import java.util.Calendar;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Step4Fragment extends Fragment {

    private FragmentStep4Binding binding;
    private ListingViewModel viewModel;
    private Calendar calendarDisplay;
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStep4Binding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        calendarDisplay = Calendar.getInstance();
        calendarDisplay.set(Calendar.DAY_OF_MONTH, 1);

        binding.tvOpenFrom.setOnClickListener(v -> showDatePicker(true));
        binding.tvOpenUntil.setOnClickListener(v -> showDatePicker(false));

        binding.btnPrevMonth.setOnClickListener(v -> {
            calendarDisplay.add(Calendar.MONTH, -1);
            renderCalendar();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            calendarDisplay.add(Calendar.MONTH, 1);
            renderCalendar();
        });

        viewModel.openFrom.observe(getViewLifecycleOwner(), date -> {
            if (date != null && !date.isEmpty()) {
                binding.tvOpenFrom.setText(date);
                renderCalendar();
            }
        });
        viewModel.openUntil.observe(getViewLifecycleOwner(), date -> {
            if (date != null && !date.isEmpty()) {
                binding.tvOpenUntil.setText(date);
                renderCalendar();
            }
        });

        viewModel.blockedDates.observe(getViewLifecycleOwner(), dates -> {
            renderCalendar();
        });

        renderCalendar();
    }

    private void renderCalendar() {
        binding.tvCurrentMonth.setText(monthYearFormat.format(calendarDisplay.getTime()));
        
        int childCount = binding.tableCalendar.getChildCount();
        if (childCount > 1) {
            binding.tableCalendar.removeViews(1, childCount - 1);
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

        Calendar startDate = parseDate(viewModel.openFrom.getValue());
        Calendar endDate = parseDate(viewModel.openUntil.getValue());

        for (int i = 0; i < 6; i++) { 
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new android.widget.TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 8, 0, 8);

            for (int j = 0; j < 7; j++) {
                TextView tv = new TextView(requireContext());
                TableRow.LayoutParams lp = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                tv.setLayoutParams(lp);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(0, 16, 0, 16);
                tv.setTextSize(13);

                final Calendar currentCellDate = (Calendar) calendarDisplay.clone();

                if (i == 0 && j < firstDayOfWeek) {
                    int prevDay = daysInPrevMonth - firstDayOfWeek + j + 1;
                    tv.setText(String.valueOf(prevDay));
                    tv.setTextColor(Color.parseColor("#E0E0E0"));
                } else if (dayCounter > daysInMonth) {
                    tv.setText(String.valueOf(nextMonthDayCounter++));
                    tv.setTextColor(Color.parseColor("#E0E0E0"));
                } else {
                    final int day = dayCounter;
                    tv.setText(String.valueOf(day));
                    currentCellDate.set(Calendar.DAY_OF_MONTH, day);

                    highlightDate(tv, currentCellDate, startDate, endDate);
                    
                    tv.setOnClickListener(v -> {
                        String dateStr = dateFormat.format(currentCellDate.getTime());
                        
                        // If it's a long click or a specific mode, we could block. 
                        // For now, let's assume if a date is already in range, clicking it again might block it, 
                        // or we add a toggle mode. To match "delete availability" policy, 
                        // let's implement a simple "toggle block" if range is already set or on long click.
                        
                        List<String> blocked = viewModel.blockedDates.getValue();
                        if (blocked == null) blocked = new java.util.ArrayList<>();

                        if (blocked.contains(dateStr)) {
                            blocked.remove(dateStr);
                            viewModel.blockedDates.setValue(blocked);
                        } else {
                            // Standard range logic
                            if (viewModel.openFrom.getValue() == null || viewModel.openFrom.getValue().isEmpty() || 
                               (viewModel.openUntil.getValue() != null && !viewModel.openUntil.getValue().isEmpty())) {
                                viewModel.openFrom.setValue(dateStr);
                                viewModel.openUntil.setValue("");
                            } else {
                                Calendar start = parseDate(viewModel.openFrom.getValue());
                                if (currentCellDate.after(start)) {
                                    viewModel.openUntil.setValue(dateStr);
                                } else {
                                    viewModel.openFrom.setValue(dateStr);
                                }
                            }
                        }
                    });

                    tv.setOnLongClickListener(v -> {
                        String dateStr = dateFormat.format(currentCellDate.getTime());
                        List<String> blocked = viewModel.blockedDates.getValue();
                        if (blocked == null) blocked = new java.util.ArrayList<>();
                        
                        if (blocked.contains(dateStr)) {
                            blocked.remove(dateStr);
                        } else {
                            blocked.add(dateStr);
                        }
                        viewModel.blockedDates.setValue(blocked);
                        renderCalendar();
                        return true;
                    });
                    dayCounter++;
                }
                row.addView(tv);
            }
            binding.tableCalendar.addView(row);
            if (dayCounter > daysInMonth && i >= 4) break;
        }
    }

    private void highlightDate(TextView tv, Calendar date, Calendar startDate, Calendar endDate) {
        String dateStr = dateFormat.format(date.getTime());
        List<String> blocked = viewModel.blockedDates.getValue();
        
        if (blocked != null && blocked.contains(dateStr)) {
            tv.setBackgroundResource(com.example.tourgo.R.drawable.bg_date_blocked);
            tv.setTextColor(Color.RED);
            return;
        }

        if (startDate != null && isSameDay(date, startDate)) {
            tv.setBackgroundResource(endDate != null ? com.example.tourgo.R.drawable.bg_date_selected_left : com.example.tourgo.R.drawable.bg_circle_blue);
            tv.setTextColor(Color.WHITE);
        } else if (endDate != null && isSameDay(date, endDate)) {
            tv.setBackgroundResource(com.example.tourgo.R.drawable.bg_date_selected_right);
            tv.setTextColor(Color.WHITE);
        } else if (startDate != null && endDate != null && date.after(startDate) && date.before(endDate)) {
            tv.setBackgroundColor(Color.parseColor("#E8F0FE"));
            tv.setTextColor(Color.BLACK);
        } else {
            tv.setBackground(null);
            tv.setTextColor(Color.BLACK);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private Calendar parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(dateStr));
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    private void showDatePicker(boolean isFrom) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            if (isFrom) {
                viewModel.openFrom.setValue(date);
            } else {
                viewModel.openUntil.setValue(date);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
