package com.ebe.miniaelec.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "reports")
public class Report {
    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id = 0;
    @DatabaseField(columnName = "client_id")
    private String clientID;
    @DatabaseField
    private String transDate;
    @DatabaseField
    private String transTime;
    @DatabaseField
    private String bankTransactionID;
    @DatabaseField
    private long totalAmount;
    @DatabaseField
    private int billsCount;
    @DatabaseField
    private int paymentType;

    public Report() {
    }

    public Report(String clientID, String transDate,long totalAmount, int billsCount, int paymentType ,String transTime, String bankTransactionID) {
        this.clientID = clientID;
        this.transDate = transDate;
        this.transTime = transTime;
        this.bankTransactionID = bankTransactionID;
        this.totalAmount = totalAmount;
        this.billsCount = billsCount;
        this.paymentType = paymentType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getBillsCount() {
        return billsCount;
    }

    public void setBillsCount(int billsCount) {
        this.billsCount = billsCount;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getBankTransactionID() {
        return bankTransactionID;
    }

    public void setBankTransactionID(String bankTransactionID) {
        this.bankTransactionID = bankTransactionID;
    }
}
