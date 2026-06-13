package com.example.tourgo.ui.listing;

import android.net.Uri;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ListingViewModel extends ViewModel {
    // Step 1
    public MutableLiveData<String> type = new MutableLiveData<>("Hotel");
    public MutableLiveData<String> name = new MutableLiveData<>("");
    public MutableLiveData<String> description = new MutableLiveData<>("");
    public MutableLiveData<List<Uri>> selectedImages = new MutableLiveData<>(new ArrayList<>());
    
    // Step 2
    public MutableLiveData<String> address = new MutableLiveData<>("");
    public MutableLiveData<Double> latitude = new MutableLiveData<>(0.0);
    public MutableLiveData<Double> longitude = new MutableLiveData<>(0.0);
    public MutableLiveData<List<String>> amenities = new MutableLiveData<>(new ArrayList<>());
    
    // Step 3
    public MutableLiveData<Double> basePrice = new MutableLiveData<>(0.0);
    public MutableLiveData<Integer> capacity = new MutableLiveData<>(1);
    public MutableLiveData<Boolean> isSeasonal = new MutableLiveData<>(false);
    public MutableLiveData<Double> cleaningFee = new MutableLiveData<>(25.0);
    public MutableLiveData<Double> lateCheckOutFee = new MutableLiveData<>(35.0);
    public MutableLiveData<Double> extraGuestFee = new MutableLiveData<>(15.0);
    public MutableLiveData<String> region = new MutableLiveData<>("");
    public MutableLiveData<String> duration = new MutableLiveData<>("");
    public MutableLiveData<String> status = new MutableLiveData<>("PENDING");

    // Step 4
    public MutableLiveData<String> openFrom = new MutableLiveData<>("");
    public MutableLiveData<String> openUntil = new MutableLiveData<>("");
    public MutableLiveData<List<String>> blockedDates = new MutableLiveData<>(new ArrayList<>());
    
    public void addImage(Uri uri) {
        List<Uri> current = selectedImages.getValue();
        if (current != null && current.size() < 6) {
            current.add(uri);
            selectedImages.setValue(current);
        }
    }

    public void removeImage(int index) {
        List<Uri> current = selectedImages.getValue();
        if (current != null && index < current.size()) {
            current.remove(index);
            selectedImages.setValue(current);
        }
    }
}
