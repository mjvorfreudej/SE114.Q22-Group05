package com.example.tourgo.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.data.local.SessionManager;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.ChatMessage;
import com.example.tourgo.models.response.ChatRoom;
import com.example.tourgo.remote.service.ChatService;

import java.util.ArrayList;
import java.util.List;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatActivity extends AppCompatActivity {

    private ChatRoom chatRoom;
    private SessionManager session;
    private RecyclerView rv;
    private ProgressBar progress;
    private EditText inputEdt;
    private ImageView sendBtn;
    private TextView partnerNameTv;
    private ImageView partnerAvatarIv;
    private TextView partnerInitialsAvatarTv;

    private MessageAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();

    private ImageView attachBtn;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages(false);
            pollHandler.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            var sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            var ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            // Top: clear the status bar / camera notch. Bottom: keep the input above
            // the keyboard when open, else above the navigation bar.
            v.setPadding(0, sys.top, 0, Math.max(ime.bottom, sys.bottom));
            return insets;
        });

        chatRoom = (ChatRoom) getIntent().getSerializableExtra("chat_room");
        if (chatRoom == null) {
            finish();
            return;
        }

        session = new SessionManager(this);

        ImageView backBtn = findViewById(R.id.chatBack);
        partnerNameTv = findViewById(R.id.chatPartnerName);
        partnerAvatarIv = findViewById(R.id.chatPartnerAvatar);
        partnerInitialsAvatarTv = findViewById(R.id.chatPartnerInitialsAvatar);
        rv = findViewById(R.id.chatMessagesRv);
        progress = findViewById(R.id.chatProgress);
        inputEdt = findViewById(R.id.chatInputEdt);
        sendBtn = findViewById(R.id.chatSendBtn);

        backBtn.setOnClickListener(v -> finish());
        sendBtn.setOnClickListener(v -> sendMessage());

        attachBtn = findViewById(R.id.chatAttachBtn);
        attachBtn.setOnClickListener(v -> openImagePicker());
        setupImagePicker();

        partnerNameTv.setText(chatRoom.getPartnerName());
        boolean isPartnerBusiness = (chatRoom.getBusiness() != null);
        int defaultAvatar = isPartnerBusiness ? R.drawable.hotel_1 : R.drawable.ic_person_24;

        if (chatRoom.getPartnerAvatar() != null && !chatRoom.getPartnerAvatar().trim().isEmpty()) {
            partnerAvatarIv.setVisibility(View.VISIBLE);
            partnerInitialsAvatarTv.setVisibility(View.GONE);
            Glide.with(this)
                    .load(chatRoom.getPartnerAvatar())
                    .placeholder(defaultAvatar)
                    .error(defaultAvatar)
                    .into(partnerAvatarIv);
        } else {
            partnerAvatarIv.setVisibility(View.GONE);
            partnerInitialsAvatarTv.setVisibility(View.VISIBLE);
            com.example.tourgo.ui.admin.AdminUi.avatar(partnerInitialsAvatarTv, chatRoom.getPartnerName());
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this, messages, session.getUserId(), chatRoom);
        rv.setAdapter(adapter);

        loadMessages(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start 3-second short polling
        pollHandler.postDelayed(pollRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop polling to save battery and data
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void loadMessages(boolean showLoading) {
        if (showLoading) progress.setVisibility(View.VISIBLE);

        ChatService.getMessages(this, chatRoom.getId(), new DataCallback<List<ChatMessage>>() {
            @Override
            public void onSuccess(List<ChatMessage> data) {
                runOnUiThread(() -> {
                    if (showLoading) progress.setVisibility(View.GONE);
                    if (data != null) {
                        // Mark this room read up to its newest message so the Detail
                        // chat badge clears (backend tracks no chat read-state).
                        String latest = null;
                        for (ChatMessage m : data) {
                            String at = m.getCreatedAt();
                            if (at != null && (latest == null || at.compareTo(latest) > 0)) {
                                latest = at;
                            }
                        }
                        if (latest != null) {
                            ChatReadStore.markRead(ChatActivity.this, chatRoom.getId(), latest);
                        }

                        // Only update and scroll if the list length has changed
                        int oldSize = messages.size();
                        if (data.size() != oldSize) {
                            messages.clear();
                            messages.addAll(data);
                            adapter.notifyDataSetChanged();
                            if (!messages.isEmpty()) {
                                rv.scrollToPosition(messages.size() - 1);
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    if (showLoading) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(ChatActivity.this,
                                msg != null ? msg : getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendMessage() {
        String text = inputEdt.getText().toString().trim();
        if (text.isEmpty()) return;

        inputEdt.setText(""); // clear immediately for responsive feel
        ChatService.sendMessage(this, chatRoom.getId(), text, new DataCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage message) {
                runOnUiThread(() -> {
                    // Force refresh messages instantly
                    loadMessages(false);
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    inputEdt.setText(text); // restore text on failure
                    Toast.makeText(ChatActivity.this,
                            msg != null ? msg : "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(intent, "Chọn ảnh để gửi"));
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri imageUri = result.getData().getData();
                sendImageMessage(imageUri);
            }
        });
    }

    private void sendImageMessage(Uri imageUri) {
        progress.setVisibility(View.VISIBLE);
        ChatService.sendImageMessage(this, chatRoom.getId(), imageUri, new DataCallback<ChatMessage>() {
            @Override
            public void onSuccess(ChatMessage message) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    loadMessages(false);
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(ChatActivity.this,
                            msg != null ? msg : "Gửi ảnh thất bại", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // ── Adapter ───────────────────────────────────────────────────────────────
    private static class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_SENT = 1;
        private static final int TYPE_RECEIVED = 2;

        private final Context context;
        private final List<ChatMessage> items;
        private final String currentUserId;
        private final ChatRoom chatRoom;

        MessageAdapter(Context context, List<ChatMessage> items, String currentUserId, ChatRoom chatRoom) {
            this.context = context;
            this.items = items;
            this.currentUserId = currentUserId;
            this.chatRoom = chatRoom;
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage msg = items.get(position);
            if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
                return TYPE_SENT;
            } else {
                return TYPE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SENT) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_chat_message_sent, parent, false);
                return new SentVH(v);
            } else {
                View v = LayoutInflater.from(context).inflate(R.layout.item_chat_message_received, parent, false);
                return new ReceivedVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage msg = items.get(position);
            boolean hasImage = msg.getImageUrl() != null && !msg.getImageUrl().trim().isEmpty();

            if (holder instanceof SentVH) {
                SentVH h = (SentVH) holder;
                if (hasImage) {
                    h.bubble.setVisibility(View.GONE);
                    h.image.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(msg.getImageUrl())
                            .placeholder(R.drawable.bg_biz_gray_box)
                            .error(R.drawable.bg_biz_gray_box)
                            .into(h.image);
                } else {
                    h.bubble.setVisibility(View.VISIBLE);
                    h.image.setVisibility(View.GONE);
                    h.text.setText(msg.getMessageText());
                }
            } else if (holder instanceof ReceivedVH) {
                ReceivedVH h = (ReceivedVH) holder;
                if (hasImage) {
                    h.bubble.setVisibility(View.GONE);
                    h.image.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(msg.getImageUrl())
                            .placeholder(R.drawable.bg_biz_gray_box)
                            .error(R.drawable.bg_biz_gray_box)
                            .into(h.image);
                } else {
                    h.bubble.setVisibility(View.VISIBLE);
                    h.image.setVisibility(View.GONE);
                    h.text.setText(msg.getMessageText());
                }

                // Bind sender avatar for received message
                if (chatRoom != null) {
                    boolean isPartnerBusiness = (chatRoom.getBusiness() != null);
                    int defaultAvatar = isPartnerBusiness ? R.drawable.hotel_1 : R.drawable.ic_person_24;

                    if (chatRoom.getPartnerAvatar() != null && !chatRoom.getPartnerAvatar().trim().isEmpty()) {
                        h.avatar.setVisibility(View.VISIBLE);
                        h.initialsAvatar.setVisibility(View.GONE);
                        Glide.with(context)
                                .load(chatRoom.getPartnerAvatar())
                                .placeholder(defaultAvatar)
                                .error(defaultAvatar)
                                .into(h.avatar);
                    } else {
                        h.avatar.setVisibility(View.GONE);
                        h.initialsAvatar.setVisibility(View.VISIBLE);
                        com.example.tourgo.ui.admin.AdminUi.avatar(h.initialsAvatar, chatRoom.getPartnerName());
                    }
                } else {
                    h.avatar.setVisibility(View.GONE);
                    h.initialsAvatar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class SentVH extends RecyclerView.ViewHolder {
            final View bubble;
            final TextView text;
            final ImageView image;
            SentVH(@NonNull View v) {
                super(v);
                bubble = v.findViewById(R.id.chatMessageBubble);
                text = v.findViewById(R.id.chatMessageText);
                image = v.findViewById(R.id.chatMessageImage);
            }
        }

        static class ReceivedVH extends RecyclerView.ViewHolder {
            final View bubble;
            final TextView text;
            final ImageView image;
            final ImageView avatar;
            final TextView initialsAvatar;
            ReceivedVH(@NonNull View v) {
                super(v);
                bubble = v.findViewById(R.id.chatMessageBubble);
                text = v.findViewById(R.id.chatMessageText);
                image = v.findViewById(R.id.chatMessageImage);
                avatar = v.findViewById(R.id.chatMessageSenderAvatar);
                initialsAvatar = v.findViewById(R.id.chatMessageSenderInitialsAvatar);
            }
        }
    }
}
