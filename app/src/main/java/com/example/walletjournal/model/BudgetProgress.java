package com.example.walletjournal.model;

/** This month's spend vs budget for the top-spending expense category. */
public class BudgetProgress {

    private final String category;
    private final long spent;
    private final long budget;

    public BudgetProgress(String category, long spent, long budget) {
        this.category = category;
        this.spent = spent;
        this.budget = budget;
    }

    public String getCategory() {
        return category;
    }

    public long getSpent() {
        return spent;
    }

    /** 0 means no budget set for this category — the UI should hide the bar. */
    public long getBudget() {
        return budget;
    }
}
