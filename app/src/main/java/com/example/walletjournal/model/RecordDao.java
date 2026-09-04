package com.example.walletjournal.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RecordDao {

    // Ordered by createdAt (the record's own date), not insertion id — a record can
    // now be back-dated via the date picker on Add Record, so the two no longer
    // always agree. id DESC is just a tiebreaker for same-timestamp rows.
    @Query("SELECT * FROM records ORDER BY createdAt DESC, id DESC")
    List<Record> getAll();

    /** Null if no record with that id exists (e.g. it was deleted elsewhere). */
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    Record getById(long id);

    @Insert
    long insert(Record record);

    @Update
    void update(Record record);

    @Delete
    void delete(Record record);

    @Query("SELECT category AS category, SUM(amount) AS total FROM records "
            + "WHERE type = 'EXPENSE' AND category IS NOT NULL AND category != '' "
            + "AND createdAt >= :start AND createdAt < :end "
            + "GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> getExpenseCategoryTotals(long start, long end);

    /** Null when there are no matching rows (SQLite SUM() of an empty set is NULL). */
    @Query("SELECT SUM(amount) FROM records WHERE type = 'EXPENSE' AND createdAt >= :start AND createdAt < :end")
    Long getExpenseTotal(long start, long end);
}
