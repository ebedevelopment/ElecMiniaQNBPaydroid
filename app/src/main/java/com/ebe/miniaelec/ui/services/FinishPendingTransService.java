package com.ebe.miniaelec.ui.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.ui.login.LoginActivity;
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
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FinishPendingTransService extends Service {


    private ArrayList<TransDataEntity> pendingTransData = new ArrayList<>();
    private ArrayList<TransDataEntity> offlineTransData;
    public static MutableLiveData<String> errorMsg = new MutableLiveData<String>("");
    public static MutableLiveData<TransDataEntity> aVoid = new MutableLiveData<>(null);
    public static MutableLiveData<Boolean> goToLogin = new MutableLiveData<>(false);
    public static MutableLiveData<Boolean> goToPayment = new MutableLiveData<>(false);
    public static MutableLiveData<ArrayList<TransDataEntity>> pendingData = new MutableLiveData<ArrayList<TransDataEntity>>(new ArrayList<>());
    public static MutableLiveData<Boolean> serviceState = new MutableLiveData<>(true);
    private ArrayList<TransDataEntity> deductsTransData;
    boolean pendingRequest = false;


    private static int index = 0;
    ApiServices services;
    ApiServices drmServices;
    private JsonArray ModelClientPaymentV = new JsonArray();

    public static MutableLiveData<Integer> indexState = new MutableLiveData<Integer>(0);
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
    public void onCreate() {

        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
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
            pendingRequest =intent.getBooleanExtra("pending",false);

            init();

        }





        return START_REDELIVER_INTENT;
    }


    void init()
    {

        compositeDisposable.add(dataBase.transDataDao().getAllTrans()
        .subscribeOn(Schedulers.computation())
       // .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<List<TransDataWithTransBill>>() {
            @Override
            public void accept(List<TransDataWithTransBill> transDataWithTransBills) throws Throwable {
                pendingTransData = new ArrayList<>();
                offlineTransData = new ArrayList<>();
                deductsTransData = new ArrayList<>();
                ArrayList<TransDataWithTransBill> transData = new ArrayList<>(transDataWithTransBills);

                for (TransDataWithTransBill b :
                        transData) {
                    if (b.getTransData().getClientID() == null || b.getTransData().getClientID().equalsIgnoreCase("null") || (b.getTransData().getStatus() == TransDataEntity.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getTransData().getDrmData() == null) || (b.getTransData().getStatus() == TransDataEntity.STATUS.DELETED_PENDING_DRM_REQ.getValue() && b.getTransData().getDrmData().equals("null"))) {
                        for (TransBillEntity bill :
                                b.getTransBills()) {

                            dataBase.transBillDao().deleteTransBill(bill.getBillUnique());
                        }
                       dataBase.transDataDao().deleteTransData(b.getTransData());
                    } else {
                        if (b.getTransData().getPaymentType() == TransDataEntity.PaymentType.OFFLINE_CASH.getValue() && b.getTransData().getStatus() == TransDataEntity.STATUS.PENDING_ONLINE_PAYMENT_REQ.getValue()) {
                            offlineTransData.add(b.getTransData());
                        } else if (b.getTransData().getStatus() != TransDataEntity.STATUS.INITIATED.getValue() && b.getTransData().getStatus() != TransDataEntity.STATUS.COMPLETED.getValue()
                                && b.getTransData().getStatus() != TransDataEntity.STATUS.CANCELLED.getValue()) {
                            if (b.getTransData().getStatus() == TransDataEntity.STATUS.PENDING_DEDUCT_REQ.getValue()) {
                                deductsTransData.add(b.getTransData());
                            }else
                            {
                                pendingTransData.add(b.getTransData());
                                pendingData.postValue(pendingTransData);
                            }

                        } else {
                            for (TransBillEntity bill :
                                    b.getTransBills()) {
                                dataBase.transBillDao().deleteTransBill(bill.getBillUnique());
                            }
                            dataBase.transDataDao().deleteTransData(b.getTransData());
                        }
                    }
                }
                if ((offlineTransData.size() > 0 || pendingTransData.size() > 0 || deductsTransData.size() > 0) && Utils.checkConnection(MiniaElectricity.getInstance())) {
                    setBills();
                } else {
                    goToPayment.postValue(true);
                    serviceState.postValue(false);
                    stopSelf();
                }

            }
        },throwable -> {
            Log.e("Init", "init: "+throwable.getLocalizedMessage() );
        }));




    }

    private void setBills() {



        if (offlineTransData.size() > 0) {
            prepareOfflineBills();
            handleOfflineBills();
        }else if (deductsTransData.size() > 0) {
            handleDeducts();
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
                                       errorMsg.postValue(Error);

                                       goToLogin.postValue(true);
                                       //serviceState.postValue(false);
                                       stopSelf();
                                   } else onFailure("فشل في مزامنة عمليات الدفع\n" + Error);

                               } else {
                                   Utils.deleteOffBillsFile();
                                   MiniaElectricity.getPrefsManager().setOfflineStartingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                           .format(new Date(System.currentTimeMillis())));
                                   MiniaElectricity.getPrefsManager().setOfflineBillValue(0);
                                   MiniaElectricity.getPrefsManager().setOfflineBillCount(0);
                                   if (billsStatus != 0) {
                                       MiniaElectricity.getPrefsManager().setOfflineBillsStatus(billsStatus);
                                   }
                                   if (billsStatus == 2) {
                                      compositeDisposable.add(Completable.fromRunnable(() -> {
                                          dataBase.offlineClientsDao().clearClients();
                                          dataBase.billDataDaoDao().clearBills();
                                      }).subscribeOn(Schedulers.io())
                                              .onErrorReturn(throwable -> {
                                                  Log.e("clearBills", "onSuccess: "+throwable.getLocalizedMessage() );
                                                  return null;
                                              })

                                              .subscribe());

                                   }


                                   for (TransDataEntity t :
                                           offlineTransData) {
                                       t.setStatus(TransDataEntity.STATUS.PAID_PENDING_DRM_REQ.getValue());
                                       dataBase.transDataDao().updateTransData(t);
                                       pendingTransData.add(t);
                                   }

                                   pendingData.postValue(pendingTransData);
                                   offlineTransData.clear();
                                  // handlePendingBills();
                                   handleDeducts();
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
                               errorMsg.postValue(failureMsg);
                           MiniaElectricity.getPrefsManager().setOfflineBillsStatus(0);
                           handleDeducts();

                       }
                   });
       }

        }



    private void handlePendingBills() {
        if (index < pendingTransData.size() && Utils.checkConnection(this)) {
            TransDataEntity transData = pendingTransData.get(index);
            index = index + 1;
            indexState.postValue(index);
            if (TransDataEntity.STATUS.PENDING_CASH_PAYMENT_REQ.getValue() == transData.getStatus() ||
                    TransDataEntity.STATUS.PENDING_CARD_PAYMENT_REQ.getValue() == transData.getStatus() ||
                    TransDataEntity.STATUS.PENDING_DELETE_REQ.getValue() == transData.getStatus()) {
                //delete the request then send void DRM
                deletePayment(transData);
            } else if (TransDataEntity.STATUS.PENDING_SALE_REQ.getValue() == transData.getStatus() ||
                    TransDataEntity.STATUS.DELETED_PENDING_VOID_REQ.getValue() == transData.getStatus()) {
                //send void by referenceNo
                aVoid.postValue(transData);
                //aVoidReq(transData);
            } else if (TransDataEntity.STATUS.DELETED_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send void DRM
                sendDRM(true, transData);
            } else if (TransDataEntity.STATUS.PAID_PENDING_DRM_REQ.getValue() == transData.getStatus()) {
                //send DRM
                sendDRM(false, transData);
            } else handlePendingBills();

        } else
        {
            if (pendingRequest)
            {
                goToPayment.postValue(true);
            }

         //  goToPayment.postValue(false);

            stopSelf();

        }

    }

    private void deletePayment(final TransDataEntity transData) {
        transData.setStatus(TransDataEntity.STATUS.PENDING_DELETE_REQ.getValue());
       services.cancelBillPayment(transData.getBankTransactionID(),
                new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        // whatever the response of delete req suppose it is succeeded
                        if (transData.getPaymentType() == TransDataEntity.PaymentType.CASH.getValue()) {
                            transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                           compositeDisposable.add(Completable.fromRunnable(
                                   () -> dataBase.transDataDao().updateTransData(transData)).subscribeOn(Schedulers.io())
                                   .subscribeWith(new DisposableCompletableObserver() {
                                       @Override
                                       public void onComplete() {

                                       }

                                       @Override
                                       public void onError(@NonNull Throwable e) {

                                           Log.e("DeleteBill", "onError: "+e.getLocalizedMessage() );
                                       }
                                   }));

                            compositeDisposable.add(
                                    dataBase.transBillDao().getTransBillsByTransData(transData.getClientID())
                                            .subscribeOn(Schedulers.io())
                                            .subscribe(new Consumer<List<TransBillEntity>>() {
                                                @Override
                                                public void accept(List<TransBillEntity> transBillEntities) throws Throwable {
                                                    for (TransBillEntity b :transBillEntities
                                                    ) {
                                                        dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                    }
                                                    dataBase.transDataDao().deleteTransData(transData);
                                                }
                                            },throwable -> {
                                                Log.e("delete payment", "onSuccess: "+throwable.getLocalizedMessage() );
                                            })
                            );

                        } else {
                            transData.setStatus(TransDataEntity.STATUS.DELETED_PENDING_VOID_REQ.getValue());
                            //DBHelper.getInstance(cntxt).deleteBillData(billData);
                            // send void request to QNB payment App
                            aVoid.postValue(transData);
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        //Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                        errorMsg.postValue(failureMsg);
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
                    loadingState.postValue(false);
                    try {
                        responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                        String ErrorMessage = responseBody.optString("ErrorMessage").trim();
                        if (!ErrorMessage.isEmpty() && ErrorMessage.equals("Approved")) {
                            transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                            dataBase.transDataDao().updateTransData(transData);

                           compositeDisposable.add(
                                   dataBase.transBillDao().getTransBillsByTransData(transData.getClientID())
                                           .subscribeOn(Schedulers.io())
                                           //.observeOn(AndroidSchedulers.mainThread())
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
                                           },throwable -> {
                                               Log.e("sendDrm", "onSuccess: "+throwable.getLocalizedMessage() );
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

    private void handleDeducts() {
        if (deductsTransData.size() == 0) {
            handlePendingBills();
        } else {
            JsonArray ModelBillKasmV = new JsonArray();
            for (TransDataEntity transData :
                    deductsTransData) {

                compositeDisposable.add(dataBase.transDataDao().getTransByClientId(transData.getClientID())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<TransDataWithTransBill>() {
                            @Override
                            public void accept(TransDataWithTransBill transDataWithTransBill) throws Throwable {
                                for (TransBillEntity b :
                                        transDataWithTransBill.getTransBills()) {
                                    JsonObject j = new JsonObject();
                                    j.addProperty("BillUnique", b.getBillUnique());
                                    j.addProperty("KTID", transData.getDeductType());
                                    ModelBillKasmV.add(j);
                                }


                                sendDeducts(ModelBillKasmV,false);

                            }
                        }, throwable -> {
                            //errorMsg.postValue(throwable.getMessage());
                            Log.e("deducts", "handleDeducts: "+ errorMsg.toString() );
                        }));


            }


        }
    }

     private void sendDeducts(JsonArray ModelBillKasmV,boolean isView)
     {
         try {
             services.sendDeducts(ModelBillKasmV,isView,
                     new RequestListener() {
                         @Override
                         public void onSuccess(String response) {
                             try {
                                 JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                                 String Error = responseBody.optString("Error").trim();
                                 String operationStatus = responseBody.getString("OperationStatus");
                                 if (!operationStatus.trim().equalsIgnoreCase("successful")) {
                                     if (Error.contains("ليس لديك صلاحيات الوصول للهندسه") || Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                                         MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                                         //ToastUtils.showMessage(FinishPendingTransActivity.this, Error);
                                         errorMsg.setValue(Error);
//                                    Toast.makeText(cntxt, Error, Toast.LENGTH_LONG).show();

                                         //   Toast.makeText(cntxt, Error, Toast.LENGTH_LONG).show();
                                         startActivity(new Intent(FinishPendingTransService.this, LoginActivity.class));
                                         serviceState.setValue(false);
                                         stopSelf();
                                     } else onFailure("فشل في خصم الفواتير\n" + Error);

                                 } else {
                                     for (TransDataEntity t :
                                             deductsTransData) {
                                         t.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                                         dataBase.transDataDao().updateTransData(t);

                                         compositeDisposable.add(dataBase.transDataDao().getTransByRefNo(t.getReferenceNo())
                                                 .subscribeOn(Schedulers.io())
                                                 .observeOn(AndroidSchedulers.mainThread())
                                                 .subscribe(transDataWithTransBill -> {
                                                     for (TransBillEntity b :
                                                             transDataWithTransBill.getTransBills()) {
                                                         dataBase.transBillDao().deleteTransBill(b.getBillUnique());
                                                     }
                                                     dataBase.transDataDao().deleteTransData(t);
                                                 },throwable -> {
                                                     //  errorMsg.setValue(throwable.getMessage());
                                                 }));
                                     }

                                     deductsTransData.clear();
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
                                 // errorMsg.setValue(failureMsg);
                                 handlePendingBills();
                         }
                     });
         }catch (Exception e)
         {
             // errorMsg.setValue(e.getMessage());

             Log.e("deductsError", "handleDeducts: "+ e.getMessage() );

         }
     }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceState.setValue(false);
        compositeDisposable.dispose();

    }


}
