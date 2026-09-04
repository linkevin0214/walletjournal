package com.example.walletjournal.model;

import android.content.Context;

import com.example.walletjournal.contract.AddCategoryContract;

/**
 * Data layer for the Add Category screen: writes into the Room database.
 */
public class AddCategoryModel implements AddCategoryContract.IAddCategory_model {

    private final CategoryDao categoryDao;

    public AddCategoryModel(Context context) {
        categoryDao = AppDatabase.getInstance(context).categoryDao();
    }

    @Override
    public void addCategory(Category category) {
        categoryDao.insert(category);
    }
}
