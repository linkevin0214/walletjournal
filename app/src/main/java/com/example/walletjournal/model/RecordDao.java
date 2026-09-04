package com.example.walletjournal.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface RecordDao {

    @Query("SELECT * FROM records ORDER BY id DESC")
    List<Record> getAll();

    @Insert
    long insert(Record record);

    @Query("SELECT category AS category, SUM(amount) AS total FROM records "
            + "WHERE type = 'EXPENSE' AND category IS NOT NULL AND category != '' "
            + "AND createdAt >= :start AND createdAt < :end "
            + "GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> getExpenseCategoryTotals(long start, long end);

    /** Null when there are no matching rows (SQLite SUM() of an empty set is NULL). */
    @Query("SELECT SUM(amount) FROM records WHERE type = 'EXPENSE' AND createdAt >= :start AND createdAt < :end")
    Long getExpenseTotal(long start, long end);
}
