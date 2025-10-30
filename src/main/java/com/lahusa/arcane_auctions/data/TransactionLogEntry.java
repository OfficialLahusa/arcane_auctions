package com.lahusa.arcane_auctions.data;

public class TransactionLogEntry {
    public String username;
    public long amount;

    public TransactionLogEntry(String username, long amount) {
        this.username = username;
        this.amount = amount;
    }
}
