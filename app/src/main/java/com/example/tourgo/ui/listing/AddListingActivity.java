package com.example.tourgo.ui.listing;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tourgo.R;
import com.example.tourgo.databinding.ActivityAddListingBinding;
import com.example.tourgo.data.ListingRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.remote.ApiService;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.models.Tour;
import com.example.tourgo.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddListingActivity extends AppCompatActivity {

    private ActivityAddListingBinding binding;
    private ListingViewModel viewModel;
    private static final int NUM_STEPS = 5;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListingViewModel.class);

        setupViewPager();
        setupButtons();
        setupProgressDialog();
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Publishing your listing...");
        progressDialog.setCancelable(false);
    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new Step1Fragment();
                    case 1: return new Step2Fragment();
                    case 2: return new Step3Fragment();
                    case 3: return new Step4Fragment();
                    case 4: return new Step5Fragment();
                    default: return new Step1Fragment();
                }
            }

            @Override
            public int getItemCount() {
                return NUM_STEPS;
            }
        });

        binding.viewPager.setUserInputEnabled(false); // Disable swiping
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUI(position);
            }
        });
    }

    private void updateUI(int position) {
        updateStepCircles(position);
        binding.tvStepIndicator.setText("Step " + (position + 1) + " of 5");
        
        binding.btnBack.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        binding.btnContinue.setText(position == NUM_STEPS - 1 ? "Publish" : "Continue");
    }

    private void updateStepCircles(int position) {
        int activeColor = ContextCompat.getColor(this, R.color.white);
        int inactiveColor = ContextCompat.getColor(this, R.color.dark_gray);
        int black = ContextCompat.getColor(this, R.color.black);

        updateCircle(binding.step1Circle, position >= 0, activeColor, inactiveColor);
        updateCircle(binding.step2Circle, position >= 1, activeColor, inactiveColor);
        updateCircle(binding.step3Circle, position >= 2, activeColor, inactiveColor);
        updateCircle(binding.step4Circle, position >= 3, activeColor, inactiveColor);
        updateCircle(binding.step5Circle, position >= 4, activeColor, inactiveColor);
        
        // Update labels (Optional: you might want to find them by index if they had IDs)
        // For simplicity, we just update the circles.
    }

    private void updateCircle(android.widget.TextView circle, boolean isActive, int activeTextColor, int inactiveTextColor) {
        circle.setBackgroundResource(isActive ? R.drawable.circle_background_black : R.drawable.circle_background_gray);
        circle.setTextColor(isActive ? activeTextColor : inactiveTextColor);
        circle.setTypeface(null, isActive ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void setupButtons() {
        binding.btnContinue.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < NUM_STEPS - 1) {
                binding.viewPager.setCurrentItem(current + 1);
            } else {
                publishListing();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current > 0) {
                binding.viewPager.setCurrentItem(current - 1);
            }
        });

        binding.btnBackHeader.setOnClickListener(v -> finish());
    }

    private void publishListing() {
        // Find Step5Fragment to check agreement
        Step5Fragment step5 = null;
        // ViewPager2 fragments are often tagged with "f" + position
        Fragment f = getSupportFragmentManager().findFragmentByTag("f" + (NUM_STEPS - 1));
        if (f instanceof Step5Fragment) {
            step5 = (Step5Fragment) f;
        }

        if (step5 != null && !step5.isAgreed()) {
            Toast.makeText(this, "Please agree to the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Uri> images = viewModel.selectedImages.getValue();
        if (images == null || images.isEmpty()) {
            Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        
        SessionManager sessionManager = new SessionManager(this);
        String accessToken = sessionManager.getAccessToken();
        String userId = sessionManager.getUserId();

        Tour tour = new Tour();
        tour.setName(viewModel.name.getValue());
        tour.setDescription(viewModel.description.getValue());
        tour.setPrice(viewModel.basePrice.getValue() != null ? viewModel.basePrice.getValue() : 0);
        tour.setDestination(viewModel.address.getValue());
        tour.setRegion(viewModel.region.getValue());
        tour.setDuration(viewModel.duration.getValue());
        tour.setLatitude(viewModel.latitude.getValue() != null ? viewModel.latitude.getValue() : 0.0);
        tour.setLongitude(viewModel.longitude.getValue() != null ? viewModel.longitude.getValue() : 0.0);
        tour.setCapacity(viewModel.capacity.getValue() != null ? viewModel.capacity.getValue() : 1);
        tour.setStatus("APPROVED");
        tour.setBusinessesId("c0d75627-d967-4b8c-8ee2-08e9fae99837");

        ListingRepository.getInstance().createTourWithAvailability(
                this,
                tour,
                images,
                viewModel.blockedDates.getValue(),
                accessToken,
                new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        progressDialog.dismiss();
                        Toast.makeText(AddListingActivity.this, "Listing published successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(ApiErrorCode code, String msg) {
                        progressDialog.dismiss();
                        Toast.makeText(AddListingActivity.this, "Failed to publish: " + msg, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
