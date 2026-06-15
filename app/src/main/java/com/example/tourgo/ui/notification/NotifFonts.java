package com.example.tourgo.ui.notification;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.example.tourgo.R;
import com.example.tourgo.utils.LocaleHelper;

/**
 * Picks the right UI typeface for the active language.
 *
 * The Urbanist files bundled in res/font are a Latin subset — they do NOT contain
 * the Vietnamese precomposed letters (Đ, đ, ề, ữ, ố, ờ, ẩ …), so rendering Vietnamese
 * in Urbanist breaks mid-word (those glyphs fall back to the system font). Urbanist is
 * therefore used for English only; Vietnamese — and any non-English locale, which falls
 * back to the default Vietnamese strings — uses the system {@code sans-serif}, the same
 * font the rest of the app renders Vietnamese in (the app theme's default fontFamily).
 */
public final class NotifFonts {

    private NotifFonts() {}

    public enum Weight { REGULAR, MEDIUM, SEMIBOLD, BOLD }

    /** Urbanist only covers the Latin/English text we ship; everything else uses sans-serif. */
    private static boolean useUrbanist() {
        return "en".equals(LocaleHelper.getCurrentLanguageTag());
    }

    public static Typeface get(Context ctx, Weight w) {
        if (useUrbanist()) {
            int res;
            switch (w) {
                case MEDIUM:   res = R.font.urbanist_medium; break;
                case SEMIBOLD: res = R.font.urbanist_semibold; break;
                case BOLD:     res = R.font.urbanist_bold; break;
                default:       res = R.font.urbanist_regular; break;
            }
            Typeface t = ResourcesCompat.getFont(ctx, res);
            if (t != null) return t;
        }
        // Vietnamese / fallback: system sans-serif at the matching weight (API 28+).
        int weight;
        switch (w) {
            case MEDIUM:   weight = 500; break;
            case SEMIBOLD: weight = 600; break;
            case BOLD:     weight = 700; break;
            default:       weight = 400; break;
        }
        return Typeface.create(Typeface.SANS_SERIF, weight, false);
    }
}
