package com.example.walletjournal.model;

import android.content.Context;

import com.example.walletjournal.contract.AddAccountContract;

/**
 * Data layer for the Add Account screen: writes into the Room database.
 *
 * NOTE: addAccount() touches the database and must be called off the main
 * thread (see AppExecutors) — Room throws if written to on the UI thread.
 */
public class AddAccountModel implements AddAccountContract.IAddAccount_model {

    private final AccountDao accountDao;

    public AddAccountModel(Context context) {
        accountDao = AppDatabase.getInstance(context).accountDao();
    }

    @Override
    public void addAccount(Account account) {
        accountDao.insert(account);
    }
}
