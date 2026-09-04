package com.example.walletjournal.contract;

import java.util.List;

import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.Record;
import com.example.walletjournal.model.RecordType;

/**
 * MVP contract for the Records list screen.
 */
public interface RecordsContract {

    interface IRecords_view extends BaseContract.IBase_View {
        /** "全部帳戶" + one entry per account; selectedIndex 0 = all accounts. */
        void showFilters(List<Account> accounts, int selectedIndex);
        /** null selectedType = 全部類型 (no type filter). TRANSFER isn't offered as a chip. */
        void showTypeFilters(RecordType selectedType);
        /** startMillis < 0 = no date filter (全部日期); otherwise the active [startMillis, endMillis] range. */
        void showDateFilter(long startMillis, long endMillis);
        /** periodLabel: e.g. "9月結餘" with no date filter, or "9/1 - 9/15 收支" with one. Both totals
         *  exclude TRANSFER records and ignore the type filter; balance = totalIncome - totalExpense. */
        void showSummary(String periodLabel, long totalExpense, long totalIncome);
        /** categories is used to resolve each record's real icon/color (built-in + custom).
         *  hasAnyRecords is true as long as the account has ANY record at all, regardless of
         *  the active filters/search — it's what tells an empty `records` apart from a true
         *  cold start (no records ever entered) vs. a filter/search that just matched nothing,
         *  so the empty-state message can point at the right fix. */
        void showRecords(List<Record> records, List<Category> categories, boolean hasAnyRecords);
    }

    interface IRecords_model extends BaseContract.IBase_Model {
        List<Account> getAccounts();
        List<Record> getRecords();
        List<Category> getCategories();

        /** 1-12. */
        int getCurrentMonth();

        /** Reverses the record's effect on its account balance(s) and removes it. */
        void deleteRecord(Record record);
    }

    interface IRecords_presenter extends BaseContract.IBase_Presenter<IRecords_view> {
        void loadData();

        /** 0 = 全部帳戶, else 1-based index into the accounts list from loadData(). */
        void selectFilter(int index);

        /** null = 全部類型 (clears the type filter). */
        void selectTypeFilter(RecordType type);

        /** Inclusive range in epoch millis; pass the whole day's bounds for each end. */
        void selectDateRange(long startMillis, long endMillis);

        void clearDateRange();

        /** Deletes a record (e.g. from swiping it away) and reloads the list. */
        void deleteRecord(Record record);

        /** Free-text search over note/category/account/toAccount, live as the user
         *  types. Narrows only the list, same as the type filter — never the summary
         *  tiles. Empty/null clears it. */
        void setSearchQuery(String query);
    }
}
