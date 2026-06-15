package com.example.tourgo.ui.business;

import com.example.tourgo.R;

import java.util.Arrays;
import java.util.List;

/**
 * In-memory sample data for the Business Console, recreated 1:1 from the TourGo
 * design-system prototype (ui_kits/business). The marketplace has no business REST API
 * in this app yet, so these screens render local state with optimistic updates + toasts —
 * exactly as the HTML prototype behaves. Record content (guest names, review and report
 * text) is kept verbatim from the design and is intentionally not localized.
 */
public final class BusinessMockData {

    private BusinessMockData() {}

    private static final int[] PHOTOS = {
            R.drawable.hotel_1, R.drawable.hotel_2, R.drawable.hotel_3, R.drawable.hotel_4
    };

    /** Cover photo for a 0-based index, wrapping over the 4 sample photos. */
    public static int photo(int i) {
        return PHOTOS[((i % 4) + 4) % 4];
    }

    // ── Listings (My Listings) ────────────────────────────────────────────────
    public static final class Listing {
        public final int id;
        public final String name, loc, cat, status;
        public final int price, photoIndex, bookings;
        public final double rating; // < 0 means "no rating yet"

        public Listing(int id, String name, String loc, int price, int photoIndex,
                       String status, String cat, int bookings, double rating) {
            this.id = id; this.name = name; this.loc = loc; this.price = price;
            this.photoIndex = photoIndex; this.status = status; this.cat = cat;
            this.bookings = bookings; this.rating = rating;
        }

        public boolean hasRating() { return rating >= 0; }
    }

    public static List<Listing> listings() {
        return Arrays.asList(
                new Listing(1, "The Grand Orchid Resort", "Bangkok", 199, 0, "active",   "hotel", 28, 4.8),
                new Listing(2, "Lavender Lakehouse Tour", "Kyoto",   78,  1, "inactive", "tour",  12, 4.6),
                new Listing(3, "Cedar Cove Seaside Stay", "Lisbon",  142, 2, "active",   "hotel", 14, 4.9),
                new Listing(4, "Sunset Dune Adventure",   "Dubai",   245, 3, "draft",    "tour",  0,  -1),
                new Listing(5, "Old Town Heritage Walk",  "Hanoi",   32,  1, "active",   "tour",  41, 4.7)
        );
    }

    // ── Recent bookings (Home) ────────────────────────────────────────────────
    public static final class RecentBooking {
        public final String guest, stay, dates, status;
        public final int photoIndex;

        public RecentBooking(String guest, String stay, String dates, String status, int photoIndex) {
            this.guest = guest; this.stay = stay; this.dates = dates;
            this.status = status; this.photoIndex = photoIndex;
        }
    }

    public static List<RecentBooking> recentBookings() {
        return Arrays.asList(
                new RecentBooking("Amelia R.", "The Grand Orchid Resort · Suite 2", "Aug 22 – Aug 25", "upcoming",  0),
                new RecentBooking("Liam K.",   "Cedar Cove Seaside Stay",           "Aug 18 – Aug 20", "completed", 2),
                new RecentBooking("Sara M.",   "Lavender Lakehouse Tour",           "Aug 15",          "completed", 1),
                new RecentBooking("Ben T.",    "The Grand Orchid Resort · Suite 1", "Aug 12 – Aug 14", "cancelled", 0)
        );
    }

    // ── Calendar bookings ─────────────────────────────────────────────────────
    public static final class CalBooking {
        public final int id, day;
        public final String guest, room, in, out, status, phone;

        public CalBooking(int id, String guest, String room, String in, String out,
                          int day, String status, String phone) {
            this.id = id; this.guest = guest; this.room = room; this.in = in;
            this.out = out; this.day = day; this.status = status; this.phone = phone;
        }
    }

    public static List<CalBooking> calBookings() {
        return Arrays.asList(
                new CalBooking(1, "Amelia R.", "Suite 2", "15:00", "11:00", 18, "checked-in", "+66 90 421 8829"),
                new CalBooking(2, "Liam K.",   "Room 14", "16:30", "10:00", 18, "pending",    "+66 81 233 9120"),
                new CalBooking(3, "Sara M.",   "Suite 1", "14:00", "12:00", 19, "confirmed",  "+44 7700 900022"),
                new CalBooking(4, "Ben T.",    "Room 9",  "13:00", "11:00", 19, "confirmed",  "+1 415 555 0144")
        );
    }

    /**
     * Aug 2025 day-by-day occupancy: "free" | "partial" | "full" | "blocked".
     * Deterministic, matching the prototype's pseudo-random map with fixed overrides.
     */
    public static String occupancy(int day) {
        if (day == 18) return "full";
        if (day == 19) return "full";
        if (day == 20) return "partial";
        int r = (day * 73 + 17) % 100;
        if (r < 12) return "blocked";
        if (r < 30) return "free";
        if (r < 70) return "partial";
        return "full";
    }

    // ── Reviews ───────────────────────────────────────────────────────────────
    public static final class Review {
        public final int id, rating;
        public final String name, when, body, listing, replyText;
        public final boolean replied;

        public Review(int id, String name, int rating, String when, String body,
                      String listing, boolean replied, String replyText) {
            this.id = id; this.name = name; this.rating = rating; this.when = when;
            this.body = body; this.listing = listing; this.replied = replied;
            this.replyText = replyText;
        }
    }

    public static final String DEFAULT_REPLY =
            "Thanks so much! We're delighted you enjoyed your stay. Hope to host you again soon.";

    public static List<Review> reviews() {
        return Arrays.asList(
                new Review(1, "Amelia R.", 5, "2 days ago",
                        "Beautiful property and warm staff. The breakfast spread was incredible. Will definitely return!",
                        "The Grand Orchid Resort", true, DEFAULT_REPLY),
                new Review(2, "Liam K.", 4, "5 days ago",
                        "Clean rooms and great location. Wi-Fi was patchy on the second night but everything else was perfect.",
                        "The Grand Orchid Resort", false, null),
                new Review(3, "Sara M.", 5, "1 week ago",
                        "Exceeded all our expectations. The view from the suite was unreal.",
                        "Cedar Cove Seaside Stay", false, null),
                new Review(4, "Ben T.", 3, "2 weeks ago",
                        "Decent stay overall. Could improve on the room service speed.",
                        "The Grand Orchid Resort", true, DEFAULT_REPLY)
        );
    }

    /** Rating distribution rows for the summary bar: {stars, count, fractionOfMax}. */
    public static final class RatingBar {
        public final int stars, count;
        public final float pct;

        public RatingBar(int stars, int count, float pct) {
            this.stars = stars; this.count = count; this.pct = pct;
        }
    }

    public static final String OVERALL_RATING = "4.8";
    public static final int TOTAL_REVIEWS = 36;

    public static List<RatingBar> ratingDistribution() {
        return Arrays.asList(
                new RatingBar(5, 24, 1.00f),
                new RatingBar(4, 8,  0.33f),
                new RatingBar(3, 2,  0.09f),
                new RatingBar(2, 1,  0.05f),
                new RatingBar(1, 1,  0.05f)
        );
    }

    // ── Reports ───────────────────────────────────────────────────────────────
    public static final class Report {
        public final int id;
        public final String kind, reporter, targetUser, when, body, reasoning;

        public Report(int id, String kind, String reporter, String targetUser,
                      String when, String body, String reasoning) {
            this.id = id; this.kind = kind; this.reporter = reporter;
            this.targetUser = targetUser; this.when = when; this.body = body;
            this.reasoning = reasoning;
        }
    }

    public static List<Report> reports() {
        return Arrays.asList(
                new Report(1, "Toxic language", "Cedar Cove LLC", "guest_jay_88", "6 hours ago",
                        "Worst stay ever. The staff are absolutely [redacted] and the place is a [redacted] dump.",
                        "Comment contains profanity and personal attacks against staff. Not constructive feedback."),
                new Report(2, "Spam / off-topic", "The Grand Orchid Resort", "travel_deals_99", "2 days ago",
                        "CHECK OUT MY DISCOUNT CODES at example-deals-dot-com — best prices anywhere, much cheaper than this hotel.",
                        "Posting external promotional links, repeated across multiple listings.")
        );
    }
}
