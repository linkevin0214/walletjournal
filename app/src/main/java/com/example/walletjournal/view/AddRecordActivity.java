package com.example.walletjournal.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.AddRecordContract;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.CategoryPalette;
import com.example.walletjournal.model.RecordType;
import com.example.walletjournal.presenter.AddRecordPresenter;

/**
 * Add Record screen: pick a type (expense / income / transfer). EXPENSE and
 * INCOME each get the same real, DB-backed category picker UI, but backed by
 * their own separate category list (with a "+新增自訂分類" entry that opens
 * AddCategoryActivity, tagging the new category with whichever tab is active).
 * Saving also updates the affected account balance(s) in Room.
 *
 * The category picker is two pieces:
 *  - an inline preview row (up to PREVIEW_SLOTS categories, the current
 *    selection always pinned first so it's never hidden off-row, plus a
 *    trailing "更多" card) built directly into the screen;
 *  - a BottomSheetDialog (dialog_category_picker.xml) showing the full,
 *    searchable grid, opened from the "更多" card.
 */
public class AddRecordActivity extends BaseActivity implements AddRecordContract.IAddRecord_view {

    private static final int GRID_COLUMNS = 4;
    private static final int PREVIEW_SLOTS = 3;

    private AddRecordPresenter presenter;

    private TextView tabExpense;
    private TextView tabIncome;
    private TextView tabTransfer;
    private EditText etAmount;
    private TextView tvAccountValue;
    private TextView tvDateValue;
    private View rowToAccount;
    private View dividerToAccount;
    private TextView tvToAccountValue;

    private View sectionCategoryGrid;
    private LinearLayout containerCategoryGrid;

    private EditText etNote;

    /** Full category list + current selection, kept for the picker sheet. */
    private List<Category> allCategories = new ArrayList<>();
    private long selectedCategoryId = -1;
    /** Tracks the active tab so "+新增自訂分類" tags the new category with the right type. */
    private RecordType currentType = RecordType.EXPENSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        presenter = new AddRecordPresenter(getApplicationContext());

        tabExpense = findViewById(R.id.tab_expense);
        tabIncome = findViewById(R.id.tab_income);
        tabTransfer = findViewById(R.id.tab_transfer);
        etAmount = findViewById(R.id.et_amount);
        tvAccountValue = findViewById(R.id.tv_account_value);
        tvDateValue = findViewById(R.id.tv_date_value);
        rowToAccount = findViewById(R.id.row_to_account);
        dividerToAccount = findViewById(R.id.divider_to_account);
        tvToAccountValue = findViewById(R.id.tv_to_account_value);

        sectionCategoryGrid = findViewById(R.id.section_category_grid);
        containerCategoryGrid = findViewById(R.id.container_category_grid);

        etNote = findViewById(R.id.et_note);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        tabExpense.setOnClickListener(v -> presenter.selectType(RecordType.EXPENSE));
        tabIncome.setOnClickListener(v -> presenter.selectType(RecordType.INCOME));
        tabTransfer.setOnClickListener(v -> presenter.selectType(RecordType.TRANSFER));

        findViewById(R.id.row_account).setOnClickListener(v -> presenter.cycleAccount());
        rowToAccount.setOnClickListener(v -> presenter.cycleToAccount());
        findViewById(R.id.row_date).setOnClickListener(v -> showDatePicker());

        findViewById(R.id.btn_save_record).setOnClickListener(v -> presenter.submit(
                etAmount.getText().toString(), etNote.getText().toString()));

        // Amount just closes the keyboard on Enter — it doesn't jump to note, since the
        // user still needs to pick a category first. Note submits, like tapping 儲存.
        // Numeric keypads (et_amount's inputType="number") often don't report a proper
        // actionId for Enter, so isEnterPressed() also falls back to the raw KeyEvent.
        etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });
        etNote.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard(v);
                v.clearFocus();
                presenter.submit(etAmount.getText().toString(), etNote.getText().toString());
                return true;
            }
            return false;
        });

        presenter.attachView(this);
        presenter.selectType(RecordType.EXPENSE);
        presenter.loadAccounts();
        presenter.loadCategories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Pick up any category just created via AddCategoryActivity.
        presenter.loadCategories();
    }

    @Override
    public void showSelectedType(RecordType type) {
        currentType = type;

        tabExpense.setBackgroundResource(type == RecordType.EXPENSE ? R.drawable.bg_segment_selected : 0);
        tabIncome.setBackgroundResource(type == RecordType.INCOME ? R.drawable.bg_segment_selected : 0);
        tabTransfer.setBackgroundResource(type == RecordType.TRANSFER ? R.drawable.bg_segment_selected : 0);

        tabExpense.setTextColor(getColor(type == RecordType.EXPENSE
                ? R.color.accounts_amount_negative : R.color.accounts_text_secondary));
        tabIncome.setTextColor(getColor(type == RecordType.INCOME
                ? R.color.accounts_text_primary : R.color.accounts_text_secondary));
        tabTransfer.setTextColor(getColor(type == RecordType.TRANSFER
                ? R.color.accounts_text_primary : R.color.accounts_text_secondary));
    }

    @Override
    public void showAccount(String account) {
        tvAccountValue.setText(account);
    }

    @Override
    public void showToAccount(String account) {
        tvToAccountValue.setText(account);
    }

    @Override
    public void showDate(String date) {
        tvDateValue.setText(date);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(presenter.getSelectedDateMillis());
        new DatePickerDialog(this, (picker, year, month, day) -> presenter.selectDate(year, month, day),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void showToAccountRowVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        rowToAccount.setVisibility(visibility);
        dividerToAccount.setVisibility(visibility);
    }

    @Override
    public void showCategoryGrid(List<Category> categories, long selectedCategoryId) {
        allCategories = categories;
        this.selectedCategoryId = selectedCategoryId;

        containerCategoryGrid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        List<Category> preview = previewOrder(categories, selectedCategoryId);
        for (int i = 0; i < PREVIEW_SLOTS; i++) {
            View cell = i < preview.size()
                    ? buildCategoryCard(inflater, containerCategoryGrid, preview.get(i), selectedCategoryId)
                    : new View(this);
            addGridCell(containerCategoryGrid, cell, i > 0);
        }

        View moreCard = inflater.inflate(R.layout.item_category_more, containerCategoryGrid, false);
        moreCard.setOnClickListener(v -> showCategoryPickerSheet());
        addGridCell(containerCategoryGrid, moreCard, true);
    }

    /** Selected category first (always visible in the row), then the rest in DB order. */
    private List<Category> previewOrder(List<Category> categories, long selectedCategoryId) {
        List<Category> ordered = new ArrayList<>(Math.min(categories.size(), PREVIEW_SLOTS));
        Category selected = null;
        for (Category category : categories) {
            if (category.getId() == selectedCategoryId) {
                selected = category;
                break;
            }
        }
        if (selected != null) {
            ordered.add(selected);
        }
        for (Category category : categories) {
            if (ordered.size() >= PREVIEW_SLOTS) {
                break;
            }
            if (category != selected) {
                ordered.add(category);
            }
        }
        return ordered;
    }

    private void addGridCell(LinearLayout row, View cell, boolean withStartMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (withStartMargin) {
            params.leftMargin = dp(8);
        }
        cell.setLayoutParams(params);
        row.addView(cell);
    }

    /** Full, searchable category grid opened from the preview row's "更多" card. */
    private void showCategoryPickerSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
        View sheet = LayoutInflater.from(this).inflate(R.layout.dialog_category_picker, null);
        dialog.setContentView(sheet);
        // Open fully expanded rather than half-peeked, so the grid is usable right away.
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        Window window = dialog.getWindow();
        if (window != null) {
            // Lets the sheet's own rounded-top background show through instead
            // of being clipped to the dialog window's default square background.
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        // BottomSheetDialog also paints its own square background on the internal
        // "design_bottom_sheet" container that wraps our content view — left as-is,
        // it peeks out square behind our rounded top corners. Clear it too so only
        // our bg_bottom_sheet_top_rounded shape is visible.
        View bottomSheetContainer = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheetContainer != null) {
            bottomSheetContainer.setBackgroundResource(android.R.color.transparent);
        }

        LinearLayout pickerGrid = sheet.findViewById(R.id.container_category_picker_grid);
        View noResults = sheet.findViewById(R.id.tv_no_results);
        EditText searchInput = sheet.findViewById(R.id.et_search_category);

        buildPickerGrid(pickerGrid, noResults, "", dialog);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buildPickerGrid(pickerGrid, noResults, s.toString(), dialog);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // The grid already filters live via the TextWatcher above; Enter just closes
        // the keyboard so the filtered results are fully visible.
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_SEARCH)) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });

        sheet.findViewById(R.id.btn_close_picker).setOnClickListener(v -> dialog.dismiss());
        sheet.findViewById(R.id.btn_add_custom_category).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, AddCategoryActivity.class);
            intent.putExtra(AddCategoryActivity.EXTRA_RECORD_TYPE, currentType.name());
            startActivity(intent);
        });

        dialog.show();
    }

    private void buildPickerGrid(LinearLayout container, View noResultsView, String query, BottomSheetDialog dialog) {
        container.removeAllViews();

        String trimmedQuery = query.trim();
        List<Category> matches = new ArrayList<>();
        for (Category category : allCategories) {
            if (trimmedQuery.isEmpty() || category.getName().contains(trimmedQuery)) {
                matches.add(category);
            }
        }

        noResultsView.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
        if (matches.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int rows = (matches.size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
        int index = 0;
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (r > 0) {
                rowParams.topMargin = dp(10);
            }
            row.setLayoutParams(rowParams);

            for (int c = 0; c < GRID_COLUMNS; c++) {
                View cell;
                if (index < matches.size()) {
                    Category category = matches.get(index);
                    cell = buildCategoryCard(inflater, row, category, selectedCategoryId);
                    long categoryId = category.getId();
                    cell.setOnClickListener(v -> {
                        presenter.selectCategory(categoryId);
                        dialog.dismiss();
                    });
                } else {
                    cell = new View(this);
                }

                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                if (c > 0) {
                    cellParams.leftMargin = dp(8);
                }
                cell.setLayoutParams(cellParams);
                row.addView(cell);
                index++;
            }

            container.addView(row);
        }
    }

    private View buildCategoryCard(LayoutInflater inflater, ViewGroup parent, Category category,
                                    long selectedCategoryId) {
        View card = inflater.inflate(R.layout.item_category_grid, parent, false);

        FrameLayout iconCircle = card.findViewById(R.id.item_icon_circle);
        TextView tvIcon = card.findViewById(R.id.item_icon_emoji);
        TextView tvName = card.findViewById(R.id.item_name);

        tvName.setText(category.getName());
        tvIcon.setText(CategoryPalette.emoji(category.getIcon()));
        tvIcon.setTextColor(CategoryPalette.baseColor(category.getColorIndex()));

        Drawable iconBg = iconCircle.getBackground().mutate();
        DrawableCompat.setTint(iconBg, CategoryPalette.pastelColor(category.getColorIndex()));
        iconCircle.setBackground(iconBg);

        boolean selected = category.getId() == selectedCategoryId;
        card.setBackgroundResource(selected
                ? R.drawable.bg_type_card_selected : R.drawable.bg_type_card_unselected);

        long categoryId = category.getId();
        card.setOnClickListener(v -> presenter.selectCategory(categoryId));
        return card;
    }

    @Override
    public void showCategoryGridVisible(boolean visible) {
        sectionCategoryGrid.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
