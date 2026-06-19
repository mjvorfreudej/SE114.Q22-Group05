package com.example.tourgo.ui.main.profile;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.data.repository.UserRepository;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.ApiResponse;
import com.example.tourgo.models.response.UploadImageResponse;
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.RetrofitClient;
import com.example.tourgo.remote.service.UserService;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivEditAvatar;
    private TextInputEditText etEditName, etEditPhone;
    private View loadingOverlay;
    private Uri selectedImageUri;
    private SessionManager session;
    private User currentUser;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(ivEditAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Handle System Bar Insets to prevent the Save button from being hidden
        View root = findViewById(R.id.editProfileRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, bars.top, 0, 0);
                return insets;
            });
        }

        session = new SessionManager(this);
        currentUser = UserRepository.getInstance().getCachedUser();

        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        etEditName = findViewById(R.id.etEditName);
        etEditPhone = findViewById(R.id.etEditPhone);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        findViewById(R.id.btnEditProfileBack).setOnClickListener(v -> finish());
        findViewById(R.id.layoutAvatarEdit).setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> attemptSave());

        if (currentUser != null) {
            etEditName.setText(currentUser.getName());
            etEditPhone.setText(currentUser.getPhone());
            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                Glide.with(this).load(currentUser.getAvatar()).circleCrop().into(ivEditAvatar);
            }
        } else {
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        setLoading(true);
        UserService.getCurrentUser(this, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                currentUser = user;
                UserRepository.getInstance().updateCachedUser(user);
                etEditName.setText(user.getName());
                etEditPhone.setText(user.getPhone());
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(EditProfileActivity.this).load(user.getAvatar()).circleCrop().into(ivEditAvatar);
                }
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptSave() {
        String name = etEditName.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.err_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (selectedImageUri != null) {
            uploadAvatarThenUpdateProfile(name, phone);
        } else {
            updateProfile(name, phone, currentUser != null ? currentUser.getAvatar() : null);
        }
    }

    private void uploadAvatarThenUpdateProfile(String name, String phone) {
        try {
            InputStream is = getContentResolver().openInputStream(selectedImageUri);
            if (is == null) {
                setLoading(false);
                return;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();
            is.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", "avatar.jpg", requestFile);

            RetrofitClient.getInstance(this).getUserApi().uploadAvatar(body).enqueue(new Callback<ApiResponse<UploadImageResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<UploadImageResponse>> call, Response<ApiResponse<UploadImageResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        String imageUrl = response.body().getData().getImageUrl();
                        updateProfile(name, phone, imageUrl);
                    } else {
                        setLoading(false);
                        Toast.makeText(EditProfileActivity.this, R.string.edprofile_update_error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UploadImageResponse>> call, Throwable t) {
                    setLoading(false);
                    Toast.makeText(EditProfileActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfile(String name, String phone, String avatar) {
        UserService.updateProfile(this, name, phone, avatar, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                UserRepository.getInstance().updateCachedUser(user);
                session.saveUserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole());
                Toast.makeText(EditProfileActivity.this, R.string.edprofile_update_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(ApiErrorCode code, String message) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
