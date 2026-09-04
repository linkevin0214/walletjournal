package com.example.walletjournal.model;

import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.AccountsContract;

/**
 * Data layer for the Accounts screen. Reads from the Room database so
 * accounts added via AddAccountActivity show up here too.
 *
 * NOTE: getAccounts() touches the database and must be called off the main
 * thread (see AppExecutors) — Room throws if queried on the UI thread.
 */
public class AccountsModel implements AccountsContract.IAccounts_model {

    private final AccountDao accountDao;

    public AccountsModel(Context context) {
        accountDao = AppDatabase.getInstance(context).accountDao();
    }

    @Override
    public List<Account> getAccounts() {
        return accountDao.getAll();
    }
}
