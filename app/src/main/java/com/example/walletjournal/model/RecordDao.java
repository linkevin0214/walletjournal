package com.example.walletjournal.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface RecordDao {

    // Ordered by createdAt (the record's own date), not insertion id — a record can
    // now be back-dated via the date picker on Add Record, so the two no longer
    // always agree. id DESC is just a tiebreaker for same-timestamp rows.
    @Query("SELECT * FROM records ORDER BY createdAt DESC, id DESC")
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
