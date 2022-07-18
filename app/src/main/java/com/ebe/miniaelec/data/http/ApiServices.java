package com.ebe.miniaelec.data.http;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.ebe.miniaelec.BuildConfig;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.data.http.api.API;
import com.ebe.miniaelec.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class
ApiServices {

    private SpotsDialog progressDialog;
    private static final String baseURL = //"http://10.224.246.181:3000/";
            //"http://10.26.23.2:3000/";
            "http://10.224.246.171:3000";
    private static final String QNB_DRM_URL = "https://10.224.246.181:6001";//"https://10.224.246.181:5020";
    API APi;

    public  MutableLiveData<Boolean> dialogState = new MutableLiveData<>(false);

    public ApiServices(Context context) {
        progressDialog = new SpotsDialog(context, R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        APi = MiniaElectricity.getApi(baseURL, false, null);
    }

    public ApiServices(Context context, boolean isDRM) {
        progressDialog = new SpotsDialog(context, R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        if (isDRM)
            APi = MiniaElectricity.getApi(QNB_DRM_URL, true, context);
        else APi = MiniaElectricity.getApi(baseURL, false, null);

    }




    //service sendDRM
    //Bill payment sendCash Drm
    public void sendDRM(Boolean isView,final JsonObject paraObj, final RequestListener listener) {
        if (isView)
        {
          showDialog();
        }else
        {
            dialogState.postValue(true);
        }

        Call<ResponseBody> call = APi.sendDRM(paraObj);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (isView)
                {
                    hideDialog();
                }else
                {
                    dialogState.postValue(false);
                }
                if (response.isSuccessful()) {
                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } /*else listener.onFailure(response.code() + ": " + response.message());*/ else if (isFirst) {
                    //Log.e("DRM_isSuccessful", "" + response.code() + response.message());
                    reTry(isView,APi.sendDRM(paraObj), this, listener);
                } else {
                    if (isView)
                    {
                        hideDialog();
                    }else
                    {
                        dialogState.postValue(false);
                    }
                    listener.onFailure(response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.e("DRM_Failure", t.getMessage() + "");
                if (isFirst) {
                    reTry(isView,APi.sendDRM(paraObj), this, listener);
                } else {
                    if (isView)
                    {
                        hideDialog();
                    }else
                    {
                        dialogState.postValue(false);
                    }

                    listener.onFailure(t.getMessage() + " ");
                }
            }
        });
    }



    private boolean isFirst = true;

    public void showDialog() {


        MiniaElectricity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
    }

    public void hideDialog() {

        MiniaElectricity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

    public void logIn(String userName, String userPassword, final RequestListener listener) {
        showDialog();
        final Map<String, String> params = new HashMap<>();
        params.put("UserName", userName);
        params.put("UserPassWord", userPassword);
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        String version = BuildConfig.VERSION_NAME;
        params.put("APKVersion", version);
        Call<ResponseBody> call = APi.logIn(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    hideDialog();
                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else {
                    Log.e("failed", response.code() + ": " + response.message());
                    if (isFirst) {
                        reTry(true,APi.logIn(params), this, listener);
                    } else {
                        hideDialog();
                        listener.onFailure("لقد تعذر الوصول للخادم!\n" + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("failed", t.getMessage() + "///");
                if (isFirst) {
                    reTry(true,APi.logIn(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure("لقد تعذر الوصول للخادم!\n" + t.getMessage());
                }
                // listener.onFailure(t.getMessage() + "");
            }
        });
    }

    private void reTry(Boolean isView,Call<ResponseBody> call, Callback<ResponseBody> callback, final RequestListener listener) {
        if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) == 1) //connected to sim2
//            MiniaElectricity.getDal().getSys().switchSimCard(1); //switch to sim1
            Utils.switchSimCard(0);
        else if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) == 0) //connected to sim1
//            MiniaElectricity.getDal().getSys().switchSimCard(2); //switch to sim2
            Utils.switchSimCard(1);
        final int[] counter = {0};
        //Log.e("counter0", String.valueOf(counter[0]));
        boolean Error = false;
        while (!Utils.checkConnection(MiniaElectricity.getInstance()) && counter[0] < 15) {
            Completable.fromRunnable(new Runnable() {
                @Override
                public void run() {

                        //Log.e("counter", String.valueOf(counter[0]));
                        counter[0]++;

                }
            }).subscribeOn(Schedulers.io())
                    //.observeOn(AndroidSchedulers.mainThread())
                    .delay(2000, TimeUnit.MILLISECONDS).subscribe(new DisposableCompletableObserver() {
                @Override
                public void onComplete() {

                }

                @Override
                public void onError(@NonNull Throwable e) {
                    if (isView)
                    {
                        hideDialog();
                    }else {
                        dialogState.postValue(false);
                    }

                    if (listener != null)
                        listener.onFailure("لقد تعذر الوصول للخادم!");

                }
            });
        }

        isFirst = false;
        if (!Utils.checkConnection(MiniaElectricity.getInstance())) {
            if (isView)
            {
                hideDialog();
            }else {
                dialogState.postValue(false);
            }
            if (listener != null)
                listener.onFailure("لقد تعذر الوصول للخادم!");
        } else
            call.enqueue(callback);

    }

    //main fragment
    public void billInquiry(String ClientID, final RequestListener listener) {
        showDialog();
        final Map<String, String> params = new HashMap<>();
        params.put("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        params.put("ClientID", ClientID);
        //  Log.e("UserSessionID", params.get("UserSessionID"));
        Call<ResponseBody> call = APi.billInquiry(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    hideDialog();
                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } //else listener.onFailure(response.code() + ": " +
                else if (isFirst) {
                    reTry(true,APi.billInquiry(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage() + "/////");
                if (isFirst) {
                    reTry(true,APi.billInquiry(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                }
            }
        });
    }

    //reprintFragment
    public void rePrint(String ClientID, final RequestListener listener) {
        showDialog();
        final Map<String, String> params = new HashMap<>();
        params.put("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        params.put("ClientID", ClientID);
        Call<ResponseBody> call = APi.rePrint(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    hideDialog();
                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } //else listener.onFailure(response.code() + ": " +
                else if (isFirst) {
                    reTry(true,APi.rePrint(params), this, listener);
                } else {
                    listener.onFailure(response.code() + ": " + response.message());
                    hideDialog();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFirst) {
                    reTry(true,APi.rePrint(params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                    hideDialog();
                }

            }
        });
    }

    //bill payment fragment
    public void billPayment(String InquiryID, int PayType, String ClientMobileNo, String ClientID, final JsonArray ModelBillPaymentV,
                            String BankDateTime, String BankReceiptNo, String BankTransactionID,
                            String ClientCreditCard, String AcceptCode, final RequestListener listener) {
        showDialog();
        final String url = baseURL + "api/APIBankQNBs?InquiryID=" + InquiryID + "&UserSessionID=" + MiniaElectricity.getPrefsManager().getSessionId() +
                "&UnitSerialNo=" + MiniaElectricity.getSerial() + "&PayType=" + PayType + "&ClientMobileNo=" + ClientMobileNo + "&ClientID=" + ClientID +
                "&BankDateTime=" + BankDateTime + "&BankReceiptNo=" + BankReceiptNo + "&BankTransactionID=" + BankTransactionID +
                "&ClientCreditCard=" + ClientCreditCard + "&AcceptCode=" + AcceptCode;
        final JsonObject params = new JsonObject();
        params.addProperty("InquiryID", InquiryID);
        params.addProperty("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.addProperty("UnitSerialNo", MiniaElectricity.getSerial());
        params.addProperty("PayType", PayType);
        params.addProperty("ClientMobileNo", ClientMobileNo);
        params.addProperty("ClientID", ClientID);
        params.addProperty("BankDateTime", BankDateTime);
        params.addProperty("BankReceiptNo", BankReceiptNo);
        params.addProperty("BankTransactionID", BankTransactionID);
        params.addProperty("ClientCreditCard", ClientCreditCard);
        params.addProperty("AcceptCode", AcceptCode);
        params.add("ModelBillPaymentV", ModelBillPaymentV);
        //Call<ResponseBody> call = APi.billPayment(url, ModelBillPaymentV);
        Call<ResponseBody> call = APi.billPayment(params);

      /*  Call<ResponseBody> call = APi.billPayment(InquiryID, MiniaElectricity.getPrefsManager().getSessionId(), MiniaElectricity.getSerial(),
                PayType, ClientMobileNo, ClientID, BillDate, BillValue, CommissionValue, BankDateTime, BankReceiptNo, BankTransactionID,
                ClientCreditCard, AcceptCode);
        Map<String, String> params = new HashMap<>();
        params.put("InquiryID", InquiryID);
        params.put("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        params.put("PayType", String.valueOf(PayType));
        params.put("ClientMobileNo", ClientMobileNo);
        params.put("ClientID", ClientID);
        params.put("BillDate", BillDate);
        params.put("BillValue", BillValue);
        params.put("CommissionValue", CommissionValue);
        params.put("BankDateTime", BankDateTime);
        params.put("BankReceiptNo", BankReceiptNo);
        params.put("BankTransactionID", BankTransactionID);
        params.put("ClientCreditCard", ClientCreditCard);
        params.put("AcceptCode", AcceptCode);

        Call<ResponseBody> call = APi.billPayment(params);*/
//        Log.e("Params", params.toString());
        Request request = call.request();
//        Log.e("URL", request.tag().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.e("onResponse", response.code() + response.message());
                if (response.isSuccessful()) {
                    hideDialog();

                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else if (isFirst) {
                    reTry(true,APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage() + "");
                if (isFirst) {
                    reTry(true,APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                    hideDialog();
                }

            }
        });
    }

    //service handle offline bills
    public void offlineBillPayment(final JsonArray ModelClintPaymentV, final RequestListener listener) {
        //showDialog();
        dialogState.postValue(true);
        final JsonObject params = new JsonObject();
        params.addProperty("InquiryID", MiniaElectricity.getPrefsManager().getInquiryID());
        params.addProperty("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.addProperty("UnitSerialNo", MiniaElectricity.getSerial());
        params.addProperty("OffLineCount", MiniaElectricity.getPrefsManager().getOfflineBillCount());
        params.addProperty("OffLineSum", MiniaElectricity.getPrefsManager().getOfflineBillValue() / 100);
        String version = BuildConfig.VERSION_NAME;
        params.addProperty("OffLineAPKVersion", version);
        params.add("ModelClintPaymentV", ModelClintPaymentV);
        Call<ResponseBody> call = APi.offlineBillsPay(params);

//        Log.e("Params", params.toString());
        Request request = call.request();
//        Log.e("URL", request.tag().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.e("onResponse", response.code() + response.message());
                if (response.isSuccessful()) {
                   // hideDialog();
                    dialogState.postValue(false);

                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else if (isFirst) {
                    reTry(false,APi.offlineBillsPay(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    //hideDialog();
                    dialogState.postValue(false);
                    listener.onFailure(response.code() + ": " + response.message());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("offlineBillPayment", t.getMessage() + "");
                if (isFirst) {
                    reTry(false,APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!" + "\n" + t.getMessage());
                    dialogState.postValue(false);
                    //hideDialog();
                }

            }
        });
    }

    //service delete payment
    public void cancelBillPayment(String BankTransactionID, final RequestListener listener) {
       // showDialog();
        dialogState.postValue(true);
        final Map<String, String> params = new HashMap<>();
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        params.put("BankTransactionID", BankTransactionID);
        Call<ResponseBody> call = APi.cancelPayment(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    //hideDialog();
                    dialogState.postValue(false);

                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else if (isFirst) {
                    reTry(false,APi.cancelPayment(params), this, listener);
                } else {
                    listener.onFailure(response.code() + ": " + response.message());
                    //hideDialog();
                    dialogState.postValue(false);
                }

                //listener.onFailure(response.code() + ": " + response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                if (isFirst) {
                    reTry(false,APi.cancelPayment(params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                    //hideDialog();
                    dialogState.postValue(false);
                }

            }
        });
    }


    //main activity get clients
    public void getClients(final RequestListener listener) {
        showDialog();
        final Map<String, String> params = new HashMap<>();
        params.put("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        Call<ResponseBody> call = APi.getOfflineClients(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    hideDialog();
                    try {
                        response.raw().toString();
                        listener.onSuccess(response.body().string());
                        Log.d("ClientsData", "onResponse: " + response.body().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else {
//                    listener.onFailure(response.code() + ": " + response.message())
                    if (isFirst) {
                        reTry(true,APi.getOfflineClients(params), this, listener);
                    } else {
                        hideDialog();
                        listener.onFailure("لقد تعذر تحميل بيانات العملاء!");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFirst) {
                    reTry(true,APi.getOfflineClients(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure("لقد تعذر تحميل بيانات العملاء!");
                }
                // listener.onFailure(t.getMessage() + "");
            }
        });
    }

    public void deductsTypes(final RequestListener listener) {
        showDialog();
        final Map<String, String> params = new HashMap<>();
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        params.put("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        Call<ResponseBody> call = APi.khasmTypes(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    hideDialog();
                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else {
                    Log.e("failed", response.code() + ": " + response.message());
                    /*if (isFirst) {
                        reTry(APi.logIn(params), this, listener);
                    } else*/
                    {
                        hideDialog();
                        listener.onFailure("لقد تعذر الوصول للخادم!\n" + response.message());
                    }
                }
            }

            @Override
            public void onFailure(@androidx.annotation.NonNull Call<ResponseBody> call, Throwable t) {
                hideDialog();
                listener.onFailure("لقد تعذر الوصول للخادم!\n" + t.getMessage());
            }
        });
    }


    public void sendDeducts(final JsonArray ModelBillKasmV, final RequestListener listener) {
        showDialog();
        final JsonObject params = new JsonObject();
        params.addProperty("InquiryID", MiniaElectricity.getPrefsManager().getInquiryID());
        params.addProperty("UserSessionID", MiniaElectricity.getPrefsManager().getSessionId());
        params.addProperty("UnitSerialNo", MiniaElectricity.getSerial());
        params.addProperty("BillKasmCount", ModelBillKasmV.size());
        String version = BuildConfig.VERSION_NAME;
        params.addProperty("APKVersion", version);
        params.add("ModelBillKasmV", ModelBillKasmV);
        Call<ResponseBody> call = APi.sendDeducts(params);

//        Log.e("Params", params.toString());
        Request request = call.request();
//        Log.e("URL", request.tag().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                Log.e("onResponse", response.code() + response.message());
                if (response.isSuccessful()) {
                    hideDialog();

                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else if (isFirst) {
                    reTry(true,APi.offlineBillsPay(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage() + "");
                if (isFirst) {
                    reTry(true,APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!" + "\n" + t.getMessage());
                    hideDialog();
                }

            }
        });
    }
}
