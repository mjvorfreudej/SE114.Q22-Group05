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
import com.example.tourgo.databinding.FragmentStep3Binding;

public class Step3Fragment extends Fragment {

    private FragmentStep3Binding binding;
    private ListingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStep3Binding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(ListingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (viewModel.basePrice.getValue() != null && viewModel.basePrice.getValue() > 0) {
            binding.etBasePrice.setText(String.valueOf(viewModel.basePrice.getValue()));
        }

        binding.etBasePrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String val = s.toString();
                    if (!val.isEmpty()) {
                        viewModel.basePrice.setValue(Double.parseDouble(val));
                    }
                } catch (Exception ignored) {}
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.switchSeasonal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.isSeasonal.setValue(isChecked);
        });

        viewModel.isSeasonal.observe(getViewLifecycleOwner(), isChecked -> {
            if (binding.switchSeasonal.isChecked() != isChecked) {
                binding.switchSeasonal.setChecked(isChecked);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
