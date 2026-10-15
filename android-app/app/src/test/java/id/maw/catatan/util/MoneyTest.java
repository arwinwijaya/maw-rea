package id.maw.catatan.util;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MoneyTest {
    private static final Locale ID = new Locale("id", "ID");

    @Test public void indonesianGrouping() {
        assertEquals(Long.valueOf(125000L), Money.parse("1.250", ID));
        assertEquals(Long.valueOf(125050L), Money.parse("1.250,50", ID));
        assertEquals(Long.valueOf(125050L), Money.parse("1250,50", ID));
    }

    @Test public void plainValues() {
        assertEquals(Long.valueOf(125000L), Money.parse("1250", ID));
        assertEquals(Long.valueOf(50L), Money.parse("0,50", ID));
        assertEquals(Long.valueOf(100L), Money.parse("1", ID));
    }

    @Test public void androidKeyboardDecimal() {
        // Same Money.parse path as TransactionActivity; dot can be keyboard decimal separator.
        assertEquals(Long.valueOf(125050L), Money.parse("1250.50", ID));
    }

    @Test public void usLocale() {
        assertEquals(Long.valueOf(125050L), Money.parse("1,250.50", Locale.US));
        assertEquals(Long.valueOf(125000L), Money.parse("1250", Locale.US));
    }

    @Test public void invalidOrOverflow() {
        assertNull(Money.parse("", ID));
        assertNull(Money.parse(null, ID));
        assertNull(Money.parse("999999999999999999999999999", ID));
    }
}
