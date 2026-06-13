package com.example.tourgo.ui.listing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;
import com.example.tourgo.R;
import com.bumptech.glide.Glide;
import com.example.tourgo.databinding.FragmentStep5Binding;

public class Step5Fragment extends Fragment {

    private FragmentStep5Binding binding;
    private ListingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStep5Binding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe and update UI
        viewModel.name.observe(getViewLifecycleOwner(), name -> binding.tvPreviewName.setText(name));
        viewModel.type.observe(getViewLifecycleOwner(), type -> {
            binding.tvPreviewTypeLabel.setText(type);
            boolean isHotel = "Hotel".equals(type);
            int color = isHotel ? ContextCompat.getColor(requireContext(), R.color.orange) : ContextCompat.getColor(requireContext(), R.color.cyan);
            binding.tvPreviewTypeIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        });
        viewModel.address.observe(getViewLifecycleOwner(), address -> binding.tvPreviewAddress.setText(address));
        viewModel.basePrice.observe(getViewLifecycleOwner(), price -> binding.tvPreviewPrice.setText("$" + price));

        viewModel.selectedImages.observe(getViewLifecycleOwner(), uris -> {
            if (uris != null && !uris.isEmpty()) {
                Glide.with(this).load(uris.get(0)).into(binding.ivPreviewCover);
            }
            updateSummary();
        });

        viewModel.amenities.observe(getViewLifecycleOwner(), amenities -> updateSummary());
        viewModel.capacity.observe(getViewLifecycleOwner(), capacity -> updateSummary());
    }

    private void updateSummary() {
        int photoCount = viewModel.selectedImages.getValue() != null ? viewModel.selectedImages.getValue().size() : 0;
        binding.tvSummaryPhotos.setText(photoCount + " added");
        
        if (viewModel.amenities.getValue() != null && !viewModel.amenities.getValue().isEmpty()) {
            binding.tvSummaryAmenities.setText(String.join(", ", viewModel.amenities.getValue()));
        } else {
            binding.tvSummaryAmenities.setText("None");
        }
        
        int guests = viewModel.capacity.getValue() != null ? viewModel.capacity.getValue() : 0;
        binding.tvSummaryCapacity.setText(guests + " guests");

        StringBuilder pricingDetail = new StringBuilder();
        pricingDetail.append("Base: $").append(viewModel.basePrice.getValue() != null ? viewModel.basePrice.getValue() : 0);
        if (Boolean.TRUE.equals(viewModel.isSeasonal.getValue())) {
            pricingDetail.append(" • Seasonal");
        }
        binding.tvSummaryPricing.setText(pricingDetail.toString());
    }

    public boolean isAgreed() {
        return binding.cbAgree.isChecked();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
