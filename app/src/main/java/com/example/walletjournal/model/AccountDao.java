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

    /** Null if no account with that title exists (e.g. it was renamed/deleted). Records
     *  store the account by title rather than id, so this is how effects on a record
     *  (add/edit/delete) resolve which account row to adjust. */
    @Query("SELECT * FROM accounts WHERE title = :title LIMIT 1")
    Account getByTitle(String title);

    @Insert
    long insert(Account account);

    @Update
    void update(Account account);
}
