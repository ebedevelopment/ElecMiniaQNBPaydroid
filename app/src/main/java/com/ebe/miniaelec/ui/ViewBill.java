package com.ebe.miniaelec.ui;

public class ViewBill {
    private String mainCode;
    private String faryCode;
    private String clientID;
    private String clientName;
    private int count;
    private double amount;

    public ViewBill() {
    }

    public ViewBill(String mainCode, String faryCode, String clientID, String clientName) {
        this.mainCode = mainCode;
        this.faryCode = faryCode;
        this.clientID = clientID;
        this.clientName = clientName;
    }

    public String getMainCode() {
        return mainCode;
    }

    public void setMainCode(String mainCode) {
        this.mainCode = mainCode;
    }

    public String getFaryCode() {
        return faryCode;
    }

    public void setFaryCode(String faryCode) {
        this.faryCode = faryCode;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
