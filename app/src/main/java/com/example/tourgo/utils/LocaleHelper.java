package com.example.tourgo.utils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class LocaleHelper {
    private LocaleHelper() {}

    /** tag ví dụ: "vi", "en". Truyền chuỗi rỗng để dùng theo hệ thống. */
    public static void setAppLocale(String languageTag) {
        LocaleListCompat locales = (languageTag == null || languageTag.isEmpty())
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(languageTag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    /** "vi", "en", hoặc "" nếu đang theo hệ thống. */
    public static String getCurrentLanguageTag() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        return locales.isEmpty() ? "" : locales.get(0).getLanguage();
    }
}