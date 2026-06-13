package com.example.tourgo.models;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Availability implements Serializable {
    private String id;
    private String listingId;
    private String listingType; // "TOUR" or "HOTEL"
    private String date; // YYYY-MM-DD
    private boolean isBlocked;

    public Availability() {}

    public Availability(String listingId, String listingType, String date, boolean isBlocked) {
        this.listingId = listingId;
        this.listingType = listingType;
        this.date = date;
        this.isBlocked = isBlocked;
    }

    public static Availability fromJson(JSONObject json) {
        Availability a = new Availability();
        a.id = json.optString("id", null);
        a.listingId = json.optString("listing_id", null);
        a.listingType = json.optString("listing_type", null);
        a.date = json.optString("date", null);
        a.isBlocked = json.optBoolean("is_blocked", false);
        return a;
    }

    public static List<Availability> fromJsonArray(JSONArray array) {
        List<Availability> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) list.add(fromJson(obj));
            }
        }
        return list;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
}
