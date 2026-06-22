package com.example.tourgo.ui.main.tour;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.request.CreateTourRequest;
import com.example.tourgo.models.response.Tour;
import com.example.tourgo.remote.service.TourService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Provider/admin form to submit a new tour. On confirm the form data is sent to
 * the backend ({@code POST /api/tours}); the server stores it with
 * {@code status = "PENDING"} and returns the created tour. The cover image is
 * then uploaded to the server ({@code POST /api/tours/{id}/images}). The app
 * never talks to the database directly — everything goes through Retrofit.
 */
public class CreateTourActivity extends AppCompatActivity {

    private static final String STATUS_PENDING = "PENDING";

    private TextInputLayout tilName, tilDescription, tilDestination, tilDuration, tilPrice, tilAmenities;
    private TextInputEditText etName, etDescription, etDestination, etDuration, etPrice, etAmenities;
    private ImageView ivPreview;
    private LinearLayout placeholder;
    private TextView tvChangeImage;
    private MaterialButton btnConfirm;
    private View loadingOverlay;
    private TextView tvLoadingMessage;

    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    showPreview(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tour);

        tilName = findViewById(R.id.tilTourName);
        tilDescription = findViewById(R.id.tilTourDescription);
        tilDestination = findViewById(R.id.tilTourDestination);
        tilDuration = findViewById(R.id.tilTourDuration);
        tilPrice = findViewById(R.id.tilTourPrice);
        tilAmenities = findViewById(R.id.tilTourAmenities);

        etName = findViewById(R.id.etTourName);
        etDescription = findViewById(R.id.etTourDescription);
        etDestination = findViewById(R.id.etTourDestination);
        etDuration = findViewById(R.id.etTourDuration);
        etPrice = findViewById(R.id.etTourPrice);
        etAmenities = findViewById(R.id.etTourAmenities);

        ivPreview = findViewById(R.id.ivTourPreview);
        placeholder = findViewById(R.id.imagePlaceholder);
        tvChangeImage = findViewById(R.id.tvChangeImage);
        btnConfirm = findViewById(R.id.btnConfirmTour);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage);

        findViewById(R.id.btnCreateTourBack).setOnClickListener(v -> finish());
        findViewById(R.id.imagePickerContainer).setOnClickListener(v -> pickImage.launch("image/*"));
        btnConfirm.setOnClickListener(v -> attemptCreate());
    }

    private void showPreview(Uri uri) {
        placeholder.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);
        tvChangeImage.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).centerCrop().into(ivPreview);
    }

    private boolean validate() {
        boolean ok = true;

        if (TextUtils.isEmpty(text(etName))) {
            tilName.setError(getString(R.string.create_tour_err_name));
            ok = false;
        } else {
            tilName.setError(null);
        }

        if (TextUtils.isEmpty(text(etDescription))) {
            tilDescription.setError(getString(R.string.create_tour_err_desc));
            ok = false;
        } else {
            tilDescription.setError(null);
        }

        if (TextUtils.isEmpty(text(etDestination))) {
            tilDestination.setError(getString(R.string.create_tour_err_destination));
            ok = false;
        } else {
            tilDestination.setError(null);
        }

        double price = parsePrice(text(etPrice));
        if (price <= 0) {
            tilPrice.setError(getString(R.string.create_tour_err_price));
            ok = false;
        } else {
            tilPrice.setError(null);
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, R.string.create_tour_err_image, Toast.LENGTH_SHORT).show();
            ok = false;
        }

        return ok;
    }

    private void attemptCreate() {
        if (!validate()) return;

        setLoading(true, getString(R.string.create_tour_saving));

        CreateTourRequest request = new CreateTourRequest(
                text(etName),
                text(etDescription),
                parsePrice(text(etPrice)),
                text(etDestination),
                null,
                emptyToNull(text(etDuration)),
                STATUS_PENDING
        );

        String rawAmenities = text(etAmenities);
        if (!TextUtils.isEmpty(rawAmenities)) {
            java.util.List<String> amenitiesList = new java.util.ArrayList<>();
            for (String item : rawAmenities.split(",")) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    amenitiesList.add(trimmed);
                }
            }
            request.setAmenities(amenitiesList);
        }

        // 1) Create the tour on the server (saved as PENDING).
        TourService.createTour(this, request, new DataCallback<Tour>() {
            @Override
            public void onSuccess(Tour tour) {
                uploadImageThenFinish(tour);
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                setLoading(false, null);
                showError(rawMessage);
            }
        });
    }

    private void uploadImageThenFinish(Tour tour) {
        if (selectedImageUri == null || tour == null || tour.getId() == null) {
            finishWithSuccess();
            return;
        }
        // 2) Upload the cover image. The tour already exists, so an image
        //    failure shouldn't block the success path.
        setLoading(true, getString(R.string.create_tour_uploading));
        TourService.uploadTourImage(this, selectedImageUri, tour.getId(), new DataCallback<String>() {
            @Override
            public void onSuccess(String imageUrl) {
                finishWithSuccess();
            }

            @Override
            public void onError(ApiErrorCode code, String rawMessage) {
                finishWithSuccess();
            }
        });
    }

    private void finishWithSuccess() {
        setLoading(false, null);
        Toast.makeText(this, R.string.create_tour_success, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showError(String rawMessage) {
        String msg = TextUtils.isEmpty(rawMessage) ? getString(R.string.err_unknown) : rawMessage;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading, String message) {
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnConfirm.setEnabled(!loading);
        if (message != null) tvLoadingMessage.setText(message);
    }

    private static String text(@NonNull TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private static String emptyToNull(String s) {
        return TextUtils.isEmpty(s) ? null : s;
    }

    private static double parsePrice(String raw) {
        if (TextUtils.isEmpty(raw)) return 0;
        try {
            return Double.parseDouble(raw.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
