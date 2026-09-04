package com.example.walletjournal.model;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    List<Account> getAll();

    @Insert
    long insert(Account account);

    @Update
    void update(Account account);
}
