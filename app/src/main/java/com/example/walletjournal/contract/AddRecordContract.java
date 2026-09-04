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
        void showDate(String date);
        /** Edit mode only — prefills the amount/note fields from the record being edited. */
        void showAmount(String amount);
        void showNote(String note);

        /** Real, DB-backed category grid for the currently selected type + a trailing "add new" card. */
        void showCategoryGrid(List<Category> categories, long selectedCategoryId);
        void showCategoryGridVisible(boolean visible);

        void closeScreen();
    }

    interface IAddRecord_model extends BaseContract.IBase_Model {
        List<Account> getAccounts();

        /** EXPENSE and INCOME each keep their own category list. */
        List<Category> getCategories(RecordType type);

        void addRecord(Record record);

        /** Null if no record with that id exists (e.g. it was deleted elsewhere). */
        Record getRecordById(long id);

        /** Reverses originalRecord's effect on its account balance(s), then applies
         *  updatedRecord's — both resolved fresh from Room by account title, so this
         *  stays correct even when the account is unchanged. */
        void updateRecord(Record originalRecord, Record updatedRecord);

        /** Reverses the record's effect on its account balance(s) and removes it. */
        void deleteRecord(Record record);
    }

    interface IAddRecord_presenter extends BaseContract.IBase_Presenter<IAddRecord_view> {
        void loadAccounts();
        void loadCategories();
        void selectType(RecordType type);
        void cycleAccount();
        void cycleToAccount();
        void selectCategory(long categoryId);
        long getSelectedDateMillis();
        void selectDate(int year, int month, int day);

        /** Loads an existing record and prefills the screen for editing it; call
         *  instead of selectType()/loadAccounts()/loadCategories() when editing. */
        void loadForEdit(long recordId);
        void submit(String amountText, String note);

        /** Deletes the record currently being edited; a no-op outside edit mode. */
        void delete();
    }
}
