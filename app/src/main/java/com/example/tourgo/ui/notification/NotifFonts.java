package com.example.tourgo.ui.notification;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.example.tourgo.R;
import com.example.tourgo.utils.LocaleHelper;

/**
 * Picks the right UI typeface for the active language.
 *
 * Two Urbanist subsets ship in res/font:
 *  • the Latin {@code .ttf} files (regular/medium/semibold/bold) — full weight range, but
 *    they do NOT contain the Vietnamese precomposed letters (Đ, đ, ề, ữ, ố, ờ, ẩ …), so
 *    rendering Vietnamese with them breaks mid-word (missing glyphs fall back to the
 *    system font). These are used for English.
 *  • the Vietnamese {@code .otf} files ({@code *_vn.otf}) — they carry the Vietnamese
 *    glyphs, so Vietnamese (and any non-English locale, which falls back to the default
 *    Vietnamese strings) renders in Urbanist without breaking. Only Light and Regular
 *    weights are shipped, so the heavier UI weights (semibold/bold) are synthesized
 *    (faux-bold) from the Regular {@code .otf} — keeping the Vietnamese glyphs.
 */
public final class NotifFonts {

    private NotifFonts() {}

    public enum Weight { REGULAR, MEDIUM, SEMIBOLD, BOLD }

    /** The Latin .ttf set only covers the English text we ship. */
    private static boolean useLatinUrbanist() {
        return "en".equals(LocaleHelper.getCurrentLanguageTag());
    }

    public static Typeface get(Context ctx, Weight w) {
        if (useLatinUrbanist()) {
            int res;
            switch (w) {
                case MEDIUM:   res = R.font.urbanist_medium; break;
                case SEMIBOLD: res = R.font.urbanist_semibold; break;
                case BOLD:     res = R.font.urbanist_bold; break;
                default:       res = R.font.urbanist_regular; break;
            }
            Typeface t = ResourcesCompat.getFont(ctx, res);
            if (t != null) return t;
        } else {
            // Vietnamese / non-English: Urbanist Vietnamese .otf subset. Only Regular is
            // shipped, so semibold/bold are synthesized (faux-bold) off the same file —
            // medium stays Regular (it's used for subtitles/timestamps, not emphasis).
            Typeface base = ResourcesCompat.getFont(ctx, R.font.urbanist_regular_vn);
            if (base != null) {
                switch (w) {
                    case SEMIBOLD:
                    case BOLD:
                        return Typeface.create(base, Typeface.BOLD);
                    default:
                        return base;
                }
            }
        }
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
