package com.example.walletjournal.presenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    /** Defaults to now; the time-of-day is kept as-is when the user only changes the date. */
    private long selectedDateMillis = System.currentTimeMillis();

    /** > 0 while editing an existing record; -1 for a fresh (create) screen. */
    private long editingRecordId = -1;
    /** The record being edited, kept so submit()/delete() can undo its old effect. */
    private Record originalRecord;
    /** Category name to select once loadCategories() for its type comes back —
     *  set by loadForEdit(), consumed and cleared the first time it resolves. */
    private String pendingCategoryName;
    /** Account titles to select once loadAccounts() comes back — same idea. */
    private String pendingFromAccountTitle;
    private String pendingToAccountTitle;

    public AddRecordPresenter(Context context) {
        model = new AddRecordModel(context);
    }

    @Override
    public void attachView(AddRecordContract.IAddRecord_view view) {
        this.view = view;
        view.showDate(formatDate(selectedDateMillis));
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

                if (pendingFromAccountTitle != null) {
                    accountIndex = indexOfAccount(pendingFromAccountTitle);
                    pendingFromAccountTitle = null;
                } else {
                    accountIndex = 0;
                }
                if (pendingToAccountTitle != null) {
                    toAccountIndex = indexOfAccount(pendingToAccountTitle);
                    pendingToAccountTitle = null;
                } else {
                    toAccountIndex = accounts.size() > 1 ? 1 : 0;
                }

                view.showAccount(currentAccountLabel(accountIndex));
                view.showToAccount(currentAccountLabel(toAccountIndex));
            });
        });
    }

    /** 0 if not found (e.g. the account was renamed/deleted since the record was created). */
    private int indexOfAccount(String title) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return 0;
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

                if (pendingCategoryName != null) {
                    selectedCategoryId = -1;
                    for (Category category : categories) {
                        if (category.getName().equals(pendingCategoryName)) {
                            selectedCategoryId = category.getId();
                            break;
                        }
                    }
                    pendingCategoryName = null;
                    if (selectedCategoryId == -1 && !categories.isEmpty()) {
                        selectedCategoryId = categories.get(0).getId();
                    }
                } else {
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
    public long getSelectedDateMillis() {
        return selectedDateMillis;
    }

    @Override
    public void selectDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        selectedDateMillis = cal.getTimeInMillis();
        if (view != null) {
            view.showDate(formatDate(selectedDateMillis));
        }
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("yyyy/M/d", Locale.TAIWAN).format(new java.util.Date(millis));
    }

    @Override
    public void loadForEdit(long recordId) {
        if (recordId <= 0) {
            return;
        }
        editingRecordId = recordId;
        AppExecutors.diskIO(() -> {
            Record fetched = model.getRecordById(recordId);
            AppExecutors.mainThread(() -> {
                if (view == null || fetched == null) {
                    return;
                }
                originalRecord = fetched;
                selectedType = RecordType.valueOf(fetched.getType());
                selectedDateMillis = fetched.getCreatedAt();
                pendingFromAccountTitle = fetched.getAccount();
                pendingToAccountTitle = fetched.getToAccount();

                boolean isTransfer = selectedType == RecordType.TRANSFER;
                view.showSelectedType(selectedType);
                view.showToAccountRowVisible(isTransfer);
                view.showCategoryGridVisible(!isTransfer);
                view.showDate(formatDate(selectedDateMillis));
                view.showAmount(String.valueOf(fetched.getAmount()));
                view.showNote(fetched.getNote());

                loadAccounts();
                if (!isTransfer) {
                    pendingCategoryName = fetched.getCategory();
                    loadCategories();
                }
            });
        });
    }

    @Override
    public void delete() {
        if (view == null || originalRecord == null) {
            return;
        }
        Record toDelete = originalRecord;
        AppExecutors.diskIO(() -> {
            model.deleteRecord(toDelete);
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.closeScreen();
                }
            });
        });
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

        boolean isEdit = editingRecordId > 0;
        Record record = new Record(isEdit ? editingRecordId : 0, selectedType.name(), amount,
                fromAccount.getTitle(), toAccountTitle, category, trimmedNote, selectedDateMillis);
        Record editedOriginal = originalRecord;

        AppExecutors.diskIO(() -> {
            if (isEdit) {
                model.updateRecord(editedOriginal, record);
            } else {
                model.addRecord(record);
            }
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
