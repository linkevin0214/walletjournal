package com.example.walletjournal.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface CategoryDao {

    /** Every category regardless of type — used where EXPENSE and INCOME records
     *  are shown mixed together (e.g. the Records list) and any of them may need
     *  its icon/color looked up. */
    @Query("SELECT * FROM categories ORDER BY id ASC")
    List<Category> getAll();

    /** type = RecordType.EXPENSE.name() or RecordType.INCOME.name(). */
    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id ASC")
    List<Category> getByType(String type);

    @Insert
    long insert(Category category);
}
