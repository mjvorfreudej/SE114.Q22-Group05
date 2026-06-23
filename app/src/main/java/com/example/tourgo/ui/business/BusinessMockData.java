package com.example.tourgo.ui.business;

import com.example.tourgo.R;

/**
 * Shared display models + cover-photo fallback for the Business Console.
 *
 * <p>Despite the historical name, this no longer holds seed data: every Business
 * surface (Listings, Calendar, Reviews, Home) is wired to the live REST API
 * (TourService / HotelService / BookingService / ReviewService) and builds these
 * structs from server DTOs. What remains is the shared display-model shapes and a
 * bundled cover photo used until a listing supplies its own image. The class
 * keeps its name so the {@code BusinessMockData.Listing/CalBooking/RatingBar}
 * references across the UI stay stable.
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
        public final String serverId;

        public Listing(int id, String name, String loc, int price, int photoIndex,
                       String status, String cat, int bookings, double rating) {
            this(id, name, loc, price, photoIndex, status, cat, bookings, rating, null);
        }

        public Listing(int id, String name, String loc, int price, int photoIndex,
                       String status, String cat, int bookings, double rating, String serverId) {
            this.id = id; this.name = name; this.loc = loc; this.price = price;
            this.photoIndex = photoIndex; this.status = status; this.cat = cat;
            this.bookings = bookings; this.rating = rating; this.serverId = serverId;
        }

        public boolean hasRating() { return rating >= 0; }
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

    // ── Reviews (rating summary bar) ──────────────────────────────────────────
    /** Rating distribution rows for the summary bar: {stars, count, fractionOfMax}. */
    public static final class RatingBar {
        public final int stars, count;
        public final float pct;

        public RatingBar(int stars, int count, float pct) {
            this.stars = stars; this.count = count; this.pct = pct;
        }
    }
}
