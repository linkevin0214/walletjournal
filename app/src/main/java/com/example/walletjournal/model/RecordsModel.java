package com.example.walletjournal.model;

import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.RecordsContract;

/**
 * Data layer for the Records list screen. Reads from the Room database.
 */
public class RecordsModel implements RecordsContract.IRecords_model {

    private final RecordDao recordDao;
    private final AccountDao accountDao;
    private final CategoryDao categoryDao;

    public RecordsModel(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        recordDao = db.recordDao();
        accountDao = db.accountDao();
        categoryDao = db.categoryDao();
    }

    @Override
    public List<Account> getAccounts() {
        return accountDao.getAll();
    }

    @Override
    public List<Category> getCategories() {
        return categoryDao.getAll();
    }

    @Override
    public List<Record> getRecords() {
        return recordDao.getAll();
    }

    @Override
    public int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }
}
