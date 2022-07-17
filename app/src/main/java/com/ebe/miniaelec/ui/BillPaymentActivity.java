package com.ebe.miniaelec.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.QRSaleMsg;
import com.ebe.ebeunifiedlibrary.message.SaleMsg;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.message.VoidMsg;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.BaseDbHelper;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.DeductType;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.model.Report;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.print.PrintListener;
import com.ebe.miniaelec.print.PrintReceipt;
import com.ebe.miniaelec.utils.ToastUtils;
import com.ebe.miniaelec.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//import android.widget.Toast;

public class BillPaymentActivity extends AppCompatActivity implements View.OnClickListener {

    FragmentManager fm;
    Context cntxt;
    TextView tv_clientID, tv_address, tv_currentMeter, tv_billValue, selected_bills_value;
    String phoneNumber;
    Button b_pay, b_deduct;
    LinearLayout ll_paymentMethods, ll_phone_number;
    // EditText et_clientMobileNo;
    String trxnTime;
    LinearLayout ll_bills;
    Spinner paymentTypes;

    public ITransAPI transAPI;
    //float commission = 0;
    private TransData transData;
    private CheckBox[] cb_bills;
    double selectedBillsValue;
    double percentageFees;
    private ArrayList<BillData> billDetails;
    private boolean offline, allowCancel = true;
    private ArrayList<TransBill> transBills;
    private ArrayList<DeductType> deductTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_bill_payment);
        cntxt = BillPaymentActivity.this;
        setStatusBarColor();
        fm = getFragmentManager();
        tv_address = findViewById(R.id.address);
        tv_billValue = findViewById(R.id.bill_value);
        tv_clientID = findViewById(R.id.client_id);
        tv_currentMeter = findViewById(R.id.current_meter);
        findViewById(R.id.cash_payment).setOnClickListener(this);
        findViewById(R.id.card_payment).setOnClickListener(this);
        findViewById(R.id.wallet_payment).setOnClickListener(this);
        b_pay = findViewById(R.id.pay);
        b_pay.setOnClickListener(this);
        b_deduct = findViewById(R.id.deduct);
        b_deduct.setOnClickListener(this);
        ll_paymentMethods = findViewById(R.id.payment_methods);
        ll_phone_number = findViewById(R.id.ll_phone_number);
        // et_clientMobileNo = findViewById(R.id.client_mobile_no);
        ll_bills = findViewById(R.id.ll_bills);
        paymentTypes = findViewById(R.id.payment_types);
        selected_bills_value = findViewById(R.id.selected_bills_value);
        deductTypes = new ArrayList<>(DBHelper.getInstance(cntxt).getDeductTypes());
        setBillData();

    }

    private void setBillData() {
        Bundle bundle = getIntent().getBundleExtra("params");
        long RECEIPT_NO = MiniaElectricity.getPrefsManager().getReceiptNo();
        MiniaElectricity.getPrefsManager().setReceiptNo(RECEIPT_NO + 1);
        offline = bundle.getBoolean("offline");
        String clientId = bundle.getString("clientID");
        String inquiryId = "";
        if (offline) {
            inquiryId = MiniaElectricity.getPrefsManager().getInquiryID();
            OfflineClient offlineClient = DBHelper.getInstance(cntxt).getClientByClientId(clientId);
            if (offlineClient == null) {
//                Toast.makeText(cntxt, "رقم الاشتراك غير صحيح!", Toast.LENGTH_SHORT).show();
                ToastUtils.showMessage(this, "رقم الاشتراك غير صحيح!");
                this.finish();
                return;
            }
            phoneNumber = offlineClient.getClientMobileNo();
            billDetails = offlineClient.getModelBillInquiryV();
        } else {
            String response = bundle.getString("response");
            // OfflineClient client = (OfflineClient) bundle.getSerializable("response");
            try {
                JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                inquiryId = responseBody.optString("InquiryID");
                phoneNumber = responseBody.optString("ClientMobileNo");

                JSONArray billsData = responseBody.optJSONArray("ModelBillInquiryV");
                billDetails = new ArrayList<>();
                for (int i = 0; i < billsData.length(); i++) {
                    BillData bill = new Gson().fromJson(billsData.getJSONObject(i).toString(), BillData.class);
                    billDetails.add(bill);
                }


            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
        transData = new TransData((int) RECEIPT_NO, clientId,
                inquiryId, TransData.STATUS.INITIATED.getValue());
        if (phoneNumber != null && !phoneNumber.isEmpty() && !phoneNumber.equalsIgnoreCase("null")) {
//            ll_phone_number.setVisibility(View.GONE);
        } else phoneNumber = "01064030305";
        //ll_phone_number.setVisibility(View.VISIBLE);

        tv_clientID.setText(transData.getClientID());
        tv_currentMeter.setText(billDetails.get(0).getPreviousRead());
        tv_address.setText(billDetails.get(billDetails.size() - 1).getClientAddress());
        cb_bills = new CheckBox[billDetails.size()];

        for (int i = 0; i < billDetails.size(); i++) {
            CheckBox checkBox = new CheckBox(cntxt);
            String s = billDetails.get(i).getClientName() + "\n" + "فاتورة شهر " + billDetails.get(i).getBillDate() + ": " + billDetails.get(i).getBillValue() + getString(R.string.egp);
            checkBox.setText(s);
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            checkBox.setEnabled(false);
            cb_bills[i] = checkBox;
            ll_bills.addView(checkBox);
            View view = new View(cntxt);
            view.setBackgroundColor(getResources().getColor(R.color.view_line));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 20, 0, 20);
            view.setLayoutParams(params);
            view.setMinimumHeight(4);
            ll_bills.addView(view);
            final int finalI = i;
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        if (finalI < cb_bills.length - 1) {
                            cb_bills[finalI + 1].setEnabled(true);
                        }
                        selectedBillsValue += billDetails.get(finalI).getBillValue() + billDetails.get(finalI).getCommissionValue() +
                                billDetails.get(finalI).getBillValue() * percentageFees;
                        tv_currentMeter.setText(billDetails.get(finalI).getCurrentRead());
                    } else {
                        for (int j = finalI; j < cb_bills.length; j++) {
                            if (cb_bills[j].isChecked())
                                selectedBillsValue -= billDetails.get(j).getBillValue() + billDetails.get(j).getCommissionValue() +
                                        billDetails.get(j).getBillValue() * percentageFees;
                            cb_bills[j].setChecked(false);
                            cb_bills[j].setEnabled(false);
                        }
                        selectedBillsValue -= (billDetails.get(finalI).getBillValue() + billDetails.get(finalI).getCommissionValue() +
                                billDetails.get(finalI).getBillValue() * percentageFees);
                        cb_bills[finalI].setEnabled(true);
                        if (finalI == 0) {
                            tv_currentMeter.setText(billDetails.get(0).getPreviousRead());
                        } else
                            tv_currentMeter.setText(billDetails.get(finalI - 1).getCurrentRead());
                        //cb_bills[0].setChecked(true);
                    }
                    selected_bills_value.setText(String.valueOf(Utils.decimalFormat(selectedBillsValue)));

                }
            });
        }
        cb_bills[0].setEnabled(true);
        paymentTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_bills_value.setText("");
                selectedBillsValue = 0;
                for (CheckBox c :
                        cb_bills) {
                    c.setChecked(false);
                    c.setEnabled(false);
                    cb_bills[0].setEnabled(true);
                }
                if (position == 0) {
                    percentageFees = 0;
                } else {
                    percentageFees = MiniaElectricity.getPrefsManager().getPercentFees();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        /*if (ll_phone_number.getVisibility() == View.VISIBLE && (et_clientMobileNo.getText().toString().trim().isEmpty() ||
                et_clientMobileNo.getText().toString().trim().length() < 11 ||
                et_clientMobileNo.getText().toString().trim().length() > 16)) {
//            Toast.makeText(cntxt, "برجاء كتابة رقم تليفون العميل بشكل صحيح", Toast.LENGTH_LONG).show();
            ToastUtils.showMessage(this, "برجاء كتابة رقم تليفون العميل بشكل صحيح");
        } else*/
        {
            transBills = new ArrayList<>();
            for (int i = 0; i < cb_bills.length; i++) {
                if (cb_bills[i].isChecked()) {
                    transBills.add(new TransBill(billDetails.get(i)));
                } else break;
            }
            if (transBills.size() == 0) {
//                Toast.makeText(cntxt, "يرجى اختيار الفواتير المطلوب سدادها!", Toast.LENGTH_LONG).show();
                ToastUtils.showMessage(this, "يرجى اختيار الفواتير المطلوب سدادها!");
                return;
            }
            /*if (ll_phone_number.getVisibility() == View.VISIBLE) {
                transData.setClientMobileNo(et_clientMobileNo.getText().toString());
            } else*/
            transData.setClientMobileNo(phoneNumber);
            if (id == R.id.pay) {
                if (paymentTypes.getSelectedItemPosition() == 1 && !offline) { // 1 card payment
                    transData.setPaymentType(TransData.PaymentType.CARD.getValue());
                    transData.setStatus(TransData.STATUS.PENDING_SALE_REQ.getValue());
                    confirmPayment();
                } else if (paymentTypes.getSelectedItemPosition() == 0) { // 0 cash payment
                    transData.setPaymentType(TransData.PaymentType.CASH.getValue());
                    transData.setStatus(TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());
                    confirmPayment();
                } else if (paymentTypes.getSelectedItemPosition() == 2 && !offline) { // 2 wallet payment
//                    transData.setPaymentType(TransData.PaymentType.WALLET.getValue());
//                    transData.setStatus(TransData.STATUS.PENDING_QR_SALE_REQ.getValue());
                    //requestCashPayment();
//                    confirmPayment();
//                    Toast.makeText(cntxt, "هذه الخدمة ستكون متوفرة قريباً", Toast.LENGTH_LONG).show();
                    ToastUtils.showMessage(this, "هذه الخدمة ستكون متوفرة قريباً");
                }
            } else if (id == R.id.deduct) {
                if (deductTypes.size() > 0) {
                    selectDeductType();
                } else if (!offline) {
                    new ApiServices(cntxt).deductsTypes(new RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            JSONObject responseBody = null;
                            try {
                                responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                                String Error = responseBody.optString("Error").trim();
                                if (!Error.isEmpty()) {
                                    JSONArray deducts = new JSONArray(response);
                                    if (deducts.length() > 0) {
                                        BaseDbHelper.getInstance(cntxt).clearDeducts();
                                    }
                                    for (int i = 0; i < deducts.length(); i++) {
                                        DeductType deductType = new Gson().fromJson(deducts.get(i).toString(), DeductType.class);
                                        DBHelper.getInstance(cntxt).addDeductType(deductType);
                                        deductTypes.add(deductType);
                                    }
                                    selectDeductType();
                                } else
                                    ToastUtils.showMessage(BillPaymentActivity.this, getString(R.string.err_no_deducts));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ToastUtils.showMessage(BillPaymentActivity.this, getString(R.string.err_no_deducts));
                            }
                        }

                        @Override
                        public void onFailure(String failureMsg) {
                            ToastUtils.showMessage(BillPaymentActivity.this, getString(R.string.err_no_deducts));
                        }
                    });
                } else
                    ToastUtils.showMessage(BillPaymentActivity.this, getString(R.string.err_no_deducts));
            }
        }
    }

    private void selectDeductType() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(cntxt);
        alertDialog.setTitle(cntxt.getString(R.string.select_deduct_type));
        ArrayList<String> types = new ArrayList<>();
        for (DeductType dt : deductTypes) {
            types.add(dt.getDeductType());
        }
        final Spinner sp_deducts = new Spinner(cntxt);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        types); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        sp_deducts.setAdapter(spinnerArrayAdapter);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        sp_deducts.setLayoutParams(params);
        alertDialog.setView(sp_deducts);
        alertDialog.setPositiveButton(cntxt.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtils.showMessage(BillPaymentActivity.this, deductTypes.get(sp_deducts.getSelectedItemPosition()).getDeductType());
                        transData.setStatus(TransData.STATUS.PENDING_DEDUCT_REQ.getValue());
                        transData.setDeductType(deductTypes.get(sp_deducts.getSelectedItemPosition()).getDeductId());
                        if (!DBHelper.getInstance(cntxt).addTransData(transData)) {
                            ToastUtils.showMessage(BillPaymentActivity.this, "برجاء اعادة المحاولة!");
                            finish();
                        } else {
                            if (offline) {
                                deleteBills();
                                finish();
                            } else {
                                sendDeductReq();
                            }
                        }
                    }
                });
        alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void sendDeductReq() {
        // TODO complete scenario and handle deducts in FinishPendingTrans
        JsonArray ModelBillKasmV = new JsonArray();
        for (TransBill b :
                transData.getTransBills()) {
            JsonObject j = new JsonObject();
            j.addProperty("BillUnique", b.getBillUnique());
            j.addProperty("KTID", transData.getDeductType());

            ModelBillKasmV.add(j);
        }
        new ApiServices(cntxt).sendDeducts(ModelBillKasmV, new RequestListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String Error = responseBody.optString("Error").trim();
                    //Log.e("requestCashPayment", response);
                    if (Error != null && !Error.isEmpty()) {
                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                            for (TransBill b :
                                    transBills) {
                                DBHelper.getInstance(cntxt).deleteTransBill(b.getBillUnique());
                            }
                            DBHelper.getInstance(cntxt).deleteTransData(transData);
                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                            startActivity(new Intent(BillPaymentActivity.this, LoginActivity.class));
                            BillPaymentActivity.this.finish();
                        } else onFailure("فشل في تسجيل خصم الفواتير!\n" + Error);
                    } else {
                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        DBHelper.getInstance(cntxt).updateTransData(transData);
                        deleteBills();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure("فشل في تسجيل خصم الفواتير!\n" + e.getMessage());
                }
            }

            @Override
            public void onFailure(String failureMsg) {
                ToastUtils.showMessage(BillPaymentActivity.this, failureMsg);
                finish();
            }
        });
    }

    private void confirmPayment() {

        double commission = 0, total = 0;
        for (TransBill b :
                transBills) {
            total += b.getBillValue();
            if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue()) {
                commission += (b.getCommissionValue());
            } else if (transData.getPaymentType() == TransData.PaymentType.CARD.getValue() ||
                    transData.getPaymentType() == TransData.PaymentType.WALLET.getValue()) {
                commission += b.getCommissionValue()
                        + b.getBillValue() * MiniaElectricity.getPrefsManager().getPercentFees();
            }
        }
        String temp =
                "إجمالي الفواتير: " + Utils.decimalFormat(total) + getString(R.string.egp) + '\n' +
                        "خدمات الكترونية: " + Utils.decimalFormat(commission) + getString(R.string.egp) + '\n' +
                        "المبلغ المطلوب سداده: " + Utils.decimalFormat(commission + total) + getString(R.string.egp);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(cntxt);
        TextView tv = new TextView(cntxt);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(params);
        alertDialog.setView(tv);
        tv.setText(temp);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.START);
        tv.setPadding(10, 25, 25, 10);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        alertDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        b_pay.setEnabled(false);
                        b_deduct.setEnabled(false);
                        allowCancel = false;
                        if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue()) {
                            requestCashPayment();
                        } else if (transData.getPaymentType() == TransData.PaymentType.CARD.getValue()) {
                            requestCardPayment();
                        } else if (transData.getPaymentType() == TransData.PaymentType.WALLET.getValue()) {
                            requestQRPayment();
                        }
                    }
                });

        alertDialog.show();
    }

    private void requestCashPayment() {
        transData.setStan(String.valueOf(transData.getReferenceNo()));
        transData.setTransDateTime(new SimpleDateFormat(Utils.DATE_PATTERN, Locale.US)//"yyyy-MM-dd HH:mm"
                .format(new Date(System.currentTimeMillis()))); /*  transData.setTransDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis())));*/
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.US);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.US);
        Date current = new Date(System.currentTimeMillis());
        //trxnTime = dateFormat1.format(current);
        /*transData.setTransTime(dateFormat2.format(current));
        transData.setCommission(trxnTime.replaceAll(":", ""));*/
        StringBuilder bankTransactionID = new StringBuilder(transData.getClientID());
        /*bankTransactionID.append(transData.getTransDate().replaceAll("/", ""))
                .append(transData.getCommission());*/// ddMMyyyyHHmmss
        bankTransactionID.append(Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_TIME_PATTERN));
        long amount = 0;
        for (TransBill b :
                transBills) {
            amount += (b.getBillValue()) * 100
                    + (b.getCommissionValue()) * 100;
        }
        final long finalAmount = amount;
        bankTransactionID.append(amount);
        //Log.e("bankTransactionID", bankTransactionID.toString());
        transData.setBankTransactionID(bankTransactionID.toString());
        final int billsCount = transBills.size();
        long offlineDiffHours = 0;
        try {
            offlineDiffHours = Utils.calcDifferenceHours(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(transData.getTransDateTime()), new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(MiniaElectricity.getPrefsManager().getOfflineStartingTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (offline) {

            if (MiniaElectricity.getPrefsManager().getMaxOfflineBillCount() >= MiniaElectricity.getPrefsManager().getOfflineBillCount() + billsCount &&
                    MiniaElectricity.getPrefsManager().getMaxOfflineBillValue() * 100 >= MiniaElectricity.getPrefsManager().getOfflineBillValue() + amount &&
                    MiniaElectricity.getPrefsManager().getMaxOfflineHours() > offlineDiffHours) {

                transData.setPaymentType(TransData.PaymentType.OFFLINE_CASH.getValue());
                transData.setStatus(TransData.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue());
                /*if (!DBHelper.getInstance(cntxt).updateTransData(transData)) {
                    Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
                    DBHelper.getInstance(cntxt).deleteTransData(transData);
                    BillPaymentActivity.this.finish();
                }*/
                sendCashDRM(false);
                if (!DBHelper.getInstance(cntxt).addTransData(transData)) {
//                    Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
                    ToastUtils.showMessage(this, "برجاء اعادة المحاولة!");
                    this.finish();
                } else {
                    deleteBills();
                    MiniaElectricity.getPrefsManager().setOfflineBillCount(MiniaElectricity.getPrefsManager().getOfflineBillCount() + billsCount);
                    MiniaElectricity.getPrefsManager().setOfflineBillValue(MiniaElectricity.getPrefsManager().getOfflineBillValue() + finalAmount);
                    MiniaElectricity.getPrefsManager().setPaidOfflineBillsCount(MiniaElectricity.getPrefsManager().getPaidOfflineBillsCount() + billsCount);
                    MiniaElectricity.getPrefsManager().setPaidOfflineBillsValue(MiniaElectricity.getPrefsManager().getPaidOfflineBillsValue() + finalAmount);
                    DBHelper.getInstance(cntxt).addReport(new Report(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), finalAmount, billsCount, transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID()));
                    // DBHelper.getInstance(cntxt).updateTransData(transData);
                    new PrintReceipt(cntxt, transBills, new PrintListener() {
                        @Override
                        public void onFinish() {
                            //sendCashDRM(false);
                            transData.setPrintCount(1);
                            DBHelper.getInstance(cntxt).updateTransData(transData);
                            finish();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }

            } else {
                // Toast.makeText(cntxt, "لقد تجاوزت الحد الأقصى لعمليات الدفع دون مزامنة. برجاء مزامنة عمليات الدفع.", Toast.LENGTH_LONG).show();
                ToastUtils.showMessage(this, "لقد تجاوزت الحد الأقصى لعمليات الدفع دون مزامنة. برجاء مزامنة عمليات الدفع.");
                // DBHelper.getInstance(cntxt).deleteTransData(transData);
                finish();
            }

        } else {
            //DBHelper.getInstance(cntxt).updateTransData(transData);
            if (!DBHelper.getInstance(cntxt).addTransData(transData)) {
//                Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
                ToastUtils.showMessage(this, "برجاء اعادة المحاولة!");
                this.finish();
            } else {
                JsonArray ModelBillPaymentV = new JsonArray();
                for (TransBill b :
                        transBills) {
                    JsonObject j = new JsonObject();
                    j.addProperty("RowNum", b.getRawNum());
                    j.addProperty("BillDate", b.getBillDate());
                    j.addProperty("BillValue", b.getBillValue());
                    j.addProperty("CommissionValue", b.getCommissionValue());
                    j.addProperty("CommissionValueCreditCard", "0");

                    ModelBillPaymentV.add(j);
                }
                new ApiServices(cntxt, false).billPayment(transData.getInquiryID(), transData.getPaymentType(),
                        transData.getClientMobileNo(), transData.getClientID(), ModelBillPaymentV, transData.getTransDateTime(),
                        transData.getStan(), transData.getBankTransactionID(),
                        "0", "0", new RequestListener() {
                            @Override
                            public void onSuccess(String response) {
                                try {
                                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                                    String Error = responseBody.optString("Error").trim();
                                    //Log.e("requestCashPayment", response);
                                    if (Error != null && !Error.isEmpty()) {
                                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                                            for (TransBill b :
                                                    transBills) {
                                                DBHelper.getInstance(cntxt).deleteTransBill(b.getBillUnique());
                                            }
                                            DBHelper.getInstance(cntxt).deleteTransData(transData);
                                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                            startActivity(new Intent(BillPaymentActivity.this, LoginActivity.class));
                                            BillPaymentActivity.this.finish();
                                        } else onFailure("فشل في عملية الدفع!\n" + Error);
                                    } else {
                                        transData.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                        DBHelper.getInstance(cntxt).updateTransData(transData);
                                        MiniaElectricity.getPrefsManager().setPaidOnlineBillsCount(MiniaElectricity.getPrefsManager().getPaidOnlineBillsCount() + billsCount);
                                        MiniaElectricity.getPrefsManager().setPaidOnlineBillsValue(MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() + finalAmount);
                                        DBHelper.getInstance(cntxt).addReport(new Report(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), finalAmount, billsCount, transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID()));
                                        deleteBills();
                                        new PrintReceipt(cntxt, transBills, new PrintListener() {
                                            @Override
                                            public void onFinish() {
                                                sendCashDRM(false);
                                            }

                                            @Override
                                            public void onCancel() {

                                            }
                                        });
                                        //printReceipt();

                                        // cancelPaymentRequest(clientID + tv_billDate.getText().toString().trim());
                                    }
                                } catch (JSONException e) {
                                    onFailure("فشل في عملية الدفع!\n");
                                    e.printStackTrace();
                                    //  onFailure(e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(String failureMsg) {
//                                Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                                ToastUtils.showMessage(BillPaymentActivity.this, failureMsg);
                                cancelPaymentRequest();
                            }
                        });
            }
        }

    }

    private boolean deleteBills() {
        /*if (offline)
            DBHelper.getInstance(cntxt).deleteOfflineClientBills(transBills);
        else {
            for (TransBill b :
                    transBills) {
                DBHelper.getInstance(cntxt).deleteClientBillByDate(transData.getClientID(), b.getBillDate());
            }
        }*/
//        if (!DBHelper.getInstance(cntxt).addTransData(transData)) {
//            Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
//            return false;
//            //this.finish();
//        } else {
        for (TransBill b :
                transBills) {
            DBHelper.getInstance(cntxt).deleteClientBill(b.getBillUnique());
            b.setBankTransactionID(transData.getBankTransactionID());
            b.setTransData(transData);
            DBHelper.getInstance(cntxt).newTransBillAppend(b);
            DBHelper.getInstance(cntxt).updateTransBill(b);
        }
        OfflineClient client = DBHelper.getInstance(cntxt).getClientByClientId(transData.getClientID());
        if (client != null && (client.getModelBillInquiryV() == null || client.getModelBillInquiryV().size() == 0)) {
            DBHelper.getInstance(cntxt).deleteOfflineClient(client);
        }
        return true;
        //   }
    }

    private void requestCardPayment() {
        /*transData.setTransDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        transData.setTransTime(new SimpleDateFormat("HH:mm", Locale.US)
                .format(new Date(System.currentTimeMillis())));*/
        // DBHelper.getInstance(cntxt).updateTransData(transData);
        SaleMsg.Request request = new SaleMsg.Request();
        long amount = 0;
        for (TransBill b :
                transBills) {
            amount += (b.getBillValue()) * 100
                    + (b.getBillValue()) * 100 * MiniaElectricity.getPrefsManager().getPercentFees()
                    + (b.getCommissionValue()) * 100;
        }

        //Log.e("Amount", String.valueOf(amount));
        request.setAmount(amount);
        request.setEcrRefNo(transData.getReferenceNo());
        request.setNeedReceipt(true);
        transAPI = TransAPIFactory.createTransAPI();
        request.setCategory(SdkConstants.CATEGORY_SALE);
        request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
        transAPI.startTrans(cntxt, request);
    }

    private void requestQRPayment() {
       /* transData.setTransDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        transData.setTransTime(new SimpleDateFormat("HH:mm", Locale.US)
                .format(new Date(System.currentTimeMillis())));*/
        //DBHelper.getInstance(cntxt).updateTransData(transData);
        QRSaleMsg.Request request = new QRSaleMsg.Request();
        long amount = 0;
        for (TransBill b :
                transBills) {
            amount += (b.getBillValue()) * 100
                    + (b.getBillValue()) * 100 * MiniaElectricity.getPrefsManager().getPercentFees()
                    + (b.getCommissionValue()) * 100;
        }

        //Log.e("Amount", String.valueOf(amount));
        request.setAmount(amount);
        request.setNeedReceipt(true);
        transAPI = TransAPIFactory.createTransAPI();
        request.setCategory(SdkConstants.CATEGORY_SALE);
        request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
        transAPI.startTrans(cntxt, request);
    }

    private void aVoidReq() {
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
        {
            BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);

            if (baseResponse == null) {
                Log.e("onActivityResult", "null");
                aVoidReq();
            }
            boolean isTransResponse = baseResponse instanceof TransResponse;
            if (isTransResponse) {
                final TransResponse transResponse = (TransResponse) baseResponse;
                // Log.e("response", "//" + transResponse.toString());
                if (transData.getStatus() == TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue()) {
                    if (transResponse.getRspCode() == 0 || transResponse.getRspCode() == -15
                            || transResponse.getRspCode() == -16 || transResponse.getRspCode() == -17 || transResponse.getRspCode() == -18) {
                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        // DBHelper.getInstance(cntxt).updateTransData(transData);
                    }
                    //DBHelper.getInstance(cntxt).deleteTransData(transData);
                    BillPaymentActivity.this.finish();
                } else if (transData.getStatus() == TransData.STATUS.PENDING_SALE_REQ.getValue()) {
                    if (transResponse.getRspCode() == 0) {
                        cardPayment(transResponse);
                    } else {
                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        //DBHelper.getInstance(cntxt).deleteTransData(transData);
//                        Toast.makeText(cntxt, "عملية دفع غير ناجحة!", Toast.LENGTH_SHORT).show();
                        ToastUtils.showMessage(BillPaymentActivity.this, "عملية دفع غير ناجحة!");
                        // DBHelper.getInstance(cntxt).updateTransData(transData);
                        BillPaymentActivity.this.finish();
                    }
                }
            } else {
                if (transData.getStatus() == TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue()) {
                    if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15
                            || baseResponse.getRspCode() == -16 || baseResponse.getRspCode() == -17 || baseResponse.getRspCode() == -18) {
                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        //DBHelper.getInstance(cntxt).deleteTransData(transData);
                        //DBHelper.getInstance(cntxt).updateTransData(transData);
                    }
                    BillPaymentActivity.this.finish();
                } else if (transData.getStatus() == TransData.STATUS.PENDING_SALE_REQ.getValue()) {
                    //DBHelper.getInstance(cntxt).deleteTransData(transData);
//                    Toast.makeText(cntxt, "عملية دفع غير ناجحة!", Toast.LENGTH_SHORT).show();
                    ToastUtils.showMessage(BillPaymentActivity.this, "عملية دفع غير ناجحة!");
                    BillPaymentActivity.this.finish();
                } else if (transData.getStatus() == TransData.STATUS.PENDING_QR_SALE_REQ.getValue()) {
                    if (baseResponse.getRspCode() == 0) {

                    } else {
                        //DBHelper.getInstance(cntxt).deleteTransData(transData);
//                        Toast.makeText(cntxt, "عملية دفع غير ناجحة!", Toast.LENGTH_SHORT).show();
                        ToastUtils.showMessage(BillPaymentActivity.this, "عملية دفع غير ناجحة!");
                        BillPaymentActivity.this.finish();
                    }
                }
                Log.e("onActivityResult", "BaseResponse");
            }
        }

    }

    private void cardPayment(final TransResponse transResponse) {
        final String BankTransactionID = transResponse.getTerminalId() +
                transResponse.getMerchantId() + transResponse.getAuthCode() + transResponse.getCardNo().substring(transResponse.getCardNo().length() - 4);
        transData.setBankTransactionID(BankTransactionID);
        transData.setStan(String.valueOf(transResponse.getVoucherNo()));
        transData.setStatus(TransData.STATUS.PENDING_CARD_PAYMENT_REQ.getValue());
        //transData.setCommission(String.valueOf(1 + Float.parseFloat(transData.getBillValue()) * MiniaElectricity.getPrefsManager().getPercentFees()));
        transData.setTransDateTime(new SimpleDateFormat(Utils.DATE_PATTERN, Locale.US)
                .format(new Date(System.currentTimeMillis())));
        //DBHelper.getInstance(cntxt).updateTransData(transData);
        JsonArray ModelBillPaymentV = new JsonArray();
        for (TransBill b :
                transBills) {
            JsonObject j = new JsonObject();
            j.addProperty("RowNum", b.getRawNum());
            j.addProperty("BillDate", b.getBillDate());
            j.addProperty("BillValue", b.getBillValue());
            j.addProperty("CommissionValue", b.getCommissionValue());
            j.addProperty("CommissionValueCreditCard", Utils.decimalFormat((b.getBillValue()) * MiniaElectricity.getPrefsManager().getPercentFees()));

            ModelBillPaymentV.add(j);
        }
        new ApiServices(cntxt, false).billPayment(transData.getInquiryID(), transData.getPaymentType(), transData.getClientMobileNo(),
                transData.getClientID(), ModelBillPaymentV, transData.getTransDateTime(), transData.getStan(),
                transData.getBankTransactionID(), transResponse.getCardNo(), transResponse.getAuthCode(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            //Log.e("billPayment", response);
                            if (Error != null && !Error.isEmpty()) {
                                if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                                    MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                    startActivity(new Intent(BillPaymentActivity.this, LoginActivity.class));
                                    BillPaymentActivity.this.finish();
                                } else onFailure("فشل في عملية الدفع!\n" + Error);

                            } else {
                                transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                                //DBHelper.getInstance(cntxt).updateTransData(transData);
                                long amount = 0;
                                for (TransBill b :
                                        transBills) {
                                    amount += (b.getBillValue()) * 100
                                            + (b.getBillValue()) * 100 * MiniaElectricity.getPrefsManager().getPercentFees()
                                            + (b.getCommissionValue()) * 100;
                                }
                                MiniaElectricity.getPrefsManager().setPaidOnlineBillsCount(MiniaElectricity.getPrefsManager().getPaidOnlineBillsCount() + transBills.size());
                                MiniaElectricity.getPrefsManager().setPaidOnlineBillsValue(MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() + amount);
                                DBHelper.getInstance(cntxt).addReport(new Report(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), amount, transBills.size(), transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID()));
                                deleteBills();
                                new PrintReceipt(cntxt, transBills, new PrintListener() {
                                    @Override
                                    public void onFinish() {
                                        /*transData.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                        DBHelper.getInstance(cntxt).updateTransData(transData);
                                        sendDRM(transResponse);*/
                                        for (TransBill b :
                                                transBills) {
                                            DBHelper.getInstance(cntxt).deleteTransBill(b.getBillUnique());
                                        }
                                        DBHelper.getInstance(cntxt).deleteTransData(transData);
                                        BillPaymentActivity.this.finish();
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
//                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        ToastUtils.showMessage(BillPaymentActivity.this, failureMsg);
                        cancelPaymentRequest();

                    }
                });
    }

    private void cancelPaymentRequest() {
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
                            sendCashDRM(true);
                            // DBHelper.getInstance(cntxt).deleteBillData(billData);
                        } else {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            aVoidReq();
                            //DBHelper.getInstance(cntxt).deleteBillData(billData);
                            // send void request to QNB payment App
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
//                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        ToastUtils.showMessage(BillPaymentActivity.this, failureMsg);
//cancelPaymentRequest(BankTransactionID);
                    }
                });
    }

    private void sendDRM(TransResponse response) {
        int TxnTypeID = -1, EntryMode = -1, CurrencyID;
        JsonObject EMVData, SendContent = new JsonObject();
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
            EMVData = new JsonObject();

            EMVData.addProperty("AID", bundle.getString("response_aid"));
            EMVData.addProperty("ApplicationLabel", bundle.getString("response_app_name"));
            EMVData.addProperty("CVMRes", bundle.getString("response_pin_entry_mode"));
            EMVData.addProperty("TSI", bundle.getString("response_tsi"));
            EMVData.addProperty("TVR", bundle.getString("response_tvr"));
            SendContent.add("EMVdata", EMVData);

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

        SendContent.addProperty("login", "PAX_POS");
        SendContent.addProperty("password", "PAX_pos3");
        SendContent.addProperty("access_key", "PAX_ACCESS");
        SendContent.addProperty("OrganizationId", 4);
        SendContent.addProperty("TID", response.getTerminalId());
        SendContent.addProperty("MID", response.getMerchantId());
        SendContent.addProperty("Header1", response.getMerchantName());
        SendContent.addProperty("Header2", bundle.getString("response_merchant_address"));
        SendContent.addProperty("Header3", "");
        SendContent.addProperty("BATCH", response.getBatchNo());
        SendContent.addProperty("STAN", response.getVoucherNo());
        SendContent.addProperty("Amount", response.getAmount());
        SendContent.addProperty("CurrencyId", CurrencyID);
        SendContent.addProperty("CardName", bundle.getString("response_app_name"));
        SendContent.addProperty("PAN", response.getCardNo());
        SendContent.addProperty("ExpDate", "****"); // masked in transdata????
        SendContent.addProperty("CardHolderName", bundle.getString("response_card_holder_name"));
        SendContent.addProperty("TransactionTypeId", TxnTypeID);
        SendContent.addProperty("IsVoided", false);
        SendContent.addProperty("TransactionStatus", response.getRspCode() == 0);
        SendContent.addProperty("ResponseCode", response.getRspCode());
        SendContent.addProperty("AuthId", response.getAuthCode());
        SendContent.addProperty("RRN", response.getRefNo());
        SendContent.addProperty("EntryModeId", EntryMode);
        SendContent.addProperty("PinEntry", bundle.getString("response_pin_entry_mode"));
        SendContent.addProperty("OnlineProcessing", "Online");
        String temp = response.getTransTime();
        if (temp != null && !temp.isEmpty()) {
            String date = temp.substring(4, 8);
            String time = temp.substring(8);
            SendContent.addProperty("TrxDate", date);
            SendContent.addProperty("TrxTime", time);
        }
        SendContent.addProperty("DCC_TRX", false);
        SendContent.addProperty("ResponseMessage1", response.getRspMsg());
        SendContent.addProperty("ResponseMessage2", "");
        SendContent.addProperty("CardHolderPhone", transData.getClientMobileNo()); // ????????
        SendContent.addProperty("Signature", "");
        Log.i("SendContent", SendContent.toString());
        //transData.setDrmData(SendContent);
        DBHelper.getInstance(cntxt).updateTransData(transData);
        new ApiServices(cntxt, true).sendDRM(SendContent, new RequestListener() {
            @Override
            public void onSuccess(String response) {
                Log.i("onSuccess", response);
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                    if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        DBHelper.getInstance(cntxt).deleteTransData(transData);
                    }
                    BillPaymentActivity.this.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    onFailure(e.getMessage() + "");
                }
            }

            @Override
            public void onFailure(String failureMsg) {
                Log.i("failureMsg", failureMsg);
                finish();
            }
        });
    }

    private void sendCashDRM(boolean isVoided) {
        JsonObject SendContent = new JsonObject(), EMVData;
        EMVData = new JsonObject();
        try {
            long amount = 0;
            for (TransBill b :
                    transBills) {
                amount += (b.getBillValue()) * 100
                        + (b.getCommissionValue()) * 100;
            }
            EMVData.addProperty("AID", String.valueOf(amount));
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
            SendContent.addProperty("Header2", /*"           MAIN ADDRESS"*/MiniaElectricity.getPrefsManager().getCollectorCode());
            //SendContent.addProperty("Header3", "MerchantAddress2");
            SendContent.addProperty("BATCH", "000001");
            SendContent.addProperty("STAN", transData.getStan());
            amount = 0;
            for (TransBill b :
                    transBills) {
                amount +=/* (b.getBillValue()) * 100
                    +*/ (b.getCommissionValue()) * 100;
            }
            SendContent.addProperty("Amount", String.valueOf(amount));
            SendContent.addProperty("CurrencyId", 1); // for EGP
            SendContent.addProperty("CardName", "cash");
            String tempString = transData.getClientMobileNo();
            String masked = "";
            if (tempString != null && tempString.length() > 4) {
                masked = tempString.substring(tempString.length() - 4);
                for (int i = 0; i < tempString.length() - 4; i++) {
                    masked = "*".concat(masked);
                }
            } else masked = tempString;
            SendContent.addProperty("PAN", masked);
            SendContent.addProperty("ExpDate", "/"); // masked in transdata????
            SendContent.addProperty("CardHolderName", transBills.get(0).getClientName());
            SendContent.addProperty("TransactionTypeId", 10);
            SendContent.addProperty("IsVoided", isVoided);
            SendContent.addProperty("TransactionStatus", true);
            SendContent.addProperty("ResponseCode", "00");
            SendContent.addProperty("AuthId", Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN));//time format HHmmss
            SendContent.addProperty("RRN", transData.getClientID());
            SendContent.addProperty("EntryModeId", 3);
            SendContent.addProperty("PinEntry", "Offline PIN Entered");
            SendContent.addProperty("OnlineProcessing", "Online");

            SendContent.addProperty("TrxDate", Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2)/* transData.getTransDate()*/);
            SendContent.addProperty("TrxTime", Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2) /*transData.getTransTime()*/);

            SendContent.addProperty("DCC_TRX", false);
            SendContent.addProperty("ResponseMessage1", "TXN. ACCEPTED 00");
            SendContent.addProperty("ResponseMessage2", "");
            SendContent.addProperty("CardHolderPhone", transData.getClientMobileNo()); // ????????
            SendContent.addProperty("Signature", "");
            //Log.i("SendContent", SendContent.toString());
            transData.setDrmData(SendContent.toString());
            DBHelper.getInstance(cntxt).updateTransData(transData);
            if (!offline) {
                new ApiServices(cntxt, true).sendDRM((JsonObject) new JsonParser().parse(transData.getDrmData()), new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        // Log.i("onSuccess", response);
                        JSONObject responseBody = null;
                        try {
                            responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                            if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                                transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                                for (TransBill b :
                                        transBills) {
                                    DBHelper.getInstance(cntxt).deleteTransBill(b.getBillUnique());
                                }
                                DBHelper.getInstance(cntxt).deleteTransData(transData);
                            }
                            //added for test should not be added here
                            //transData.setStatus(TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());
                            BillPaymentActivity.this.finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage() + "");
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Log.i("failureMsg", failureMsg);
                        finish();
                    }
                });
            }
        } catch (Exception e) {
            transData.setDrmData(SendContent.toString());
            DBHelper.getInstance(cntxt).updateTransData(transData);
            e.printStackTrace();
        }
    }


    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
    }

    @Override
    public void onBackPressed() {
        if (allowCancel) {
            transData.setStatus(TransData.STATUS.CANCELLED.getValue());
            //DBHelper.getInstance(cntxt).deleteTransData(transData);
            super.onBackPressed();
        }

    }
}
