package com.ebe.miniaelec.model;

public class CollectedReport {
    private String collectDate;
    private double onlineCashAmount;
    private int onlineCashCount;
    private double offlineCashAmount;
    private int offlineCashCount;
    private double cardAmount;
    private int cardCount;
    private double walletAmount;
    private int walletCount;


    public CollectedReport() {
    }

    public CollectedReport(String collectDate) {
        this.collectDate = collectDate;
    }

    public CollectedReport(String collectDate, long onlineCashAmount, int onlineCashCount, long offlineCashAmount, int offlineCashCount, long cardAmount, int cardCount, long walletAmount, int walletCount) {
        this.collectDate = collectDate;
        this.onlineCashAmount = onlineCashAmount;
        this.onlineCashCount = onlineCashCount;
        this.offlineCashAmount = offlineCashAmount;
        this.offlineCashCount = offlineCashCount;
        this.cardAmount = cardAmount;
        this.cardCount = cardCount;
        this.walletAmount = walletAmount;
        this.walletCount = walletCount;
    }

    public String getCollectDate() {
        return collectDate;
    }

    public void setCollectDate(String collectDate) {
        this.collectDate = collectDate;
    }

    public double getOnlineCashAmount() {
        return onlineCashAmount;
    }

    public void setOnlineCashAmount(double onlineCashAmount) {
        this.onlineCashAmount = onlineCashAmount;
    }

    public int getOnlineCashCount() {
        return onlineCashCount;
    }

    public void setOnlineCashCount(int onlineCashCount) {
        this.onlineCashCount = onlineCashCount;
    }

    public double getOfflineCashAmount() {
        return offlineCashAmount;
    }

    public void setOfflineCashAmount(double offlineCashAmount) {
        this.offlineCashAmount = offlineCashAmount;
    }

    public int getOfflineCashCount() {
        return offlineCashCount;
    }

    public void setOfflineCashCount(int offlineCashCount) {
        this.offlineCashCount = offlineCashCount;
    }

    public double getCardAmount() {
        return cardAmount;
    }

    public void setCardAmount(double cardAmount) {
        this.cardAmount = cardAmount;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public double getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(double walletAmount) {
        this.walletAmount = walletAmount;
    }

    public int getWalletCount() {
        return walletCount;
    }

    public void setWalletCount(int walletCount) {
        this.walletCount = walletCount;
    }
}
