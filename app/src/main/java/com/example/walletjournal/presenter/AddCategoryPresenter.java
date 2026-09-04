package com.example.walletjournal.presenter;

import android.content.Context;

import com.example.walletjournal.contract.AddCategoryContract;
import com.example.walletjournal.model.AddCategoryModel;
import com.example.walletjournal.model.AppExecutors;
import com.example.walletjournal.model.Category;
import com.example.walletjournal.model.CategoryPalette;
import com.example.walletjournal.model.RecordType;

/**
 * Presenter for the Add Category screen. The database write happens on a
 * background thread (AppExecutors.diskIO); closing the screen is posted
 * back to the main thread once it completes.
 */
public class AddCategoryPresenter implements AddCategoryContract.IAddCategory_presenter {

    private AddCategoryContract.IAddCategory_view view;
    private final AddCategoryContract.IAddCategory_model model;
    /** EXPENSE or INCOME — which list the new category is saved into. */
    private final RecordType type;

    private String selectedIcon = CategoryPalette.ICON_KEYS[0];
    private int selectedColorIndex = 0;

    public AddCategoryPresenter(Context context, RecordType type) {
        model = new AddCategoryModel(context);
        this.type = type;
    }

    @Override
    public void attachView(AddCategoryContract.IAddCategory_view view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void selectIcon(String icon) {
        selectedIcon = icon;
        if (view != null) {
            view.showSelection(selectedIcon, selectedColorIndex);
        }
    }

    @Override
    public void selectColor(int colorIndex) {
        selectedColorIndex = colorIndex;
        if (view != null) {
            view.showSelection(selectedIcon, selectedColorIndex);
        }
    }

    @Override
    public void submit(String name) {
        if (view == null) {
            return;
        }

        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            view.showError("請輸入分類名稱");
            return;
        }

        Category category = new Category(trimmedName, selectedIcon, selectedColorIndex, type.name());

        AppExecutors.diskIO(() -> {
            model.addCategory(category);
            AppExecutors.mainThread(() -> {
                if (view != null) {
                    view.closeScreen();
                }
            });
        });
    }
}
