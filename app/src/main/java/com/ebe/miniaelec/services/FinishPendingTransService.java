package com.ebe.miniaelec.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.database.AppDataBase;
import com.ebe.miniaelec.database.BaseDbHelper;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.database.entities.TransBillEntity;
import com.ebe.miniaelec.database.entities.TransDataEntity;
import com.ebe.miniaelec.database.entities.TransDataWithTransBill;
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
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FinishPendingTransService extends Service {

    private static ArrayList<TransDataEntity> pendingTransData = new ArrayList<>();
    private ArrayList<TransDataEntity> offlineTransData;
    public static MutableLiveData<String> errorMsg = new MutableLiveData<String>("");
    public static MutableLiveData<TransDataEntity> aVoid = new MutableLiveData<>(null);
    public static MutableLiveData<Boolean> goToLogin = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> goToPayment = new MutableLiveData<>(false);
    public static MutableLiveData<ArrayList<TransDataEntity>> pendingData = new MutableLiveData<ArrayList<TransDataEntity>>(pendingTransData);
    public static MutableLiveData<Boolean> serviceState = new MutableLiveData<>(true);

    private static int index = 0;
    ApiServices services;
    ApiServices drmServices;
    private JsonArray ModelClientPaymentV = new JsonArray();

    public static MutableLiveData<Integer> indexState = new MutableLiveData<Integer>(index);
    public static MutableLiveData<Boolean> loadingState = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> drmLoadingState = new MutableLiveData<>(false);

    AppDataBase dataBase;
    CompositeDisposable compositeDisposable;




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        services = new ApiServices(this, false);
         drmServices = new ApiServices(this, true);

        loadingState = services.dialogState;
        drmLoadingState = drmServices.dialogState;
        dataBase = AppDataBase.getInstance(this);
        compositeDisposable = new CompositeDisposable();



        if (intent != null)
        {
            boolean pendingRequest =intent.getBooleanExtra("pending",false);

            if (pendingRequest)
            {
                handlePendingBills();
            }else
            {
                init();
            }

        }





        return START_REDELIVER_INTENT;
    }


    void init()
    {

        compositeDisposable.add(dataBase.transDataDao().getAllTrans()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<TransDataWithTransBill>>() {
            @Override
            public void accept(List<TransDataWithTransBill> transDataWithTransBills) throws Throwable {
                pendingTransData = new ArrayList<>();
                offlineTransData = new ArrayList<>();
                ArrayList<TransDataWithTransBill> transData = new ArrayList<>(transDataWithTransBills);

                for (TransDataWithTransBill b :
                        transData) {
                    if (b.getTransData().getClientID() == null || b.getTransData().getClientID().equalsIgnoreCase("null") || (b.getTransData().getStatus() == TransDataEntity.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getTransData().getDrmData() == null) || (b.getTransData().getStatus() == TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getTransData().getDrmData().equals("null"))) {
                        for (TransBillEntity bill :
                                b.getTransBills()) {
                            dataBase.transBillDao().deleteTransBill(bill.getBillUnique());
                        }
                       dataBase.transDataDao().deleteTransData(b.getTransData());
                    } else {
                        if (b.getTransData().getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue() && b.getTransData().getStatus() == TransDataEntity.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                            offlineTransData.add(b.getTransData());
                        } else if (b.getTransData().getStatus() != TransData.STATUS.INITIATED.getValue() && b.getTransData().getStatus() != TransData.STATUS.COMPLETED.getValue()
                                && b.getTransData().getStatus() != TransData.STATUS.CANCELLED.getValue()) {
                            pendingTransData.add(b.getTransData());
                        } else {
                            for (TransBillEntity bill :
                                    b.getTransBills()) {
                                dataBase.transBillDao().deleteTransBill(bill.getBillUnique());
                            }
                            dataBase.transDataDao().deleteTransData(b.getTransData());
                        }
                    }
                }
                if ((offlineTransData.size() > 0 || pendingTransData.size() > 0) && Utils.checkConnection(MiniaElectricity.getInstance())) {
                    setBills();
                } else {
                    goToPayment.setValue(true);
                    stopSelf();
                }

            }
        }));




    }

    private void setBills() {
//        ArrayList<TransData> all = new ArrayList<>(pendingTransData);
//        all.addAll(offlineTransData);
//        AdapterBills adapterBills = new AdapterBills(this, all);


        if (offlineTransData.size() > 0) {
            prepareOfflineBills();
            handleOfflineBills();
        } else
            handlePendingBills();
    }

    void prepareOfflineBills()
    {
        for (TransDataEntity transData :
                offlineTransData) {
            JsonObject i = new JsonObject();
            i.addProperty("BankTransactionID", transData.getBankTransactionID());
            i.addProperty("ClientMobileNo", transData.getClientMobileNo());
            i.addProperty("BankDateTime", transData.getTransDateTime());
            i.addProperty("BankReceiptNo", transData.getStan());
            i.addProperty("ClientID", transData.getClientID());
            i.addProperty("PrintCount", transData.getPrintCount());

            JsonArray ModelBillPaymentV = new JsonArray();

            dataBase.transBillDao().getTransBillsByTransData(transData.getClientID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .blockingSubscribe(new Consumer<List<TransBillEntity>>() {
                                           @Override
                                           public void accept(List<TransBillEntity> transBillEntities) throws Throwable {
                                               for (TransBillEntity b :
                                                       transBillEntities) {
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
                                       }

                    );

        }

    }

    private void handleOfflineBills() {

       if (ModelClientPaymentV.size() >0)
       {
           services.offlineBillPayment(ModelClientPaymentV,
                   new RequestListener() {
                       @Override
                       public void onSuccess(String response) {
                           try {

                               JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                               String Error = responseBody.optString("Error").trim();
                               int billsStatus = responseBody.optInt("UserNewBillStatus");
                               int userStatus = responseBody.getInt("UserStatus");
                               String operationStatus = responseBody.getString("OperationStatus");

                               if (!operationStatus.trim().equalsIgnoreCase("successful")) {
                                   if (Error.contains("ليس لديك صلاحيات الوصول للهندسه") || Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول") || userStatus == 0) {
                                       MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                       errorMsg.setValue(Error);
                                       //Toast.makeText(cntxt, Error, Toast.LENGTH_LONG).show();
                                       //startActivity(new Intent(FinishPendingTransActivity.this, LoginActivity.class));
                                       goToLogin.setValue(true);
                                       stopSelf();
                                   } else onFailure("فشل في مزامنة عمليات الدفع\n" + Error);

                               } else {
                                   MiniaElectricity.getPrefsManager().setOfflineStartingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                           .format(new Date(System.currentTimeMillis())));
                                   MiniaElectricity.getPrefsManager().setOfflineBillValue(0);
                                   MiniaElectricity.getPrefsManager().setOfflineBillCount(0);
                                   if (billsStatus != 0) {
                                       MiniaElectricity.getPrefsManager().setOfflineBillsStatus(billsStatus);
                                   }
                                   if (billsStatus == 2) {
                                       dataBase.offlineClientsDao().clearClients();
                                       dataBase.billDataDaoDao().clearBills();
                                   }


                                   for (TransDataEntity t :
                                           offlineTransData) {
                                       t.setStatus(TransData.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                       dataBase.transDataDao().addTransData(t);
                                       pendingTransData.add(t);
                                   }

                                   offlineTransData.clear();
                                   onFailure(null);
                                   handlePendingBills();
                               }
                           } catch (JSONException e) {
                               e.printStackTrace();
                               onFailure(e.getMessage());
                           }
                       }

                       @Override
                       public void onFailure(String failureMsg) {
                           if (failureMsg != null)
                               //Toast.makeText(this, failureMsg, Toast.LENGTH_LONG).show();
                               errorMsg.setValue(failureMsg);
                           MiniaElectricity.getPrefsManager().setOfflineBillsStatus(0);

                       }
                   });
       }

        }



    private void handlePendingBills() {
        if (index < pendingTransData.size() && Utils.checkConnection(MiniaElectricity.getInstance())) {
            TransDataEntity transData = pendingTransData.get(index);
            index = index + 1;
            if (TransData.STATUS.PENDING_CASH_PAYMENT_REQ.getValue() == transData.getStatus() ||
                    TransData.STATUS.PENDING_CARD_PAYMENT_REQ.getValue() == transData.getStatus() ||
                    TransData.STATUS.PENDING_DELETE_REQ.getValue() == transData.getStatus()) {
                //delete the request then send void DRM
                deletePayment(transData);
            } else if (TransData.STATUS.PENDING_SALE_REQ.getValue() == transData.getStatus() ||
                    TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue() == transData.getStatus()) {
                //send void by referenceNo
                aVoid.setValue(transData);
                //aVoidReq(transData);
            } else if (TransData.STATUS.DELETED_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send void DRM
                sendDRM(true, transData);
            } else if (TransData.STATUS.PAID_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send DRM
                sendDRM(false, transData);
            } else handlePendingBills();

        } else
        {
            goToPayment.setValue(true);
            stopSelf();
        }

    }

    private void deletePayment(final TransDataEntity transData) {
        transData.setStatus(TransDataEntity.STATUS.PENDING_DELETE_REQ.getValue());
       dataBase.transDataDao().addTransData(transData);
       services.cancelBillPayment(transData.getBankTransactionID(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        // whatever the response of delete req suppose it is succeeded
                        if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue()) {
                            transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                            dataBase.transDataDao().addTransData(transData);
                            compositeDisposable.add(
                                    dataBase.transBillDao().getTransBillsByTransData(transData.getClientID())
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Consumer<List<TransBillEntity>>() {
                                                @Override
                                                public void accept(List<TransBillEntity> transBillEntities) throws Throwable {
                                                    for (TransBillEntity b :transBillEntities
                                                    ) {
                                                        dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                    }
                                                    dataBase.transDataDao().deleteTransData(transData);
                                                }
                                            })
                            );

                        } else {
                            transData.setStatus(TransData.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            //DBHelper.getInstance(cntxt).deleteBillData(billData);
                            // send void request to QNB payment App
                            aVoid.setValue(transData);
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        //Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        errorMsg.setValue(failureMsg);
                        handlePendingBills();
                    }
                });
    }

    private void sendDRM(boolean isView, final TransDataEntity transData) {
       // Log.i("onSuccess", transData.getDrmData());
        if (transData.getDrmData() != null && !transData.getDrmData().isEmpty())
        {

            drmServices.sendDRM(false,(JsonObject) new JsonParser().parse(transData.getDrmData()), new RequestListener() {
                @Override
                public void onSuccess(String response) {
                    Log.i("onSuccess", response);
                    JSONObject responseBody = null;
                    loadingState.setValue(false);
                    try {
                        responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                        String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                        if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                            transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                            dataBase.transDataDao().addTransData(transData);

                           compositeDisposable.add(
                                   dataBase.transBillDao().getTransBillsByTransData(transData.getClientID())
                                           .subscribeOn(Schedulers.io())
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .subscribe(new Consumer<List<TransBillEntity>>() {
                                               @Override
                                               public void accept(List<TransBillEntity> transBillEntities) throws Throwable {
                                                   for (TransBillEntity b :
                                                           transBillEntities) {
                                                       dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                   }
                                                  dataBase.transDataDao().deleteTransData(transData);

                                                   handlePendingBills();
                                               }
                                           })
                           );

                        }else
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
        }else
        {
            handlePendingBills();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceState.setValue(false);
        compositeDisposable.clear();
    }
}
