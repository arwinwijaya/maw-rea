package id.maw.catatan.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (22 * getResources().getDisplayMetrics().density + 0.5f);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("Pengaturan");
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        TextView info = new TextView(this);
        info.setText("Mata uang: Rupiah Indonesia (IDR)\n\nVersi 0.1.0\n\nData transaksi tersimpan lokal di perangkat. Fitur backup dan sinkronisasi belum tersedia.");
        info.setTextSize(16);
        info.setPadding(0, pad, 0, 0);
        root.addView(info);
        setContentView(root);
    }
}
