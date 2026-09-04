package com.example.walletjournal.view;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.AddAccountContract;
import com.example.walletjournal.model.AccountType;
import com.example.walletjournal.presenter.AddAccountPresenter;

/**
 * Add Account screen: pick a type (cash / bank / credit card), fill in a
 * name and opening balance, then save it into the shared AccountRepository.
 */
public class AddAccountActivity extends BaseActivity implements AddAccountContract.IAddAccount_view {

    private AddAccountPresenter presenter;

    private LinearLayout typeCash;
    private LinearLayout typeBank;
    private LinearLayout typeCreditCard;
    private EditText etAccountName;
    private EditText etOpeningBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        presenter = new AddAccountPresenter(getApplicationContext());

        typeCash = findViewById(R.id.type_cash);
        typeBank = findViewById(R.id.type_bank);
        typeCreditCard = findViewById(R.id.type_credit_card);
        etAccountName = findViewById(R.id.et_account_name);
        etOpeningBalance = findViewById(R.id.et_opening_balance);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        typeCash.setOnClickListener(v -> presenter.selectType(AccountType.CASH));
        typeBank.setOnClickListener(v -> presenter.selectType(AccountType.BANK));
        typeCreditCard.setOnClickListener(v -> presenter.selectType(AccountType.CREDIT_CARD));

        findViewById(R.id.btn_add_account_submit).setOnClickListener(v -> presenter.submit(
                etAccountName.getText().toString(),
                etOpeningBalance.getText().toString()));

        // Keyboard "Next"/"Done" keys were unwired, so pressing Enter did nothing —
        // chain name -> balance, then submit from balance like tapping 新增帳戶.
        etAccountName.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_NEXT)) {
                etOpeningBalance.requestFocus();
                return true;
            }
            return false;
        });
        // etOpeningBalance is inputType="number" — numeric keypads often don't report a
        // proper actionId for Enter, so isEnterPressed() also falls back to the raw KeyEvent.
        etOpeningBalance.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard(v);
                v.clearFocus();
                presenter.submit(etAccountName.getText().toString(), etOpeningBalance.getText().toString());
                return true;
            }
            return false;
        });

        presenter.attachView(this);
        presenter.selectType(AccountType.CASH);
    }

    @Override
    public void showSelectedType(AccountType type) {
        typeCash.setBackgroundResource(
                type == AccountType.CASH ? R.drawable.bg_type_card_selected : R.drawable.bg_type_card_unselected);
        typeBank.setBackgroundResource(
                type == AccountType.BANK ? R.drawable.bg_type_card_selected : R.drawable.bg_type_card_unselected);
        typeCreditCard.setBackgroundResource(
                type == AccountType.CREDIT_CARD ? R.drawable.bg_type_card_selected : R.drawable.bg_type_card_unselected);
    }

    @Override
    public void showAccountName(String name) {
        etAccountName.setText(name);
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
