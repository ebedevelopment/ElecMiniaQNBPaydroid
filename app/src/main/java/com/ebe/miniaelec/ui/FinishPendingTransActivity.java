package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.message.VoidMsg;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
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

import dmax.dialog.SpotsDialog;

public class FinishPendingTransActivity extends AppCompatActivity {

    private Activity cntxt;
    private SpotsDialog progressDialog;
    private ArrayList<TransData> pendingTransData;
    private ArrayList<TransData> offlineTransData;
    private ListView bills;
    public ITransAPI transAPI;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_finish_pending_trans);
        cntxt = this;
        bills = findViewById(R.id.pending_bills);
        progressDialog = new SpotsDialog(cntxt, R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        setStatusBarColor();
        pendingTransData = new ArrayList<>();
        offlineTransData = new ArrayList<>();
        transAPI = TransAPIFactory.createTransAPI();
        //pendingBillData.addAll(DBHelper.getInstance(cntxt).getAllBills());
        //Log.e("FinishPendingTrans", String.valueOf(pendingTransData.size()));
        ArrayList<TransData> transData = new ArrayList<>(DBHelper.getInstance(cntxt).getAllTrans());
        for (TransData b :
                transData) {
            if (b.getClientID() == null || b.getClientID().equalsIgnoreCase("null") || (b.getStatus() == TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getDrmData() == null) || (b.getStatus() == TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getDrmData().equals("null"))) {
                for (TransBill bill :
                        b.getTransBills()) {
                    DBHelper.getInstance(cntxt).deleteTransBill(bill.getBillUnique());
                }
                DBHelper.getInstance(cntxt).deleteTransData(b);
            } else {
                if (b.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue() && b.getStatus() == TransData.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                    offlineTransData.add(b);
                } else if (b.getStatus() != TransData.STATUS.INITIATED.getValue() && b.getStatus() != TransData.STATUS.COMPLETED.getValue()
                        && b.getStatus() != TransData.STATUS.CANCELLED.getValue()) {
                    pendingTransData.add(b);
                } else {
                    for (TransBill bill :
                            b.getTransBills()) {
                        DBHelper.getInstance(cntxt).deleteTransBill(bill.getBillUnique());
                    }
                    DBHelper.getInstance(cntxt).deleteTransData(b);
                }
            }
        }
        if ((offlineTransData.size() > 0 || pendingTransData.size() > 0) && Utils.checkConnection(MiniaElectricity.getInstance())) {
            setBills();
        } else {
            finishOK();
        }
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    private void setBills() {
        bills.setVisibility(View.VISIBLE);
        ArrayList<TransData> all = new ArrayList<>(pendingTransData);
        all.addAll(offlineTransData);
        AdapterBills adapterBills = new AdapterBills(cntxt, all);
        bills.setAdapter(adapterBills);
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
        new ApiServices(cntxt, false).offlineBillPayment(ModelClientPaymentV,
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            int billsStatus = responseBody.optInt("UserNewBillStatus");
                            int userStatus = responseBody.getInt("UserStatus");
                            String operationStatus = responseBody.getString("OperationStatus");
                            //Log.e("OfflinePayment", response);
                            if (!operationStatus.trim().equalsIgnoreCase("successful")) {
                                if (Error.contains("ليس لديك صلاحيات الوصول للهندسه") || Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول") || userStatus == 0) {
                                    MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                    Toast.makeText(cntxt, Error, Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(FinishPendingTransActivity.this, LoginActivity.class));
                                    finish();
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
                                    BaseDbHelper.getInstance(cntxt).dropTables();
                                }

                                // DBHelper.getInstance(cntxt).deleteTransData(offlineTransData);
                                for (TransData t :
                                        offlineTransData) {
                                    t.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                    DBHelper.getInstance(cntxt).updateTransData(t);
                                    pendingTransData.add(t);
                                }

                                offlineTransData.clear();
                                onFailure(null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        if (failureMsg != null)
                            Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        MiniaElectricity.getPrefsManager().setOfflineBillsStatus(0);
                        handlePendingBills();
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
                aVoidReq(transData);
            } else if (TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send void DRM
                sendDRM(true, transData);
            } else if (TransData.STATUS.PAID_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send DRM
                sendDRM(false, transData);
            } else handlePendingBills();

        } else finishOK();
    }

    private void deletePayment(final TransData transData) {
        transData.setStatus(TransData.STATUS.PENDING_DELETE_REQ.getValue());
        DBHelper.getInstance(cntxt).updateTransData(transData);
        new ApiServices(cntxt, false).cancelBillPayment(transData.getBankTransactionID(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        // whatever the response of delete req suppose it is succeeded
                        if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue()) {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue());
                            DBHelper.getInstance(cntxt).updateTransData(transData);
                            sendDRM(true, transData);
                            // DBHelper.getInstance(cntxt).deleteBillData(billData);
                        } else {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            //DBHelper.getInstance(cntxt).deleteBillData(billData);
                            // send void request to QNB payment App
                            aVoidReq(transData);
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        handlePendingBills();
                    }
                });
    }

    private void sendDRM(boolean isVoided, final TransData transData) {
       /* JsonObject SendContent = new JsonObject(), EMVData;
        EMVData = new JsonObject();

        EMVData.addProperty("AID", "A0000000031010");
        EMVData.addProperty("ApplicationLabel", "VISA DEBIT");
        EMVData.addProperty("CVMRes", "440302");
        EMVData.addProperty("TSI", "F800");
        EMVData.addProperty("TVR", "0080008000");
        SendContent.add("EMVdata", EMVData);

        SendContent.addProperty("login", "PAX_POS");
        SendContent.addProperty("password", "PAX_pos3");
        SendContent.addProperty("access_key", "PAX_ACCESS");
        SendContent.addProperty("OrganizationId", 4);
        SendContent.addProperty("TID", MiniaElectricity.getPrefsManager().getTerminalId());
        SendContent.addProperty("MID", MiniaElectricity.getPrefsManager().getMerchantId());
        SendContent.addProperty("Header1", "test ECR");
        SendContent.addProperty("Header2", *//*"           MAIN ADDRESS"*//*MiniaElectricity.getPrefsManager().getCollectorCode());
        //SendContent.addProperty("Header3", "MerchantAddress2");
        SendContent.addProperty("BATCH", "000001");
        SendContent.addProperty("STAN", transData.getStan());
        long amount = 0;
        for (BillDetails b :
                transData.getDetails()) {
            amount +=*//* (b.getBillValue()) * 100
                    +*//* (b.getCommissionValue()) * 100;
        }
        SendContent.addProperty("Amount", String.valueOf(amount));
        SendContent.addProperty("Amount", amount);
        SendContent.addProperty("CurrencyId", 1); // for EGP
        SendContent.addProperty("CardName", "cash");
        String tempString = transData.getClientMobileNo();
        String masked = "";
        if (tempString.length() > 4) {
            masked = tempString.substring(tempString.length() - 4);
            for (int i = 0; i < tempString.length() - 4; i++) {
                masked = "*".concat(masked);
            }
        }
        SendContent.addProperty("PAN", masked);
        SendContent.addProperty("ExpDate", "/"); // masked in transdata????
        SendContent.addProperty("CardHolderName", transData.getDetails().get(0).getClientName());
        SendContent.addProperty("TransactionTypeId", 10);
        SendContent.addProperty("IsVoided", isVoided);
        SendContent.addProperty("TransactionStatus", true);
        SendContent.addProperty("ResponseCode", "00");
        SendContent.addProperty("AuthId", transData.getCommission());
        SendContent.addProperty("RRN", transData.getClientID());
        SendContent.addProperty("EntryModeId", 3);
        SendContent.addProperty("PinEntry", "Offline PIN Entered");
        SendContent.addProperty("OnlineProcessing", "Online");

        SendContent.addProperty("TrxDate", transData.getTransDate());
        SendContent.addProperty("TrxTime", transData.getTransTime());

        SendContent.addProperty("DCC_TRX", false);
        SendContent.addProperty("ResponseMessage1", "TXN. ACCEPTED 00");
        SendContent.addProperty("ResponseMessage2", "");
        SendContent.addProperty("CardHolderPhone", transData.getClientMobileNo()); // ????????
        SendContent.addProperty("Signature", "");
        Log.i("SendContent", SendContent.toString());
*/
        //Log.e("getDrmData", transData.getDrmData());
        if (transData.getDrmData() != null && !transData.getDrmData().isEmpty())
            new ApiServices(cntxt, true).sendDRM((JsonObject) new JsonParser().parse(transData.getDrmData()), new RequestListener() {
                @Override
                public void onSuccess(String response) {
                    //Log.i("onSuccess", response);
                    JSONObject responseBody = null;
                    try {
                        responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                        String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                        if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                            transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                            DBHelper.getInstance(cntxt).updateTransData(transData);
                            for (TransBill b :
                                    transData.getTransBills()) {
                                DBHelper.getInstance(cntxt).deleteTransBill(b.getBillUnique());
                            }
                            DBHelper.getInstance(cntxt).deleteTransData(transData);
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
                    handlePendingBills();
                }
            });
    }

    private void aVoidReq(TransData transData) {
        VoidMsg.Request request = new VoidMsg.Request();

        request.setEcrRef(transData.getReferenceNo());
        request.setNeedReceipt(true);
        request.setNeedToConfirm(false);
        transAPI = TransAPIFactory.createTransAPI();
        request.setCategory(SdkConstants.CATEGORY_VOID);
        request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
        transAPI.startTrans(cntxt, request);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);
        if (baseResponse == null) {
            //Log.e("onActivityResult", "null");
            handlePendingBills();
        } else {
            boolean isTransResponse = baseResponse instanceof TransResponse;
            if (isTransResponse) {
                final TransResponse transResponse = (TransResponse) baseResponse;
                //Log.e("response", "//" + transResponse.toString());
                if (transResponse.getRspCode() == 0 || transResponse.getRspCode() == -15
                        || transResponse.getRspCode() == -16 || transResponse.getRspCode() == -17 || transResponse.getRspCode() == -18) {
                    TransData transData = DBHelper.getInstance(cntxt).getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo());
                    if (transData != null) {

                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        for (TransBill bill :
                                transData.getTransBills()) {
                            DBHelper.getInstance(cntxt).deleteTransBill(bill.getBillUnique());
                        }
                        DBHelper.getInstance(cntxt).deleteTransData(transData);
                    }
                    //DBHelper.getInstance(cntxt).updateTransData(transData);
                }
            } else {
                //Log.e("onActivityResult", "BaseResponse");
                if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15
                        || baseResponse.getRspCode() == -16 || baseResponse.getRspCode() == -17 || baseResponse.getRspCode() == -18) {
                    TransData transData = DBHelper.getInstance(cntxt).getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo());
                    if (transData != null) {

                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        for (TransBill bill :
                                transData.getTransBills()) {
                            DBHelper.getInstance(cntxt).deleteTransBill(bill.getBillUnique());
                        }
                        DBHelper.getInstance(cntxt).deleteTransData(transData);
                    }
                    //DBHelper.getInstance(cntxt).updateTransData(transData);
                }
            }
            handlePendingBills();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);

    }

    private void finishOK() {
        setResult(RESULT_OK, null);
        finish();
    }
}
