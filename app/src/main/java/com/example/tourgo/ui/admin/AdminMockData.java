package com.example.tourgo.ui.admin;

import com.example.tourgo.R;

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

        public PendingListing(int id, String business, String name, String cat, String city,
                              String date, int price, int photoRes, String status, String desc,
                              List<String[]> history) {
            this.id = id; this.business = business; this.name = name; this.cat = cat;
            this.city = city; this.date = date; this.price = price; this.photoRes = photoRes;
            this.status = status; this.desc = desc; this.history = history;
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
        public final String name, owner, joined;
        public final int listings, bookings;
        public boolean suspended;

        public BizAccount(int id, String name, String owner, int listings, int bookings,
                          boolean suspended, String joined) {
            this.id = id; this.name = name; this.owner = owner; this.listings = listings;
            this.bookings = bookings; this.suspended = suspended; this.joined = joined;
        }
    }

    // ── Users directory ──────────────────────────────────────────────────────
    public static class AdminUser {
        public final int id;
        public final String name, email, joined, tier, loc;
        public final int bookings, reported;
        public String status; // active | flagged | suspended

        public AdminUser(int id, String name, String email, String joined, String status,
                         int bookings, int reported, String tier, String loc) {
            this.id = id; this.name = name; this.email = email; this.joined = joined;
            this.status = status; this.bookings = bookings; this.reported = reported;
            this.tier = tier; this.loc = loc;
        }
    }

    private static List<String[]> history(String... pairs) {
        List<String[]> out = new ArrayList<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) out.add(new String[]{pairs[i], pairs[i + 1]});
        return out;
    }

    public static List<PendingListing> pendingListings() {
        return new ArrayList<>(Arrays.asList(
                new PendingListing(1, "Orchid Hospitality", "The Grand Orchid Resort", "hotel", "Bangkok, TH", "Aug 17", 199, R.drawable.hotel_1, "pending",
                        "A boutique riverside hotel with handcrafted suites, rooftop pool and concierge service.", history()),
                new PendingListing(2, "Riverbend Tours Co.", "Cherry Blossom Walk", "tour", "Kyoto, JP", "Aug 17", 78, R.drawable.hotel_2, "pending",
                        "A guided 3-hour walk through Kyoto's most beautiful blossom alleys.", history("Aug 16", "Revision: clarified meeting point.")),
                new PendingListing(3, "Skyline Marina", "Sunset Yacht Charter", "tour", "Singapore", "Aug 16", 320, R.drawable.hotel_3, "revision",
                        "Private yacht charter at sunset, 4-hour cruise with light dinner.", history("Aug 14", "Rejected: missing safety certificates.")),
                new PendingListing(4, "Cedar Cove LLC", "Cedar Cove Seaside Stay", "hotel", "Lisbon, PT", "Aug 15", 142, R.drawable.hotel_3, "pending",
                        "Bright seaside apartments with private balconies overlooking the Tagus.", history()),
                new PendingListing(5, "Dune & Desert Adv.", "Sunset Dune Adventure", "tour", "Dubai, AE", "Aug 14", 245, R.drawable.hotel_4, "pending",
                        "Half-day desert safari with sandboarding, camel ride and BBQ dinner.", history())
        ));
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

    public static List<BizAccount> businesses() {
        return new ArrayList<>(Arrays.asList(
                new BizAccount(1, "Orchid Hospitality", "Maya Chen", 4, 286, false, "Jun 2024"),
                new BizAccount(2, "Riverbend Tours Co.", "Kenji Tanaka", 2, 124, false, "Mar 2024"),
                new BizAccount(3, "Cedar Cove LLC", "Inês Costa", 6, 412, false, "Jan 2024"),
                new BizAccount(4, "Skyline Marina", "Wei Zhang", 7, 198, true, "Nov 2023"),
                new BizAccount(5, "Dune & Desert Adv.", "Layla Hassan", 3, 88, false, "Aug 2023"),
                new BizAccount(6, "Sandbar Cliff Resorts", "James Park", 9, 624, false, "May 2023")
        ));
    }

    public static List<AdminUser> users() {
        return new ArrayList<>(Arrays.asList(
                new AdminUser(1, "Amelia Robinson", "amelia.r@gmail.com", "Mar 2025", "active", 14, 0, "Silver", "London, UK"),
                new AdminUser(2, "Liam Kowalski", "liam.k@outlook.com", "Apr 2025", "active", 8, 0, "Bronze", "Warsaw, PL"),
                new AdminUser(3, "guest_jay_88", "jay.88@protonmail.com", "Jun 2025", "flagged", 3, 4, "Bronze", "Unknown"),
                new AdminUser(4, "Sara Mendes", "sara.m@gmail.com", "Jan 2024", "active", 32, 0, "Gold", "Lisbon, PT"),
                new AdminUser(5, "travel_deals_99", "deals99@temp-mail.org", "Aug 2025", "suspended", 0, 7, "Bronze", "Unknown"),
                new AdminUser(6, "Ben Tanaka", "ben.t@me.com", "May 2025", "active", 6, 1, "Bronze", "Osaka, JP")
        ));
    }
}
