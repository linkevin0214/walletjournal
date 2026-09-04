package com.example.walletjournal.model;

/** One ranked category row: this month's total + % change vs last month. */
public class CategoryStat {

    private final int rank;
    private final String category;
    private final long amount;

    /** null when there's no prior-month data for this category to compare against. */
    private final Integer changePercent;

    public CategoryStat(int rank, String category, long amount, Integer changePercent) {
        this.rank = rank;
        this.category = category;
        this.amount = amount;
        this.changePercent = changePercent;
    }

    public int getRank() {
        return rank;
    }

    public String getCategory() {
        return category;
    }

    public long getAmount() {
        return amount;
    }

    public Integer getChangePercent() {
        return changePercent;
    }
}
