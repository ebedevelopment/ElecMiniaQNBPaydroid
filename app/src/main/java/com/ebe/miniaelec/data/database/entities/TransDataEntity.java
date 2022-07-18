package com.ebe.miniaelec.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TransData")
public class TransDataEntity {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    @ColumnInfo(name = "reference_no")
    private int referenceNo;
     @ColumnInfo(name = "client_id")
    private String clientID;

    private String inquiryID;

    private String transDateTime;

    private int paymentType;

    private String stan;

    private int status;

    private String clientMobileNo;

    private String bankTransactionID;


    private String drmData;

    private int deductType;


    private int printCount;

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

    public int getDeductType() {
        return deductType;
    }

    public TransDataEntity() {
    }

    public TransDataEntity(int referenceNo, String clientID, String inquiryID, int status) {
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

    public int getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(int referenceNo) {
        this.referenceNo = referenceNo;
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

    public String getTransDateTime() {
        return transDateTime;
    }

    public void setTransDateTime(String transDateTime) {
        this.transDateTime = transDateTime;
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

    public String getBankTransactionID() {
        return bankTransactionID;
    }

    public void setBankTransactionID(String bankTransactionID) {
        this.bankTransactionID = bankTransactionID;
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

    public void setDeductType(int deductType) {
        this.deductType = deductType;
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
        PENDING_ONLINE_PAYMENT_REQ(12), //IN CASE OF OFFLINE CASH PAYMENT
        PENDING_DEDUCT_REQ(13);

        private final int value;

        private STATUS(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}