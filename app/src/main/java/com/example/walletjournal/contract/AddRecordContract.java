package com.example.walletjournal.contract;

import java.util.List;

import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.Record;
import com.example.walletjournal.model.RecordType;

/**
 * MVP contract for the Add Record screen.
 */
public interface AddRecordContract {

    interface IAddRecord_view extends BaseContract.IBase_View {
        void showSelectedType(RecordType type);
        void showAccount(String account);
        void showToAccount(String account);
        void showToAccountRowVisible(boolean visible);

        /** Real, DB-backed category grid for the currently selected type + a trailing "add new" card. */
        void showCategoryGrid(List<Category> categories, long selectedCategoryId);
        void showCategoryGridVisible(boolean visible);

        void closeScreen();
    }

    interface IAddRecord_model extends BaseContract.IBase_Model {
        List<Account> getAccounts();

        /** EXPENSE and INCOME each keep their own category list. */
        List<Category> getCategories(RecordType type);

        /** toAccount may be null unless type is TRANSFER. */
        void addRecord(Record record, RecordType type, Account fromAccount, Account toAccount);
    }

    interface IAddRecord_presenter extends BaseContract.IBase_Presenter<IAddRecord_view> {
        void loadAccounts();
        void loadCategories();
        void selectType(RecordType type);
        void cycleAccount();
        void cycleToAccount();
        void selectCategory(long categoryId);
        void submit(String amountText, String note);
    }
}
