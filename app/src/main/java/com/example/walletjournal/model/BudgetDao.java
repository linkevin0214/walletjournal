package com.example.walletjournal.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BudgetDao {

    @Query("SELECT * FROM budgets")
    List<Budget> getAll();

    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    Budget getByCategory(String category);

    /** Insert, or replace if a budget for this category already exists (category is the primary key). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(Budget budget);
}
