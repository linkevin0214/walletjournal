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

    public AddRecordModel(Context context) {
        db = AppDatabase.getInstance(context);
        recordDao = db.recordDao();
        accountDao = db.accountDao();
        categoryDao = db.categoryDao();
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
    public void addRecord(Record record, RecordType type, Account fromAccount, Account toAccount) {
        db.runInTransaction(() -> {
            recordDao.insert(record);
            switch (type) {
                case EXPENSE:
                    accountDao.update(withAmount(fromAccount, fromAccount.getAmount() - record.getAmount()));
                    break;
                case INCOME:
                    accountDao.update(withAmount(fromAccount, fromAccount.getAmount() + record.getAmount()));
                    break;
                case TRANSFER:
                    accountDao.update(withAmount(fromAccount, fromAccount.getAmount() - record.getAmount()));
                    accountDao.update(withAmount(toAccount, toAccount.getAmount() + record.getAmount()));
                    break;
            }
        });
    }

    private Account withAmount(Account account, long newAmount) {
        return new Account(account.getId(), account.getTitle(), account.getSubtitle(), newAmount, account.getType());
    }
}
