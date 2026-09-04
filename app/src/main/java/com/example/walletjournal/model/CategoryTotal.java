package com.example.walletjournal.model;

/**
 * Plain (non-@Entity) result row for RecordDao's category aggregation query.
 * Room maps raw query columns onto these public fields by name.
 */
public class CategoryTotal {
    public String category;
    public long total;
}
