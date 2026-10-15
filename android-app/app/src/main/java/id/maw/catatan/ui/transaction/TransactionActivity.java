package id.maw.catatan.ui.transaction;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import id.maw.catatan.CatatanApplication;
import id.maw.catatan.data.Account;
import id.maw.catatan.data.CatatanRepository;
import id.maw.catatan.data.Transaction;
import id.maw.catatan.util.Money;

import java.util.List;
import java.util.Locale;

public final class TransactionActivity extends Activity {
    public static final String EXTRA_KIND = "extra_kind";
    private final int ink = Color.rgb(20, 45, 54);
    private final int teal = Color.rgb(21, 94, 117);
    private final int muted = Color.rgb(97, 115, 122);
    private CatatanRepository repository;
    private String kind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = CatatanApplication.getInstance().getRepository();
        kind = getIntent().getStringExtra(EXTRA_KIND);
        if (kind == null) kind = Transaction.KIND_EXPENSE;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(20));

        boolean isIncome = Transaction.KIND_INCOME.equals(kind);
        TextView title = new TextView(this);
        title.setText(isIncome ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        title.setTextSize(22);
        title.setTextColor(isIncome ? Color.rgb(22, 125, 79) : Color.rgb(172, 66, 61));
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView amLbl = new TextView(this);
        amLbl.setText("Jumlah (Rp)");
        amLbl.setTextSize(13);
        amLbl.setTextColor(muted);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(24);
        root.addView(amLbl, lp);

        EditText amountInput = new EditText(this);
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amountInput.setTextSize(24);
        root.addView(amountInput, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView noteLbl = new TextView(this);
        noteLbl.setText("Catatan");
        noteLbl.setTextSize(13);
        noteLbl.setTextColor(muted);
        LinearLayout.LayoutParams lpn = new LinearLayout.LayoutParams(-1, -2);
        lpn.topMargin = dp(16);
        root.addView(noteLbl, lpn);

        EditText noteInput = new EditText(this);
        noteInput.setTextSize(16);
        root.addView(noteInput, new LinearLayout.LayoutParams(-1, dp(50)));

        Button save = new Button(this);
        save.setText("Simpan Transaksi");
        save.setTextSize(14);
        save.setAllCaps(false);
        save.setTextColor(Color.WHITE);
        save.setBackgroundTintList(android.content.res.ColorStateList.valueOf(teal));
        LinearLayout.LayoutParams lps = new LinearLayout.LayoutParams(-1, dp(52));
        lps.topMargin = dp(28);
        save.setOnClickListener(v -> {
            String raw = amountInput.getText().toString().trim();
            Long minor = Money.parse(raw, new Locale("id", "ID"));
            if (minor == null || minor <= 0) {
                Toast.makeText(this, "Masukkan jumlah yang valid", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Account> accounts = repository.listAccounts();
            if (accounts.isEmpty()) {
                Toast.makeText(this, "Buat akun terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            long accountId = accounts.get(0).id;
            repository.addTransaction(accountId, kind, minor, noteInput.getText().toString(), System.currentTimeMillis());
            Toast.makeText(this, "Transaksi disimpan", Toast.LENGTH_SHORT).show();
            finish();
        });
        root.addView(save, lps);

        setContentView(root);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
