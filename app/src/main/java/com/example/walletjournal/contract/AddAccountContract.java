package com.example.walletjournal.contract;

import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.AccountType;

/**
 * MVP contract for the Add Account screen.
 */
public interface AddAccountContract {

    interface IAddAccount_view extends BaseContract.IBase_View {
        void showSelectedType(AccountType type);
        void showAccountName(String name);
        void closeScreen();
    }

    interface IAddAccount_model extends BaseContract.IBase_Model {
        void addAccount(Account account);
    }

    interface IAddAccount_presenter extends BaseContract.IBase_Presenter<IAddAccount_view> {
        void selectType(AccountType type);
        void submit(String name, String openingBalanceText);
    }
}
