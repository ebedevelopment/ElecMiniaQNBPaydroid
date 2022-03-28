package com.ebe.miniaelec.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.database.BaseDbHelper;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FinishPendingTransService extends Service {

    private static ArrayList<TransData> pendingTransData = new ArrayList<>();
    private ArrayList<TransData> offlineTransData;
    public static MutableLiveData<String> errorMsg = new MutableLiveData<String>("");
    public static MutableLiveData<TransData> aVoid = new MutableLiveData<>(null);
    public static MutableLiveData<Boolean> goToLogin = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> goToPayment = new MutableLiveData<>(false);
    public static MutableLiveData<ArrayList<TransData>> pendingData = new MutableLiveData<ArrayList<TransData>>(pendingTransData);
    public static MutableLiveData<Boolean> serviceState = new MutableLiveData<>(true);
    private static int index = 0;
    public static MutableLiveData<Integer> indexState = new MutableLiveData<Integer>(index);
    public static MutableLiveData<Boolean> loadingState= new MutableLiveData<>(false);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




        if (intent != null)
        {
            boolean pendingRequest =intent.getBooleanExtra("pending",false);

            if (pendingRequest)
            {
                handlePendingBills();
            }else
            {
                init();
            }

        }





        return START_REDELIVER_INTENT;
    }


    void init()
    {
        pendingTransData = new ArrayList<>();
        offlineTransData = new ArrayList<>();

        ArrayList<TransData> transData = new ArrayList<>(DBHelper.getInstance(this).getAllTrans());
        for (TransData b :
                transData) {
            if (b.getClientID() == null || b.getClientID().equalsIgnoreCase("null") || (b.getStatus() == TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getDrmData() == null) || (b.getStatus() == TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getDrmData().equals("null"))) {
                for (TransBill bill :
                        b.getTransBills()) {
                    DBHelper.getInstance(this).deleteTransBill(bill.getBillUnique());
                }
                DBHelper.getInstance(this).deleteTransData(b);
            } else {
                if (b.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue() && b.getStatus() == TransData.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                    offlineTransData.add(b);
                } else if (b.getStatus() != TransData.STATUS.INITIATED.getValue() && b.getStatus() != TransData.STATUS.COMPLETED.getValue()
                        && b.getStatus() != TransData.STATUS.CANCELLED.getValue()) {
                    pendingTransData.add(b);
                } else {
                    for (TransBill bill :
                            b.getTransBills()) {
                        DBHelper.getInstance(this).deleteTransBill(bill.getBillUnique());
                    }
                    DBHelper.getInstance(this).deleteTransData(b);
                }
            }
        }
        if ((offlineTransData.size() > 0 || pendingTransData.size() > 0) && Utils.checkConnection(MiniaElectricity.getInstance())) {
            setBills();
        } else {
            goToPayment.setValue(true);
            stopSelf();
        }

    }

    private void setBills() {
//        ArrayList<TransData> all = new ArrayList<>(pendingTransData);
//        all.addAll(offlineTransData);
//        AdapterBills adapterBills = new AdapterBills(this, all);

        if (offlineTransData.size() > 0) {
            handleOfflineBills();
        } else
            handlePendingBills();
    }

    private void handleOfflineBills() {
        JsonArray ModelClientPaymentV = new JsonArray();
        for (TransData transData :
                offlineTransData) {
            JsonObject i = new JsonObject();
            i.addProperty("BankTransactionID", transData.getBankTransactionID());
            i.addProperty("ClientMobileNo", transData.getClientMobileNo());
            i.addProperty("BankDateTime", transData.getTransDateTime());
            i.addProperty("BankReceiptNo", transData.getStan());
            i.addProperty("ClientID", transData.getClientID());
            i.addProperty("PrintCount", transData.getPrintCount());

            JsonArray ModelBillPaymentV = new JsonArray();
            for (TransBill b :
                    transData.getTransBills()) {
                JsonObject j = new JsonObject();
                j.addProperty("RowNum", b.getRawNum());
                j.addProperty("BillDate", b.getBillDate());
                j.addProperty("BillValue", b.getBillValue());
                j.addProperty("CommissionValue", b.getCommissionValue());
                //j.addProperty("CommissionValueCreditCard", "0");

                ModelBillPaymentV.add(j);
            }
            i.add("ModelBillPaymentV", ModelBillPaymentV);
            ModelClientPaymentV.add(i);
        }
        loadingState.setValue(true);
        new ApiServices(this, false).offlineBillPayment(ModelClientPaymentV,
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            loadingState.setValue(false);
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            int billsStatus = responseBody.optInt("UserNewBillStatus");
                            int userStatus = responseBody.getInt("UserStatus");
                            String operationStatus = responseBody.getString("OperationStatus");

                            if (!operationStatus.trim().equalsIgnoreCase("successful")) {
                                if (Error.contains("ليس لديك صلاحيات الوصول للهندسه") || Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول") || userStatus == 0) {
                                    MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                    errorMsg.setValue(Error);
                                    //Toast.makeText(cntxt, Error, Toast.LENGTH_LONG).show();
                                    //startActivity(new Intent(FinishPendingTransActivity.this, LoginActivity.class));
                                    goToLogin.setValue(true);
                                    stopSelf();
                                } else onFailure("فشل في مزامنة عمليات الدفع\n" + Error);

                            } else {
                                MiniaElectricity.getPrefsManager().setOfflineStartingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                        .format(new Date(System.currentTimeMillis())));
                                MiniaElectricity.getPrefsManager().setOfflineBillValue(0);
                                MiniaElectricity.getPrefsManager().setOfflineBillCount(0);
                                if (billsStatus != 0)
                                {
                                    MiniaElectricity.getPrefsManager().setOfflineBillsStatus(billsStatus);
                                }if (billsStatus == 2)
                                {
                                    BaseDbHelper.getInstance(FinishPendingTransService.this).dropTables();
                                }


                                for (TransData t :
                                        offlineTransData) {
                                    t.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                    DBHelper.getInstance(FinishPendingTransService.this).updateTransData(t);
                                    pendingTransData.add(t);
                                }

                                offlineTransData.clear();
                                onFailure(null);
                                handlePendingBills();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loadingState.setValue(false);
                            onFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        loadingState.setValue(false);
                        if (failureMsg != null)
                            //Toast.makeText(this, failureMsg, Toast.LENGTH_LONG).show();
                            errorMsg.setValue(failureMsg);
                        MiniaElectricity.getPrefsManager().setOfflineBillsStatus(0);

                    }
                });

    }


    private void handlePendingBills() {
        if (index < pendingTransData.size() && Utils.checkConnection(MiniaElectricity.getInstance())) {
            TransData transData = pendingTransData.get(index);
            index = index + 1;
            if (TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue() == transData.getStatus() ||
                    TransData.STATUS.PENDING_CARD_PAYMENT_REQ.getValue() == transData.getStatus() ||
                    TransData.STATUS.PENDING_DELETE_REQ.getValue() == transData.getStatus()) {
                //delete the request then send void DRM
                deletePayment(transData);
            } else if (TransData.STATUS.PENDING_SALE_REQ.getValue() == transData.getStatus() ||
                    TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue() == transData.getStatus()) {
                //send void by referenceNo
                aVoid.setValue(transData);
                //aVoidReq(transData);
            } else if (TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send void DRM
                sendDRM(true, transData);
            } else if (TransData.STATUS.PAID_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send DRM
                sendDRM(false, transData);
            } else handlePendingBills();

        } else
        {
            goToPayment.setValue(true);
            stopSelf();
        }

    }

    private void deletePayment(final TransData transData) {
        transData.setStatus(TransData.STATUS.PENDING_DELETE_REQ.getValue());
        DBHelper.getInstance(this).updateTransData(transData);
        loadingState.setValue(true);
        new ApiServices(this, false).cancelBillPayment(transData.getBankTransactionID(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        loadingState.setValue(false);
                        // whatever the response of delete req suppose it is succeeded
                        if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue()) {
                            transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                            DBHelper.getInstance(FinishPendingTransService.this).updateTransData(transData);
                            for (TransBill b :
                                    transData.getTransBills()) {
                                DBHelper.getInstance(FinishPendingTransService.this).deleteTransBill(b.getBillUnique());
                            }
                            DBHelper.getInstance(FinishPendingTransService.this).deleteTransData(transData);
                            //sendDRM(true, transData);
//                             DBHelper.getInstance().deleteBillData(billData);
                        } else {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            //DBHelper.getInstance(cntxt).deleteBillData(billData);
                            // send void request to QNB payment App
                            aVoid.setValue(transData);
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        //Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        loadingState.setValue(false);
                        errorMsg.setValue(failureMsg);
                        handlePendingBills();
                    }
                });
    }

    private void sendDRM(boolean isView, final TransData transData) {
       // Log.i("onSuccess", transData.getDrmData());
        if (transData.getDrmData() != null && !transData.getDrmData().isEmpty())
        {
            loadingState.setValue(true);
            new ApiServices(this, true).sendDRM((JsonObject) new JsonParser().parse(transData.getDrmData()), new RequestListener() {
                @Override
                public void onSuccess(String response) {
                    Log.i("onSuccess", response);
                    JSONObject responseBody = null;
                    loadingState.setValue(false);
                    try {
                        responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                        String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                        if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                            transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                            DBHelper.getInstance(FinishPendingTransService.this).updateTransData(transData);
                            for (TransBill b :
                                    transData.getTransBills()) {
                                DBHelper.getInstance(FinishPendingTransService.this).deleteTransBill(b.getBillUnique());
                            }
                            DBHelper.getInstance(FinishPendingTransService.this).deleteTransData(transData);
                        }
                        //added for test should not be added here
                        //transData.setStatus(TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());
                        handlePendingBills();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFailure(e.getMessage() + "");
                        //handlePendingBills();
                    }
                }

                @Override
                public void onFailure(String failureMsg) {
                    Log.i("failureMsg", failureMsg);
                    loadingState.setValue(false);
                    handlePendingBills();
                }
            });
        }else
        {
            loadingState.setValue(false);
            handlePendingBills();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceState.setValue(false);
    }
}
