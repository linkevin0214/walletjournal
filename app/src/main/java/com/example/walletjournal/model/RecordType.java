package com.example.walletjournal.model;

/** The three record types selectable on the Add Record screen. */
public enum RecordType {
    EXPENSE("支出"),
    INCOME("收入"),
    TRANSFER("轉帳");

    private final String label;

    RecordType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
