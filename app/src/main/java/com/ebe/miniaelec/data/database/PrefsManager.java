package com.ebe.miniaelec.data.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ebe.miniaelec.MiniaElectricity;

import java.util.Calendar;

public class PrefsManager {
    private static final String FILE_NAME = "MINIA_ELECTRICITY";
    private static final String LOG_STATUS = "LOG_STATUS";
    private static final String APP_LANGUAGE = "LANGUAGE";
    private static final String COLLECTOR_CODE = "COLLECTOR_CODE";
    private static final String PASSWORD = "PASSWORD";
    private static final String SESSION_ID = "SESSION_ID";
    private static final String RECEIPT_NO = "RECEIPT_NO";
    private static final String TERMINAL_TYPE = "TERMINAL_TYPE";
    private static final String KEEP_LOGIN = "KEEP_LOGIN";
    private static final String PACKAGE_NAME = "PACKAGE_NAME";
    private static final String MERCHANT_ID = "MERCHANT_ID";
    private static final String TERMINAL_ID = "TERMINAL_ID";
    private static final String FIXED_FEES = "FIXED_FEES";
    private static final String PERCENT_FEES = "PERCENT_FEES";
    private static final String InquiryID = "InquiryID";
    private static final String MaxOfflineHoure = "MaxOfflineHoure";
    private static final String MaxOfflineBillCount = "MaxOfflineBillCount";
    private static final String MaxOfflineBillValue = "MaxOfflineBillValue";
    private static final String OfflineStartingTime = "OfflineStartTime";
    private static final String OfflineBillCount = "OfflineBillCount";
    private static final String OfflineBillValue = "OfflineBillValue";
    private static final String OFFLINE_BILLS_STATUS = "OfflineBillsStatus";
    private static final String PAID_OFFLINE_BILLS_COUNT = "OfflinePaidBillsCount";
    private static final String PAID_OFFLINE_BILLS_AMOUNT = "OfflinePaidBillsValue";
    private static final String PAID_ONLINE_BILLS_COUNT = "OnlinePaidBillsCount";
    private static final String PAID_ONLINE_BILLS_AMOUNT = "OnlinePaidBillsValue";
    private static final String LAST_RESET_LOGS_MONTH = "LastResetLogsMonth";

    private final SharedPreferences mSharedPreferences;


    public PrefsManager() {
        mSharedPreferences = MiniaElectricity.getInstance().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedStatus(boolean loggedIn) {
        mSharedPreferences.edit().putBoolean(LOG_STATUS, loggedIn).apply();
    }

    public boolean isLoggedIn() {
        return mSharedPreferences.getBoolean(LOG_STATUS, false);
    }

    public String getAppLanguage() {
        return mSharedPreferences.getString(APP_LANGUAGE, "ar");
    }

    public void setAppLanguage(String language) {
        mSharedPreferences.edit().putString(APP_LANGUAGE, language).apply();
    }

    public String getCollectorCode() {
        return mSharedPreferences.getString(COLLECTOR_CODE, "");
    }

    public void setCollectorCode(String CollectorCode) {
        mSharedPreferences.edit().putString(COLLECTOR_CODE, CollectorCode).apply();
    }

    public String getPassword() {
        return mSharedPreferences.getString(PASSWORD, null);
    }

    public void setPassword(String Password) {
        mSharedPreferences.edit().putString(PASSWORD, Password).apply();
    }

    public String getSessionId() {
        return mSharedPreferences.getString(SESSION_ID, null);
    }

    public void setSessionId(String SessionId) {
        mSharedPreferences.edit().putString(SESSION_ID, SessionId).apply();
    }

    public String getPackageName() {
        return mSharedPreferences.getString(PACKAGE_NAME, "com.ebe.edc.qnb");
    }

    public void setPackageName(String packageName) {
        mSharedPreferences.edit().putString(PACKAGE_NAME, packageName).apply();
    }

    public long getReceiptNo() {
        return mSharedPreferences.getLong(RECEIPT_NO, 1);
    }

    public void setReceiptNo(long receiptNo) {
        mSharedPreferences.edit().putLong(RECEIPT_NO, receiptNo).apply();
    }

    public int getTerminalType() {
        if (mSharedPreferences.getInt(TERMINAL_TYPE, 1) == 1)
            setTerminalType(new MiniaElectricity().defineDeviceType());
        return mSharedPreferences.getInt(TERMINAL_TYPE, 1);
    }

    public void setTerminalType(int type) {
        mSharedPreferences.edit().putInt(TERMINAL_TYPE, type).apply();
    }


    public boolean getKeepLogin() {
        return mSharedPreferences.getBoolean(KEEP_LOGIN, false);
    }

    public void setKeepLogin(boolean keep) {
        mSharedPreferences.edit().putBoolean(KEEP_LOGIN, keep).apply();
    }

    public String getTerminalId() {
        return mSharedPreferences.getString(TERMINAL_ID, null);
    }

    public void setTerminalId(String terminalId) {
        mSharedPreferences.edit().putString(TERMINAL_ID, terminalId).apply();
    }

    public String getMerchantId() {
        return mSharedPreferences.getString(MERCHANT_ID, null);
    }

    public void setMerchantId(String merchantId) {
        mSharedPreferences.edit().putString(MERCHANT_ID, merchantId).apply();
    }

    public double getFixedFees() {
        return mSharedPreferences.getFloat(FIXED_FEES, 0);
    }

    public void setFixedFees(double fixedFees) {
        mSharedPreferences.edit().putFloat(FIXED_FEES, (float) fixedFees).apply();
    }

    public double getPercentFees() {
        return mSharedPreferences.getFloat(PERCENT_FEES, 0);
    }

    public void setPercentFees(double percentFees) {
        mSharedPreferences.edit().putFloat(PERCENT_FEES, (float) percentFees).apply();
    }

    public String getInquiryID() {
        return mSharedPreferences.getString(InquiryID, "");
    }

    public void setInquiryID(String inquiryID) {
        mSharedPreferences.edit().putString(InquiryID, inquiryID).apply();
    }

    public double getMaxOfflineHours() {
        return mSharedPreferences.getInt(MaxOfflineHoure, 0);
    }

    public void setMaxOfflineHours(int maxOfflineHours) {
        mSharedPreferences.edit().putInt(MaxOfflineHoure, maxOfflineHours).apply();
    }

    public double getMaxOfflineBillCount() {
        return mSharedPreferences.getInt(MaxOfflineBillCount, 0);
    }

    public void setMaxOfflineBillCount(int maxOfflineBillCount) {
        mSharedPreferences.edit().putInt(MaxOfflineBillCount, maxOfflineBillCount).apply();
    }

    public double getMaxOfflineBillValue() {
        return mSharedPreferences.getFloat(MaxOfflineBillValue, 0);
    }

    public void setMaxOfflineBillValue(double maxOfflineBillValue) {
        mSharedPreferences.edit().putFloat(MaxOfflineBillValue, (float) maxOfflineBillValue).apply();
    }

    public int getPaidOfflineBillsCount() {
        return mSharedPreferences.getInt(PAID_OFFLINE_BILLS_COUNT, 0);
    }

    public void setPaidOfflineBillsCount(int maxOfflineBillCount) {
        mSharedPreferences.edit().putInt(PAID_OFFLINE_BILLS_COUNT, maxOfflineBillCount).apply();
    }

    public double getPaidOfflineBillsValue() {
        return mSharedPreferences.getFloat(PAID_OFFLINE_BILLS_AMOUNT, 0);
    }

    public void setPaidOfflineBillsValue(double maxOfflineBillValue) {
        mSharedPreferences.edit().putFloat(PAID_OFFLINE_BILLS_AMOUNT, (float) maxOfflineBillValue).apply();
    }

    public int getPaidOnlineBillsCount() {
        return mSharedPreferences.getInt(PAID_ONLINE_BILLS_COUNT, 0);
    }

    public void setPaidOnlineBillsCount(int maxOfflineBillCount) {
        mSharedPreferences.edit().putInt(PAID_ONLINE_BILLS_COUNT, maxOfflineBillCount).apply();
    }

    public double getPaidOnlineBillsValue() {
        return mSharedPreferences.getFloat(PAID_ONLINE_BILLS_AMOUNT, 0);
    }

    public void setPaidOnlineBillsValue(double maxOfflineBillValue) {
        mSharedPreferences.edit().putFloat(PAID_ONLINE_BILLS_AMOUNT, (float) maxOfflineBillValue).apply();
    }

    public double getOfflineBillValue() {
        return mSharedPreferences.getFloat(OfflineBillValue, 0);
    }

    public void setOfflineBillValue(double maxOfflineBillValue) {
        mSharedPreferences.edit().putFloat(OfflineBillValue, (float) maxOfflineBillValue).apply();
    }

    public String getOfflineStartingTime() {
        return mSharedPreferences.getString(OfflineStartingTime, null);
    }

    public void setOfflineStartingTime(String startingTime) {
        mSharedPreferences.edit().putString(OfflineStartingTime, startingTime).apply();
    }

    public int getOfflineBillCount() {
        return mSharedPreferences.getInt(OfflineBillCount, 0);
    }

    public void setOfflineBillCount(int maxOfflineBillCount) {
        mSharedPreferences.edit().putInt(OfflineBillCount, maxOfflineBillCount).apply();
    }

    public int getOfflineBillStatus() {
        //return 0;
        return mSharedPreferences.getInt(OFFLINE_BILLS_STATUS, 0);
    }

    public void setOfflineBillsStatus(int billsStatus) {
        mSharedPreferences.edit().putInt(OFFLINE_BILLS_STATUS, billsStatus).apply();
    }

    public int getLastResetLogsMonth() {
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        Log.d("Month", " thisMonth : " + (thisMonth));
        if (mSharedPreferences.getInt(LAST_RESET_LOGS_MONTH, 0) == 0) {
            setLastResetLogsMonth(thisMonth);
            return thisMonth;
        }
        return mSharedPreferences.getInt(LAST_RESET_LOGS_MONTH, 0);
    }

    public void setLastResetLogsMonth(int month) {
        mSharedPreferences.edit().putInt(LAST_RESET_LOGS_MONTH, month).apply();
    }
}
