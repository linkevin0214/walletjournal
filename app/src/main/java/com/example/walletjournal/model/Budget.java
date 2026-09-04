package com.example.walletjournal.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** One category's monthly budget. One row per expense category. */
@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey
    @NonNull
    private String category;

    private long amount;

    public Budget(@NonNull String category, long amount) {
        this.category = category;
        this.amount = amount;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public long getAmount() {
        return amount;
    }
}
