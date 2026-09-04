package com.example.walletjournal.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.AccountsContract;
import com.example.walletjournal.model.Account;
import com.example.walletjournal.presenter.AccountsPresenter;

/**
 * Accounts overview screen: total assets + the full list of account rows,
 * fetched from Room and rendered in a RecyclerView.
 */
public class AccountsActivity extends BaseActivity implements AccountsContract.IAccounts_view {

    private AccountsPresenter presenter;

    private TextView tvTotalAssets;
    private RecyclerView rvAccounts;
    private AccountsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        presenter = new AccountsPresenter(getApplicationContext());

        tvTotalAssets = findViewById(R.id.tv_total_assets);

        rvAccounts = findViewById(R.id.rv_accounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AccountsAdapter();
        rvAccounts.setAdapter(adapter);

        findViewById(R.id.btn_add_account).setOnClickListener(v ->
                startActivity(new Intent(this, AddAccountActivity.class)));

        findViewById(R.id.btn_fab_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddRecordActivity.class)));

        findViewById(R.id.tab_list).setOnClickListener(v ->
                startActivity(new Intent(this, RecordsActivity.class)));

        findViewById(R.id.tab_stats).setOnClickListener(v ->
                startActivity(new Intent(this, StatsActivity.class)));

        presenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.loadAccounts();
    }

    @Override
    public void showTotalAssets(String formattedTotal) {
        tvTotalAssets.setText(formattedTotal);
    }

    @Override
    public void showAccounts(List<Account> accounts) {
        adapter.submitList(accounts);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }
}
