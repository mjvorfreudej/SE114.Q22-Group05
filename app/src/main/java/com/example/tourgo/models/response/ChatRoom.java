package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChatRoom implements Serializable {
    private String id;
    
    @SerializedName("user_id")
    private String userId;
    
    @SerializedName("business_id")
    private String businessId;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("partner_name")
    private String partnerName;
    
    @SerializedName("partner_avatar")
    private String partnerAvatar;
    
    private BusinessInfo business;

    public static class BusinessInfo implements Serializable {
        private String id;
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getPartnerName() { 
        if (partnerName != null) return partnerName;
        if (business != null) return business.getName();
        return "Đối tác";
    }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getPartnerAvatar() { return partnerAvatar; }
    public void setPartnerAvatar(String partnerAvatar) { this.partnerAvatar = partnerAvatar; }

    public BusinessInfo getBusiness() { return business; }
    public void setBusiness(BusinessInfo business) { this.business = business; }
}
