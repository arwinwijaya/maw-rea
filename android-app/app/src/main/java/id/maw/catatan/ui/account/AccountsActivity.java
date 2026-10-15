package id.maw.catatan.ui.account;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import id.maw.catatan.CatatanApplication;
import id.maw.catatan.data.Account;
import id.maw.catatan.data.CatatanRepository;
import id.maw.catatan.util.Money;

import java.util.List;
import java.util.Locale;

public final class AccountsActivity extends Activity {
    private CatatanRepository repository;
    private LinearLayout root;
    private int teal = Color.rgb(21, 94, 117);
    private int ink = Color.rgb(20, 45, 54);
    private int muted = Color.rgb(97, 115, 122);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = CatatanApplication.getInstance().getRepository();
        render();
    }

    private void render() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(20));
        addText("Akun kas", 24, ink, true);
        addText("Saldo dihitung dari saldo awal dan transaksi.", 14, muted, false);
        for (Account account : repository.listAccounts()) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(18), 0, dp(18));
            TextView name = new TextView(this);
            name.setText(account.name);
            name.setTextColor(ink);
            name.setTextSize(16);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            TextView balance = new TextView(this);
            balance.setText("Rp " + Money.format(repository.balanceMinor(account.id), new Locale("id", "ID")));
            balance.setTextColor(teal);
            balance.setTextSize(15);
            balance.setTypeface(null, android.graphics.Typeface.BOLD);
            row.addView(balance);
            root.addView(row);
        }
        Button add = new Button(this);
        add.setText("＋ Tambah akun");
        add.setAllCaps(false);
        add.setOnClickListener(v -> showAddDialog());
        root.addView(add, new LinearLayout.LayoutParams(-1, dp(52)));
        setContentView(root);
    }

    private void showAddDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), 0);
        EditText name = new EditText(this);
        name.setHint("Nama akun");
        form.addView(name);
        EditText opening = new EditText(this);
        opening.setHint("Saldo awal (Rp)");
        opening.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        form.addView(opening);
        new android.app.AlertDialog.Builder(this).setTitle("Akun baru").setView(form)
                .setNegativeButton("Batal", null)
                .setPositiveButton("Simpan", (d, w) -> {
                    String n = name.getText().toString().trim();
                    Long amount = Money.parse(opening.getText().toString(), new Locale("id", "ID"));
                    if (n.isEmpty() || amount == null) {
                        Toast.makeText(this, "Nama dan saldo awal harus valid", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        repository.addAccount(n, amount);
                        render();
                    } catch (android.database.SQLException e) {
                        Toast.makeText(this, "Nama akun sudah digunakan", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void addText(String text, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(10);
        root.addView(t, p);
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
