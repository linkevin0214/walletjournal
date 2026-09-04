package com.example.walletjournal.presenter;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.example.walletjournal.contract.AccountsContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.AccountsModel;
import com.example.walletjournal.model.AppExecutors;

/**
 * Presenter for the Accounts screen. Mediates between AccountsModel and the
 * View defined in AccountsContract. Database reads happen on a background
 * thread (AppExecutors.diskIO) and results are posted back to the main thread.
 */
public class AccountsPresenter implements AccountsContract.IAccounts_presenter {

    private static final String TAG = "WalletJournal";

    private AccountsContract.IAccounts_view view;
    private final AccountsContract.IAccounts_model model;

    public AccountsPresenter(Context context) {
        model = new AccountsModel(context);
    }

    @Override
    public void attachView(AccountsContract.IAccounts_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadAccounts() {
        if (view == null) {
            return;
        }

        AppExecutors.diskIO(() -> {
            List<Account> accounts = model.getAccounts();
            Log.d(TAG, "loadAccounts: fetched " + accounts.size() + " row(s) from DB");

            long total = 0;
            for (Account account : accounts) {
                total += account.getAmount();
            }
            String formattedTotal = "NT$ " + formatAmount(total);

            AppExecutors.mainThread(() -> {
                if (view == null) {
                    return;
                }
                view.showTotalAssets(formattedTotal);
                view.showAccounts(accounts);
            });
        });
    }

    private String formatAmount(long amount) {
        return String.format(Locale.TAIWAN, "%,d", amount);
    }
}
