package com.example.walletjournal.model;

/** This month's (year, month) total expense, used by the 趨勢 tab's bar chart. */
public class MonthlyTotal {

    private final int year;
    private final int month;
    private final long total;

    public MonthlyTotal(int year, int month, long total) {
        this.year = year;
        this.month = month;
        this.total = total;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public long getTotal() {
        return total;
    }
}
