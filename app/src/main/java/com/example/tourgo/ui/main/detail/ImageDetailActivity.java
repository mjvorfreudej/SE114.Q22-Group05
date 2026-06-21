package com.example.tourgo.ui.main.detail;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tourgo.R;
import com.example.tourgo.adapters.ImageDetailAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageDetailActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGES = "extra_images";
    public static final String EXTRA_POSITION = "extra_position";

    private ViewPager2 viewPager;
    private TextView tvCounter;
    private ImageButton btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        viewPager = findViewById(R.id.viewPager);
        tvCounter = findViewById(R.id.tvCounter);
        btnClose = findViewById(R.id.btnClose);

        List<String> extraImageUrls = getIntent().getStringArrayListExtra(EXTRA_IMAGES);
        final List<String> imageUrls = (extraImageUrls != null) ? extraImageUrls : new ArrayList<>();
        int startPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);

        ImageDetailAdapter adapter = new ImageDetailAdapter(imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition, false);

        updateCounter(startPosition, imageUrls.size());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateCounter(position, imageUrls.size());
            }
        });

        btnClose.setOnClickListener(v -> finish());
    }

    private void updateCounter(int position, int total) {
        tvCounter.setText(String.format("%d / %d", position + 1, total));
    }
}
