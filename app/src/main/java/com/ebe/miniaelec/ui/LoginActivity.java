package com.ebe.miniaelec.ui;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.ParamsMsg;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.BaseDbHelper;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.utils.ToastUtils;
import com.ebe.miniaelec.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Action;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Activity cntxt;
    private EditText et_collector_code, et_password;
    private SpotsDialog progressDialog;
    public ITransAPI transAPI;
    static int FINISH_PENDING_TRANS_START = 999;
     int billsStatus = -1;
    boolean syncState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        FinishPendingTransActivity.finishPendingState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean != null)
                    syncState = aBoolean;
            }
        });
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_login);
        findViewById(R.id.login).setOnClickListener(this);
        et_collector_code = findViewById(R.id.collector_code);
        et_password = findViewById(R.id.password);
        cntxt = this;
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
        String[] permissions = {Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};

        Utils.callPermissions(this, permissions,
                new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                }, getString(R.string.enable_required_permossions)
        );
        progressDialog = new SpotsDialog(cntxt, R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        transAPI = TransAPIFactory.createTransAPI();
        setStatusBarColor();
        if (MiniaElectricity.getPrefsManager().isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
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
    public void onClick(View v) {
        if (v.getId() == R.id.login) {
            if (et_collector_code.getText().toString().trim().isEmpty()) {
                ToastUtils.showMessage(cntxt, "أدخل اسم المستخدم!");
//                Toast.makeText(cntxt, "أدخل اسم المستخدم!", Toast.LENGTH_SHORT).show();
            } else if (et_password.getText().toString().trim().isEmpty()) {
                ToastUtils.showMessage(cntxt,"أدخل كلمة المرور!");

//                Toast.makeText(cntxt, "أدخل كلمة المرور!", Toast.LENGTH_SHORT).show();
            } else {
                if (MiniaElectricity.getPrefsManager().getCollectorCode().equals(et_collector_code.getText().toString().trim())) {
                    login();
                } else {
                    boolean allowLogin = checkPendingTrans();

                    StringBuilder warning = new StringBuilder();
                    if (allowLogin) {
                        BaseDbHelper.getInstance(this).clearOfflineData();
                        //DBHelper.getInstance(cntxt).clearOfflineData();
                        login();
                    } else {
                        warning.append("برجاء مزامنة فواتير المحصل السابق ");
                        warning.append(MiniaElectricity.getPrefsManager().getCollectorCode());
                        warning.append("لتمكين تسجيل الدخول.");
                        ToastUtils.showMessage(cntxt,warning.toString());
//                        Toast.makeText(cntxt, warning.toString(), Toast.LENGTH_LONG).show();
                    }


                }

            }
        }
    }

    private boolean checkPendingTrans() {
        ArrayList<TransData> transData = new ArrayList<>(DBHelper.getInstance(cntxt).getAllTrans());
        for (TransData b :
                transData) {
            if (b.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue() && b.getStatus() == TransData.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                return false;
            } else if (b.getStatus() != TransData.STATUS.INITIATED.getValue() && b.getStatus() != TransData.STATUS.COMPLETED.getValue()
                    && b.getStatus() != TransData.STATUS.CANCELLED.getValue()) {
                return false;
            }
        }
        return true;
    }

    private void login() {
        if (Utils.checkConnection(this))
            new ApiServices(this, false).logIn(et_collector_code.getText().toString().trim(), et_password.getText().toString().trim(),
                    new RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
//                                Log.e("response", response);
                                JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                                String UserSessionID = responseBody.optString("UserSessionID").trim();
                                String Error = responseBody.optString("Error").trim();
                                 billsStatus = responseBody.optInt("UserNewBillStatus");
//                                    Log.e("UserSessionID", UserSessionID);
//                                    Log.e("Error", Error+"//////");
                                if (!Error.isEmpty()) {
                                    onFailure("فشل في عملية تسجيل الدخول!\n" + Error);
                                } else if (!UserSessionID.isEmpty()) {
                                    progressDialog.show();
                                    MiniaElectricity.getPrefsManager().setCollectorCode(et_collector_code.getText().toString().trim());
                                    MiniaElectricity.getPrefsManager().setPassword(et_password.getText().toString().trim());
                                    MiniaElectricity.getPrefsManager().setSessionId(UserSessionID);
                                    MiniaElectricity.getPrefsManager().setOfflineBillsStatus(billsStatus);

                                    if (billsStatus == 2) {
                                        BaseDbHelper.getInstance(cntxt).clearOfflineData();
                                    }
                                /*startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();*/
/*
                                    //added to show demo with upper egypt
                                    MiniaElectricity.getPrefsManager().setLoggedStatus(true);
                                    MiniaElectricity.getPrefsManager().setTerminalId("00123456");
                                    MiniaElectricity.getPrefsManager().setMerchantId("000000123456789");
                                    MiniaElectricity.getPrefsManager().setFixedFees(1);
                                    MiniaElectricity.getPrefsManager().setPercentFees(1);
                                    Intent intent = new Intent(cntxt, MainActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("after_login", true);
                                    intent.putExtra("params", bundle);
                                    startActivity(intent);
                                    finish();*/
                                    if (checkPendingTrans()) {
                                        ParamsMsg.Request request = new ParamsMsg.Request();
                                        transAPI = TransAPIFactory.createTransAPI();
                                        //request.setCategory(SdkConstants.CATEGORY_VOID);
                                        request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
                                        transAPI.startTrans(cntxt, request);
                                    } else {
                                        startActivityForResult(new Intent(LoginActivity.this, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                                    }

                                } else onFailure("فشل في عملية تسجيل الدخول!");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                onFailure(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String failureMsg) {
                            // Toast.makeText(LoginActivity.this, failureMsg, Toast.LENGTH_LONG).show();
                            ToastUtils.showMessage(cntxt,failureMsg);
                            et_collector_code.setText("");
                            et_password.setText("");
                        }
                    });
        else ToastUtils.showMessage(cntxt, "لا يوجد اتصال بالانترنت");
//Toast.makeText(cntxt, "لا يوجد اتصال بالانترنت!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressDialog.dismiss();
        if (requestCode == FINISH_PENDING_TRANS_START) {
            if (checkPendingTrans()) {
                ParamsMsg.Request request = new ParamsMsg.Request();
                transAPI = TransAPIFactory.createTransAPI();
                //request.setCategory(SdkConstants.CATEGORY_VOID);
                request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
                transAPI.startTrans(cntxt, request);
            } else {
                ToastUtils.showMessage(cntxt, "فشل في عملية تسجيل الدخول");
                et_collector_code.setText("");
                et_password.setText("");
            }
        } else {
            BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);
            if (baseResponse == null) {
                ToastUtils.showMessage(cntxt,"فشل في تحميل البيانات برجاء المحاولة مرة اخرى");
                //Toast.makeText(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
                finish();
            }
            boolean isTransResponse = baseResponse instanceof TransResponse;
            if (isTransResponse) {
                final ParamsMsg.Response transResponse = (ParamsMsg.Response) baseResponse;
                //Log.e("response", "//" + transResponse.toString());
                MiniaElectricity.getPrefsManager().setLoggedStatus(true);
                MiniaElectricity.getPrefsManager().setTerminalId(transResponse.getTerminalId());
                MiniaElectricity.getPrefsManager().setMerchantId(transResponse.getMerchantId());
                MiniaElectricity.getPrefsManager().setFixedFees(transResponse.getFixedFees());
                MiniaElectricity.getPrefsManager().setPercentFees(transResponse.getPercentFees());
                MiniaElectricity.getPrefsManager().setOfflineStartingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                        .format(new Date(System.currentTimeMillis())));
                Intent intent = new Intent(cntxt, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("after_login", true);
                intent.putExtra("params", bundle);
                startActivity(intent);
                finish();

            } else {
                ToastUtils.showMessage(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى");
                //Toast.makeText(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
            }
        }
    }
}
