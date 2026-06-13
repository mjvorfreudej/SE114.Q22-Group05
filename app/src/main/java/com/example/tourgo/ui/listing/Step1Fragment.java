package com.example.tourgo.ui.listing;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tourgo.R;
import com.example.tourgo.databinding.FragmentStep1Binding;

public class Step1Fragment extends Fragment {

    private FragmentStep1Binding binding;
    private ListingViewModel viewModel;
    private ImagePickerAdapter adapter;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(6), uris -> {
                if (!uris.isEmpty()) {
                    for (Uri uri : uris) {
                        viewModel.addImage(uri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStep1Binding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategorySelection();
        setupImagePicker();
        setupInputListeners();

        viewModel.selectedImages.observe(getViewLifecycleOwner(), uris -> {
            adapter.setImages(uris);
            binding.tvPhotoCount.setText("Photos - " + uris.size() + "/6");
            boolean canUpload = uris.size() < 6;
            binding.btnUpload.setEnabled(canUpload);
            binding.btnUpload.setAlpha(canUpload ? 1.0f : 0.5f);
        });
    }

    private void setupCategorySelection() {
        binding.cardHotel.setOnClickListener(v -> viewModel.type.setValue("Hotel"));
        binding.cardTour.setOnClickListener(v -> viewModel.type.setValue("Tour"));

        viewModel.type.observe(getViewLifecycleOwner(), type -> {
            boolean isHotel = "Hotel".equals(type);
            updateHotelCard(isHotel);
            updateTourCard(!isHotel);
        });
    }

    private void updateHotelCard(boolean isSelected) {
        int strokeColor = ContextCompat.getColor(requireContext(), isSelected ? R.color.orange : R.color.light_gray);
        binding.cardHotel.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));
        binding.cardHotel.setStrokeWidth(isSelected ? (int) (2 * getResources().getDisplayMetrics().density) : (int) (1 * getResources().getDisplayMetrics().density));
    }

    private void updateTourCard(boolean isSelected) {
        int strokeColor = ContextCompat.getColor(requireContext(), isSelected ? R.color.cyan : R.color.light_gray);
        binding.cardTour.setStrokeColor(android.content.res.ColorStateList.valueOf(strokeColor));
        binding.cardTour.setStrokeWidth(isSelected ? (int) (2 * getResources().getDisplayMetrics().density) : (int) (1 * getResources().getDisplayMetrics().density));
    }

    private void setupImagePicker() {
        adapter = new ImagePickerAdapter(viewModel.selectedImages.getValue(), position -> viewModel.removeImage(position));
        binding.rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvImages.setAdapter(adapter);

        binding.btnUpload.setOnClickListener(v -> pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void setupInputListeners() {
        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.name.setValue(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.description.setValue(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
