package com.ebe.miniaelec.ui.billpayment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

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
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.ClientWithBillData;
import com.ebe.miniaelec.data.database.entities.DeductType;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.ReportEntity;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.data.print.PrintListener;
import com.ebe.miniaelec.data.print.PrintReceipt;
import com.ebe.miniaelec.ui.login.LoginActivity;
import com.ebe.miniaelec.utils.CustomDialog;
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
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener{


    public ITransAPI transAPI;
    JsonObject SendContent , EMVData;
    AppDataBase dataBase;
    Context cntxt;
    TextView tv_clientID, tv_address, tv_currentMeter, tv_billValue, selected_bills_value;
    String phoneNumber;
    Button b_pay, b_deduct;
    LinearLayout ll_paymentMethods, ll_phone_number;
    EditText et_clientMobileNo;
    String trxnTime;
    LinearLayout ll_bills;
    Spinner paymentTypes;
    double selectedBillsValue;
    double percentageFees;
    SpotsDialog progressDialog;
    CompositeDisposable compositeDisposable;
    private ArrayList<DeductType> deductTypes = new ArrayList<>();
    //float commission = 0;
    private TransDataEntity transData;
    @Nullable
    private int transDataId = -1;
    private CheckBox[] cb_bills;
    private ArrayList<BillDataEntity> billDetails;
    private boolean offline, allowCancel = true;
    private ArrayList<TransBillEntity> transBills;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);


       // Objects.requireNonNull(getSupportActionBar()).setTitle("دفع فواتير");

        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        compositeDisposable = new CompositeDisposable();
        Toolbar toolbar = findViewById(R.id.toolbar);
        // title = findViewById(R.id.title);
        toolbar.setTitle("دفع فواتير");
        setSupportActionBar(toolbar);


        cntxt = this;
        dataBase = AppDataBase.getInstance(cntxt);
        dataBase.transDataDao().getAllTrans().subscribeOn(Schedulers.io()).subscribe(new Consumer<List<TransDataWithTransBill>>() {
            @Override
            public void accept(List<TransDataWithTransBill> transDataWithTransBills) throws Throwable {
                List<TransDataWithTransBill> lst =transDataWithTransBills;
                Log.e("trans", "accept: "+ transDataWithTransBills.size());
            }
        });
        progressDialog = new SpotsDialog(cntxt, R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        setStatusBarColor();
        //tv_billDate = view.findViewById(R.id.bill_date);
        // tv_billValue = view.findViewById(R.id.bill_value);
        tv_clientID = findViewById(R.id.client_id);
        //tv_clientName = view.findViewById(R.id.client_name);
        tv_address = findViewById(R.id.address);
        tv_clientID = findViewById(R.id.client_id);
        tv_currentMeter = findViewById(R.id.current_meter);
        findViewById(R.id.cash_payment).setOnClickListener(this);
        findViewById(R.id.card_payment).setOnClickListener(this);
        findViewById(R.id.wallet_payment).setOnClickListener(this);
        b_pay = findViewById(R.id.pay);
        b_pay.setOnClickListener(this);
        ll_paymentMethods = findViewById(R.id.payment_methods);
        // ll_phone_number = view.findViewById(R.id.ll_phone_number);
        b_deduct = findViewById(R.id.deduct);
        b_deduct.setOnClickListener(this);
        ll_bills = findViewById(R.id.ll_bills);
        paymentTypes = findViewById(R.id.payment_types);
        selected_bills_value = findViewById(R.id.selected_bills_value);
        //deductTypes = new ArrayList<>(DBHelper.getInstance(cntxt).getDeductTypes());
        getDeductTypes();

        setBillData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        transData.setStatus(TransDataEntity.STATUS.CANCELLED.getValue());
    }

    private void getDeductTypes()
    {
        compositeDisposable.add(dataBase.deductsDao().getDeductTypes().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deductTypes -> {
                            this.deductTypes.addAll(deductTypes);
                        }


                ));
    }
    private void setBillData() {
        //Bundle bundle = getIntent().getBundleExtra("params");
        long RECEIPT_NO = MiniaElectricity.getPrefsManager().getReceiptNo();
        MiniaElectricity.getPrefsManager().setReceiptNo(RECEIPT_NO + 1);
        Bundle bundle = getIntent().getBundleExtra("data");
        assert bundle != null;
        offline = bundle.getBoolean("offline");
        String clientId = bundle.getString("clientID");
        // String inquiryId = "";
        if (offline) {
            String inquiryId = MiniaElectricity.getPrefsManager().getInquiryID();

            compositeDisposable.add(dataBase.offlineClientsDao().getClientByClientId(clientId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<ClientWithBillData>() {
                        @Override
                        public void accept(ClientWithBillData clientWithBillData) throws Throwable {
                            OfflineClientEntity offlineClient = clientWithBillData.getClient();
                            if (offlineClient == null) {
                                CustomDialog.showMessage((Activity) cntxt , "رقم الاشتراك غير صحيح!");
                               finish();

                            }
                            assert offlineClient != null;
                            phoneNumber = offlineClient.getClientMobileNo();
                            billDetails = new ArrayList<>(clientWithBillData.getBills());
                            prepareBillsToShow(RECEIPT_NO,clientId,inquiryId);
                        }
                    },throwable -> {
                        Log.e(null, "setBillData: "+throwable.getLocalizedMessage() );
                    })
            );


        } else {
            String response = bundle.getString("response");
            // OfflineClient client = (OfflineClient) bundle.getSerializable("response");
            try {
                JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                String inquiryId = responseBody.optString("InquiryID");
                phoneNumber = responseBody.optString("ClientMobileNo");

                JSONArray billsData = responseBody.optJSONArray("ModelBillInquiryV");
                billDetails = new ArrayList<>();
                for (int i = 0; i < billsData.length(); i++) {
                    BillDataEntity bill = new Gson().fromJson(billsData.getJSONObject(i).toString(), BillDataEntity.class);
                    billDetails.add(bill);
                }

                prepareBillsToShow(RECEIPT_NO,clientId,inquiryId);

            } catch (JSONException e) {
                e.printStackTrace();

            }
        }

    }

    void prepareBillsToShow(long RECEIPT_NO,String clientId,String inquiryId  )
    {
        transData = new TransDataEntity((int) RECEIPT_NO, clientId,
                inquiryId, TransDataEntity.STATUS.INITIATED.getValue());
        if (phoneNumber != null && !phoneNumber.isEmpty() && !phoneNumber.equalsIgnoreCase("null")) {
            //ll_phone_number.setVisibility(View.GONE);
        } else phoneNumber = "01064030305";;

        tv_clientID.setText(transData.getClientID());
        cb_bills = new CheckBox[billDetails.size()];
        tv_currentMeter.setText(billDetails.get(0).getPreviousRead());
        tv_address.setText(billDetails.get(billDetails.size() - 1).getClientAddress());

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
        for (TransBillEntity b :
                transBills) {
            total += b.getBillValue();
            if (transData.getPaymentType() == TransDataEntity.PaymentType.CASH.getValue()) {
                commission += (b.getCommissionValue());
            } else if (transData.getPaymentType() == TransDataEntity.PaymentType.CARD.getValue() ||
                    transData.getPaymentType() == TransDataEntity.PaymentType.WALLET.getValue()) {
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
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        b_pay.setEnabled(false);
                        allowCancel = false;
                        if (transData.getPaymentType() == TransDataEntity.PaymentType.CASH.getValue()) {
                            requestCashPayment();
                        } else if (transData.getPaymentType() == TransDataEntity.PaymentType.CARD.getValue()) {
                            requestCardPayment();
                        } else if (transData.getPaymentType() == TransDataEntity.PaymentType.WALLET.getValue()) {
                            requestQRPayment();
                        }
                    }
                });

        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void requestCashPayment() {
        transData.setStan(String.valueOf(transData.getReferenceNo()));
        transData.setTransDateTime(new SimpleDateFormat(Utils.DATE_PATTERN, Locale.US)//"yyyy-MM-dd HH:mm"
                .format(new Date(System.currentTimeMillis()))); /*  transData.setTransDate(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                .format(new Date(System.currentTimeMillis())));*/
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss", Locale.US);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.US);
        Date current = new Date(System.currentTimeMillis());
        StringBuilder bankTransactionID = new StringBuilder(transData.getClientID());

        bankTransactionID.append(Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_TIME_PATTERN));
        long amount = 0;
        for (TransBillEntity b :
                transBills) {
            amount += (b.getBillValue()) * 100
                    + (b.getCommissionValue()) * 100;
        }
        final long finalAmount = amount;
        bankTransactionID.append(amount);
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

                transData.setPaymentType(TransDataEntity.PaymentType.OFFLINE_CASH.getValue());
                transData.setStatus(TransDataEntity.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue());

                sendCashDRM(false);
                transDataId = Math.toIntExact(dataBase.transDataDao().addTransData(transData));

                if (transDataId < 0) {
                    CustomDialog.showMessage((Activity) cntxt, "برجاء اعادة المحاولة!");
                    finish();
                } else {
                    transData.setId(transDataId);

                    MiniaElectricity.getPrefsManager().setOfflineBillCount(MiniaElectricity.getPrefsManager().getOfflineBillCount() + billsCount);
                    MiniaElectricity.getPrefsManager().setOfflineBillValue(MiniaElectricity.getPrefsManager().getOfflineBillValue() + finalAmount);
                    MiniaElectricity.getPrefsManager().setPaidOfflineBillsCount(MiniaElectricity.getPrefsManager().getPaidOfflineBillsCount() + billsCount);
                    MiniaElectricity.getPrefsManager().setPaidOfflineBillsValue(MiniaElectricity.getPrefsManager().getPaidOfflineBillsValue() + finalAmount);
                    Completable.fromRunnable(new Runnable() {
                                @Override
                                public void run() {
                                    ReportEntity report = new ReportEntity(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), finalAmount, billsCount, transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID());
                                    dataBase.reportEntityDaoDao().addReport(report);

                                    deleteBills();
                                    Utils.copyToFile(transData,transBills);
                                    Log.e("addReport", "run: "+report );
                                }
                            }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableCompletableObserver() {
                                           @Override
                                           public void onComplete() {
                                               new PrintReceipt(cntxt, transBills,transData, new PrintListener() {
                                                   @Override
                                                   public void onFinish() {
                                                       //sendCashDRM(false);
                                                       transData.setPrintCount(1);

                                                       dataBase.transDataDao().updateTransData(transData);
                                                       //navController.popBackStack();
                                                       finish();
                                                   }

                                                   @Override
                                                   public void onCancel() {
                                                   }
                                               });
                                           }

                                           @Override
                                           public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                               Log.e(null, "onError: "+ e.getLocalizedMessage() );
                                           }
                                       }

                            );

                }

            }else {
                CustomDialog.showMessage((Activity) cntxt, "لقد تجاوزت الحد الأقصى لعمليات الدفع دون مزامنة. برجاء مزامنة عمليات الدفع.");
                // DBHelper.getInstance(cntxt).deleteTransData(transData);
                finish();
            }

        } else {
            //DBHelper.getInstance(cntxt).updateTransData(transData);
            transDataId = Math.toIntExact(dataBase.transDataDao().addTransData(transData));
            if (transDataId < 0) {
                Toast.makeText(cntxt, "برجاء اعادة المحاولة!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                transData.setId(transDataId);
                JsonArray ModelBillPaymentV = new JsonArray();
                for (TransBillEntity b :
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
                                            compositeDisposable.add(Completable.fromAction(() -> {
                                                        for (TransBillEntity b :
                                                                transBills) {
                                                            dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                        }
                                                        dataBase.transDataDao().deleteTransData(transData);
                                                    }).subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeWith(new DisposableCompletableObserver() {
                                                        @Override
                                                        public void onComplete() {
                                                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                                            startActivity(new Intent(cntxt, LoginActivity.class));
                                                            finish();
                                                        }

                                                        @Override
                                                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                                            Log.e("requestCashPayment", "onError: "+e.getLocalizedMessage() );
                                                        }
                                                    }));

                                        } else onFailure("فشل في عملية الدفع!\n" + Error);
                                    } else {
                                        transData.setStatus(TransDataEntity.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                        dataBase.transDataDao().updateTransData(transData);

                                        MiniaElectricity.getPrefsManager().setPaidOnlineBillsCount(MiniaElectricity.getPrefsManager().getPaidOnlineBillsCount() + billsCount);
                                        MiniaElectricity.getPrefsManager().setPaidOnlineBillsValue(MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() + finalAmount);
                                        Completable.fromRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dataBase.reportEntityDaoDao().addReport(new ReportEntity(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), finalAmount, billsCount, transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID()));
                                                        deleteBills();

                                                    }
                                                }).subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new DisposableCompletableObserver() {
                                                    @Override
                                                    public void onComplete() {
                                                        new PrintReceipt(cntxt, transBills,transData, new PrintListener() {
                                                            @Override
                                                            public void onFinish() {
                                                                sendCashDRM(false);
                                                            }

                                                            @Override
                                                            public void onCancel() {

                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                                        Log.e(null, "onError: "+e.getLocalizedMessage() );
                                                    }
                                                });

                                        JsonObject SendContent = new JsonObject(), EMVData;



                                        Log.d("TransBill", "onSuccess: "+ transBills.get(0));

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
                                CustomDialog.showMessage((Activity) cntxt, failureMsg);
                                cancelPaymentRequest();
                            }
                        });
            }
        }

    }

    private boolean deleteBills() {

        for (TransBillEntity b :
                transBills) {
            dataBase.billDataDaoDao().deleteClientBill(b.getBillUnique());
            long unique = b.getBillUnique();
            b.setBankTransactionID(transData.getBankTransactionID());
            b.setTransDataId(transData.getClientID());
            dataBase.transBillDao().newTransBillAppend(b);
        }
        ClientWithBillData clientWithBillData = dataBase.offlineClientsDao().getClientByClientIdForAdapter(transData.getClientID());
        OfflineClientEntity client = clientWithBillData.getClient();
        if (client != null && (clientWithBillData.getBills() == null || clientWithBillData.getBills().size() == 0)) {
            dataBase.offlineClientsDao().deleteOfflineClient(client);
        }


//          compositeDisposable.add(Completable.fromRunnable(new Runnable() {
//              @Override
//              public void run() {
//                  for (TransBillEntity b :
//                          transBills) {
//                      dataBase.billDataDaoDao().deleteClientBill(b.getBillUnique());
//                      b.setBankTransactionID(transData.getBankTransactionID());
//                      b.setTransDataId(transDataId);
//                      dataBase.transBillDao().newTransBillAppend(b);
//
//                  }
//              }
//          }).subscribeOn(Schedulers.io())
//                 .subscribeWith(new DisposableCompletableObserver() {
//                      @Override
//                      public void onComplete() {
//
//                      }
//
//                      @Override
//                      public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
//
//                          Log.e("deleteBills", "onError: "+ e.getLocalizedMessage() );
//                      }
//                  }));
//
//
//
//
//
//
//        compositeDisposable.add(dataBase.offlineClientsDao().getClientByClientId(transData.getClientID())
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Consumer<ClientWithBillData>() {
//                    @Override
//                    public void accept(ClientWithBillData clientWithBillData) throws Throwable {
//                        OfflineClientEntity client = clientWithBillData.getClient();
//                        if (client != null && (clientWithBillData.getBills() == null || clientWithBillData.getBills().size() == 0)) {
//                            dataBase.offlineClientsDao().deleteOfflineClient(client);
//                        }
//                    }},throwable -> {
//                    Log.e("DeleteBills", "deleteBills: "+ throwable.getLocalizedMessage() );
//                }));

        return true;
        //   }
    }

    private void requestCardPayment() {

        SaleMsg.Request request = new SaleMsg.Request();
        long amount = 0;
        for (TransBillEntity b :
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
        for (TransBillEntity b :
                transBills) {
            amount += (b.getBillValue()) * 100
                    + (b.getBillValue()) * 100 * MiniaElectricity.getPrefsManager().getPercentFees()
                    + (b.getCommissionValue()) * 100;
        }

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
        transData.setStatus(TransDataEntity.STATUS.PENDING_CARD_PAYMENT_REQ.getValue());
        //transData.setCommission(String.valueOf(1 + Float.parseFloat(transData.getBillValue()) * MiniaElectricity.getPrefsManager().getPercentFees()));
        transData.setTransDateTime(new SimpleDateFormat(Utils.DATE_PATTERN, Locale.US)
                .format(new Date(System.currentTimeMillis())));

        JsonArray ModelBillPaymentV = new JsonArray();
        for (TransBillEntity b :
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
                            if (Error != null && !Error.isEmpty()) {
                                if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                                    MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                    startActivity(new Intent(cntxt, LoginActivity.class));
                                    finish();
                                } else onFailure("فشل في عملية الدفع!\n" + Error);

                            } else {
                                transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                                //DBHelper.getInstance(cntxt).updateTransData(transData);
                                long amount = 0;
                                for (TransBillEntity b :
                                        transBills) {
                                    amount += (b.getBillValue()) * 100
                                            + (b.getBillValue()) * 100 * MiniaElectricity.getPrefsManager().getPercentFees()
                                            + (b.getCommissionValue()) * 100;
                                }
                                MiniaElectricity.getPrefsManager().setPaidOnlineBillsCount(MiniaElectricity.getPrefsManager().getPaidOnlineBillsCount() + transBills.size());
                                MiniaElectricity.getPrefsManager().setPaidOnlineBillsValue(MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() + amount);
                                long finalAmount = amount;
                                compositeDisposable.add(Completable.fromRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                dataBase.reportEntityDaoDao().addReport(new ReportEntity(transData.getClientID(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.DATE_PATTERN2), finalAmount, transBills.size(), transData.getPaymentType(), Utils.convert(transData.getTransDateTime(), Utils.DATE_PATTERN, Utils.TIME_PATTERN2), transData.getBankTransactionID()));
                                            }
                                        }).subscribeOn(Schedulers.io())
                                        .onErrorReturn(throwable -> {
                                            Log.d("request CashPayment", "onSuccess: "+throwable.getMessage());
                                            return null;
                                        }).subscribe());
                                deleteBills();
                                new PrintReceipt(cntxt, transBills,transData, new PrintListener() {
                                    @Override
                                    public void onFinish() {

                                        Completable.fromRunnable(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        for (TransBillEntity b :
                                                                transBills) {
                                                            dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                        }
                                                        dataBase.transDataDao().deleteTransData(transData);
                                                    }
                                                }).subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(() ->  {
                                                    finish();
                                                },throwable -> {
                                                    Log.e("BillPaymentFragment", "onFinish: "+throwable.getLocalizedMessage() );
                                                });

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
                        CustomDialog.showMessage((Activity) cntxt, failureMsg);
                        cancelPaymentRequest();

                    }
                });
    }

    private void cancelPaymentRequest() {
        transData.setStatus(TransDataEntity.STATUS.PENDING_DELETE_REQ.getValue());
        dataBase.transDataDao().updateTransData(transData);
        new ApiServices(cntxt, false).cancelBillPayment(transData.getBankTransactionID(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {

                        // whatever the response of delete req suppose it is succeeded
                        if (transData.getPaymentType() == TransDataEntity.PaymentType.CASH.getValue()) {
                            transData.setStatus(TransDataEntity.STATUS.DELETED_PENDING_DRM_REQ.getValue());
                            dataBase.transDataDao().updateTransData(transData);
                            sendCashDRM(true);

                        } else {
                            transData.setStatus(TransDataEntity.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            aVoidReq();

                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        CustomDialog.showMessage((Activity) cntxt, failureMsg);
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
                new ApiServices(cntxt, true).sendDRM(true,(JsonObject) new JsonParser().parse(transData.getDrmData()), new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        // Log.i("onSuccess", response);
                        JSONObject responseBody = null;
                        try {
                            responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                            if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                                transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                                compositeDisposable.add(Completable.fromRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (TransBillEntity b :
                                                        transBills) {
                                                    dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                }
                                                dataBase.transDataDao().deleteTransData(transData);
                                            }
                                        }).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(()->{
                                            finish();
                                        },throwable -> {
                                            Log.e("sendCashDrm", "onSuccess: "+throwable.getLocalizedMessage() );
                                        }));
                            }
                            //added for test should not be added here
                            //transData.setStatus(TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());

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
            //update
            dataBase.transDataDao().updateTransData(transData);
            e.printStackTrace();
        }
    }


    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(cntxt, R.color.colorPrimaryDark));
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
        transBills = new ArrayList<>();
        for (int i = 0; i < cb_bills.length; i++) {
            if (cb_bills[i].isChecked()) {
                TransBillEntity transBillEntity = new TransBillEntity(billDetails.get(i));
                transBillEntity.setTransDataId(transData.getClientID());
                transBills.add(transBillEntity);

            } else break;
        }
        if (transBills.size() == 0) {
            CustomDialog.showMessage((Activity) cntxt, "يرجى اختيار الفواتير المطلوب سدادها!");
            return;
        }

        transData.setClientMobileNo(phoneNumber);
        if (id == R.id.pay)
        {
            if (paymentTypes.getSelectedItemPosition() == 1 && !offline) { // 1 card payment
                transData.setPaymentType(TransDataEntity.PaymentType.CARD.getValue());
                transData.setStatus(TransDataEntity.STATUS.PENDING_SALE_REQ.getValue());
                // requestCardPayment();
                confirmPayment();
            } else if (paymentTypes.getSelectedItemPosition() == 0) { // 0 cash payment
                transData.setPaymentType(TransDataEntity.PaymentType.CASH.getValue());
                transData.setStatus(TransDataEntity.STATUS.PENDING_CASH_PAYMENT_REQ.getValue());
                //requestCashPayment();
                confirmPayment();
            } else if (paymentTypes.getSelectedItemPosition() == 2 && !offline) { // 2 wallet payment
//                    transData.setPaymentType(TransData.PaymentType.WALLET.getValue());
//                    transData.setStatus(TransData.STATUS.PENDING_QR_SALE_REQ.getValue());
                //requestCashPayment();
//                    confirmPayment();
                CustomDialog.showMessage((Activity) cntxt, "هذه الخدمة ستكون متوفرة قريباً");
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
                            if (Error.isEmpty()) {
                                JSONArray deducts = new JSONArray(response);
                                if (deducts.length() > 0) {
                                    dataBase.deductsDao().clearDeducts();
                                }
                                for (int i = 0; i < deducts.length(); i++) {
                                    DeductType deductType = new Gson().fromJson(deducts.get(i).toString(), DeductType.class);
                                    dataBase.deductsDao().addDeductType(deductType);
                                    deductTypes.add(deductType);
                                }
                                selectDeductType();
                            } else
                                CustomDialog.showMessage((Activity) cntxt, getString(R.string.err_no_deducts));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            CustomDialog.showMessage((Activity) cntxt, getString(R.string.err_no_deducts));
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        CustomDialog.showMessage((Activity) cntxt, getString(R.string.err_no_deducts));
                    }
                });
            } else
                CustomDialog.showMessage((Activity) cntxt, getString(R.string.err_no_deducts));
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
            if (transData.getStatus() == TransDataEntity.STATUS.DELETED_PENDING_VOID_REQ.getValue()) {
                if (transResponse.getRspCode() == 0 || transResponse.getRspCode() == -15
                        || transResponse.getRspCode() == -16 || transResponse.getRspCode() == -17 || transResponse.getRspCode() == -18) {
                    transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());

                }

                finish();
            } else if (transData.getStatus() == TransDataEntity.STATUS.PENDING_SALE_REQ.getValue()) {
                if (transResponse.getRspCode() == 0) {
                    cardPayment(transResponse);
                } else {
                    transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                    //DBHelper.getInstance(cntxt).deleteTransData(transData);


                        CustomDialog.showMessage((Activity) cntxt, "عملية دفع غير ناجحة!");


                    // DBHelper.getInstance(cntxt).updateTransData(transData);
                    finish();
                }
            }
        } else {
            if (transData.getStatus() == TransDataEntity.STATUS.DELETED_PENDING_VOID_REQ.getValue()) {
                if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15
                        || baseResponse.getRspCode() == -16 || baseResponse.getRspCode() == -17 || baseResponse.getRspCode() == -18) {
                    transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());

                }
                finish();
            } else if (transData.getStatus() == TransDataEntity.STATUS.PENDING_SALE_REQ.getValue()) {


                    CustomDialog.showMessage((Activity) cntxt, "عملية دفع غير ناجحة!");


                finish();
            } else if (transData.getStatus() == TransDataEntity.STATUS.PENDING_QR_SALE_REQ.getValue()) {
                if (baseResponse.getRspCode() == 0) {

                } else {

                        CustomDialog.showMessage((Activity) cntxt, "عملية دفع غير ناجحة!");

                    finish();
                }
            }

        }
    }

    void  setDrm(JsonObject EMVData,JsonObject SendContent,boolean isVoided)
    {
        long amount = 0;
        for (TransBillEntity b :
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
       // MiniaElectricity.getPrefsManager().getTerminalId()
        SendContent.addProperty("TID", //999999999
                MiniaElectricity.getPrefsManager().getTerminalId());
        SendContent.addProperty("MID", MiniaElectricity.getPrefsManager().getMerchantId());
        SendContent.addProperty("Header1", "test ECR");
        SendContent.addProperty("Header2", /*"           MAIN ADDRESS"*/MiniaElectricity.getPrefsManager().getCollectorCode());
        //SendContent.addProperty("Header3", "MerchantAddress2");
        SendContent.addProperty("BATCH", "000001");
        SendContent.addProperty("STAN", transData.getStan());
        amount = 0;
        for (TransBillEntity b :
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
        transData.setDrmData(SendContent.toString());
        dataBase.transDataDao().updateTransData(transData);

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
                (cntxt, android.R.layout.simple_spinner_item,
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
                (dialog, which) -> {
                    //CustomDialog.showMessage((Activity) cntxt, deductTypes.get(sp_deducts.getSelectedItemPosition()).getDeductType());
                    transData.setStatus(TransDataEntity.STATUS.PENDING_DEDUCT_REQ.getValue());
                    transData.setDeductType(deductTypes.get(sp_deducts.getSelectedItemPosition()).getDeductId());

                    try {
                        dataBase.transDataDao().updateTransData(transData);
                        if (offline) {
                            deleteBills();
                            finish();
                        } else {
                            sendDeductReq();
                        }
                    }catch (Exception e)
                    {
                        CustomDialog.showMessage((Activity) cntxt, "برجاء اعادة المحاولة!");
                        finish();
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


//        compositeDisposable.add(dataBase.transDataDao().getTransByClientId(transData.getClientID())
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(transDataWithTransBill -> {
//
//                }));


        for (TransBillEntity b :
                transBills) {
            JsonObject j = new JsonObject();
            j.addProperty("BillUnique", b.getBillUnique());
            j.addProperty("KTID", transData.getDeductType());

            ModelBillKasmV.add(j);
        }

        new ApiServices(cntxt).sendDeducts(ModelBillKasmV,true, new RequestListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String Error = responseBody.optString("Error").trim();
                    //Log.e("requestCashPayment", response);
                    if (Error != null && !Error.isEmpty()) {
                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                            for (TransBillEntity b :
                                    transBills) {
                                dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                            }
                            dataBase.transDataDao().deleteTransData(transData);
                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                            startActivity(new Intent(cntxt, LoginActivity.class));
                            finish();
                        } else onFailure("فشل في تسجيل خصم الفواتير!\n" + Error);
                    } else {
                        transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                        dataBase.transDataDao().updateTransData(transData);
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
                CustomDialog.showMessage((Activity) cntxt, failureMsg);
                // navController.navigateUp();
            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.dispose();
        CustomDialog.dismissCustomDialog();
    }
}