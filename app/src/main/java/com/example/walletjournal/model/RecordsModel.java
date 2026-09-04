package com.example.walletjournal.model;

import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.RecordsContract;

/**
 * Data layer for the Records list screen. Reads from the Room database.
 */
public class RecordsModel implements RecordsContract.IRecords_model {

    private final AppDatabase db;
    private final RecordDao recordDao;
    private final AccountDao accountDao;
    private final CategoryDao categoryDao;
    private final RecordEffects effects;

    public RecordsModel(Context context) {
        db = AppDatabase.getInstance(context);
        recordDao = db.recordDao();
        accountDao = db.accountDao();
        categoryDao = db.categoryDao();
        effects = new RecordEffects(accountDao);
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

    @Override
    public void deleteRecord(Record record) {
        db.runInTransaction(() -> {
            effects.reverse(record);
            recordDao.delete(record);
        });
    }
}
