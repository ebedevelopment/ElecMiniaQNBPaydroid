package com.ebe.miniaelec.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.ParamsMsg;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.AppDataBase;
import com.ebe.miniaelec.database.BaseDbHelper;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.database.entities.TransDataEntity;
import com.ebe.miniaelec.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;

//import android.util.Log;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Activity cntxt;
    private EditText et_collector_code, et_password;
    private SpotsDialog progressDialog;
    public ITransAPI transAPI;
    private static final int APP_PERMISSIONS = 5;
    private AppDataBase dataBase;
    private CompositeDisposable disposable;
    private ApiServices services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_login);
        findViewById(R.id.login).setOnClickListener(this);
        et_collector_code = findViewById(R.id.collector_code);
        et_password = findViewById(R.id.password);
        cntxt = this;
        dataBase = AppDataBase.getInstance(cntxt);
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
        String[] permissions = {Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};
        services = new ApiServices(this, false);

        requestPermissions(permissions);

//        Utils.callPermissions(this, permissions,
//                new Action() {
//                    @Override
//                    public void run() throws Exception {
//
//                    }
//                }, getString(R.string.enable_required_permossions)
//        );
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
                Toast.makeText(cntxt, "أدخل اسم المستخدم!", Toast.LENGTH_SHORT).show();
            } else if (et_password.getText().toString().trim().isEmpty()) {
                Toast.makeText(cntxt, "أدخل كلمة المرور!", Toast.LENGTH_SHORT).show();
            } else {
                if (MiniaElectricity.getPrefsManager().getCollectorCode().equals(et_collector_code.getText().toString().trim())) {
                    login();
                } else {

                    List<TransDataWithTransBill>transDataWithTransBills=dataBase.transDataDao().getAllTrans();
                    boolean allowLogin = true;
                    //ArrayList<TransDataEntity> transData = new ArrayList<TransDataEntity>(transDataWithTransBills);
                    for (TransDataWithTransBill b :
                            transDataWithTransBills) {
                        if (b.getTransData().getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue() && b.getTransData().getStatus() == TransDataEntity.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                            allowLogin = false;
                            break;
                        } else if (b.getTransData().getStatus() != TransData.STATUS.INITIATED.getValue() && b.getTransData().getStatus() != TransData.STATUS.COMPLETED.getValue()
                                && b.getTransData().getStatus() != TransData.STATUS.CANCELLED.getValue()) {
                            allowLogin = false;
                            break;
                        }
                    }
                    StringBuilder warning = new StringBuilder();
                    if (allowLogin) {
                        dataBase.offlineClientsDao().clearClients();
                        dataBase.billDataDaoDao().clearBills();

                        //DBHelper.getInstance(cntxt).clearOfflineData();
                        login();
                    } else

                        warning.append("برجاء مزامنة فواتير المحصل السابق");
                    warning.append(MiniaElectricity.getPrefsManager().getCollectorCode());
                    warning.append("لتمكين تسجيل الدخول.");

                    Toast.makeText(cntxt, warning.toString(), Toast.LENGTH_LONG).show();


                    //Toast.makeText(cntxt, " برجاء مزامنة فواتير المحصل السابق لتمكين تسجيل الدخول. "+"( "+MiniaElectricity.getPrefsManager().getCollectorCode()+")" , Toast.LENGTH_LONG).show();

                }

            }
        }
    }

    private void login() {
        if (Utils.checkConnection(this))
            services.logIn(et_collector_code.getText().toString().trim(), et_password.getText().toString().trim(),
                    new RequestListener() {
                        @Override
                        public void onSuccess(String response) {
                            try {
                                //Log.e("response", response);
                                JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                                String UserSessionID = responseBody.optString("UserSessionID").trim();
                                String Error = responseBody.optString("Error").trim();
                                int billsStatus = responseBody.optInt("UserNewBillStatus");
//                                    Log.e("UserSessionID", UserSessionID);
//                                    Log.e("Error", Error+"//////");
                                if (!Error.isEmpty()) {
                                    onFailure("فشل في عملية تسجيل الدخول!\n" + Error);
                                } else if (!UserSessionID.isEmpty()) {
                                    progressDialog.show();
                                    MiniaElectricity.getPrefsManager().setCollectorCode(et_collector_code.getText().toString().trim());
                                    MiniaElectricity.getPrefsManager().setPassword(et_password.getText().toString().trim());
                                    MiniaElectricity.getPrefsManager().setSessionId(UserSessionID);
                                    if (billsStatus != 0)
                                        MiniaElectricity.getPrefsManager().setOfflineBillsStatus(billsStatus);
                                    if (billsStatus == 2)
                                    {
                                        dataBase.offlineClientsDao().clearClients();
                                        dataBase.billDataDaoDao().clearBills();
                                    }
                                /*startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();*/

                                   ParamsMsg.Request request = new ParamsMsg.Request();
                                    transAPI = TransAPIFactory.createTransAPI();
                                    request.setCategory(SdkConstants.CATEGORY_VOID);
                                    request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
                                             transAPI.startTrans(cntxt, request);

//                                    MiniaElectricity.getPrefsManager().setLoggedStatus(true);
//                                    MiniaElectricity.getPrefsManager().setTerminalId("");
//                                    MiniaElectricity.getPrefsManager().setMerchantId("");
//                                    MiniaElectricity.getPrefsManager().setFixedFees(0);
//                                    MiniaElectricity.getPrefsManager().setPercentFees(0);
//                                    MiniaElectricity.getPrefsManager().setOfflineStartingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
//                                            .format(new Date(System.currentTimeMillis())));
//                                    Intent intent = new Intent(cntxt, MainActivity.class);
//                                    Bundle bundle = new Bundle();
//                                    bundle.putBoolean("after_login", true);
//                                    intent.putExtra("params", bundle);
//                                    startActivity(intent);
//                                    finish();

                                } else onFailure("فشل في عملية تسجيل الدخول!");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                onFailure(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String failureMsg) {
                            Toast.makeText(LoginActivity.this, failureMsg, Toast.LENGTH_LONG).show();
                            et_collector_code.setText("");
                            et_password.setText("");
                        }
                    });
        else Toast.makeText(cntxt, "لا يوجد اتصال بالانترنت!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressDialog.dismiss();

        BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);
        if (baseResponse == null) {
            Toast.makeText(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
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
            Toast.makeText(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
        }

    }

    void requestPermissions(String[] perms)
    {

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.enable_required_permossions),
                    APP_PERMISSIONS, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
