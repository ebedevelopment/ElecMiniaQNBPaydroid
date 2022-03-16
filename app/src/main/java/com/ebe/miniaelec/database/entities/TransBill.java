package com.ebe.miniaelec.database.entities;

import androidx.room.Entity;

import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.TransData;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

@Entity
public class TransBill {
    @DatabaseField(generatedId = true)
    private int id = 0;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    com.ebe.miniaelec.model.TransData transData;

    @DatabaseField
    String bankTransactionID;

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
}
