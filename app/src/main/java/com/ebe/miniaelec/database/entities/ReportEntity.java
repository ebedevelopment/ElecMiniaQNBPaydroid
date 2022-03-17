package com.ebe.miniaelec.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Reports")
public class ReportEntity {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    @ColumnInfo(name = "client_id")
    private String clientID;

    private String transDate;


    private String transTime;

    private String bankTransactionID;

    private long totalAmount;

    private int billsCount;

    private int paymentType;

    public ReportEntity() {
    }

    public ReportEntity(String clientID, String transDate, long totalAmount, int billsCount, int paymentType , String transTime, String bankTransactionID) {
        this.clientID = clientID;
        this.transDate = transDate;
        this.transTime = transTime;
        this.bankTransactionID = bankTransactionID;
        this.totalAmount = totalAmount;
        this.billsCount = billsCount;
        this.paymentType = paymentType;
    }
    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
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
}
