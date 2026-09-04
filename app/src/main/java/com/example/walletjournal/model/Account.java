package com.example.walletjournal.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * A single account row shown on the Accounts screen
 * (e.g. cash, bank account, credit card). Room entity for the "accounts" table.
 */
@Entity(tableName = "accounts")
public class Account {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String subtitle;
    private long amount;

    /** AccountType.name() (e.g. "CASH" / "BANK" / "CREDIT_CARD"), used to pick the row icon. */
    private String type;

    /** Used by Room to reconstruct rows read from the database. */
    public Account(long id, String title, String subtitle, long amount, String type) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.type = type;
    }

    /** Convenience constructor for creating a new (not-yet-saved) account. */
    @Ignore
    public Account(String title, String subtitle, long amount, String type) {
        this(0, title, subtitle, amount, type);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    /** Positive for assets, negative for liabilities (e.g. credit card balance owed). */
    public long getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }
}
