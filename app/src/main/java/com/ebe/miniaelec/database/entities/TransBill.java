package com.ebe.miniaelec.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.TransData;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

@Entity
public class TransBill {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;

    TransData transData;


    String bankTransactionID;



    private String BillUnique;


    private int rawNum;

    private String sectorName;

    private String branchName;

    private String clientAddress;

    private String clientActivity;

    private String clientPlace;

    private String currentRead;

    private String previousRead;

    private String consumption;

    private String installments;

    private String fees;

    private String payments;

    private double commissionValue;

    private String clientName;

    private String billDate;

    private double billValue;


    private String mntkaCode;

    private String dayCode;

    private String mainCode;

    private String faryCode;

    public TransBill() {
    }

    public TransBill(BillData data) {
        BillUnique = data.getBillUnique();
        this.rawNum = data.getRawNum();
        this.sectorName = data.getSectorName();
        this.branchName = data.getBranchName();
        this.clientAddress = data.getClientAddress();
        this.clientActivity = data.getClientActivity();
        this.clientPlace = data.getClientPlace();
        this.currentRead = data.getCurrentRead();
        this.previousRead = data.getPreviousRead();
        this.consumption = data.getConsumption();
        this.installments = data.getInstallments();
        this.fees = data.getFees();
        this.payments = data.getPayments();
        this.commissionValue = data.getCommissionValue();
        this.clientName = data.getClientName();
        this.billDate = data.getBillDate();
        this.billValue = data.getBillValue();
        this.mntkaCode = data.getMntkaCode();
        this.dayCode = data.getDayCode();
        this.mainCode = data.getMainCode();
        this.faryCode = data.getFaryCode();
    }

    public TransBill(String billUnique, int rawNum, String sectorName, String branchName, String clientAddress, String clientActivity, String clientPlace, String currentRead, String previousRead, String consumption, String installments, String fees, String payments, double commissionValue, String clientName, String billDate, double billValue, String mntkaCode, String dayCode, String mainCode, String faryCode) {
        BillUnique = billUnique;
        this.rawNum = rawNum;
        this.sectorName = sectorName;
        this.branchName = branchName;
        this.clientAddress = clientAddress;
        this.clientActivity = clientActivity;
        this.clientPlace = clientPlace;
        this.currentRead = currentRead;
        this.previousRead = previousRead;
        this.consumption = consumption;
        this.installments = installments;
        this.fees = fees;
        this.payments = payments;
        this.commissionValue = commissionValue;
        this.clientName = clientName;
        this.billDate = billDate;
        this.billValue = billValue;
        this.mntkaCode = mntkaCode;
        this.dayCode = dayCode;
        this.mainCode = mainCode;
        this.faryCode = faryCode;
    }

    public TransBill(TransData transData, String bankTransactionID, String billUnique, int rawNum, double commissionValue, String billDate, double billValue, String clientName) {
        this.transData = transData;
        this.bankTransactionID = bankTransactionID;
        BillUnique = billUnique;
        this.rawNum = rawNum;
        this.commissionValue = commissionValue;
        this.billDate = billDate;
        this.billValue = billValue;
        this.clientName = clientName;
    }

    public TransData getTransData() {
        return transData;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
    }

    public String getBankTransactionID() {
        return bankTransactionID;
    }

    public void setBankTransactionID(String bankTransactionID) {
        this.bankTransactionID = bankTransactionID;
    }

    public String getBillUnique() {
        return BillUnique;
    }

    public void setBillUnique(String billUnique) {
        BillUnique = billUnique;
    }

    public int getRawNum() {
        return rawNum;
    }

    public void setRawNum(int rawNum) {
        this.rawNum = rawNum;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientActivity() {
        return clientActivity;
    }

    public void setClientActivity(String clientActivity) {
        this.clientActivity = clientActivity;
    }

    public String getClientPlace() {
        return clientPlace;
    }

    public void setClientPlace(String clientPlace) {
        this.clientPlace = clientPlace;
    }

    public String getCurrentRead() {
        return currentRead;
    }

    public void setCurrentRead(String currentRead) {
        this.currentRead = currentRead;
    }

    public String getPreviousRead() {
        return previousRead;
    }

    public void setPreviousRead(String previousRead) {
        this.previousRead = previousRead;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public String getInstallments() {
        return installments;
    }

    public void setInstallments(String installments) {
        this.installments = installments;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getPayments() {
        return payments;
    }

    public void setPayments(String payments) {
        this.payments = payments;
    }

    public double getCommissionValue() {
        return commissionValue;
    }

    public void setCommissionValue(double commissionValue) {
        this.commissionValue = commissionValue;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public double getBillValue() {
        return billValue;
    }

    public void setBillValue(double billValue) {
        this.billValue = billValue;
    }

    public String getMntkaCode() {
        return mntkaCode;
    }

    public void setMntkaCode(String mntkaCode) {
        this.mntkaCode = mntkaCode;
    }

    public String getDayCode() {
        return dayCode;
    }

    public void setDayCode(String dayCode) {
        this.dayCode = dayCode;
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

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


    public void setFaryCode(String faryCode) {
        this.faryCode = faryCode;
    }
}
