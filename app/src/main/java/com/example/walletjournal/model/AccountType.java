package com.example.walletjournal.model;

/** The three account types selectable on the Add Account screen. */
public enum AccountType {
    CASH("現金", "錢包"),
    BANK("銀行", "我的銀行"),
    CREDIT_CARD("信用卡", "本期帳單");

    private final String label;
    private final String defaultSubtitle;

    AccountType(String label, String defaultSubtitle) {
        this.label = label;
        this.defaultSubtitle = defaultSubtitle;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultSubtitle() {
        return defaultSubtitle;
    }
}
