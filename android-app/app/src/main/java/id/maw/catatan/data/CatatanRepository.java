package id.maw.catatan.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/** Single point of data access. All writes are transactional; balances are computed from the ledger. */
public final class CatatanRepository {
    private final CatatanDbHelper mHelper;

    public CatatanRepository(Context context) {
        mHelper = new CatatanDbHelper(context.getApplicationContext());
    }

    public List<Account> listAccounts() {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        List<Account> out = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, name, opening_balance FROM accounts ORDER BY name COLLATE NOCASE", null)) {
            while (c.moveToNext()) {
                out.add(new Account(c.getLong(0), c.getString(1), c.getLong(2)));
            }
        }
        return out;
    }

    /** Balance = opening balance + signed ledger sum. */
    public long balanceMinor(long accountId) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT a.opening_balance + COALESCE((SELECT SUM(CASE t.kind WHEN 'INCOME' THEN t.amount_minor ELSE -t.amount_minor END) "
                        + "FROM transactions t WHERE t.account_id = a.id), 0) FROM accounts a WHERE a.id = ?",
                new String[]{Long.toString(accountId)})) {
            return c.moveToFirst() ? c.getLong(0) : 0L;
        }
    }

    public List<Transaction> listTransactions(long accountId, int limit) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        List<Transaction> out = new ArrayList<>();
        try (Cursor c = db.rawQuery(
                "SELECT id, account_id, kind, amount_minor, note, occurred_at FROM transactions "
                        + "WHERE account_id = ? ORDER BY occurred_at DESC, id DESC LIMIT ?",
                new String[]{Long.toString(accountId), Integer.toString(limit)})) {
            while (c.moveToNext()) {
                out.add(new Transaction(c.getLong(0), c.getLong(1), c.getString(2),
                        c.getLong(3), c.getString(4), c.getLong(5)));
            }
        }
        return out;
    }

    public long addAccount(String name, long openingBalanceMinor) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name.trim());
        v.put("opening_balance", openingBalanceMinor);
        v.put("created_at", System.currentTimeMillis());
        return db.insertOrThrow("accounts", null, v);
    }

    public long addTransaction(long accountId, String kind, long amountMinor, String note, long occurredAt) {
        if (amountMinor <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (!Transaction.KIND_INCOME.equals(kind) && !Transaction.KIND_EXPENSE.equals(kind)) {
            throw new IllegalArgumentException("unknown kind: " + kind);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("account_id", accountId);
            v.put("kind", kind);
            v.put("amount_minor", amountMinor);
            v.put("note", note == null ? "" : note.trim());
            v.put("occurred_at", occurredAt);
            v.put("created_at", System.currentTimeMillis());
            long id = db.insertOrThrow("transactions", null, v);
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    /** Aggregate totals for one account, computed from the ledger. */
    public long[] totalsMinor(long accountId) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COALESCE(SUM(CASE WHEN kind='INCOME' THEN amount_minor END),0), "
                        + "COALESCE(SUM(CASE WHEN kind='EXPENSE' THEN amount_minor END),0) "
                        + "FROM transactions WHERE account_id = ?",
                new String[]{Long.toString(accountId)})) {
            if (c.moveToFirst()) {
                return new long[]{c.getLong(0), c.getLong(1)};
            }
        }
        return new long[]{0L, 0L};
    }
}
