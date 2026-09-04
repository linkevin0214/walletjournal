package com.example.walletjournal.presenter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.example.walletjournal.contract.AddRecordContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.model.AddRecordModel;
import com.example.walletjournal.model.AppExecutors;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.Record;
import com.example.walletjournal.model.RecordType;

/**
 * Presenter for the Add Record screen. Mediates between AddRecordModel and
 * the View defined in AddRecordContract.
 *
 * EXPENSE and INCOME each use the same real, DB-backed category grid UI, but
 * with their own separate category list (Category.type, editable via Add
 * Category) — switching tabs reloads the grid for the newly selected type.
 * Accounts are the real ones from Room. The database write happens on a
 * background thread (AppExecutors.diskIO); closing the screen is posted
 * back to the main thread once it completes.
 */
public class AddRecordPresenter implements AddRecordContract.IAddRecord_presenter {

    private AddRecordContract.IAddRecord_view view;
    private final AddRecordContract.IAddRecord_model model;

    private List<Account> accounts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private RecordType selectedType = RecordType.EXPENSE;
    private int accountIndex = 0;
    private int toAccountIndex = 0;
    private long selectedCategoryId = -1;

    public AddRecordPresenter(Context context) {
        model = new AddRecordModel(context);
    }

    @Override
    public void attachView(AddRecordContract.IAddRecord_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void loadAccounts() {
        AppExecutors.diskIO(() -> {
            List<Account> fetched = model.getAccounts();
            AppExecutors.mainThread(() -> {
                if (view == null) {
                    return;
                }
                accounts = fetched;
                accountIndex = 0;
                toAccountIndex = accounts.size() > 1 ? 1 : 0;
                view.showAccount(currentAccountLabel(accountIndex));
                view.showToAccount(currentAccountLabel(toAccountIndex));
            });
        });
    }

    @Override
    public void loadCategories() {
        // TRANSFER has no category grid to feed — nothing to load.
        if (selectedType == RecordType.TRANSFER) {
            return;
        }
        RecordType typeAtRequestTime = selectedType;
        AppExecutors.diskIO(() -> {
            List<Category> fetched = model.getCategories(typeAtRequestTime);
            AppExecutors.mainThread(() -> {
                // The user may have switched EXPENSE/INCOME again while this was in
                // flight — a stale result for the type no longer selected would show
                // the wrong list, so drop it.
                if (view == null || typeAtRequestTime != selectedType) {
                    return;
                }
                categories = fetched;

                boolean stillPresent = false;
                for (Category category : categories) {
                    if (category.getId() == selectedCategoryId) {
                        stillPresent = true;
                        break;
                    }
                }
                if (!stillPresent && !categories.isEmpty()) {
                    selectedCategoryId = categories.get(0).getId();
                }

                // Always push, even if the grid isn't visible right now — this is the
                // ONLY place that seeds it, so it's never shown starting from an empty
                // list (RecyclerView-in-ScrollView doesn't reliably resize when going
                // from few items to many via notifyDataSetChanged()).
                view.showCategoryGrid(categories, selectedCategoryId);
            });
        });
    }

    @Override
    public void selectType(RecordType type) {
        boolean typeChanged = type != selectedType;
        selectedType = type;
        if (view == null) {
            return;
        }
        view.showSelectedType(type);

        boolean isTransfer = type == RecordType.TRANSFER;

        view.showToAccountRowVisible(isTransfer);
        // EXPENSE and INCOME each show their own category grid; only TRANSFER hides it.
        view.showCategoryGridVisible(!isTransfer);

        // EXPENSE and INCOME keep separate category lists, so switching between them
        // needs a fresh fetch — the grid's data is otherwise pushed exclusively by
        // loadCategories() (see the comment there).
        if (typeChanged && !isTransfer) {
            selectedCategoryId = -1;
            loadCategories();
        }
    }

    @Override
    public void cycleAccount() {
        if (accounts.isEmpty()) {
            return;
        }
        accountIndex = (accountIndex + 1) % accounts.size();
        if (view != null) {
            view.showAccount(currentAccountLabel(accountIndex));
        }
    }

    @Override
    public void cycleToAccount() {
        if (accounts.isEmpty()) {
            return;
        }
        toAccountIndex = (toAccountIndex + 1) % accounts.size();
        if (view != null) {
            view.showToAccount(currentAccountLabel(toAccountIndex));
        }
    }

    @Override
    public void selectCategory(long categoryId) {
        selectedCategoryId = categoryId;
        if (view != null) {
            view.showCategoryGrid(categories, selectedCategoryId);
        }
    }

    @Override
    public void submit(String amountText, String note) {
        if (view == null) {
            return;
        }

        if (accounts.isEmpty()) {
            view.showError("請先新增帳戶");
            return;
        }

        String trimmedAmount = amountText == null ? "" : amountText.trim();
        if (trimmedAmount.isEmpty()) {
            view.showError("請輸入金額");
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(trimmedAmount);
        } catch (NumberFormatException e) {
            view.showError("金額請輸入數字");
            return;
        }
        if (amount <= 0) {
            view.showError("金額需大於 0");
            return;
        }

        Account fromAccount = accounts.get(accountIndex);
        Account toAccount = null;

        if (selectedType == RecordType.TRANSFER) {
            if (accounts.size() < 2) {
                view.showError("至少需要 2 個帳戶才能轉帳");
                return;
            }
            toAccount = accounts.get(toAccountIndex);
            if (toAccount.getId() == fromAccount.getId()) {
                view.showError("轉出、轉入帳戶不可相同");
                return;
            }
        } else if (categories.isEmpty()) {
            view.showError("請先新增分類");
            return;
        }

        String category = resolveCategory();
        String trimmedNote = note == null ? "" : note.trim();
        String toAccountTitle = toAccount == null ? null : toAccount.getTitle();

        Record record = new Record(selectedType.name(), amount, fromAccount.getTitle(), toAccountTitle,
                category, trimmedNote, System.currentTimeMillis());

        Account finalFromAccount = fromAccount;
        Account finalToAccount = toAccount;
        RecordType finalType = selectedType;

        AppExecutors.diskIO(() -> {
            model.addRecord(record, finalType, finalFromAccount, finalToAccount);
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.closeScreen();
                }
            });
        });
    }

    private String resolveCategory() {
        if (selectedType == RecordType.TRANSFER) {
            return null;
        }
        for (Category category : categories) {
            if (category.getId() == selectedCategoryId) {
                return category.getName();
            }
        }
        return categories.isEmpty() ? "" : categories.get(0).getName();
    }

    private String currentAccountLabel(int index) {
        return accounts.isEmpty() ? "尚未有帳戶" : accounts.get(index).getTitle();
    }
}
