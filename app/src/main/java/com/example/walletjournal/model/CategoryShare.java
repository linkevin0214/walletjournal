package com.example.walletjournal.model;

/** One category's amount and its share (%) of this month's total expenses. */
public class CategoryShare {

    private final String category;
    private final long amount;
    private final double percent;

    public CategoryShare(String category, long amount, double percent) {
        this.category = category;
        this.amount = amount;
        this.percent = percent;
    }

    public String getCategory() {
        return category;
    }

    public long getAmount() {
        return amount;
    }

    /** 0-100. */
    public double getPercent() {
        return percent;
    }
}
