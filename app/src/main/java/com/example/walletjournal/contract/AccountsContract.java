package com.example.walletjournal.contract;

import java.util.List;

import com.example.walletjournal.model.Account;

/**
 * MVP contract for the Accounts screen.
 */
public interface AccountsContract {

    interface IAccounts_view extends BaseContract.IBase_View {
        void showTotalAssets(String formattedTotal);
        void showAccounts(List<Account> accounts);
    }

    interface IAccounts_model extends BaseContract.IBase_Model {
        List<Account> getAccounts();
    }

    interface IAccounts_presenter extends BaseContract.IBase_Presenter<IAccounts_view> {
        void loadAccounts();
    }
}
