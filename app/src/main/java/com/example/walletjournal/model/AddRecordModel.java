package com.example.walletjournal.model;

import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.AddRecordContract;

/**
 * Data layer for the Add Record screen: writes into the Room database.
 *
 * NOTE: every method here touches the database and must be called off the
 * main thread (see AppExecutors) — Room throws if used on the UI thread.
 */
public class AddRecordModel implements AddRecordContract.IAddRecord_model {

    private final AppDatabase db;
    private final RecordDao recordDao;
    private final AccountDao accountDao;
    private final CategoryDao categoryDao;
    private final RecordEffects effects;

    public AddRecordModel(Context context) {
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
    public List<Category> getCategories(RecordType type) {
        return categoryDao.getByType(type.name());
    }

    @Override
    public Record getRecordById(long id) {
        return recordDao.getById(id);
    }

    @Override
    public void addRecord(Record record) {
        db.runInTransaction(() -> {
            recordDao.insert(record);
            effects.apply(record);
        });
    }

    @Override
    public void updateRecord(Record originalRecord, Record updatedRecord) {
        db.runInTransaction(() -> {
            effects.reverse(originalRecord);
            effects.apply(updatedRecord);
            recordDao.update(updatedRecord);
        });
    }

    @Override
    public void deleteRecord(Record record) {
        db.runInTransaction(() -> {
            effects.reverse(record);
            recordDao.delete(record);
        });
    }
}
