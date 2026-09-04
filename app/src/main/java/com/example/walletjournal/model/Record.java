package com.example.walletjournal.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * A single expense/income/transfer entry created on the Add Record screen.
 * Room entity for the "records" table.
 */
@Entity(tableName = "records")
public class Record {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** RecordType.name(), e.g. "EXPENSE" / "INCOME" / "TRANSFER". */
    private String type;

    /** Always a positive magnitude; `type` says the direction. */
    private long amount;

    private String account;

    /** Only set for TRANSFER records; null otherwise. */
    private String toAccount;

    private String category;
    private String note;
    private long createdAt;

    /** Used by Room to reconstruct rows read from the database. */
    public Record(long id, String type, long amount, String account, String toAccount, String category,
                  String note, long createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.account = account;
        this.toAccount = toAccount;
        this.category = category;
        this.note = note;
        this.createdAt = createdAt;
    }

    /** Convenience constructor for creating a new (not-yet-saved) record. */
    @Ignore
    public Record(String type, long amount, String account, String toAccount, String category, String note,
                  long createdAt) {
        this(0, type, amount, account, toAccount, category, note, createdAt);
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public long getAmount() {
        return amount;
    }

    public String getAccount() {
        return account;
    }

    public String getToAccount() {
        return toAccount;
    }

    public String getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
