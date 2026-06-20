package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Global moderation policy, GET/PUT /api/admin/settings/moderation. Serialized
 * both ways (Gson reads the {@code @SerializedName} keys for the request body too).
 */
public class ModerationPolicy {
    @SerializedName("auto_hide")
    private boolean autoHide;
    @SerializedName("profanity")
    private boolean profanity;
    @SerializedName("photo_review")
    private boolean photoReview;
    @SerializedName("geo_block")
    private boolean geoBlock;
    @SerializedName("hide_at")
    private int hideAt;
    @SerializedName("sla_hours")
    private int slaHours;
    @SerializedName("terms")
    private List<String> terms;

    public ModerationPolicy() {
    }

    public ModerationPolicy(boolean autoHide, boolean profanity, boolean photoReview, boolean geoBlock,
                            int hideAt, int slaHours, List<String> terms) {
        this.autoHide = autoHide;
        this.profanity = profanity;
        this.photoReview = photoReview;
        this.geoBlock = geoBlock;
        this.hideAt = hideAt;
        this.slaHours = slaHours;
        this.terms = terms;
    }

    public boolean isAutoHide() { return autoHide; }
    public boolean isProfanity() { return profanity; }
    public boolean isPhotoReview() { return photoReview; }
    public boolean isGeoBlock() { return geoBlock; }
    public int getHideAt() { return hideAt; }
    public int getSlaHours() { return slaHours; }
    public List<String> getTerms() { return terms != null ? terms : new ArrayList<>(); }
}
