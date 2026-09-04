package com.example.walletjournal.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.BudgetsContract;
import com.example.walletjournal.model.CategoryBudget;
import com.example.walletjournal.presenter.BudgetsPresenter;

/**
 * Budgets screen: set a monthly budget per fixed expense category.
 * Rows are built programmatically (there are only a handful of categories,
 * and each needs an editable amount field — simpler than a RecyclerView
 * that would have to survive view recycling while being edited).
 */
public class BudgetsActivity extends BaseActivity implements BudgetsContract.IBudgets_view {

    private BudgetsPresenter presenter;

    private LinearLayout container;
    private final List<String> categories = new ArrayList<>();
    private final List<EditText> amountFields = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budgets);

        presenter = new BudgetsPresenter(getApplicationContext());

        container = findViewById(R.id.container_budgets);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_budgets).setOnClickListener(v -> submit());

        presenter.attachView(this);
        presenter.loadBudgets();
    }

    @Override
    public void showBudgets(List<CategoryBudget> budgets) {
        container.removeAllViews();
        categories.clear();
        amountFields.clear();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CategoryBudget budget : budgets) {
            View row = inflater.inflate(R.layout.item_budget_row, container, false);

            TextView label = row.findViewById(R.id.row_category_label);
            EditText amount = row.findViewById(R.id.row_amount);

            label.setText(budget.getCategory());
            if (budget.getAmount() > 0) {
                amount.setText(String.valueOf(budget.getAmount()));
            }

            container.addView(row);
            categories.add(budget.getCategory());
            amountFields.add(amount);
        }
    }

    private void submit() {
        List<CategoryBudget> result = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            String text = amountFields.get(i).getText().toString().trim();
            long amount = 0;
            if (!text.isEmpty()) {
                try {
                    amount = Long.parseLong(text);
                } catch (NumberFormatException e) {
                    amount = 0;
                }
            }
            result.add(new CategoryBudget(categories.get(i), amount));
        }
        presenter.save(result);
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
}
