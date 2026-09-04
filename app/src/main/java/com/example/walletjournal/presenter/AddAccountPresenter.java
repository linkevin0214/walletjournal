package com.example.walletjournal.presenter;

import android.content.Context;
import android.util.Log;

import com.example.walletjournal.contract.AddAccountContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.AccountType;
import com.example.walletjournal.model.AddAccountModel;
import com.example.walletjournal.model.AppExecutors;

/**
 * Presenter for the Add Account screen. Mediates between AddAccountModel and
 * the View defined in AddAccountContract. The database write happens on a
 * background thread (AppExecutors.diskIO); closing the screen is posted back
 * to the main thread once the write completes.
 */
public class AddAccountPresenter implements AddAccountContract.IAddAccount_presenter {

    private static final String TAG = "WalletJournal";

    private AddAccountContract.IAddAccount_view view;
    private final AddAccountContract.IAddAccount_model model;

    private AccountType selectedType = AccountType.CASH;

    public AddAccountPresenter(Context context) {
        model = new AddAccountModel(context);
    }

    @Override
    public void attachView(AddAccountContract.IAddAccount_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void selectType(AccountType type) {
        selectedType = type;
        if (view == null) {
            return;
        }
        view.showSelectedType(type);
        view.showAccountName(type.getLabel());
    }

    @Override
    public void submit(String name, String openingBalanceText) {
        if (view == null) {
            return;
        }

        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            view.showError("請輸入帳戶名稱");
            return;
        }

        long balance;
        try {
            String trimmedBalance = openingBalanceText == null ? "" : openingBalanceText.trim();
            balance = trimmedBalance.isEmpty() ? 0 : Long.parseLong(trimmedBalance);
        } catch (NumberFormatException e) {
            view.showError("期初餘額請輸入數字");
            return;
        }

        long amount = selectedType == AccountType.CREDIT_CARD ? -balance : balance;
        Account newAccount = new Account(
                trimmedName, selectedType.getDefaultSubtitle(), amount, selectedType.name());

        AppExecutors.diskIO(() -> {
            model.addAccount(newAccount);
            Log.d(TAG, "addAccount: inserted title=" + newAccount.getTitle()
                    + " amount=" + newAccount.getAmount() + " type=" + newAccount.getType());
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.closeScreen();
                }
            });
        });
    }
}
