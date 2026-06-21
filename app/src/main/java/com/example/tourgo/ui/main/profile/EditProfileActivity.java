package com.example.tourgo.ui.main.profile;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.tourgo.models.response.User;
import com.example.tourgo.remote.service.UserService;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivEditAvatar;
    private TextInputEditText etEditName, etEditPhone;
    private View loadingOverlay;
    private Uri selectedImageUri;
    private SessionManager session;
    private User currentUser;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEditAvatar.setImageTintList(null); // Clear placeholder tint
                    Glide.with(this).load(uri).circleCrop().into(ivEditAvatar);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

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
            bindUserData(currentUser);
        } else {
            loadUserProfile();
        }
    }

    private void bindUserData(User user) {
        etEditName.setText(user.getName());
        etEditPhone.setText(user.getPhone());
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            ivEditAvatar.setImageTintList(null);
            Glide.with(this).load(user.getAvatar()).circleCrop().into(ivEditAvatar);
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
                bindUserData(user);
                session.saveUserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getAvatar());
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
            updateProfileMultipart(name, phone);
        } else {
            updateProfile(name, phone, currentUser != null ? currentUser.getAvatar() : null);
        }
    }

    private void updateProfileMultipart(String name, String phone) {
        executor.execute(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                if (is == null) {
                    mainHandler.post(() -> {
                        setLoading(false);
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                byte[] bytes = byteBuffer.toByteArray();
                is.close();

                String mimeType = getContentResolver().getType(selectedImageUri);
                if (mimeType == null) mimeType = "image/jpeg";

                // RequestBody.create(content, contentType) for OkHttp 4.x
                RequestBody imageBody = RequestBody.create(bytes, MediaType.parse(mimeType));
                // Field name MUST be "file" to match backend upload.single('file')
                MultipartBody.Part file = MultipartBody.Part.createFormData("avatar", "profile_image.jpg", imageBody);

                // Use MultipartBody.FORM for text parts as requested
                RequestBody namePart = RequestBody.create(name, MultipartBody.FORM);
                RequestBody phonePart = RequestBody.create(phone, MultipartBody.FORM);

                // Debug: Check headers to confirm name="file"
                Log.d("IMG", file.headers().toString());

                mainHandler.post(() -> {
                    UserService.updateProfileMultipart(this, namePart, phonePart, file, new DataCallback<User>() {
                        @Override
                        public void onSuccess(User user) {
                            setLoading(false);
                            UserRepository.getInstance().updateCachedUser(user);
                            session.saveUserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getAvatar());
                            Toast.makeText(EditProfileActivity.this, R.string.edprofile_update_success, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(ApiErrorCode code, String message) {
                            setLoading(false);
                            Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateProfile(String name, String phone, String avatar) {
        UserService.updateProfile(this, name, phone, avatar, new DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                UserRepository.getInstance().updateCachedUser(user);
                session.saveUserInfo(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getAvatar());
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
