package com.ebe.miniaelec.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = "billdetails")
public class BillData implements Serializable {
    @DatabaseField( generatedId = true)
    private int id = 0;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    OfflineClient client;

    @DatabaseField
    String clientId;
    @SerializedName("BillUnique")
    @DatabaseField(unique = true)
    private String BillUnique;
    @DatabaseField
    @SerializedName("RowNum")
    private int rawNum;
    @SerializedName("SectorName")
    @DatabaseField
    private String sectorName;
    @SerializedName("BranchName")
    @DatabaseField
    private String branchName;
    @SerializedName("ClientAddress")
    @DatabaseField
    private String clientAddress;
    @SerializedName("ClientActivity")
    @DatabaseField
    private String clientActivity;
    @SerializedName("ClientPlace")
    @DatabaseField
    private String clientPlace;
    @SerializedName("CurrentRead")
    @DatabaseField
    private String currentRead;
    @SerializedName("PreviousRead")
    @DatabaseField
    private String previousRead;
    @SerializedName("Consumption")
    @DatabaseField
    private String consumption;
    @SerializedName("Installment")
    @DatabaseField
    private String installments;
    @SerializedName("Fees")
    @DatabaseField
    private String fees;
    @SerializedName("Payments")
    @DatabaseField
    private String payments;
    @SerializedName("CommissionValue")
    @DatabaseField
    private double commissionValue;
    @SerializedName("ClientName")
    @DatabaseField
    private String clientName;
    @SerializedName("BillDate")
    @DatabaseField
    private String billDate;
    @SerializedName("BillValue")
    @DatabaseField
    private double billValue;

    @SerializedName("MntkaCode")
    @DatabaseField
    private String mntkaCode;
    @SerializedName("DayCode")
    @DatabaseField
    private String dayCode;
    @SerializedName("MainCode")
    @DatabaseField
    private String mainCode;
    @SerializedName("FaryCode")
    @DatabaseField
    private String faryCode;

    public BillData() {
    }

    public BillData(int rawNum, String sectorName, String branchName, String clientAddress, String clientActivity,
                    String clientPlace, String currentRead, String previousRead, String consumption, String installments,
                    String fees, String payments, double commissionValue, String clientName, String billDate, double billValue) {
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

    public OfflineClient getClient() {
        return client;
    }

    public void setClient(OfflineClient client) {
        this.client = client;
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

    public void setFaryCode(String faryCode) {
        this.faryCode = faryCode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getBillUnique() {
        return BillUnique;
    }

    public void setBillUnique(String billUnique) {
        BillUnique = billUnique;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
