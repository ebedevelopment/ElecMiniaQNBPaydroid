package com.ebe.miniaelec.data.repositories;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.domain.MainRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainRepositoryImpl implements MainRepository {

    MutableLiveData<String> inquiryError = new MutableLiveData<>("");
    MutableLiveData<Bundle> inquiryBundle = new MutableLiveData<>(null);
    private AppDataBase dataBase;
    private ApiServices apiServices;

    public MainRepositoryImpl(AppDataBase dataBase, ApiServices apiServices) {
        this.dataBase = dataBase;
        this.apiServices = apiServices;
    }

    @Override
    public void billInquiry(String ClientID, RequestListener listener) {
       apiServices.billInquiry(ClientID, new RequestListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String Error = responseBody.optString("Error").trim();
                    //Log.e("response", response);
                    if (!Error.isEmpty()) {
                        onFailure("فشل في الاستعلام!\n" + Error);
                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                        }
                    } else {

                        Bundle bundle = new Bundle();
                        bundle.putString("response", response);
                        bundle.putString("clientID", ClientID);
                        // fragment.setArguments(bundle);
                        bundle.putBoolean("offline", false);
                        inquiryBundle.setValue(bundle);

                        // MainActivity.fragmentTransaction(fragment, "BillPayment");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onFailure(e.getMessage());
                }
            }

            @Override
            public void onFailure(String failureMsg) {
                inquiryError.setValue(failureMsg);
            }
        });

    }



    @Override
    public Flowable<List<String>> getDistinctMntka() {
        return dataBase.billDataDaoDao().getDistinctMntka()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Flowable<List<String>> getDistinctDaysOfMntka(String mntka) {
        return dataBase.billDataDaoDao().getDistinctDaysOfMntka(mntka)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Flowable<List<String>> getDistinctMainsOfMntkaAndDay(String mntka, String day) {
        return dataBase.billDataDaoDao().getDistinctMainsOfMntkaAndDay(mntka, day)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Flowable<List<String>> getDistinctFaryOfMntkaAndDayAndMain(String mntka, String day, String main) {
        return dataBase.billDataDaoDao().getDistinctFaryOfMntkaAndDayAndMain(mntka, day, main)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public LiveData<List<BillDataEntity>> getDistinctBills() {
        return dataBase.billDataDaoDao().getDistinctBills();
    }

    @Override
    public Single<List<BillDataEntity>> getDistinctBillsOfMntka(String mntka) {
        return dataBase.billDataDaoDao().getDistinctBillsOfMntka(mntka)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<List<BillDataEntity>> getDistinctBillsByMntkaAndDay(String mntka, String day) {
        return dataBase.billDataDaoDao().getDistinctBillsByMntkaAndDay(mntka, day)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<List<BillDataEntity>> getDistinctBillsByMntkaDayAndMain(String mntka, String day, String main) {
        return dataBase.billDataDaoDao().getDistinctBillsByMntkaDayAndMain(mntka, day, main)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<List<BillDataEntity>> getDistinctBillsByMntkaDayMainAndFary(String mntka, String day, String main, String fary) {
        return dataBase.billDataDaoDao().getDistinctBillsByMntkaDayMainAndFary(mntka, day, main, fary)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Single<List<BillDataEntity>> getDistinctBillsByClientName(String clientName) {
        return dataBase.billDataDaoDao().getDistinctBillsByClientName(clientName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }



    @Override
    public Single<TransDataWithTransBill> getTransByRefNo(int refNo) {
        return dataBase.transDataDao().getTransByRefNo(refNo)
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable->{
                    Log.d(null, "onActivityResult: "+throwable.getMessage());
                    return null;
                });
    }

    @Override
    public void deleteTransBill(long billUnique)
    {
        dataBase.transBillDao().deleteTransBill(billUnique);
    }

    @Override
    public void deleteTransData(TransDataEntity transData)

    {
        dataBase.transDataDao().deleteTransData(transData);
    }
}
