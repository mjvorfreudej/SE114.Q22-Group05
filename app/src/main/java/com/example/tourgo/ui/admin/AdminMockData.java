package com.example.tourgo.ui.admin;

import com.example.tourgo.R;
import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.BusinessAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * Display models + backend-mapping factories for the Admin Console. Every screen
 * is wired to the live admin REST API (see {@link com.example.tourgo.remote.service.AdminService}
 * and {@link com.example.tourgo.remote.service.TourService}); each model below is
 * built from a server DTO via its {@code fromServer}/{@code fromTour} factory.
 */
public final class AdminMockData {

    private AdminMockData() {}

    // ── Pending listings (Moderation › Pending) ──────────────────────────────
    public static class PendingListing implements java.io.Serializable {
        public final int id;
        public final String business, name, cat, city, date, status, desc;
        public final int price, photoRes;
        public final List<String[]> history; // {at, note}

        public final String serverId;   // backend tour id, used by the approve API
        public final String imageUrl;   // network cover image (else use photoRes)
        public final String priceText;  // preformatted price (else "$" + price)

        public com.example.tourgo.models.response.Tour originalTour;
        public com.example.tourgo.models.response.Hotel originalHotel;

        private PendingListing(String serverId, String name, String city, String date,
                               String priceText, String imageUrl, String desc, String cat) {
            this.id = 0; this.business = ""; this.name = name; this.cat = cat;
            this.city = city; this.date = date; this.price = 0; this.photoRes = R.drawable.hotel_1;
            this.status = "pending"; this.desc = desc; this.history = new ArrayList<>();
            this.serverId = serverId; this.imageUrl = imageUrl; this.priceText = priceText;
        }

        /** Map a backend tour into the moderation display model. */
        public static PendingListing fromTour(android.content.Context ctx,
                                              com.example.tourgo.models.response.Tour tour) {
            List<String> imgs = tour.getImageUrls();
            String img = (imgs != null && !imgs.isEmpty()) ? imgs.get(0) : null;
            String city = tour.getLocation() != null ? tour.getLocation() : "";
            String created = tour.getCreatedAt();
            String date = (created != null && created.length() >= 10) ? created.substring(0, 10) : "";
            PendingListing pl = new PendingListing(
                    tour.getId(),
                    tour.getName(),
                    city,
                    date,
                    tour.getPriceString(ctx),
                    img,
                    tour.getDescription() != null ? tour.getDescription() : "",
                    "tour");
            pl.originalTour = tour;
            return pl;
        }

        /** Map a backend hotel into the moderation display model. */
        public static PendingListing fromHotel(android.content.Context ctx,
                                               com.example.tourgo.models.response.Hotel hotel) {
            String img = null;
            if (hotel.getHotelImages() != null && !hotel.getHotelImages().isEmpty()) {
                img = hotel.getHotelImages().get(0).getImageUrl();
            }
            String address = hotel.getAddress() != null ? hotel.getAddress() : "";
            String city = address;
            if (address.contains(",")) {
                city = address.substring(address.lastIndexOf(",") + 1).trim();
            }
            String created = hotel.getCreatedAt();
            String date = (created != null && created.length() >= 10) ? created.substring(0, 10) : "";
            String priceText = String.format(java.util.Locale.getDefault(), "%,.0f đ", hotel.getPricePerNight());
            PendingListing pl = new PendingListing(
                    hotel.getId(),
                    hotel.getName(),
                    city,
                    date,
                    priceText,
                    img,
                    hotel.getDescription() != null ? hotel.getDescription() : "",
                    "hotel");
            pl.originalHotel = hotel;
            return pl;
        }
    }

    // ── User reports (Moderation › Reports) ──────────────────────────────────
    public static class UserReport {
        public final String serverId;     // backend report id (used by dismiss/resolve)
        public final String kind, reporter, target, when, body, reasoning, context, severity;

        private UserReport(String serverId, String kind, String reporter, String target, String when,
                           String body, String reasoning, String context, String severity) {
            this.serverId = serverId;
            this.kind = kind != null ? kind : "";
            this.reporter = reporter != null ? reporter : "";
            this.target = target != null ? target : "";
            this.when = when != null ? when : "";
            this.body = body != null ? body : "";
            this.reasoning = reasoning != null ? reasoning : "";
            this.context = context != null ? context : "";
            this.severity = severity != null ? severity : "mid";
        }

        /** Map a backend report into the moderation display model. */
        public static UserReport fromServer(com.example.tourgo.models.response.AdminReport dto) {
            return new UserReport(dto.getId(), dto.getType(), dto.getReporter(), dto.getTarget(),
                    relativeShort(dto.getCreatedAt()), dto.getBody(), dto.getReasoning(),
                    dto.getContext(), dto.getSeverity());
        }
    }

    /** Short relative time ("now", "6h", "3d") from an ISO timestamp; "" on failure. */
    static String relativeShort(String iso) {
        if (iso == null || iso.length() < 19) return "";
        try {
            java.text.SimpleDateFormat f =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            long then = f.parse(iso.substring(0, 19)).getTime();
            long diff = Math.max(0, System.currentTimeMillis() - then);
            long min = diff / 60000;
            if (min < 1) return "now";
            if (min < 60) return min + "m";
            long hr = min / 60;
            if (hr < 24) return hr + "h";
            return (hr / 24) + "d";
        } catch (Exception e) {
            return "";
        }
    }

    // ── Business directory ───────────────────────────────────────────────────
    public static class BizAccount {
        public final int id;
        public final String serverId;      // backend business id
        public final String name, owner, joined;
        public final int listings, bookings;
        public boolean suspended;
        public String status;              // "pending" | "active" | "suspended"

        private BizAccount(String serverId, String name, String owner, int listings, int bookings,
                           String status, String joined) {
            this.id = 0; this.serverId = serverId;
            this.name = name != null ? name : "";
            this.owner = owner != null ? owner : "";
            this.listings = listings; this.bookings = bookings;
            this.status = status != null ? status : "pending";
            this.suspended = "suspended".equals(this.status);
            this.joined = joined != null ? joined : "";
        }

        /** Map a backend business account into the directory display model. */
        public static BizAccount fromServer(BusinessAccount dto) {
            // The directory's subtitle/search use "owner"; fall back to the
            // contact email when the server doesn't supply an owner name.
            String owner = dto.getOwner() != null && !dto.getOwner().isEmpty()
                    ? dto.getOwner() : dto.getEmail();
            return new BizAccount(dto.getId(), dto.getName(), owner,
                    dto.getListings(), dto.getBookings(), dto.getStatus(), dateOnly(dto.getCreatedAt()));
        }
    }

    // ── Users directory ──────────────────────────────────────────────────────
    public static class AdminUser {
        public final int id;
        public final String serverId;      // backend user id
        public final String name, email, joined, tier, loc;
        public final int bookings, reported;
        public String status; // active | flagged | suspended

        private AdminUser(String serverId, String name, String email, String joined, String status,
                          int bookings, int reported, String tier, String loc) {
            this.id = 0; this.serverId = serverId;
            this.name = name != null ? name : "";
            this.email = email != null ? email : "";
            this.joined = joined != null ? joined : "";
            this.status = status != null ? status : "active";
            this.bookings = bookings; this.reported = reported;
            this.tier = tier != null ? tier : "—";
            this.loc = loc != null ? loc : "—";
        }

        /** Map a backend user account into the users-directory display model. */
        public static AdminUser fromServer(AdminAccount dto) {
            return new AdminUser(dto.getId(), dto.getName(), dto.getEmail(), dateOnly(dto.getCreatedAt()),
                    dto.getStatus(), dto.getBookings(), dto.getReported(), dto.getTier(), dto.getLocation());
        }
    }

    /** Trim an ISO timestamp down to its date portion (yyyy-MM-dd) for display. */
    static String dateOnly(String iso) {
        if (iso == null) return "";
        return iso.length() >= 10 ? iso.substring(0, 10) : iso;
    }

}
