package com.ebe.miniaelec.model;

import java.io.Serializable;

public class DRMData implements Serializable {
    private String header1;
    private String header2;
    private String header3;
    private long batch;
    private long stan;
    private String amount;
    private String currencyId;
    private String cardName;
    private String PAN;
    private String expDate;
    private String cardHolderName;
    private String transactionTypeId;
    private boolean isVoided;
    private String transactionStatus;
    private String responseCode;
    private String authId;
    private String RRN;
    private String entryModeId;
    private String pinEntry;
    private String onlineProcessing;
    private String trxDate;
    private String trxTime;
    private String DCC_TRX;
    private String responseMessage1;
    private String responseMessage2;
    private String signature;
    private CardEMVData emvData;

    public DRMData() {
    }

    public String getHeader1() {
        return header1;
    }

    public void setHeader1(String header1) {
        this.header1 = header1;
    }

    public String getHeader2() {
        return header2;
    }

    public void setHeader2(String header2) {
        this.header2 = header2;
    }

    public String getHeader3() {
        return header3;
    }

    public void setHeader3(String header3) {
        this.header3 = header3;
    }

    public long getBatch() {
        return batch;
    }

    public void setBatch(long batch) {
        this.batch = batch;
    }

    public long getStan() {
        return stan;
    }

    public void setStan(long stan) {
        this.stan = stan;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getPAN() {
        return PAN;
    }

    public void setPAN(String PAN) {
        this.PAN = PAN;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(String transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public boolean getIsVoided() {
        return isVoided;
    }

    public void setIsVoided(boolean isVoided) {
        this.isVoided = isVoided;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getRRN() {
        return RRN;
    }

    public void setRRN(String RRN) {
        this.RRN = RRN;
    }

    public String getEntryModeId() {
        return entryModeId;
    }

    public void setEntryModeId(String entryModeId) {
        this.entryModeId = entryModeId;
    }

    public String getPinEntry() {
        return pinEntry;
    }

    public void setPinEntry(String pinEntry) {
        this.pinEntry = pinEntry;
    }

    public String getOnlineProcessing() {
        return onlineProcessing;
    }

    public void setOnlineProcessing(String onlineProcessing) {
        this.onlineProcessing = onlineProcessing;
    }

    public String getTrxDate() {
        return trxDate;
    }

    public void setTrxDate(String trxDate) {
        this.trxDate = trxDate;
    }

    public String getTrxTime() {
        return trxTime;
    }

    public void setTrxTime(String trxTime) {
        this.trxTime = trxTime;
    }

    public String getDCC_TRX() {
        return DCC_TRX;
    }

    public void setDCC_TRX(String DCC_TRX) {
        this.DCC_TRX = DCC_TRX;
    }

    public String getResponseMessage1() {
        return responseMessage1;
    }

    public void setResponseMessage1(String responseMessage1) {
        this.responseMessage1 = responseMessage1;
    }

    public String getResponseMessage2() {
        return responseMessage2;
    }

    public void setResponseMessage2(String responseMessage2) {
        this.responseMessage2 = responseMessage2;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public CardEMVData getEmvData() {
        return emvData;
    }

    public void setEmvData(CardEMVData emvData) {
        this.emvData = emvData;
    }
}
