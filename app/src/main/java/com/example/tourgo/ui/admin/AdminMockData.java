package com.example.tourgo.ui.admin;

import com.example.tourgo.R;
import com.example.tourgo.models.response.AdminAccount;
import com.example.tourgo.models.response.BusinessAccount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * In-memory sample data for the Admin Console, mirroring the TourGo design-system
 * prototype (ui_kits/admin). The marketplace has no admin REST API in this app yet,
 * so these screens are wired to local state with optimistic updates + toasts — exactly
 * as the HTML prototype behaves. Record content (names, emails, report text) is kept
 * verbatim from the design and is intentionally not localized.
 */
public final class AdminMockData {

    private AdminMockData() {}

    // ── Pending listings (Moderation › Pending) ──────────────────────────────
    public static class PendingListing {
        public final int id;
        public final String business, name, cat, city, date, status, desc;
        public final int price, photoRes;
        public final List<String[]> history; // {at, note}

        // Populated when the listing comes from the backend (real pending tour);
        // null/0 for the legacy mock entries.
        public final String serverId;   // backend tour id, used by the approve API
        public final String imageUrl;   // network cover image (else use photoRes)
        public final String priceText;  // preformatted price (else "$" + price)

        public PendingListing(int id, String business, String name, String cat, String city,
                              String date, int price, int photoRes, String status, String desc,
                              List<String[]> history) {
            this.id = id; this.business = business; this.name = name; this.cat = cat;
            this.city = city; this.date = date; this.price = price; this.photoRes = photoRes;
            this.status = status; this.desc = desc; this.history = history;
            this.serverId = null; this.imageUrl = null; this.priceText = null;
        }

        private PendingListing(String serverId, String name, String city, String date,
                               String priceText, String imageUrl, String desc) {
            this.id = 0; this.business = ""; this.name = name; this.cat = "tour";
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
            return new PendingListing(
                    tour.getId(),
                    tour.getName(),
                    city,
                    date,
                    tour.getPriceString(ctx),
                    img,
                    tour.getDescription() != null ? tour.getDescription() : "");
        }
    }

    // ── User reports (Moderation › Reports) ──────────────────────────────────
    public static class UserReport {
        public final int id;
        public final String kind, reporter, target, when, body, reasoning, context, severity;

        public UserReport(int id, String kind, String reporter, String target, String when,
                          String body, String reasoning, String context, String severity) {
            this.id = id; this.kind = kind; this.reporter = reporter; this.target = target;
            this.when = when; this.body = body; this.reasoning = reasoning;
            this.context = context; this.severity = severity;
        }
    }

    // ── Business directory ───────────────────────────────────────────────────
    public static class BizAccount {
        public final int id;
        public final String serverId;      // backend id (null for legacy mock rows)
        public final String name, owner, joined;
        public final int listings, bookings;
        public boolean suspended;
        public String status;              // "pending" | "active" | "suspended"

        public BizAccount(int id, String name, String owner, int listings, int bookings,
                          boolean suspended, String joined) {
            this.id = id; this.serverId = null; this.name = name; this.owner = owner;
            this.listings = listings; this.bookings = bookings; this.suspended = suspended;
            this.joined = joined; this.status = suspended ? "suspended" : "active";
        }

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
            return new BizAccount(dto.getId(), dto.getName(), dto.getOwner(),
                    dto.getListings(), dto.getBookings(), dto.getStatus(), dateOnly(dto.getCreatedAt()));
        }
    }

    // ── Users directory ──────────────────────────────────────────────────────
    public static class AdminUser {
        public final int id;
        public final String serverId;      // backend id (null for legacy mock rows)
        public final String name, email, joined, tier, loc;
        public final int bookings, reported;
        public String status; // active | flagged | suspended

        public AdminUser(int id, String name, String email, String joined, String status,
                         int bookings, int reported, String tier, String loc) {
            this.id = id; this.serverId = null; this.name = name; this.email = email;
            this.joined = joined; this.status = status; this.bookings = bookings;
            this.reported = reported; this.tier = tier; this.loc = loc;
        }

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

    public static List<UserReport> userReports() {
        return new ArrayList<>(Arrays.asList(
                new UserReport(1, "Toxic language", "Cedar Cove LLC", "guest_jay_88", "6h",
                        "Worst stay ever. The staff are absolutely [redacted] and the place is a [redacted] dump.",
                        "Comment contains profanity and personal attacks against staff. Not constructive feedback.",
                        "Review of \"Cedar Cove Seaside Stay\"", "high"),
                new UserReport(2, "Spam / promotion", "The Grand Orchid Resort", "travel_deals_99", "2d",
                        "CHECK OUT MY DISCOUNT CODES at example-deals-dot-com — best prices anywhere, much cheaper.",
                        "Posting external promotional links, repeated across multiple listings.",
                        "Comment on listing review", "mid"),
                new UserReport(3, "Fake review", "Riverbend Tours Co.", "definitelynotabot", "3d",
                        "Five stars amazing tour really good fun experience would recommend",
                        "Same user has posted nearly-identical 5-star reviews across 14 unrelated listings in 2 days.",
                        "Multiple review submissions", "high"),
                new UserReport(4, "Harassment", "Skyline Marina", "angryuser_42", "4d",
                        "I know where you live, your business is going to burn",
                        "Direct threat made via DM after a disputed booking.",
                        "Direct message", "critical")
        ));
    }

}
