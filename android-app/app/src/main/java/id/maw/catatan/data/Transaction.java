package id.maw.catatan.data;

/** One ledger entry. Amount is a positive integer in minor currency units. */
public final class Transaction {
    public static final String KIND_INCOME = "INCOME";
    public static final String KIND_EXPENSE = "EXPENSE";

    public final long id;
    public final long accountId;
    public final String kind;
    public final long amountMinor;
    public final String note;
    public final long occurredAt;

    public Transaction(long id, long accountId, String kind, long amountMinor, String note, long occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.kind = kind;
        this.amountMinor = amountMinor;
        this.note = note;
        this.occurredAt = occurredAt;
    }

    public boolean isIncome() {
        return KIND_INCOME.equals(kind);
    }

    /** Signed contribution of this entry to its account balance. */
    public long signedAmountMinor() {
        return isIncome() ? amountMinor : -amountMinor;
    }
}
