package id.maw.catatan.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formats and parses currency for Indonesian locale (IDR: '.' for thousands, ',' for decimals). */
public final class Money {
    private Money() {
    }

    public static String format(long minorUnits, Locale locale) {
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(minorUnits / 100.0);
    }

    /**
     * Parses user input string into integer minor units (cents).
     * <p>
     * For Indonesian locale ("id", "ID"):
     * <ul>
     *   <li>If both '.' and ',' are present, '.' is grouping and ',' is decimal.</li>
     *   <li>If only ',' is present, ',' is treated as decimal (e.g. "1250,50").</li>
     *   <li>If only '.' is present, '.' can be either grouping (e.g. "1.250") or decimal
     *       depending on context. To support mobile keyboards that emit '.' as decimal
     *       when no comma exists (e.g. "1250.50"), if there is a single '.' with 1 or 2 digits
     *       following it, it is treated as a decimal separator for IDR parsing.</li>
     * </ul>
     */
    public static Long parse(String raw, Locale locale) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim().replaceAll("[^0-9.,]", "");
        if (cleaned.isEmpty()) {
            return null;
        }

        boolean isId = locale != null && "id".equalsIgnoreCase(locale.getLanguage());

        String intPart;
        String fracPart = "";

        if (isId) {
            int lastDot = cleaned.lastIndexOf('.');
            int lastComma = cleaned.lastIndexOf(',');

            if (lastComma >= 0) {
                // Standard Indonesian: '.' grouping, ',' decimal
                intPart = cleaned.substring(0, lastComma).replace(".", "");
                fracPart = cleaned.substring(lastComma + 1);
            } else if (lastDot >= 0) {
                // Only dot is present. Could be grouping ("1.250") or decimal ("1250.50").
                String tail = cleaned.substring(lastDot + 1);
                boolean isDecimalDot = tail.length() > 0 && tail.length() <= 2 && cleaned.indexOf('.') == lastDot;
                if (isDecimalDot) {
                    intPart = cleaned.substring(0, lastDot).replace(".", "");
                    fracPart = tail;
                } else {
                    intPart = cleaned.replace(".", "");
                }
            } else {
                intPart = cleaned;
            }
        } else {
            int dotIdx = cleaned.lastIndexOf('.');
            if (dotIdx >= 0) {
                intPart = cleaned.substring(0, dotIdx).replace(",", "");
                fracPart = cleaned.substring(dotIdx + 1);
            } else {
                intPart = cleaned.replace(",", "");
            }
        }

        intPart = intPart.replaceAll("[^0-9]", "");
        fracPart = fracPart.replaceAll("[^0-9]", "");

        if (intPart.isEmpty() && fracPart.isEmpty()) {
            return null;
        }

        if (fracPart.length() > 2) {
            fracPart = fracPart.substring(0, 2);
        }
        while (fracPart.length() < 2) {
            fracPart = fracPart + "0";
        }

        try {
            long major = intPart.isEmpty() ? 0L : Long.parseLong(intPart);
            long minor = Long.parseLong(fracPart);
            return Math.addExact(Math.multiplyExact(major, 100L), minor);
        } catch (ArithmeticException | NumberFormatException e) {
            return null;
        }
    }
}
