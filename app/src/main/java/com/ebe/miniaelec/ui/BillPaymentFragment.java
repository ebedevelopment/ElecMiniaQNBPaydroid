package com.ebe.miniaelec.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.model.Report;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.print.PrintListener;
import com.ebe.miniaelec.print.PrintReceipt;
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
import java.util.Objects;


public class BillPaymentFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    JsonObject SendContent , EMVData;
    Context cntxt;
    TextView tv_clientID, tv_clientName, tv_billDate, tv_billValue, selected_bills_value;
    String phoneNumber;
    Button b_pay;
    LinearLayout ll_paymentMethods, ll_phone_number;
    EditText et_clientMobileNo;
    String trxnTime;
    LinearLayout ll_bills;
    Spinner paymentTypes;
    NavController navController;

    public ITransAPI transAPI;
    //float commission = 0;
    private TransData transData;
    private CheckBox[] cb_bills;
    double selectedBillsValue;
    double percentageFees;
    private ArrayList<BillData> billDetails;
    private boolean offline, allowCancel = true;
    private ArrayList<TransBill> transBills;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = Navigation.findNavController(requireActivity(),R.id.content);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (transData != null)
                {
                    transData.setStatus(TransData.STATUS.CANCELLED.getValue());
                }
                if (navController != null)
                    navController.popBackStack(R.id.mainFragment,true);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bill_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        requireActivity().getBaseContext().getResources().updateConfiguration(config,
                requireActivity().getBaseContext().getResources().getDisplayMetrics());

        navController = Navigation.findNavController(requireActivity(),R.id.content);

        cntxt = requireContext();
        setStatusBarColor();
        //fm = getFragmentManager();
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
        ll_phone_number = view.findViewById(R.id.ll_phone_number);
        et_clientMobileNo = view.findViewById(R.id.client_mobile_no);
        ll_bills = view.findViewById(R.id.ll_bills);
        paymentTypes = view.findViewById(R.id.payment_types);
        selected_bills_value = view.findViewById(R.id.selected_bills_value);

        setBillData();

    }
    private void setBillData() {
        //Bundle bundle = getIntent().getBundleExtra("params");
        long RECEIPT_NO = MiniaElectricity.getPrefsManager().getReceiptNo();
        MiniaElectricity.getPrefsManager().setReceiptNo(RECEIPT_NO + 1);
        assert getArguments() != null;
        offline = getArguments().getBoolean("offline");
        String clientId = getArguments().getString("clientID");
        String inquiryId = "";
        if (offline) {
            inquiryId = MiniaElectricity.getPrefsManager().getInquiryID();
            OfflineClient offlineClient = DBHelper.getInstance(cntxt).getClientByClientId(clientId);
            if (offlineClient == null) {
                Toast.makeText(cntxt, "رقم الاشتراك غير صحيح!", Toast.LENGTH_SHORT).show();
                navController.popBackStack();
                return;
            }
            phoneNumber = offlineClient.getClientMobileNo();
            billDetails = offlineClient.getModelBillInquiryV();
        } else {
            String response = getArguments().getString("response");
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
            ll_phone_number.setVisibility(View.GONE);
        } else ll_phone_number.setVisibility(View.VISIBLE);

        tv_clientID.setText(transData.getClientID());
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
                    Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
                    navController.popBackStack();
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
                            navController.popBackStack();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }

            }else {
                Toast.makeText(cntxt, "لقد تجاوزت الحد الأقصى لعمليات الدفع دون مزامنة. برجاء مزامنة عمليات الدفع.", Toast.LENGTH_LONG).show();
                // DBHelper.getInstance(cntxt).deleteTransData(transData);
                navController.popBackStack();
            }

        } else {
            //DBHelper.getInstance(cntxt).updateTransData(transData);
            if (!DBHelper.getInstance(cntxt).addTransData(transData)) {
                Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
                navController.popBackStack();
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
                                            startActivity(new Intent(requireActivity(), LoginActivity.class));
                                            navController.popBackStack();
                                        } else onFailure("فشل في عملية الدفع!\n" + Error);
                                    } else {
                                        transData.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                        DBHelper.getInstance(cntxt).updateTransData(transData);
                                        MiniaElectricity.getPrefsManager().setPaidOnlineBillsCount(MiniaElectricity.getPrefsManager().getPaidOnlineBillsCount() + billsCount);
                                        MiniaElectricity.getPrefsManager().setPaidOnlineBillsValue(MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() + finalAmount);
                                        DBHelper.getInstance(cntxt).addReport(new Report(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), finalAmount, billsCount, transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID()));
                                        JsonObject SendContent = new JsonObject(), EMVData;
                                        EMVData = new JsonObject();
                                        setDrm(EMVData,SendContent,false);
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
                                Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                                cancelPaymentRequest();
                            }
                        });
            }
        }

    }

    private boolean deleteBills() {

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
                                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                                    navController.popBackStack();
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

                                        for (TransBill b :
                                                transBills) {
                                            DBHelper.getInstance(cntxt).deleteTransBill(b.getBillUnique());
                                        }
                                        DBHelper.getInstance(cntxt).deleteTransData(transData);
                                        navController.popBackStack();
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
                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
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

                        } else {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            aVoidReq();

                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        //cancelPaymentRequest(BankTransactionID);
                    }
                });
    }

    private void sendCashDRM(boolean isVoided) {


        SendContent = new JsonObject();
        EMVData = new JsonObject();
        setDrm(EMVData,SendContent,isVoided);
        try {

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
                            navController.popBackStack();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage() + "");
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Log.i("failureMsg", failureMsg);
                        navController.popBackStack();
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
            Window window = requireActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pay) {
            if (ll_phone_number.getVisibility() == View.VISIBLE && (et_clientMobileNo.getText().toString().trim().isEmpty() ||
                    et_clientMobileNo.getText().toString().trim().length() < 11 ||
                    et_clientMobileNo.getText().toString().trim().length() > 16)) {
                Toast.makeText(cntxt, "برجاء كتابة رقم تليفون العميل بشكل صحيح", Toast.LENGTH_LONG).show();
            } else {
                transBills = new ArrayList<>();
                for (int i = 0; i < cb_bills.length; i++) {
                    if (cb_bills[i].isChecked()) {
                        transBills.add(new TransBill(billDetails.get(i)));
                    } else break;
                }
                if (transBills.size() == 0) {
                    Toast.makeText(cntxt, "يرجى اختيار الفواتير المطلوب سدادها!", Toast.LENGTH_LONG).show();
                    return;
                }
                //b_pay.setVisibility(View.GONE);
//                transData.setTransBills(transBills);
                if (ll_phone_number.getVisibility() == View.VISIBLE) {
                    transData.setClientMobileNo(et_clientMobileNo.getText().toString());
                } else transData.setClientMobileNo(phoneNumber);
                //DBHelper.getInstance(cntxt).updateTransData(transData);
                //ll_paymentMethods.setVisibility(View.GONE);
                //ll_phone_number.setVisibility(View.GONE);
                if (paymentTypes.getSelectedItemPosition() == 1 && !offline) { // 1 card payment
                    transData.setPaymentType(TransData.PaymentType.CARD.getValue());
                    transData.setStatus(TransData.STATUS.PENDING_SALE_REQ.getValue());
                    // requestCardPayment();
                    confirmPayment();
                } else if (paymentTypes.getSelectedItemPosition() == 0) { // 0 cash payment
                    transData.setPaymentType(TransData.PaymentType.CASH.getValue());
                    transData.setStatus(TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());
                    //requestCashPayment();
                    confirmPayment();
                } else if (paymentTypes.getSelectedItemPosition() == 2 && !offline) { // 2 wallet payment
//                    transData.setPaymentType(TransData.PaymentType.WALLET.getValue());
//                    transData.setStatus(TransData.STATUS.PENDING_QR_SALE_REQ.getValue());
                    //requestCashPayment();
//                    confirmPayment();
                    Toast.makeText(cntxt, "هذه الخدمة ستكون متوفرة قريباً", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                navController.popBackStack();
            } else if (transData.getStatus() == TransData.STATUS.PENDING_SALE_REQ.getValue()) {
                if (transResponse.getRspCode() == 0) {
                    cardPayment(transResponse);
                } else {
                    transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                    //DBHelper.getInstance(cntxt).deleteTransData(transData);
                    Toast.makeText(cntxt, "عملية دفع غير ناجحة!", Toast.LENGTH_SHORT).show();
                    // DBHelper.getInstance(cntxt).updateTransData(transData);
                    navController.popBackStack();
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
                navController.popBackStack();
            } else if (transData.getStatus() == TransData.STATUS.PENDING_SALE_REQ.getValue()) {
                //DBHelper.getInstance(cntxt).deleteTransData(transData);
                Toast.makeText(cntxt, "عملية دفع غير ناجحة!", Toast.LENGTH_SHORT).show();
                navController.popBackStack();
            } else if (transData.getStatus() == TransData.STATUS.PENDING_QR_SALE_REQ.getValue()) {
                if (baseResponse.getRspCode() == 0) {

                } else {
                    //DBHelper.getInstance(cntxt).deleteTransData(transData);
                    Toast.makeText(cntxt, "عملية دفع غير ناجحة!", Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                }
            }
            Log.e("onActivityResult", "BaseResponse");
        }
    }

   void  setDrm(JsonObject EMVData,JsonObject SendContent,boolean isVoided)
    {
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

    }
}