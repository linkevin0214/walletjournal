package com.example.walletjournal.presenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.example.walletjournal.contract.RecordsContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.AppExecutors;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.Record;
import com.example.walletjournal.model.RecordType;
import com.example.walletjournal.model.RecordsModel;

/**
 * Presenter for the Records list screen. Accounts + all records are fetched
 * once from Room (background thread); switching any filter (account, type,
 * or date range) is then pure in-memory filtering, no extra DB hit.
 */
public class RecordsPresenter implements RecordsContract.IRecords_presenter {

    private RecordsContract.IRecords_view view;
    private final RecordsContract.IRecords_model model;

    private List<Account> accounts = new ArrayList<>();
    private List<Record> allRecords = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private int selectedFilterIndex = 0;
    private int currentMonth = 1;

    /** null = 全部類型 — the type filter only narrows the record list, not the summary tiles. */
    private RecordType typeFilter = null;
    /** < 0 = 全部日期 (no date filter active). Inclusive [dateRangeStart, dateRangeEnd]. */
    private long dateRangeStart = -1;
    private long dateRangeEnd = -1;
    /** Lowercased, trimmed; empty = no search filter active. */
    private String searchQuery = "";

    public RecordsPresenter(Context context) {
        model = new RecordsModel(context);
    }

    @Override
    public void attachView(RecordsContract.IRecords_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadData() {
        if (view == null) {
            return;
        }
        AppExecutors.diskIO(() -> {
            List<Account> fetchedAccounts = model.getAccounts();
            List<Record> fetchedRecords = model.getRecords();
            List<Category> fetchedCategories = model.getCategories();
            int month = model.getCurrentMonth();
            AppExecutors.mainThread(() -> {
                if (view == null) {
                    return;
                }
                accounts = fetchedAccounts;
                allRecords = fetchedRecords;
                categories = fetchedCategories;
                currentMonth = month;
                if (selectedFilterIndex > accounts.size()) {
                    selectedFilterIndex = 0;
                }
                applyFilter();
            });
        });
    }

    @Override
    public void selectFilter(int index) {
        selectedFilterIndex = index;
        applyFilter();
    }

    @Override
    public void selectTypeFilter(RecordType type) {
        typeFilter = type;
        applyFilter();
    }

    @Override
    public void selectDateRange(long startMillis, long endMillis) {
        dateRangeStart = startMillis;
        dateRangeEnd = endMillis;
        applyFilter();
    }

    @Override
    public void clearDateRange() {
        dateRangeStart = -1;
        dateRangeEnd = -1;
        applyFilter();
    }

    @Override
    public void deleteRecord(Record record) {
        AppExecutors.diskIO(() -> {
            model.deleteRecord(record);
            AppExecutors.mainThread(this::loadData);
        });
    }

    @Override
    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.trim().toLowerCase(Locale.TAIWAN);
        applyFilter();
    }

    private void applyFilter() {
        if (view == null) {
            return;
        }

        view.showFilters(accounts, selectedFilterIndex);
        view.showTypeFilters(typeFilter);
        view.showDateFilter(dateRangeStart, dateRangeEnd);

        String accountFilter = selectedFilterIndex == 0 ? null : accounts.get(selectedFilterIndex - 1).getTitle();
        boolean hasDateRange = dateRangeStart >= 0;
        // Computed once per call rather than via isCurrentMonth()'s old per-record
        // Calendar.getInstance() pair — that ran unconditionally over every one of
        // allRecords (not just search-matched ones) on every keystroke, and
        // Calendar.getInstance() itself (not the field math) is the expensive part.
        // At 10000 records this alone was enough to make typing in the search box
        // feel laggy.
        long[] currentMonthRange = currentMonthRange();
        long currentMonthStart = currentMonthRange[0];
        long currentMonthEnd = currentMonthRange[1];

        long totalExpense = 0;
        long totalIncome = 0;
        List<Record> filtered = new ArrayList<>();
        for (Record record : allRecords) {
            boolean matchesAccount = accountFilter == null
                    || accountFilter.equals(record.getAccount())
                    || accountFilter.equals(record.getToAccount());
            if (!matchesAccount) {
                continue;
            }

            // The list: an explicit date range restricts it; with none, it shows full
            // history like before. The type and search filters only ever narrow the list.
            boolean withinListRange = !hasDateRange || isWithinRange(record.getCreatedAt());
            boolean matchesType = typeFilter == null || typeFilter.name().equals(record.getType());
            boolean matchesQuery = searchQuery.isEmpty()
                    || containsQuery(record.getCategory())
                    || containsQuery(record.getNote())
                    || containsQuery(record.getAccount())
                    || containsQuery(record.getToAccount());
            if (withinListRange && matchesType && matchesQuery) {
                filtered.add(record);
            }

            // The summary tiles: current month by default, or the active date range —
            // independent of the type filter, since expense/income ARE the breakdown.
            // Transfers move money between the user's own accounts, so they never count.
            boolean fromThisAccount = accountFilter == null || accountFilter.equals(record.getAccount());
            boolean withinSummaryPeriod = hasDateRange
                    ? isWithinRange(record.getCreatedAt())
                    : (record.getCreatedAt() >= currentMonthStart && record.getCreatedAt() < currentMonthEnd);
            if (fromThisAccount && withinSummaryPeriod) {
                if (RecordType.EXPENSE.name().equals(record.getType())) {
                    totalExpense += record.getAmount();
                } else if (RecordType.INCOME.name().equals(record.getType())) {
                    totalIncome += record.getAmount();
                }
            }
        }

        view.showSummary(periodLabel(), totalExpense, totalIncome);
        view.showRecords(filtered, categories, !allRecords.isEmpty());
    }

    private boolean containsQuery(String value) {
        return value != null && value.toLowerCase(Locale.TAIWAN).contains(searchQuery);
    }

    private boolean isWithinRange(long millis) {
        return millis >= dateRangeStart && millis <= dateRangeEnd;
    }

    /** {start, endExclusive} of the current calendar month, in epoch millis. */
    private long[] currentMonthRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        return new long[] {start, cal.getTimeInMillis()};
    }

    private String periodLabel() {
        if (dateRangeStart < 0) {
            return currentMonth + "月結餘";
        }
        SimpleDateFormat format = new SimpleDateFormat("M/d", Locale.TAIWAN);
        return format.format(new Date(dateRangeStart)) + " - " + format.format(new Date(dateRangeEnd)) + " 收支";
    }
}
