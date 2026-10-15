package id.maw.catatan.data;

/** Immutable account row. Balance is derived, never stored. */
public final class Account {
    public final long id;
    public final String name;
    public final long openingBalanceMinor;

    public Account(long id, String name, long openingBalanceMinor) {
        this.id = id;
        this.name = name;
        this.openingBalanceMinor = openingBalanceMinor;
    }
}
