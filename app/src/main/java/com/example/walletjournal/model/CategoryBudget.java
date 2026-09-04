package com.example.walletjournal.model;

/** A category's editable budget amount, shown on the Budgets screen. */
public class CategoryBudget {

    private final String category;
    private final long amount;

    public CategoryBudget(String category, long amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    /** 0 means no budget set yet. */
    public long getAmount() {
        return amount;
    }
}
