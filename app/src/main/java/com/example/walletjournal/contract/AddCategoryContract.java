package com.example.walletjournal.contract;

import com.example.walletjournal.model.Category;

/**
 * MVP contract for the Add Category screen.
 */
public interface AddCategoryContract {

    interface IAddCategory_view extends BaseContract.IBase_View {
        /** icon = one of CategoryPalette.ICON_KEYS, colorIndex = index into CategoryPalette colors. */
        void showSelection(String icon, int colorIndex);
        void closeScreen();
    }

    interface IAddCategory_model extends BaseContract.IBase_Model {
        void addCategory(Category category);
    }

    interface IAddCategory_presenter extends BaseContract.IBase_Presenter<IAddCategory_view> {
        void selectIcon(String icon);
        void selectColor(int colorIndex);
        void submit(String name);
    }
}
