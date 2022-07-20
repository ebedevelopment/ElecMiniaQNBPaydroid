package com.ebe.miniaelec.ui.login;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

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
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.ui.MainActivity;
import com.ebe.miniaelec.ui.services.FinishPendingTransService;
import com.ebe.miniaelec.utils.CustomDialog;
import com.ebe.miniaelec.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
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
    DisposableCompletableObserver observer;
    private ApiServices services;
    private volatile boolean allowLogin;
    boolean pendingTransState = true;
    int serviceCount = 0;

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
        disposable = new CompositeDisposable();
        addObservers();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
        String[] permissions = {Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};
        services = new ApiServices(LoginActivity.this, false);

        requestPermissions(permissions);

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
               // Toast.makeText(cntxt, "أدخل اسم المستخدم!", Toast.LENGTH_SHORT).show();
                CustomDialog.showMessage(this,"أدخل اسم المستخدم!");
            } else if (et_password.getText().toString().trim().isEmpty()) {
               // Toast.makeText(cntxt, "أدخل كلمة المرور!", Toast.LENGTH_SHORT).show();
                CustomDialog.showMessage(this,"أدخل كلمة المرور!");
            } else {
                if (MiniaElectricity.getPrefsManager().getCollectorCode().equals(et_collector_code.getText().toString().trim())) {
                    login();
                } else {
                     allowLogin = true;
                    dataBase.transDataDao().getAllTransBills().observe(this, transDataWithTransBills -> {
                        //ArrayList<TransDataEntity> transData = new ArrayList<TransDataEntity>(transDataWithTransBills);
                        for (TransDataWithTransBill b :
                                transDataWithTransBills) {
                            if (b.getTransData().getPaymentType() == TransDataEntity.PaymentType.OFFLINE_CASH.getValue() && b.getTransData().getStatus() == TransDataEntity.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                                allowLogin = false;
                                break;
                            } else if (b.getTransData().getStatus() != TransDataEntity.STATUS.INITIATED.getValue() && b.getTransData().getStatus() != TransDataEntity.STATUS.COMPLETED.getValue()
                                    && b.getTransData().getStatus() != TransDataEntity.STATUS.CANCELLED.getValue()) {
                                allowLogin = false;
                                break;
                            }
                        }
                    });





                    StringBuilder warning = new StringBuilder();
                    if (allowLogin) {
                        progressDialog.show();
//                        dataBase.offlineClientsDao().clearClients();
//                        dataBase.billDataDaoDao().clearBills();
//                        login();
                  observer = Completable.fromRunnable(new Runnable() {
                            @Override
                            public void run() {
                                dataBase.offlineClientsDao().clearClients();
                                dataBase.billDataDaoDao().clearBills();
                            }
                        }).subscribeOn(AndroidSchedulers.mainThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        progressDialog.dismiss();
                                        login();
                                    }

                                    @Override
                                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                    progressDialog.dismiss();
                                   CustomDialog.showMessage(LoginActivity.this,"UnKnownError");

                                    }
                                });





                    } else
                    {
                        warning.append("برجاء مزامنة فواتير المحصل السابق");
                        warning.append(MiniaElectricity.getPrefsManager().getCollectorCode());
                        warning.append("لتمكين تسجيل الدخول.");

                        CustomDialog.showMessage(cntxt, warning.toString());
                    }





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
                                   // MiniaElectricity.getPrefsManager().setLoggedStatus(true);
                                    MiniaElectricity.getPrefsManager().setCollectorCode(et_collector_code.getText().toString().trim());
                                    MiniaElectricity.getPrefsManager().setPassword(et_password.getText().toString().trim());
                                    MiniaElectricity.getPrefsManager().setSessionId(UserSessionID);
                                    if (billsStatus != 0)
                                        MiniaElectricity.getPrefsManager().setOfflineBillsStatus(billsStatus);
                                    if (billsStatus == 2)
                                    {
                                      disposable.add( Completable.fromRunnable(new Runnable() {
                                          @Override
                                          public void run() {
                                              dataBase.offlineClientsDao().clearClients();
                                              dataBase.billDataDaoDao().clearBills();
                                          }
                                      }).subscribeOn(Schedulers.io())
                                              .onErrorReturn(throwable -> {
                                                  Log.d("Login", "onSuccess: "+throwable.getMessage());
                                                  return null;
                                              }).subscribe());

                                    }


                                    checkPendingTrans();


                                } else onFailure("فشل في عملية تسجيل الدخول!");
                            } catch (JSONException e) {
                                e.printStackTrace();
                                onFailure(e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(String failureMsg) {
                            progressDialog.dismiss();
                            CustomDialog.showMessage(LoginActivity.this, failureMsg);
                            et_collector_code.setText("");
                            et_password.setText("");
                        }
                    });
        else CustomDialog.showMessage(cntxt, "لا يوجد اتصال بالانترنت!");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressDialog.dismiss();

        BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);
        if (baseResponse == null) {
            CustomDialog.showMessage(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى");
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
            CustomDialog.showMessage(cntxt, "فشل في تحميل البيانات برجاء المحاولة مرة اخرى");
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

    @Override
    protected void onStop() {
        super.onStop();
        disposable.dispose();
        progressDialog.dismiss();
        if (observer !=null)
        observer.dispose();
    }

    private void checkPendingTrans() {
        ArrayList<TransDataEntity> transDataEntities= new ArrayList<>();
        disposable.add(AppDataBase.getInstance(cntxt).transDataDao().getAllTrans().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response->
                {

                    for(TransDataWithTransBill b : response)
                    {

                        if (b.getTransData().getPaymentType() == TransDataEntity.PaymentType.OFFLINE_CASH.getValue() && b.getTransData().getStatus() == TransDataEntity.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                            pendingTransState = false;
                        } else if (b.getTransData().getStatus() != TransDataEntity.STATUS.INITIATED.getValue() && b.getTransData().getStatus() != TransDataEntity.STATUS.COMPLETED.getValue()
                                && b.getTransData().getStatus() != TransDataEntity.STATUS.CANCELLED.getValue()) {
                            pendingTransState =false;
                        }

                    }


                    return pendingTransState;


                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Throwable {

                        if (aBoolean)
                        {
                            ParamsMsg.Request request = new ParamsMsg.Request();
                            transAPI = TransAPIFactory.createTransAPI();
                            request.setCategory(SdkConstants.CATEGORY_VOID);
                            request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
                            transAPI.startTrans(cntxt, request);
                        }else
                        {

                            if (serviceCount ==0)
                            {


                              startService(new Intent(cntxt, FinishPendingTransService.class));
                            }

                            else
                            {
                                progressDialog.dismiss();
                                CustomDialog.showMessage(cntxt, "فشل في عملية تسجيل الدخول");
                            }
                        }
                    }
                }));
    }


    private void addObservers()
    {
        FinishPendingTransService.loadingState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                {
                    services.showDialog();
                }else
                {
                    services.hideDialog();
                }
            }
        });

        FinishPendingTransService.drmLoadingState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                {
                    services.showDialog();
                }else
                {
                    services.hideDialog();
                }
            }
        });

        FinishPendingTransService.serviceState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                {
                    serviceCount=1;
                    if (!MiniaElectricity.getPrefsManager().isLoggedIn()) {
                        finish();
                    } else {

                        checkPendingTrans();

                    }
                }

            }
        });

    }


}
