package com.ebe.miniaelec.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

//import androidx.annotation.NonNull;

import com.ebe.miniaelec.BuildConfig;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.http.api.API;
import com.ebe.miniaelec.model.Bill;
import com.ebe.miniaelec.transactions.PaymentFragment;
import com.ebe.miniaelec.transactions.ReadCounterMeterFragment;
import com.ebe.miniaelec.ui.HomeFragment;
import com.ebe.miniaelec.ui.MainActivity;
import com.ebe.miniaelec.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class
ApiServices {

    private SpotsDialog progressDialog;
    private static final String baseURL = "http://10.224.246.181:3000/";
            //"http://10.26.23.2:3000/";
           // "http://10.224.246.171:3000";
    private static final String QNB_DRM_URL = "https://10.224.246.181:6001";//"https://10.224.246.181:5020";
    API APi;

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

    public void getCustomerBillsData(final String customerNumber) {
        showDialog();

        Call<ResponseBody> call = APi.getCustomerBillsData(customerNumber);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideDialog();
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        JSONObject responseBody = new JSONObject(responseString.subSequence(responseString.indexOf("{"), responseString.length()).toString());
                        //Log.i("responseBody", responseString);
                        if (responseBody.optInt("responseCode") == 0) {
                            JSONObject data = responseBody.optJSONObject("data");
                            JSONArray bills = data.optJSONArray("bills");
                            ArrayList<Bill> billArrayList = new ArrayList<>();
                            for (int i = 0; i < bills.length(); i++) {
                                billArrayList.add(new Gson().fromJson(bills.get(i).toString(), Bill.class));
                            }
                            PaymentFragment.setBillData(data.optString("customerNo"), data.optString("customerName"), data.optString("areaName"), billArrayList);
                        } else {
                            Toast.makeText(MiniaElectricity.getInstance(), responseBody.optString("message"), Toast.LENGTH_LONG).show();
                            MainActivity.fragmentTransaction(new HomeFragment(), null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                    }
                } else {
                    Log.i("responseError", response.message());
                    MainActivity.fragmentTransaction(new HomeFragment(), null);

                    //ShowPopUpDialog.ShowDialogError(context);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideDialog();

                Log.i("responseFailure", t.getMessage() + "");
                MainActivity.fragmentTransaction(new HomeFragment(), null);

            }

        });
    }

    public void getBuildingBillsData(final String buildingNo) {
        showDialog();

        Call<ResponseBody> call = APi.getBuildingBillsData(buildingNo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideDialog();
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        JSONObject responseBody = new JSONObject(responseString.subSequence(responseString.indexOf("{"), responseString.length()).toString());
                        //Log.i("responseString", responseString);
                        if (responseBody.optInt("responseCode") == 0) {
                            JSONObject data = responseBody.optJSONObject("data");
                            JSONArray bills = data.optJSONArray("bills");
                            ArrayList<Bill> billArrayList = new ArrayList<>();
                            for (int i = 0; i < bills.length(); i++) {
                                billArrayList.add(new Gson().fromJson(bills.get(i).toString(), Bill.class));
                            }
                            //BuildingBillsFragment.setBillData( data.optString("buildingNo"), billArrayList);
                        } else {
                            Toast.makeText(MiniaElectricity.getInstance(), responseBody.optString("message"), Toast.LENGTH_LONG).show();
                            MainActivity.fragmentTransaction(new HomeFragment(), null);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                    } catch (JSONException e) {
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        e.printStackTrace();
                    }
                } else {
                    Log.i("responseError", response.message());
                    MainActivity.fragmentTransaction(new HomeFragment(), null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideDialog();
                MainActivity.fragmentTransaction(new HomeFragment(), null);
                Log.i("responseFailure", t.getMessage() + "");
            }

        });
    }

    public void getMeterReadData(final String customerNumber) {
        showDialog();

        Call<ResponseBody> call = APi.getMeterReadData(customerNumber);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideDialog();
                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        JSONObject responseBody = new JSONObject(responseString.subSequence(responseString.indexOf("{"), responseString.length()).toString());
                        //Log.i("responseString", responseString);
                        if (responseBody.optInt("responseCode") == 0) {
                            JSONObject data = responseBody.optJSONObject("data");
                            ReadCounterMeterFragment.setValidationData(data.optInt("closeDate"), data.optInt("lastMeterValue"),
                                    data.optString("customerNo"));
                        } else {
                            Toast.makeText(MiniaElectricity.getInstance(), responseBody.optString("message"), Toast.LENGTH_LONG).show();
                            MainActivity.fragmentTransaction(new HomeFragment(), null);
                        }
                    } catch (IOException e) {
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        e.printStackTrace();
                    } catch (JSONException e) {
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        e.printStackTrace();
                    }
                } else {
                    MainActivity.fragmentTransaction(new HomeFragment(), null);
                    Log.i("responseError", response.message());
                    //ShowPopUpDialog.ShowDialogError(context);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideDialog();
                Log.i("responseFailure", t.getMessage() + "");
                MainActivity.fragmentTransaction(new HomeFragment(), null);

            }

        });
    }

    public void sendDRM(final JsonObject paraObj, final RequestListener listener) {
        showDialog();
        Call<ResponseBody> call = APi.sendDRM(paraObj);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideDialog();
                if (response.isSuccessful()) {
                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } /*else listener.onFailure(response.code() + ": " + response.message());*/ else if (isFirst) {
                    //Log.e("DRM_isSuccessful", "" + response.code() + response.message());
                    reTry(APi.sendDRM(paraObj), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Log.e("DRM_Failure", t.getMessage() + "");
                if (isFirst) {
                    reTry(APi.sendDRM(paraObj), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(t.getMessage() + " ");
                }
            }
        });
    }

    public void updateMeterReadData(final String customerNumber, String currentMeterValue) {
        showDialog();
        JSONObject paramObject = new JSONObject();
        try {
            paramObject.put("CustomerNo", customerNumber);
            paramObject.put("CurrentMeterValue", Integer.parseInt(currentMeterValue));
            // Log.i("paramsObject", paramObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObject paraObj = new JsonObject();
        paraObj.addProperty("CustomerNo", customerNumber);
        paraObj.addProperty("CurrentMeterValue", Integer.parseInt(currentMeterValue));
        Call<ResponseBody> call = APi.updateMeterReadData(paraObj);
        //  Log.i("call", call.request().toString());

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideDialog();
                Log.i("responseString", new Gson().toJson(response));

                if (response.isSuccessful()) {
                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        JSONObject responseBody = new JSONObject(responseString.subSequence(responseString.indexOf("{"), responseString.length()).toString());
                        // Log.i("responseString", responseString);
                        Toast.makeText(MiniaElectricity.getInstance(), responseBody.optString("message"), Toast.LENGTH_LONG).show();
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                    } catch (IOException e) {
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        e.printStackTrace();
                    } catch (JSONException e) {
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        e.printStackTrace();
                    }
                } else {
                    MainActivity.fragmentTransaction(new HomeFragment(), null);
                    //ShowPopUpDialog.ShowDialogError(context);
                    try {
                        Log.i("responseError", response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideDialog();
                Log.i("responseFailure", t.getMessage() + "");
                MainActivity.fragmentTransaction(new HomeFragment(), null);
            }

        });
    }

    private boolean isFirst = true;

    private void showDialog() {
        MiniaElectricity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
    }

    private void hideDialog() {
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
                        reTry(APi.logIn(params), this, listener);
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
                    reTry(APi.logIn(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure("لقد تعذر الوصول للخادم!\n" + t.getMessage());
                }
                // listener.onFailure(t.getMessage() + "");
            }
        });
    }

    private void reTry(Call<ResponseBody> call, Callback<ResponseBody> callback, final RequestListener listener) {
        if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) == 1) //connected to sim2
//            MiniaElectricity.getDal().getSys().switchSimCard(1); //switch to sim1
            Utils.switchSimCard(0);
        else if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) == 0) //connected to sim1
//            MiniaElectricity.getDal().getSys().switchSimCard(2); //switch to sim2
            Utils.switchSimCard(1);
        final int[] counter = {0};
        //Log.e("counter0", String.valueOf(counter[0]));
        while (!Utils.checkConnection(MiniaElectricity.getInstance()) && counter[0] < 15) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(2000);
                        //Log.e("counter", String.valueOf(counter[0]));
                        counter[0]++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        hideDialog();
                        if (listener != null)
                            listener.onFailure("لقد تعذر الوصول للخادم!");

                    }
                }
            }.start();
        }
        isFirst = false;
        if (!Utils.checkConnection(MiniaElectricity.getInstance())) {
            hideDialog();
            if (listener != null)
                listener.onFailure("لقد تعذر الوصول للخادم!");
        } else
            call.enqueue(callback);
    }

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
                    reTry(APi.billInquiry(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage() + "/////");
                if (isFirst) {
                    reTry(APi.billInquiry(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                }
            }
        });
    }

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
                    reTry(APi.rePrint(params), this, listener);
                } else {
                    listener.onFailure(response.code() + ": " + response.message());
                    hideDialog();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFirst) {
                    reTry(APi.rePrint(params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                    hideDialog();
                }

            }
        });
    }

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
                    reTry(APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage() + "");
                if (isFirst) {
                    reTry(APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                    hideDialog();
                }

            }
        });
    }

    public void offlineBillPayment(final JsonArray ModelClintPaymentV, final RequestListener listener) {
        showDialog();
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
                    hideDialog();

                    try {
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else if (isFirst) {
                    reTry(APi.offlineBillsPay(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure(response.code() + ": " + response.message());

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", t.getMessage() + "");
                if (isFirst) {
                    reTry(APi.billPayment(/*url, ModelBillPaymentV*/params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!" + "\n" + t.getMessage());
                    hideDialog();
                }

            }
        });
    }

    public void cancelBillPayment(String BankTransactionID, final RequestListener listener) {
        showDialog();
        final Map<String, String> params = new HashMap<>();
        params.put("UnitSerialNo", MiniaElectricity.getSerial());
        params.put("BankTransactionID", BankTransactionID);
        Call<ResponseBody> call = APi.cancelPayment(params);
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
                } else if (isFirst) {
                    reTry(APi.cancelPayment(params), this, listener);
                } else {
                    listener.onFailure(response.code() + ": " + response.message());
                    hideDialog();
                }

                //listener.onFailure(response.code() + ": " + response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                if (isFirst) {
                    reTry(APi.cancelPayment(params), this, listener);
                } else {
                    listener.onFailure("لقد تعذر الوصول للخادم!");
                    hideDialog();
                }

            }
        });
    }

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
                        listener.onSuccess(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e.getMessage() + "");
                    }
                } else {
//                    listener.onFailure(response.code() + ": " + response.message())
                    if (isFirst) {
                        reTry(APi.getOfflineClients(params), this, listener);
                    } else {
                        hideDialog();
                        listener.onFailure("لقد تعذر تحميل بيانات العملاء!");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (isFirst) {
                    reTry(APi.getOfflineClients(params), this, listener);
                } else {
                    hideDialog();
                    listener.onFailure("لقد تعذر تحميل بيانات العملاء!");
                }
                // listener.onFailure(t.getMessage() + "");
            }
        });
    }
}
