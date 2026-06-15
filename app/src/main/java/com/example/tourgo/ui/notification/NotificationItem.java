package com.example.tourgo.ui.notification;

import androidx.annotation.DrawableRes;

import java.util.List;

/**
 * A single Traveler notification, mirroring the design data model in
 * ui_kits/notifications/traveler.html:
 *   { id, cat, icon, title, body, when, group, read, actions[] }
 *
 * {@link #read} is mutable so a tap (or "mark all read") can flip it in place,
 * exactly as the prototype's state machine does.
 */
public class NotificationItem {

    /** Date bucket the row is grouped under (design: Today / Yesterday / Earlier). */
    public enum Group { TODAY, YESTERDAY, EARLIER }

    /** Quick-action button rendered under the body (design TMiniBtn). */
    public static class QuickAction {
        public final String label;
        public final boolean primary; // primary = black pill, otherwise white/outline

        public QuickAction(String label, boolean primary) {
            this.label = label;
            this.primary = primary;
        }
    }

    public final String id;
    public final String cat;            // category key: bookings|payments|offers|trips|account
    @DrawableRes public final int iconRes;
    public final String title;
    public final String body;
    public final String when;           // relative timestamp, e.g. "12m", "2h"
    public final Group group;
    public boolean read;                // mutable
    public final List<QuickAction> actions;

    public NotificationItem(String id, String cat, @DrawableRes int iconRes, String title,
                            String body, String when, Group group, boolean read,
                            List<QuickAction> actions) {
        this.id = id;
        this.cat = cat;
        this.iconRes = iconRes;
        this.title = title;
        this.body = body;
        this.when = when;
        this.group = group;
        this.read = read;
        this.actions = actions;
    }
}
