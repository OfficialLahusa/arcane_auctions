package com.lahusa.arcane_auctions.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatter {
    private static final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    private static final DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

    static {
        //symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);
    }

    public static String longToString(long val) {
        return formatter.format(val);
    }

    public static long stringToLong(String val) {
        String cleanVal = val.replaceAll("[,.]", "");
        return Long.parseLong(cleanVal);
    }
}
