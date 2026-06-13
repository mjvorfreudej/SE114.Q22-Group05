package com.example.tourgo.ui.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.adapters.CreateTourImageAdapter;
import com.example.tourgo.data.ListingRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.Tour;
import com.example.tourgo.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateTourActivity extends AppCompatActivity {

    private static final int TOTAL_STEP = 5;
    private int currentStep = 1;

    private TextView btnBack, btnSaveDraft, btnCancelBottom, btnContinue, btnDemo;
    private TextView tvStep, tvPhotoCount;

    private TextView tvStepCircle1, tvStepCircle2, tvStepCircle3, tvStepCircle4, tvStepCircle5;
    private TextView tvStepLabel1, tvStepLabel2, tvStepLabel3, tvStepLabel4, tvStepLabel5;
    private View lineStep1, lineStep2, lineStep3, lineStep4;

    private View stepBasic, stepLocation, stepPricing, stepPublishSetting, stepReview;
    private View layoutHotel, layoutTour;

    private EditText edtTourName, edtDescription, edtDestination, edtRegion, edtPrice, edtDuration, edtLatitude, edtLongitude;
    private TextView btnUploadImage, tvCapacity, btnMinusCapacity, btnPlusCapacity;
    private RecyclerView rvImages;

    private android.widget.TableLayout tableCalendar;
    private TextView tvCurrentMonth, btnPrevMonth, btnNextMonth;
    private TextView tvOpenFrom, tvOpenUntil;
    private java.util.Calendar calendarDisplay;
    private final java.text.SimpleDateFormat monthYearFormat = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());
    private final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault());

    private String selectedOpenFrom = "";
    private String selectedOpenUntil = "";
    private List<String> blockedDates = new ArrayList<>();

    private TextView tvReviewName, tvReviewDestination, tvReviewPrice, tvReviewImages, tvReviewAmenities, tvReviewDuration;
    private ImageView imgReviewCover;
    private CheckBox cbAgree;

    private final List<Uri> imageUris = new ArrayList<>();
    private CreateTourImageAdapter imageAdapter;
    private ActivityResultLauncher<String> pickImagesLauncher;

    private SessionManager session;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tour);

        session = new SessionManager(this);

        bindViews();
        setupImagePicker();
        setupImageRecycler();
        setupEvents();

        showStep(1);
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);
        btnDemo = findViewById(R.id.btnDemo);
        btnCancelBottom = findViewById(R.id.btnCancelBottom);
        btnContinue = findViewById(R.id.btnContinue);

        tvStep = findViewById(R.id.tvStep);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);

        tvStepCircle1 = findViewById(R.id.tvStepCircle1);
        tvStepCircle2 = findViewById(R.id.tvStepCircle2);
        tvStepCircle3 = findViewById(R.id.tvStepCircle3);
        tvStepCircle4 = findViewById(R.id.tvStepCircle4);
        tvStepCircle5 = findViewById(R.id.tvStepCircle5);

        tvStepLabel1 = findViewById(R.id.tvStepLabel1);
        tvStepLabel2 = findViewById(R.id.tvStepLabel2);
        tvStepLabel3 = findViewById(R.id.tvStepLabel3);
        tvStepLabel4 = findViewById(R.id.tvStepLabel4);
        tvStepLabel5 = findViewById(R.id.tvStepLabel5);

        lineStep1 = findViewById(R.id.lineStep1);
        lineStep2 = findViewById(R.id.lineStep2);
        lineStep3 = findViewById(R.id.lineStep3);
        lineStep4 = findViewById(R.id.lineStep4);

        stepBasic = findViewById(R.id.stepBasic);
        stepLocation = findViewById(R.id.stepLocation);
        stepPricing = findViewById(R.id.stepPricing);
        stepPublishSetting = findViewById(R.id.stepPublishSetting);
        stepReview = findViewById(R.id.stepReview);

        layoutHotel = findViewById(R.id.layoutHotel);
        layoutTour = findViewById(R.id.layoutTour);

        edtTourName = findViewById(R.id.edtTourName);
        edtDescription = findViewById(R.id.edtDescription);
        edtDestination = findViewById(R.id.edtDestination);
        edtRegion = findViewById(R.id.edtRegion);
        edtLatitude = findViewById(R.id.edtLatitude);
        edtLongitude = findViewById(R.id.edtLongitude);
        edtPrice = findViewById(R.id.edtPrice);
        edtDuration = findViewById(R.id.edtDuration);

        tvCapacity = findViewById(R.id.tvCapacity);
        btnMinusCapacity = findViewById(R.id.btnMinusCapacity);
        btnPlusCapacity = findViewById(R.id.btnPlusCapacity);

        btnUploadImage = findViewById(R.id.btnUploadImage);
        rvImages = findViewById(R.id.rvImages);

        tableCalendar = findViewById(R.id.tableCalendar);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvOpenFrom = findViewById(R.id.tvOpenFrom);
        tvOpenUntil = findViewById(R.id.tvOpenUntil);

        tvReviewName = findViewById(R.id.tvReviewName);
        tvReviewDestination = findViewById(R.id.tvReviewDestination);
        tvReviewPrice = findViewById(R.id.tvReviewPrice);
        tvReviewImages = findViewById(R.id.tvReviewImages);
        tvReviewAmenities = findViewById(R.id.tvReviewAmenities);
        tvReviewDuration = findViewById(R.id.tvReviewDuration);
        imgReviewCover = findViewById(R.id.imgReviewCover);
        cbAgree = findViewById(R.id.cbAgree);
    }

    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris == null || uris.isEmpty()) return;

                    for (Uri uri : uris) {
                        if (imageUris.size() >= 6) {
                            Toast.makeText(this, "Chỉ được chọn tối đa 6 ảnh", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        imageUris.add(uri);
                    }

                    imageAdapter.notifyDataSetChanged();
                    updatePhotoCount();
                }
        );
    }

    private void setupImageRecycler() {
        imageAdapter = new CreateTourImageAdapter(imageUris, position -> {
            if (position >= 0 && position < imageUris.size()) {
                imageUris.remove(position);
                imageAdapter.notifyDataSetChanged();
                updatePhotoCount();
            }
        });

        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);
    }

    private void setupEvents() {
        calendarDisplay = java.util.Calendar.getInstance();
        calendarDisplay.set(java.util.Calendar.DAY_OF_MONTH, 1);

        btnPrevMonth.setOnClickListener(v -> {
            calendarDisplay.add(java.util.Calendar.MONTH, -1);
            renderCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            calendarDisplay.add(java.util.Calendar.MONTH, 1);
            renderCalendar();
        });

        tvOpenFrom.setOnClickListener(v -> showDatePicker(true));
        tvOpenUntil.setOnClickListener(v -> showDatePicker(false));

        btnBack.setOnClickListener(v -> handleBack());
        btnCancelBottom.setOnClickListener(v -> handleBack());

        btnContinue.setOnClickListener(v -> {
            if (!validateCurrentStep()) return;

            if (currentStep < TOTAL_STEP) {
                showStep(currentStep + 1);
            } else {
                publishTour("pending");
            }
        });

        btnSaveDraft.setOnClickListener(v -> {
            if (!validateDraftMinimum()) return;
            publishTour("draft");
        });

        btnDemo.setOnClickListener(v -> fillDemoData());

        layoutHotel.setOnClickListener(v -> selectCategory(true));
        layoutTour.setOnClickListener(v -> selectCategory(false));

        btnMinusCapacity.setOnClickListener(v -> updateCapacity(-1));
        btnPlusCapacity.setOnClickListener(v -> updateCapacity(1));

        btnUploadImage.setOnClickListener(v -> {
            if (imageUris.size() >= 6) {
                Toast.makeText(this, "Chỉ được chọn tối đa 6 ảnh", Toast.LENGTH_SHORT).show();
                return;
            }
            pickImagesLauncher.launch("image/*");
        });
    }

    private void selectCategory(boolean isHotel) {
        layoutHotel.setBackgroundResource(isHotel ? R.drawable.bg_listing_option_selected : R.drawable.bg_listing_option);
        layoutTour.setBackgroundResource(isHotel ? R.drawable.bg_listing_option : R.drawable.bg_listing_option_selected);
        
        TextView tvTourTag = findViewById(R.id.tvTourTag);
        if (tvTourTag != null) {
            tvTourTag.setText(isHotel ? "Hotel" : "Tour");
        }
    }

    private void updateCapacity(int delta) {
        try {
            int current = Integer.parseInt(tvCapacity.getText().toString());
            int newVal = Math.max(1, current + delta);
            tvCapacity.setText(String.valueOf(newVal));
        } catch (Exception e) {
            tvCapacity.setText("1");
        }
    }

    private void fillDemoData() {
        edtTourName.setText("Demo Tour " + System.currentTimeMillis() % 1000);
        edtDescription.setText("This is a demo tour description for testing purposes. Experience the best of the city with our guided tour.");
        edtDestination.setText("Bangkok, Thailand");
        edtRegion.setText("Southeast Asia");
        edtLatitude.setText("13.7563");
        edtLongitude.setText("100.5018");
        edtPrice.setText("1500000");
        edtDuration.setText("3 Days 2 Nights");
        tvCapacity.setText("10");

        // Thêm ảnh mẫu từ drawable vào danh sách
        if (imageUris.isEmpty()) {
            imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.hotel_1));
            imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.hotel_2));
            imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.hotel_3));
            imageUris.add(Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.hotel_4));
            
            imageAdapter.notifyDataSetChanged();
            updatePhotoCount();
        }

        Toast.makeText(this, "Đã tự động điền dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
    }

    private void handleBack() {
        if (currentStep == 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Thoát form?")
                    .setMessage("Dữ liệu chưa lưu có thể bị mất. Bạn có chắc muốn thoát?")
                    .setPositiveButton("Thoát", (dialog, which) -> finish())
                    .setNegativeButton("Ở lại", null)
                    .show();
        } else {
            showStep(currentStep - 1);
        }
    }

    private void showStep(int step) {
        currentStep = step;

        tvStep.setText("Step " + currentStep + " of " + TOTAL_STEP);

        stepBasic.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        stepLocation.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        stepPricing.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
        stepPublishSetting.setVisibility(step == 4 ? View.VISIBLE : View.GONE);
        stepReview.setVisibility(step == 5 ? View.VISIBLE : View.GONE);

        if (step == 4) {
            renderCalendar();
        }

        btnCancelBottom.setText(step > 1 ? "‹  Back" : "‹  Cancel");

        if (step < TOTAL_STEP) {
            btnContinue.setText("Continue  ›");
        } else {
            bindReviewData();
            btnContinue.setText("Publish  ↗");
        }

        android.util.Log.d("CreateTourActivity", "showStep: currentStep=" + currentStep + " btnContinue.getText()=" + btnContinue.getText());

        updateStepperUI();
    }

    private void updateStepperUI() {
        TextView[] circles = {tvStepCircle1, tvStepCircle2, tvStepCircle3, tvStepCircle4, tvStepCircle5};
        TextView[] labels = {tvStepLabel1, tvStepLabel2, tvStepLabel3, tvStepLabel4, tvStepLabel5};
        View[] lines = {lineStep1, lineStep2, lineStep3, lineStep4};

        for (int i = 0; i < circles.length; i++) {
            int stepNumber = i + 1;

            if (stepNumber < currentStep) {
                circles[i].setText("✓");
                circles[i].setBackgroundResource(R.drawable.bg_step_active);
                circles[i].setTextColor(0xFFFFFFFF);
                labels[i].setTextColor(0xFF101828);
                labels[i].setTypeface(null, Typeface.BOLD);
            } else if (stepNumber == currentStep) {
                circles[i].setText(String.valueOf(stepNumber));
                circles[i].setBackgroundResource(R.drawable.bg_step_current);
                circles[i].setTextColor(0xFF101828);
                labels[i].setTextColor(0xFF101828);
                labels[i].setTypeface(null, Typeface.BOLD);
            } else {
                circles[i].setText(String.valueOf(stepNumber));
                circles[i].setBackgroundResource(R.drawable.bg_step_inactive);
                circles[i].setTextColor(0xFF98A2B3);
                labels[i].setTextColor(0xFF98A2B3);
                labels[i].setTypeface(null, Typeface.NORMAL);
            }
        }

        for (int i = 0; i < lines.length; i++) {
            lines[i].setBackgroundColor(i + 1 < currentStep ? 0xFF101828 : 0xFFF2F4F7);
        }
    }

    private void updatePhotoCount() {
        tvPhotoCount.setText("Photos · " + imageUris.size() + "/6");
    }

    private boolean validateCurrentStep() {
        if (currentStep == 1) {
            String name = edtTourName.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                edtTourName.setError("Vui lòng nhập tên tour");
                edtTourName.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(desc)) {
                edtDescription.setError("Vui lòng nhập mô tả");
                edtDescription.requestFocus();
                return false;
            }

            if (imageUris.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (currentStep == 2) {
            String destination = edtDestination.getText().toString().trim();
            String region = edtRegion.getText().toString().trim();
            String lat = edtLatitude.getText().toString().trim();
            String lng = edtLongitude.getText().toString().trim();

            if (TextUtils.isEmpty(destination)) {
                edtDestination.setError("Vui lòng nhập địa điểm");
                edtDestination.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(region)) {
                edtRegion.setError("Vui lòng nhập khu vực");
                edtRegion.requestFocus();
                return false;
            }

            if (!isValidDouble(lat)) {
                edtLatitude.setError("Vĩ độ không hợp lệ");
                edtLatitude.requestFocus();
                return false;
            }

            if (!isValidDouble(lng)) {
                edtLongitude.setError("Kinh độ không hợp lệ");
                edtLongitude.requestFocus();
                return false;
            }
        }

        if (currentStep == 3) {
            if (!isValidPrice()) {
                edtPrice.setError("Giá phải là số lớn hơn 0");
                edtPrice.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(edtDuration.getText().toString().trim())) {
                edtDuration.setError("Vui lòng nhập thời lượng tour");
                edtDuration.requestFocus();
                return false;
            }
        }

        if (currentStep == 5 && !cbAgree.isChecked()) {
            Toast.makeText(this, "Vui lòng xác nhận điều khoản trước khi đăng", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateDraftMinimum() {
        String name = edtTourName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtTourName.setError("Cần nhập tên tour để lưu nháp");
            edtTourName.requestFocus();
            showStep(1);
            return false;
        }

        if (!isValidPrice()) {
            edtPrice.setText("0");
        }

        return true;
    }

    private boolean isValidPrice() {
        try {
            String value = edtPrice.getText().toString().trim();
            double price = Double.parseDouble(value);
            return price > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidDouble(String value) {
        if (TextUtils.isEmpty(value)) return false;
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void bindReviewData() {
        String name = getText(edtTourName, "The Grand Orchid Resort");
        String destination = getText(edtDestination, "Bangkok, Thailand");
        String price = getText(edtPrice, "199");
        String duration = getText(edtDuration, "3 Days 2 Nights");

        tvReviewName.setText(name);
        tvReviewDestination.setText(destination);
        tvReviewPrice.setText(formatReviewPrice(price));
        tvReviewImages.setText("Photos                                      " + imageUris.size() + " added");
        tvReviewAmenities.setText("Amenities                                  Wi-Fi, Pool");
        tvReviewDuration.setText("Duration                                  " + duration);

        if (!imageUris.isEmpty()) {
            imgReviewCover.setImageURI(imageUris.get(0));
        }
    }

    private String getText(EditText editText, String fallback) {
        String value = editText.getText().toString().trim();
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private String formatReviewPrice(String raw) {
        try {
            double value = Double.parseDouble(raw);
            return String.format(Locale.getDefault(), "%,.0f₫ / tour", value);
        } catch (Exception e) {
            return raw + "₫ / tour";
        }
    }

    private Tour buildTour(String status) {
        Tour tour = new Tour();

        tour.setName(getText(edtTourName, "Untitled tour"));
        tour.setDescription(getText(edtDescription, ""));
        tour.setDestination(getText(edtDestination, ""));
        tour.setRegion(getText(edtRegion, ""));
        tour.setDuration(getText(edtDuration, ""));

        try {
            tour.setLatitude(Double.parseDouble(edtLatitude.getText().toString().trim()));
        } catch (Exception e) {
            tour.setLatitude(0);
        }

        try {
            tour.setLongitude(Double.parseDouble(edtLongitude.getText().toString().trim()));
        } catch (Exception e) {
            tour.setLongitude(0);
        }

        try {
            tour.setCapacity(Integer.parseInt(tvCapacity.getText().toString().trim()));
        } catch (Exception e) {
            tour.setCapacity(1);
        }

        try {
            tour.setPrice(Double.parseDouble(edtPrice.getText().toString().trim()));
        } catch (Exception e) {
            tour.setPrice(0);
        }

        tour.setStatus(status);

        // Sử dụng ID của người dùng đang đăng nhập thay vì hard-code
        tour.setBusinessesId(session.getUserId());

        return tour;
    }

    private void publishTour(String status) {
        android.util.Log.d("CreateTourActivity", "publishTour called with status: " + status);
        if (!session.isLoggedIn()) {
            android.util.Log.w("CreateTourActivity", "publishTour: User not logged in");
            Toast.makeText(this, "Bạn cần đăng nhập để đăng tour", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("pending".equals(status) && !cbAgree.isChecked()) {
            android.util.Log.w("CreateTourActivity", "publishTour: cbAgree not checked");
            showStep(5);
            Toast.makeText(this, "Vui lòng xác nhận điều khoản trước khi đăng", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.i("CreateTourActivity", "publishTour: Starting tour creation flow");
        showLoading("Đang xử lý...");

        Tour tour = buildTour(status);
        android.util.Log.d("CreateTourActivity", "publishTour: Tour built: " + tour.getName() + ", BusinessId: " + tour.getBusinessesId());

        ListingRepository.getInstance().createTourWithAvailability(
                this,
                tour,
                imageUris,
                blockedDates,
                session.getAccessToken(),
                new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        android.util.Log.i("CreateTourActivity", "publishTour: Success");
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(
                                    CreateTourActivity.this,
                                    "draft".equals(status)
                                            ? "Đã lưu nháp"
                                            : "Đăng tour thành công, đang chờ duyệt",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(ApiErrorCode code, String rawMessage) {
                        android.util.Log.e("CreateTourActivity", "publishTour: Error " + code + " - " + rawMessage);
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(
                                    CreateTourActivity.this,
                                    "Lỗi: " + rawMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void showLoading(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void renderCalendar() {
        if (tableCalendar == null) return;
        tvCurrentMonth.setText(monthYearFormat.format(calendarDisplay.getTime()));

        int childCount = tableCalendar.getChildCount();
        if (childCount > 1) {
            tableCalendar.removeViews(1, childCount - 1);
        }

        java.util.Calendar cal = (java.util.Calendar) calendarDisplay.clone();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        java.util.Calendar prevMonth = (java.util.Calendar) cal.clone();
        prevMonth.add(java.util.Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        int dayCounter = 1;
        int nextMonthDayCounter = 1;

        java.util.Calendar startDate = parseDate(selectedOpenFrom);
        java.util.Calendar endDate = parseDate(selectedOpenUntil);

        for (int i = 0; i < 6; i++) {
            android.widget.TableRow row = new android.widget.TableRow(this);
            row.setLayoutParams(new android.widget.TableLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setPadding(0, 8, 0, 8);

            for (int j = 0; j < 7; j++) {
                TextView tv = new TextView(this);
                android.widget.TableRow.LayoutParams lp = new android.widget.TableRow.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                tv.setLayoutParams(lp);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(0, 16, 0, 16);
                tv.setTextSize(13);

                final java.util.Calendar currentCellDate = (java.util.Calendar) calendarDisplay.clone();

                if (i == 0 && j < firstDayOfWeek) {
                    int prevDay = daysInPrevMonth - firstDayOfWeek + j + 1;
                    tv.setText(String.valueOf(prevDay));
                    tv.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
                } else if (dayCounter > daysInMonth) {
                    tv.setText(String.valueOf(nextMonthDayCounter++));
                    tv.setTextColor(android.graphics.Color.parseColor("#E0E0E0"));
                } else {
                    final int day = dayCounter;
                    tv.setText(String.valueOf(day));
                    currentCellDate.set(java.util.Calendar.DAY_OF_MONTH, day);

                    highlightDate(tv, currentCellDate, startDate, endDate);

                    tv.setOnClickListener(v -> {
                        String dateStr = dateFormat.format(currentCellDate.getTime());
                        if (blockedDates.contains(dateStr)) {
                            blockedDates.remove(dateStr);
                        } else {
                            if (selectedOpenFrom.isEmpty() || !selectedOpenUntil.isEmpty()) {
                                selectedOpenFrom = dateStr;
                                selectedOpenUntil = "";
                            } else {
                                java.util.Calendar start = parseDate(selectedOpenFrom);
                                if (currentCellDate.after(start)) {
                                    selectedOpenUntil = dateStr;
                                } else {
                                    selectedOpenFrom = dateStr;
                                }
                            }
                        }
                        tvOpenFrom.setText(selectedOpenFrom.isEmpty() ? "▣   Open from" : selectedOpenFrom);
                        tvOpenUntil.setText(selectedOpenUntil.isEmpty() ? "▣   Open until" : selectedOpenUntil);
                        renderCalendar();
                    });

                    tv.setOnLongClickListener(v -> {
                        String dateStr = dateFormat.format(currentCellDate.getTime());
                        if (blockedDates.contains(dateStr)) {
                            blockedDates.remove(dateStr);
                        } else {
                            blockedDates.add(dateStr);
                        }
                        renderCalendar();
                        return true;
                    });
                    dayCounter++;
                }
                row.addView(tv);
            }
            tableCalendar.addView(row);
            if (dayCounter > daysInMonth && i >= 4) break;
        }
    }

    private void highlightDate(TextView tv, java.util.Calendar date, java.util.Calendar startDate, java.util.Calendar endDate) {
        String dateStr = dateFormat.format(date.getTime());
        if (blockedDates.contains(dateStr)) {
            tv.setBackgroundResource(R.drawable.bg_date_blocked);
            tv.setTextColor(android.graphics.Color.RED);
            return;
        }

        if (startDate != null && isSameDay(date, startDate)) {
            tv.setBackgroundResource(endDate != null ? R.drawable.bg_date_selected : R.drawable.bg_date_selected);
            tv.setTextColor(android.graphics.Color.WHITE);
        } else if (endDate != null && isSameDay(date, endDate)) {
            tv.setBackgroundResource(R.drawable.bg_date_selected);
            tv.setTextColor(android.graphics.Color.WHITE);
        } else if (startDate != null && endDate != null && date.after(startDate) && date.before(endDate)) {
            tv.setBackgroundColor(android.graphics.Color.parseColor("#E8F0FE"));
            tv.setTextColor(android.graphics.Color.BLACK);
        } else {
            tv.setBackground(null);
            tv.setTextColor(android.graphics.Color.BLACK);
        }
    }

    private boolean isSameDay(java.util.Calendar cal1, java.util.Calendar cal2) {
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
                cal1.get(java.util.Calendar.DAY_OF_MONTH) == cal2.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private java.util.Calendar parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(dateFormat.parse(dateStr));
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    private void showDatePicker(boolean isFrom) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            if (isFrom) {
                selectedOpenFrom = date;
                tvOpenFrom.setText(date);
            } else {
                selectedOpenUntil = date;
                tvOpenUntil.setText(date);
            }
            renderCalendar();
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }
}
