package com.example.tourgo.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tourgo.R;
import com.example.tourgo.interfaces.ApiErrorCode;
import com.example.tourgo.interfaces.DataCallback;
import com.example.tourgo.models.response.ChatRoom;
import com.example.tourgo.remote.service.ChatService;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView empty;
    private ChatRoomAdapter adapter;
    private List<ChatRoom> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_list);

        ImageView backBtn = findViewById(R.id.chatListBack);
        rv = findViewById(R.id.chatListRv);
        progress = findViewById(R.id.chatListProgress);
        empty = findViewById(R.id.chatListEmpty);

        backBtn.setOnClickListener(v -> finish());

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatRoomAdapter(this, rooms, this::openChatRoom);
        rv.setAdapter(adapter);

        loadChatRooms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatRooms();
    }

    private void loadChatRooms() {
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        ChatService.getRooms(this, new DataCallback<List<ChatRoom>>() {
            @Override
            public void onSuccess(List<ChatRoom> data) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    rooms.clear();
                    if (data != null && !data.isEmpty()) {
                        rooms.addAll(data);
                        adapter.notifyDataSetChanged();
                    } else {
                        empty.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(ApiErrorCode code, String msg) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    rooms.clear();
                    adapter.notifyDataSetChanged();
                    empty.setVisibility(View.VISIBLE);
                    Toast.makeText(ChatListActivity.this,
                            msg != null ? msg : getString(R.string.err_network), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openChatRoom(ChatRoom room) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chat_room", room);
        startActivity(intent);
    }

    // ── Adapter ───────────────────────────────────────────────────────────────
    private static class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.VH> {
        private final Context context;
        private final List<ChatRoom> items;
        private final Listener listener;

        interface Listener {
            void onClick(ChatRoom item);
        }

        ChatRoomAdapter(Context context, List<ChatRoom> items, Listener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ChatRoom it = items.get(position);
            h.name.setText(it.getPartnerName());

            boolean isPartnerBusiness = (it.getBusiness() != null);
            int defaultAvatar = isPartnerBusiness ? R.drawable.hotel_1 : R.drawable.ic_person_24;

            if (it.getPartnerAvatar() != null && !it.getPartnerAvatar().trim().isEmpty()) {
                h.avatar.setVisibility(View.VISIBLE);
                h.initialsAvatar.setVisibility(View.GONE);
                Glide.with(context)
                        .load(it.getPartnerAvatar())
                        .placeholder(defaultAvatar)
                        .error(defaultAvatar)
                        .into(h.avatar);
            } else {
                h.avatar.setVisibility(View.GONE);
                h.initialsAvatar.setVisibility(View.VISIBLE);
                com.example.tourgo.ui.admin.AdminUi.avatar(h.initialsAvatar, it.getPartnerName());
            }

            h.cardContainer.setOnClickListener(v -> {
                if (listener != null) listener.onClick(it);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final View cardContainer;
            final ImageView avatar;
            final TextView initialsAvatar;
            final TextView name;
            final TextView lastMsg;

            VH(@NonNull View v) {
                super(v);
                cardContainer = v.findViewById(R.id.chatRoomCardContainer);
                avatar = v.findViewById(R.id.chatRoomAvatar);
                initialsAvatar = v.findViewById(R.id.chatRoomInitialsAvatar);
                name = v.findViewById(R.id.chatRoomPartnerName);
                lastMsg = v.findViewById(R.id.chatRoomLastMessage);
            }
        }
    }
}
