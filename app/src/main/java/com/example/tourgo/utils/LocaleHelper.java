package com.example.tourgo.utils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public final class LocaleHelper {
    private LocaleHelper() {}

    /** tag ví dụ: "vi", "en". Truyền chuỗi rỗng để dùng theo hệ thống. */
    public static void setAppLocale(String languageTag) {
        LocaleListCompat locales = (languageTag == null || languageTag.isEmpty())
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(languageTag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    /** Lấy ngôn ngữ hiện tại, ưu tiên ngôn ngữ đã cài đặt cho app, nếu không lấy của hệ thống. */
    public static String getCurrentLanguageTag() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return Locale.getDefault().getLanguage();
        }
        return locales.get(0).getLanguage();
    }
}