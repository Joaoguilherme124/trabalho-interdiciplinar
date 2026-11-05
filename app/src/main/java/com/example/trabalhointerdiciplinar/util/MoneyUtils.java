package com.example.trabalhointerdiciplinar.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {
    private MoneyUtils() {
    }

    public static String format(double value) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(value);
    }
}
