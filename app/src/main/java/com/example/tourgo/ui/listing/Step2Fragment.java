package com.example.tourgo.ui.listing;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.tourgo.databinding.FragmentStep2Binding;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class Step2Fragment extends Fragment {

    private FragmentStep2Binding binding;
    private ListingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStep2Binding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupInitialState();
        setupInputListeners();
        setupAmenities();
        setupCapacity();
    }

    private void setupInitialState() {
        if (viewModel.address.getValue() != null) {
            binding.etAddress.setText(viewModel.address.getValue());
        }
        if (viewModel.latitude.getValue() != null) {
            binding.etLat.setText(String.valueOf(viewModel.latitude.getValue()));
        }
        if (viewModel.longitude.getValue() != null) {
            binding.etLng.setText(String.valueOf(viewModel.longitude.getValue()));
        }
    }

    private void setupInputListeners() {
        binding.etAddress.addTextChangedListener(createWatcher(s -> viewModel.address.setValue(s)));
        binding.etLat.addTextChangedListener(createWatcher(s -> {
            try { viewModel.latitude.setValue(Double.parseDouble(s)); } catch (Exception ignored) {}
        }));
        binding.etLng.addTextChangedListener(createWatcher(s -> {
            try { viewModel.longitude.setValue(Double.parseDouble(s)); } catch (Exception ignored) {}
        }));
    }

    private void setupCapacity() {
        viewModel.capacity.observe(getViewLifecycleOwner(), capacity -> {
            binding.tvCapacity.setText(String.valueOf(capacity));
        });

        binding.btnPlus.setOnClickListener(v -> {
            Integer current = viewModel.capacity.getValue();
            if (current == null) current = 1;
            viewModel.capacity.setValue(current + 1);
        });

        binding.btnMinus.setOnClickListener(v -> {
            Integer current = viewModel.capacity.getValue();
            if (current == null) current = 1;
            if (current > 1) {
                viewModel.capacity.setValue(current - 1);
            }
        });
    }

    private void setupAmenities() {
        for (int i = 0; i < binding.chipGroupAmenities.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupAmenities.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                List<String> current = viewModel.amenities.getValue();
                if (current == null) current = new ArrayList<>();
                String amenity = chip.getText().toString();
                if (isChecked) {
                    if (!current.contains(amenity)) current.add(amenity);
                } else {
                    current.remove(amenity);
                }
                viewModel.amenities.setValue(current);
            });
        }
    }

    private TextWatcher createWatcher(java.util.function.Consumer<String> consumer) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                consumer.accept(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
