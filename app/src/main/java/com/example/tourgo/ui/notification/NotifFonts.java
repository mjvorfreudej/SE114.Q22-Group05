package com.example.tourgo.ui.notification;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.example.tourgo.R;
import com.example.tourgo.utils.LocaleHelper;

/**
 * Picks the right UI typeface for the active language.
 *
 * Urbanist (the Latin .ttf set bundled in res/font) does NOT contain the Vietnamese
 * precomposed letters (Đ, đ, ề, ữ, ố, ờ, ẩ …), so rendering Vietnamese in it breaks
 * mid-word (missing glyphs fall back to the system font). It is therefore used for
 * English only. Vietnamese — and any non-English locale, which falls back to the
 * default Vietnamese strings — uses Be Vietnam Pro (bevietnampro_*.ttf), a Vietnamese
 * typeface with the full 400/500/600/700 weight range. This mirrors the app theme,
 * whose @font/urbanist resolves to Be Vietnam Pro by default and to Urbanist under
 * res/font-en for the "en" locale.
 */
public final class NotifFonts {

    private NotifFonts() {}

    public enum Weight { REGULAR, MEDIUM, SEMIBOLD, BOLD }

    /** Urbanist only covers the Latin/English text we ship; everything else uses Be Vietnam Pro. */
    private static boolean useUrbanist() {
        return "en".equals(LocaleHelper.getCurrentLanguageTag());
    }

    public static Typeface get(Context ctx, Weight w) {
        int res;
        if (useUrbanist()) {
            switch (w) {
                case MEDIUM:   res = R.font.urbanist_medium; break;
                case SEMIBOLD: res = R.font.urbanist_semibold; break;
                case BOLD:     res = R.font.urbanist_bold; break;
                default:       res = R.font.urbanist_regular; break;
            }
        } else {
            // Vietnamese / non-English: Be Vietnam Pro carries the Vietnamese glyphs.
            switch (w) {
                case MEDIUM:   res = R.font.bevietnampro_medium; break;
                case SEMIBOLD: res = R.font.bevietnampro_semibold; break;
                case BOLD:     res = R.font.bevietnampro_bold; break;
                default:       res = R.font.bevietnampro_regular; break;
            }
        }
        Typeface t = ResourcesCompat.getFont(ctx, res);
        if (t != null) return t;

        // Last-resort fallback: system sans-serif at the matching weight (API 28+).
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
