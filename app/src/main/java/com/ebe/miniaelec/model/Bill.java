package com.ebe.miniaelec.model;

import com.google.gson.annotations.SerializedName;

public class Bill {
    @SerializedName("id")
    private String invoiceNumber;
    @SerializedName("billDate")
    private String month;
    @SerializedName("billAmount")
    private String amount;
    private String oldMeterValue;
    private String currentMeterValue;
    private String consumption;
    private String customerNo;
    private String customerName;
    private int count;
    private int totalAmount;

    @Override
    public String toString() {
        return "شهر الإصدار: " + getMonth() + "\n\n" +
                "قيمة الفاتورة: " + getAmount() + "\n\n" +
                "القراءة الحالية: " + getCurrentMeterValue() + "\n\n" +
                "القراءة السابقة: " + getOldMeterValue() + "\n\n" +
                "الاستهلاك: " + getConsumption() + '\n';
    }

    public Bill(String customerNo, String customerName, int count) {
        this.customerNo = customerNo;
        this.customerName = customerName;
        this.count = count;
    }

    public Bill(String invoiceNumber, String month, String amount) {
        this.invoiceNumber = invoiceNumber;
        this.month = month;
        this.amount = amount;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getMonth() {
        return month.substring(4).concat("/").concat(month.substring(0, 4));
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOldMeterValue() {
        return oldMeterValue;
    }

    public void setOldMeterValue(String oldMeterValue) {
        this.oldMeterValue = oldMeterValue;
    }

    public String getCurrentMeterValue() {
        return currentMeterValue;
    }

    public void setCurrentMeterValue(String currentMeterValue) {
        this.currentMeterValue = currentMeterValue;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public String getCustomerNo() {
        return customerNo;
    }

    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }
}
