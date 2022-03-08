/*
package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.print.PrintReceipt;
import com.google.gson.JsonObject;
import com.pax.unifiedsdk.factory.ITransAPI;
import com.pax.unifiedsdk.factory.TransAPIFactory;
import com.pax.unifiedsdk.message.BaseResponse;
import com.pax.unifiedsdk.message.SaleMsg;
import com.pax.unifiedsdk.message.TransResponse;
import com.pax.unifiedsdk.sdkconstants.SdkConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class UnitBillsPaymentFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    Context cntxt;
    TextView tv_clientID, tv_clientName, tv_billDate, tv_billValue;
    String response;
    Button b_pay;
    LinearLayout ll_paymentMethods;
    EditText et_clientMobileNo;

    public ITransAPI transAPI;
    //float commission = 0;
    SpotsDialog progressDialog;
    private static TransData transData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cntxt = getActivity();
        fm = getFragmentManager();

        progressDialog = new SpotsDialog(cntxt, R.style.SwitchingProgress);
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unit_bill_payment, container, false);

        MainActivity.setTitleText(getString(R.string.unit_details));
        MainActivity.setBackAction(1);
        tv_billDate = view.findViewById(R.id.bill_date);
        tv_billValue = view.findViewById(R.id.bill_value);
        tv_clientID = view.findViewById(R.id.client_id);
        tv_clientName = view.findViewById(R.id.client_name);
        view.findViewById(R.id.cash_payment).setOnClickListener(this);
        view.findViewById(R.id.card_payment).setOnClickListener(this);
        view.findViewById(R.id.wallet_payment).setOnClickListener(this);
        b_pay = view.findViewById(R.id.pay);
        b_pay.setOnClickListener(this);
        ll_paymentMethods = view.findViewById(R.id.payment_methods);
        et_clientMobileNo = view.findViewById(R.id.client_mobile_no);
        setBillData();
        return view;
    }

    private void setBillData() {
        Bundle bundle = getArguments();
        long RECEIPT_NO = MiniaElectricity.getPrefsManager().getReceiptNo();
        MiniaElectricity.getPrefsManager().setReceiptNo(RECEIPT_NO + 1);
        response = bundle.getString("response");
        try {
            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
            transData = new TransData(String.valueOf(RECEIPT_NO), bundle.getString("clientID"), responseBody.optString("ClientName"), responseBody.optString("InquiryID"), responseBody.optString("BillDate"), responseBody.optString("BillValue"), TransData.STATUS.INITIATED.getValue());
            tv_clientID.setText(transData.getClientID());
            tv_clientName.setText(transData.getClientName());
            tv_billValue.setText(transData.getBillValue());
            tv_billDate.setText(transData.getBillDate());
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.card_payment) {
            if (et_clientMobileNo.getText().toString().isEmpty()) {
                Toast.makeText(cntxt, "برجاء كتابة رقم تليفون العميل", Toast.LENGTH_LONG).show();
            } else {
                transData.setClientMobileNo(et_clientMobileNo.getText().toString());
                requestCardPayment();
            }
        } else if (id == R.id.cash_payment) {
            if (et_clientMobileNo.getText().toString().isEmpty()) {
                Toast.makeText(cntxt, "برجاء كتابة رقم تليفون العميل", Toast.LENGTH_LONG).show();
            } else {
                transData.setClientMobileNo(et_clientMobileNo.getText().toString());
                requestCashPayment();
            }
        } else if (id == R.id.wallet_payment) {
            if (et_clientMobileNo.getText().toString().isEmpty()) {
                Toast.makeText(cntxt, "برجاء كتابة رقم تليفون العميل", Toast.LENGTH_LONG).show();
            } else {
                transData.setClientMobileNo(et_clientMobileNo.getText().toString());
                Toast.makeText(cntxt, "هذه الخدمة ستكون متوفرة قريباً", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.pay) {
            b_pay.setVisibility(View.GONE);
            ll_paymentMethods.setVisibility(View.VISIBLE);
        }
    }

    private void requestCashPayment() {
        transData.setStan(transData.getReferenceNo());
        transData.setTransDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        transData.setTransTime(new SimpleDateFormat("HH:mm", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        transData.setCommission(String.valueOf(1.0f));
        transData.setStatus(TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());
        DBHelper.getInstance(cntxt).updateTransData(transData);
        new ApiServices(cntxt, false).billPayment(transData.getInquiryID(), TransData.PaymentType.CASH.getValue(),
                transData.getClientMobileNo(), transData.getClientID(), transData.getBillDate(),
                transData.getBillValue(), "0.0", transData.getTransDate(),
                transData.getStan(), transData.getClientID() + transData.getBillDate(),
                "0", "0", new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            Log.e("requestCashPayment", response);
                            if (Error != null && !Error.isEmpty()) {
                                onFailure("فشل في عملية الدفع!\n" + Error);
                            } else {
                                new PrintReceipt(cntxt, transData);
                                //printReceipt();
                                transData.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                DBHelper.getInstance(cntxt).updateTransData(transData);
                                sendCashDRM(false);
                                // cancelPaymentRequest(clientID + tv_billDate.getText().toString().trim());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                      */
/*  if (isFirst) {
                            isFirst = false;
                            if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) == 1) //connected to sim2
                                MiniaElectricity.getDal().getSys().switchSimCard(1); //switch to sim1
                            else if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) == 0) //connected to sim1
                                MiniaElectricity.getDal().getSys().switchSimCard(2); //switch to sim2
                            while (!Utils.checkConnection(MiniaElectricity.getInstance())) {
                            }
                            requestCashPayment();
                        } else*//*

                        cancelPaymentRequest(transData.getClientID() + transData.getBillDate());
                    }
                });
    }

    private void requestCardPayment() {
        transData.setTransDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        transData.setTransTime(new SimpleDateFormat("HH:mm", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        transData.setStatus(TransData.STATUS.PENDING_SALE_REQ.getValue());
        DBHelper.getInstance(cntxt).updateTransData(transData);
        SaleMsg.Request request = new SaleMsg.Request();
        String temp = String.valueOf((Math.ceil(Double.parseDouble(transData.getBillValue()) * 100)));
        if (temp.contains(".")) {
            temp = temp.substring(0, temp.indexOf("."));
        }
        Log.e("Amount", temp);
        request.setAmount(Long.parseLong(temp));
        transAPI = TransAPIFactory.createTransAPI();
        request.setCategory(SdkConstants.CATEGORY_SALE);
        request.setPackageName("com.ebe.edc.qnb");
        transAPI.startTrans(cntxt, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);
        if (baseResponse == null) {
            */
/*final String BankTransactionID = clientID + tv_billDate.getText().toString().trim();
            Log.e("BankTransactionID", BankTransactionID);
            new ApiServices(cntxt).billPayment(inquiryID, CARD, et_clientMobileNo.getText().toString().trim(), clientID, tv_billDate.getText().toString().trim(),
                    tv_billValue.getText().toString().trim(), "0", new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(new Date(System.currentTimeMillis())), String.valueOf(MiniaElectricity.getPrefsManager().getReceiptNo()),
                    BankTransactionID, "5686", "969627",
                    new RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                                String Error = responseBody.optString("Error").trim();
                                Log.e("billPayment", response);
                                if (Error != null && !Error.isEmpty()) {
                                    onFailure("فشل في عملية الدفع!\n" + Error);
                                } else {
                                    commission = (float) (1.5 + Float.parseFloat(tv_billValue.getText().toString().trim()) * 0.8);
                                    printReceipt();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                onFailure(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String failureMsg) {
                            Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                            cancelPaymentRequest(BankTransactionID);
                        }
                    });*//*


            Log.e("onActivityResult", "null");
        }
        boolean isTransResponse = baseResponse instanceof TransResponse;
        if (isTransResponse) {
            final TransResponse transResponse = (TransResponse) baseResponse;
            Log.e("response", "//" + transResponse.toString());
            if (transResponse.getRspCode() == 0) {
                cardPayment(transResponse);
            }
        } else Log.e("onActivityResult", "BaseResponse");

    }

    private void cardPayment(final TransResponse transResponse) {
        final String BankTransactionID = transResponse.getTerminalId() +
                transResponse.getMerchantId() + transResponse.getAuthCode() + transResponse.getCardNo().substring(transResponse.getCardNo().length() - 4);
        Log.e("BankTransactionID", BankTransactionID);
        transData.setStan(String.valueOf(transResponse.getVoucherNo()));
        transData.setStatus(TransData.STATUS.PENDING_CARD_PAYMENT_REQ.getValue());
        DBHelper.getInstance(cntxt).updateTransData(transData);
        new ApiServices(cntxt, false).billPayment(transData.getInquiryID(), TransData.PaymentType.CARD.getValue(), transData.getClientMobileNo(), transData.getClientID(),
                transData.getBillDate(), transData.getBillValue(), "0", */
/*response.getTransTime()*//*
new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(new Date(System.currentTimeMillis())), transData.getStan(),
                BankTransactionID, transResponse.getCardNo(), transResponse.getAuthCode(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            Log.e("billPayment", response);
                            if (Error != null && !Error.isEmpty()) {
                                onFailure("فشل في عملية الدفع!\n" + Error);
                            } else {
                                transData.setCommission(String.valueOf(1 + Float.parseFloat(transData.getBillValue()) * 0.007));
                                new PrintReceipt(cntxt, transData);
//                                printReceipt();
                                transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                                DBHelper.getInstance(cntxt).deleteTransData(transData);
                                //sendDRM(transResponse);
                                //cancelPaymentRequest(BankTransactionID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        cancelPaymentRequest(BankTransactionID);

                    }
                });
    }

    private void cancelPaymentRequest(final String BankTransactionID) {
        transData.setStatus(TransData.STATUS.PENDING_DELETE_REQ.getValue());
        DBHelper.getInstance(cntxt).updateTransData(transData);
        new ApiServices(cntxt, false).cancelBillPayment(BankTransactionID,
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                       */
/* try {
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            Log.e("cancelPaymentRequest", response);
                            if (Error != null && !Error.isEmpty()) {
                                onFailure("فشل في عملية الغاء الدفع!\n" + Error);
                            } else {
                                Toast.makeText(cntxt, "تمَّ إلغاء عملية الدفع!", Toast.LENGTH_LONG).show();
                                MainActivity.fragmentTransaction(new NewHomeFragment(), null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage());
                        }*//*

                        // whatever the response of delete req suppose it is succeeded
                        if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue()) {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue());
                            DBHelper.getInstance(cntxt).updateTransData(transData);
                            sendCashDRM(true);
                            // DBHelper.getInstance(cntxt).deleteBillData(billData);
                        } else {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            DBHelper.getInstance(cntxt).deleteTransData(transData);
                            // send void request to QNB payment App
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        //cancelPaymentRequest(BankTransactionID);
                    }
                });
    }

    private void sendDRM(TransResponse response) {
        int TxnTypeID = -1, EntryMode = -1, CurrencyID;
        try {
            JSONObject EMVData, SendContent = new JSONObject();
            Bundle bundle = response.getExtraBundle();
            String transType = bundle.getString("TransactionType");
            String PinEntryMode = bundle.getString("response_pin_entry_mode");
            if (SdkConstants.CATEGORY_SALE.equals(transType)) {
                if (response.getCardType() == TransResponse.ICC || response.getCardType() == TransResponse.PICC) {
                    TxnTypeID = 10;
                } else TxnTypeID = 1;
            }
            if (response.getCardType() == TransResponse.ICC || response.getCardType() == TransResponse.PICC ||
                    PinEntryMode.equals("ONLINE PIN VERIFIED") || PinEntryMode.equals("OFFLINE PIN VERIFIED")) {
                if (response.getCardType() == TransResponse.PICC) {
                    EntryMode = 4;
                } else EntryMode = 3;
                EMVData = new JSONObject();

                EMVData.put("AID", bundle.getString("response_aid"));
                EMVData.put("ApplicationLabel", bundle.getString("response_app_name"));
                EMVData.put("CVMRes", bundle.getString("response_pin_entry_mode"));
                EMVData.put("TSI", bundle.getString("response_tsi"));
                EMVData.put("TVR", bundle.getString("response_tvr"));
                SendContent.put("EMVdata", EMVData);

            } else if (response.getCardType() == TransResponse.MAG)
                EntryMode = 2;
            else if (response.getCardType() == TransResponse.FALLBACK)
                EntryMode = 6;
            else if (response.getCardType() == TransResponse.MANUAL)
                EntryMode = 1;

            switch (bundle.getString("response_currency_numeric")) {
                case "818":
                    CurrencyID = 1;
                    break;
                case "840":
                    CurrencyID = 2;
                    break;
                case "978":
                    CurrencyID = 3;
                    break;
                case "826":
                    CurrencyID = 4;
                    break;
                default:
                    CurrencyID = -1;
                    break;
            }

            SendContent.put("login", "PAX_POS");
            SendContent.put("password", "PAX_pos3");
            SendContent.put("access_key", "PAX_ACCESS");
            SendContent.put("OrganizationId", 4);
            SendContent.put("TID", response.getTerminalId());
            SendContent.put("MID", response.getMerchantId());
            SendContent.put("Header1", response.getMerchantName());
            SendContent.put("Header2", bundle.getString("response_merchant_address"));
            SendContent.put("Header3", "");
            SendContent.put("BATCH", response.getBatchNo());
            SendContent.put("STAN", response.getVoucherNo());
            SendContent.put("Amount", response.getAmount());
            SendContent.put("CurrencyId", CurrencyID);
            SendContent.put("CardName", bundle.getString("response_app_name"));
            SendContent.put("PAN", response.getCardNo());
            SendContent.put("ExpDate", "****"); // masked in transdata????
            SendContent.put("CardHolderName", bundle.getString("response_card_holder_name"));
            SendContent.put("TransactionTypeId", TxnTypeID);
            SendContent.put("IsVoided", false);
            SendContent.put("TransactionStatus", response.getRspCode() == 0);
            SendContent.put("ResponseCode", response.getRspCode());
            SendContent.put("AuthId", response.getAuthCode());
            SendContent.put("RRN", response.getRefNo());
            SendContent.put("EntryModeId", EntryMode);
            SendContent.put("PinEntry", bundle.getString("response_pin_entry_mode"));
            SendContent.put("OnlineProcessing", "Online");
            String temp = response.getTransTime();
            if (temp != null && !temp.isEmpty()) {
                String date = temp.substring(4, 8);
                String time = temp.substring(8);
                SendContent.put("TrxDate", date);
                SendContent.put("TrxTime", time);
            }
            SendContent.put("DCC_TRX", false);
            SendContent.put("ResponseMessage1", response.getRspMsg());
            SendContent.put("ResponseMessage2", "");
            SendContent.put("CardHolderPhone", et_clientMobileNo.getText().toString()); // ????????
            SendContent.put("Signature", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendCashDRM(boolean isVoided) {
        JsonObject SendContent = new JsonObject(), EMVData;
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
        SendContent.addProperty("TID", "00129742");
        SendContent.addProperty("MID", "000000010002913");
        SendContent.addProperty("Header1", "test ECR");
        SendContent.addProperty("Header2", */
/*"           MAIN ADDRESS"*//*
MiniaElectricity.getPrefsManager().getCollectorCode());
        //SendContent.addProperty("Header3", "MerchantAddress2");
        SendContent.addProperty("BATCH", "000001");
        SendContent.addProperty("STAN", transData.getStan());
        double temp = (Double.parseDouble(transData.getBillValue()) + Double.parseDouble(transData.getCommission())) * 100;
        SendContent.addProperty("Amount", String.valueOf((long) temp));
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
        SendContent.addProperty("CardHolderName", transData.getClientName());
        SendContent.addProperty("TransactionTypeId", 10);
        SendContent.addProperty("IsVoided", isVoided);
        SendContent.addProperty("TransactionStatus", true);
        SendContent.addProperty("ResponseCode", "00");
        SendContent.addProperty("AuthId", "123456");
        SendContent.addProperty("RRN", transData.getClientID());
        SendContent.addProperty("EntryModeId", 3);
        SendContent.addProperty("PinEntry", "Offline PIN Entered");
        SendContent.addProperty("OnlineProcessing", "Online");

        SendContent.addProperty("TrxDate", transData.getTransDate());
        SendContent.addProperty("TrxTime", transData.getTransTime());

        SendContent.addProperty("DCC_TRX", false);
        SendContent.addProperty("ResponseMessage1", "TXN. ACCEPTED 00");
        SendContent.addProperty("ResponseMessage2", "");
        SendContent.addProperty("CardHolderPhone", transData.getClientName()); // ????????
        SendContent.addProperty("Signature", "");
        Log.i("SendContent", SendContent.toString());

        new ApiServices(cntxt, true).sendDRM(SendContent, new RequestListener() {
            @Override
            public void onSuccess(String response) {
                Log.i("onSuccess", response);
                transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                DBHelper.getInstance(cntxt).updateTransData(transData);
            }

            @Override
            public void onFailure(String failureMsg) {
                Log.i("failureMsg", failureMsg);
            }
        });
    }
}
*/
