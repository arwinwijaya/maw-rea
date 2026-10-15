package id.maw.catatan.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Local-only schema. Monetary values are integer minor units (e.g. cents); no floats. */
public final class CatatanDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "catatan.db";
    private static final int DATABASE_VERSION = 1;

    public CatatanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE accounts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, opening_balance INTEGER NOT NULL DEFAULT 0, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER NOT NULL, kind TEXT NOT NULL CHECK(kind IN ('INCOME','EXPENSE')), amount_minor INTEGER NOT NULL CHECK(amount_minor > 0), note TEXT NOT NULL DEFAULT '', occurred_at INTEGER NOT NULL, created_at INTEGER NOT NULL, FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE RESTRICT)");
        db.execSQL("CREATE INDEX idx_transactions_account_date ON transactions(account_id, occurred_at DESC)");
        db.execSQL("INSERT INTO accounts (name, opening_balance, created_at) VALUES ('Kas Utama', 0, " + System.currentTimeMillis() + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Migration needed from " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}
