package id.maw.catatan.ui.summary;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.maw.catatan.CatatanApplication;
import id.maw.catatan.data.Account;
import id.maw.catatan.data.CatatanRepository;
import id.maw.catatan.util.Money;

import java.util.Locale;

public final class SummaryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CatatanRepository repo = CatatanApplication.getInstance().getRepository();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(22);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("Ringkasan keuangan");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(20, 45, 54));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        for (Account account : repo.listAccounts()) {
            long[] totals = repo.totalsMinor(account.id);
            TextView section = new TextView(this);
            section.setText(account.name);
            section.setTextSize(18);
            section.setTextColor(Color.rgb(21, 94, 117));
            section.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.topMargin = dp(24);
            root.addView(section, p);
            add(root, "Total pemasukan", totals[0], Color.rgb(22, 125, 79));
            add(root, "Total pengeluaran", totals[1], Color.rgb(172, 66, 61));
            add(root, "Saldo saat ini", repo.balanceMinor(account.id), Color.rgb(20, 45, 54));
        }
        setContentView(root);
    }

    private void add(LinearLayout root, String label, long amount, int color) {
        TextView t = new TextView(this);
        t.setText(label + "     Rp " + Money.format(amount, new Locale("id", "ID")));
        t.setTextSize(15);
        t.setTextColor(color);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(12);
        root.addView(t, p);
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
