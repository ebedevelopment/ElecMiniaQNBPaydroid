package com.ebe.miniaelec.database.entities;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "BillDataEntity")
public class BillDataEntity {


    private int id = 0;


    long clientRowId;

    String clientId;

    @SerializedName("BillUnique")
    @NonNull
    @PrimaryKey
    private String billUnique;

    @SerializedName("RowNum")
    private int rawNum;


    @SerializedName("SectorName")
    private String sectorName;

    @SerializedName("BranchName")
    private String branchName;

    @SerializedName("ClientAddress")
    private String clientAddress;

    @SerializedName("ClientActivity")
    private String clientActivity;

    @SerializedName("ClientPlace")
    private String clientPlace;

    @SerializedName("CurrentRead")
    private String currentRead;

    @SerializedName("PreviousRead")
    private String previousRead;

    @SerializedName("Consumption")
    private String consumption;

    @SerializedName("Installment")
    private String installments;

    @SerializedName("Fees")
    private String fees;

    @SerializedName("Payments")
    private String payments;

    @SerializedName("CommissionValue")
    private double commissionValue;

    @SerializedName("ClientName")
    private String clientName;

    @SerializedName("BillDate")
    private String billDate;

    @SerializedName("BillValue")
    private double billValue;

    @SerializedName("MntkaCode")
    private String mntkaCode;

    @SerializedName("DayCode")
    private String dayCode;

    @SerializedName("MainCode")
    private String mainCode;

    @SerializedName("FaryCode")
    private String faryCode;

    public BillDataEntity() {

    }

    public BillDataEntity(String billUnique, int rawNum, String sectorName, String branchName, String clientAddress, String clientActivity,
                          String clientPlace, String currentRead, String previousRead, String consumption, String installments,
                          String fees, String payments, double commissionValue, String clientName, String billDate, double billValue) {
        this.billUnique = billUnique;
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
    }

    public long getClient() {
        return clientRowId;
    }

    public int getId() {
        return id;
    }
    public void setClient(long id) {
        this.clientRowId = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBillUnique() {
        return billUnique;
    }

    public void setBillUnique(String billUnique) {
        billUnique = billUnique;
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

    public long getClientRowId() {
        return clientRowId;
    }

    public void setClientRowId(int clientRowId) {
        this.clientRowId = clientRowId;
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

    public void setId(int id) {
        this.id = id;
    }
}
