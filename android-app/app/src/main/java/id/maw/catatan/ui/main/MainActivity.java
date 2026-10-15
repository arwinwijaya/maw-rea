package id.maw.catatan.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.maw.catatan.CatatanApplication;
import id.maw.catatan.data.Account;
import id.maw.catatan.data.CatatanRepository;
import id.maw.catatan.data.Transaction;
import id.maw.catatan.ui.account.AccountsActivity;
import id.maw.catatan.ui.settings.SettingsActivity;
import id.maw.catatan.ui.summary.SummaryActivity;
import id.maw.catatan.ui.transaction.TransactionActivity;
import id.maw.catatan.util.Money;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private final int ink = Color.rgb(20, 45, 54);
    private final int teal = Color.rgb(21, 94, 117);
    private final int muted = Color.rgb(97, 115, 122);
    private LinearLayout root;
    private CatatanRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = CatatanApplication.getInstance().getRepository();
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) render();
    }

    private void render() {
        root = column();
        root.setPadding(dp(22), dp(24), dp(22), dp(20));
        TextView eyebrow = text("CATATAN HARIAN", 13, teal, true);
        root.addView(eyebrow);
        root.addView(text("Keuangan lebih teratur.", 26, ink, true), params(0, 10));
        root.addView(text(DateFormat.getDateInstance(DateFormat.FULL, new Locale("id", "ID")).format(new Date()), 14, muted, false), params(0, 2));

        List<Account> accounts = repository.listAccounts();
        if (accounts.isEmpty()) {
            repository.addAccount("Kas Utama", 0);
            accounts = repository.listAccounts();
        }
        Account account = accounts.get(0);
        long balance = repository.balanceMinor(account.id);
        LinearLayout card = column();
        card.setBackgroundColor(Color.rgb(232, 245, 247));
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        TextView label = text("SALDO · " + account.name, 13, muted, true);
        card.addView(label);
        card.addView(text("Rp " + Money.format(balance, new Locale("id", "ID")), 30, ink, true), params(0, 6));
        long[] totals = repository.totalsMinor(account.id);
        card.addView(text("Masuk  Rp " + Money.format(totals[0], new Locale("id", "ID"))
                + "     Keluar  Rp " + Money.format(totals[1], new Locale("id", "ID")), 13, muted, false), params(0, 10));
        root.addView(card, params(0, 24));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button income = button("＋ Pemasukan");
        income.setOnClickListener(v -> openTransaction(Transaction.KIND_INCOME));
        Button expense = button("− Pengeluaran");
        expense.setOnClickListener(v -> openTransaction(Transaction.KIND_EXPENSE));
        actions.addView(income, new LinearLayout.LayoutParams(0, dp(52), 1));
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0, dp(52), 1);
        ep.leftMargin = dp(10);
        actions.addView(expense, ep);
        root.addView(actions);

        TextView recentHeader = text("Transaksi terbaru", 19, ink, true);
        root.addView(recentHeader, params(0, 28));
        List<Transaction> transactions = repository.listTransactions(account.id, 6);
        if (transactions.isEmpty()) {
            root.addView(text("Belum ada transaksi. Tambahkan pemasukan atau pengeluaran pertama Anda.", 14, muted, false), params(0, 12));
        } else {
            for (Transaction tx : transactions) {
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, dp(12), 0, dp(12));
                LinearLayout copy = column();
                copy.addView(text(tx.note.isEmpty() ? (tx.isIncome() ? "Pemasukan" : "Pengeluaran") : tx.note, 15, ink, true));
                copy.addView(text(DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("id", "ID")).format(new Date(tx.occurredAt)), 12, muted, false), params(0, 3));
                row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
                int color = tx.isIncome() ? Color.rgb(22, 125, 79) : Color.rgb(172, 66, 61);
                row.addView(text((tx.isIncome() ? "+ Rp " : "− Rp ") + Money.format(tx.amountMinor, new Locale("id", "ID")), 14, color, true));
                root.addView(row);
                View divider = new View(this);
                divider.setBackgroundColor(Color.rgb(230, 235, 237));
                root.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.BOTTOM);
        footer.addView(navButton("Akun", AccountsActivity.class), new LinearLayout.LayoutParams(0, dp(48), 1));
        footer.addView(navButton("Ringkasan", SummaryActivity.class), new LinearLayout.LayoutParams(0, dp(48), 1));
        footer.addView(navButton("Pengaturan", SettingsActivity.class), new LinearLayout.LayoutParams(0, dp(48), 1));
        root.addView(footer, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
    }

    private void openTransaction(String kind) {
        Intent i = new Intent(this, TransactionActivity.class);
        i.putExtra(TransactionActivity.EXTRA_KIND, kind);
        startActivity(i);
    }

    private Button navButton(String title, Class<?> target) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setOnClickListener(v -> startActivity(new Intent(this, target)));
        return b;
    }

    private Button button(String title) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(teal));
        return b;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout.LayoutParams params(int width, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width == 0 ? -1 : width, -2);
        p.topMargin = dp(top);
        return p;
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
}
