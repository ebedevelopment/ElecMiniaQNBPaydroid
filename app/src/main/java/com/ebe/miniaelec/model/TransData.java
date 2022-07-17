package com.ebe.miniaelec.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

@DatabaseTable(tableName = "transdata")
public class TransData {
    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id = 0;
    @DatabaseField(columnName = "reference_no")
    private int referenceNo;
    @DatabaseField(columnName = "client_id")
    private String clientID;
    @DatabaseField
    private String inquiryID;
    @DatabaseField
    private String transDateTime;
    @DatabaseField
    private int paymentType;
    @DatabaseField
    private String stan;
    @DatabaseField
    private int status;
    @DatabaseField
    private String clientMobileNo;
    @DatabaseField(unique = true)
    private String bankTransactionID;
    @ForeignCollectionField
    private ForeignCollection<TransBill> transBills;
    @DatabaseField
    private String drmData;

    @DatabaseField
    private int printCount;

    @DatabaseField
    private int deductType;

    public enum PaymentType {
        CASH(1),
        CARD(2),
        WALLET(3),
        OFFLINE_CASH(4);
        private final int value;

        private PaymentType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum STATUS {
        INITIATED(1),
        PENDING_CASH_PAYMENT_REQ(2),
        PENDING_SALE_REQ(3),
        PENDING_CARD_PAYMENT_REQ(4),
        PENDING_DELETE_REQ(5),
        DELETED_PENDING_VOID_REQ(6),
        PAID_PENDING_DRM_REQ(7),
        DELETED_PENDING_DRM_REQ(8),
        PENDING_QR_SALE_REQ(13),
        COMPLETED(9),
        CANCELLED(10),
        REPRINT(11),
        PENDING_ONLINE_PAYMENT_REQ(12),  //IN CASE OF OFFLINE CASH PAYMENT
        PENDING_DEDUCT_REQ(13);

        private final int value;

        private STATUS(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public TransData() {
    }

    public TransData(int referenceNo, String clientID, String inquiryID, int status) {
        this.referenceNo = referenceNo;
        this.clientID = clientID;
        this.inquiryID = inquiryID;
        this.status = status;
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

    public String getInquiryID() {
        return inquiryID;
    }

    public void setInquiryID(String inquiryID) {
        this.inquiryID = inquiryID;
    }


    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getClientMobileNo() {
        return clientMobileNo;
    }

    public void setClientMobileNo(String clientMobileNo) {
        this.clientMobileNo = clientMobileNo;
    }

    public int getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(int referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getBankTransactionID() {
        return bankTransactionID;
    }

    public void setBankTransactionID(String bankTransactionID) {
        this.bankTransactionID = bankTransactionID;
    }

    public ArrayList<TransBill> getTransBills() {
        if (transBills == null) {
            return new ArrayList<>();
        } else
            return new ArrayList<>(transBills);
    }

    public void setTransBills(ForeignCollection<TransBill> transBills) {
        this.transBills = transBills;
    }

    public String getTransDateTime() {
        return transDateTime;
    }

    public void setTransDateTime(String transDateTime) {
        this.transDateTime = transDateTime;
    }

    public String getDrmData() {
        return drmData;
    }

    public void setDrmData(String drmData) {
        this.drmData = drmData;
    }

    public int getPrintCount() {
        return printCount;
    }

    public void setPrintCount(int printCount) {
        this.printCount = printCount;
    }

    public int getDeductType() {
        return deductType;
    }

    public void setDeductType(int deductType) {
        this.deductType = deductType;
    }
}
