package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatMessage implements Serializable {
    private String id;
    
    @SerializedName("room_id")
    private String roomId;
    
    @SerializedName("sender_id")
    private String senderId;
    
    @SerializedName("message_text")
    private String messageText;
    
    @SerializedName("is_read")
    private boolean isRead;
    
    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("image_url")
    private String imageUrl;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
