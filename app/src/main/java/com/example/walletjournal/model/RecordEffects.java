package com.example.walletjournal.model;

/**
 * Applies/reverses a Record's effect on its account balance(s) — subtracting
 * for an EXPENSE, adding for an INCOME, moving between accounts for a
 * TRANSFER. Shared by AddRecordModel (add/edit/delete on that screen) and
 * RecordsModel (swipe-to-delete on the list), so the two never drift apart.
 *
 * Accounts are always resolved fresh from Room right before each update
 * (never a caller-held instance), so a reverse() immediately followed by an
 * apply() on the same account — editing a record without changing its
 * account, say — stays correct. An account missing since the record was
 * created (renamed/deleted) is skipped rather than crashing.
 *
 * Callers must run apply()/reverse() inside the same db.runInTransaction()
 * as whatever they do to the "records" table, same as before this was
 * extracted — this class only touches "accounts".
 */
public class RecordEffects {

    private final AccountDao accountDao;

    public RecordEffects(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void apply(Record record) {
        adjustAccounts(record, 1);
    }

    /** Undoes apply() for this record — used when editing or deleting it. */
    public void reverse(Record record) {
        adjustAccounts(record, -1);
    }

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
