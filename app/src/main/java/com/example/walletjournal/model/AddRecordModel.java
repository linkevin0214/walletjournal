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
    public Record getRecordById(long id) {
        return recordDao.getById(id);
    }

    @Override
    public void addRecord(Record record) {
        db.runInTransaction(() -> {
            recordDao.insert(record);
            applyEffect(record);
        });
    }

    @Override
    public void updateRecord(Record originalRecord, Record updatedRecord) {
        db.runInTransaction(() -> {
            reverseEffect(originalRecord);
            applyEffect(updatedRecord);
            recordDao.update(updatedRecord);
        });
    }

    @Override
    public void deleteRecord(Record record) {
        db.runInTransaction(() -> {
            reverseEffect(record);
            recordDao.delete(record);
        });
    }

    /** Applies a record's amount to its account balance(s) — subtracting for an
     *  EXPENSE, adding for an INCOME, moving between accounts for a TRANSFER. */
    private void applyEffect(Record record) {
        adjustAccounts(record, 1);
    }

    /** Undoes applyEffect() for this record — used when editing or deleting it. */
    private void reverseEffect(Record record) {
        adjustAccounts(record, -1);
    }

    /**
     * Accounts are resolved fresh from Room right before each update (rather than
     * reusing an Account instance the caller already had), so this stays correct
     * even when reverseEffect() and applyEffect() touch the same account back to
     * back within one transaction — e.g. editing a record without changing its
     * account. Missing accounts (renamed/deleted since the record was created) are
     * skipped rather than crashing.
     */
    private void adjustAccounts(Record record, int sign) {
        RecordType type = RecordType.valueOf(record.getType());
        Account fromAccount = accountDao.getByTitle(record.getAccount());
        switch (type) {
            case EXPENSE:
                if (fromAccount != null) {
                    accountDao.update(withAmount(fromAccount, fromAccount.getAmount() - sign * record.getAmount()));
                }
                break;
            case INCOME:
                if (fromAccount != null) {
                    accountDao.update(withAmount(fromAccount, fromAccount.getAmount() + sign * record.getAmount()));
                }
                break;
            case TRANSFER:
                Account toAccount = accountDao.getByTitle(record.getToAccount());
                if (fromAccount != null) {
                    accountDao.update(withAmount(fromAccount, fromAccount.getAmount() - sign * record.getAmount()));
                }
                if (toAccount != null) {
                    accountDao.update(withAmount(toAccount, toAccount.getAmount() + sign * record.getAmount()));
                }
                break;
        }
    }

    private Account withAmount(Account account, long newAmount) {
        return new Account(account.getId(), account.getTitle(), account.getSubtitle(), newAmount, account.getType());
    }
}
