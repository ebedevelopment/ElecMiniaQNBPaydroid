package com.ebe.miniaelec.domain;

import androidx.lifecycle.LiveData;

import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.RequestListener;
import com.google.gson.JsonArray;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface MainRepository {

    //remote
    public void billInquiry(String ClientID, final RequestListener listener);
    public void offlineBillPayment(final JsonArray ModelClintPaymentV, final RequestListener listener);
    public void getClients(final RequestListener listener);


    ///local

    //BillData
    void newOfflineBillAppend(BillDataEntity bill);
    Single<List<String>> getDistinctMntka();
    Single<List<String>> getDistinctDaysOfMntka(String mntka);
    Single<List<String>> getDistinctMainsOfMntkaAndDay(String mntka, String day);
    Single<List<String>> getDistinctFaryOfMntkaAndDayAndMain(String mntka, String day, String main);
    LiveData<List<BillDataEntity>> getDistinctBills();
    Single<List<BillDataEntity>> getDistinctBillsOfMntka(String mntka);
    Single<List<BillDataEntity>> getDistinctBillsByMntkaAndDay(String mntka, String day);
    Single<List<BillDataEntity>> getDistinctBillsByMntkaDayAndMain(String mntka, String day, String main);
    Single<List<BillDataEntity>> getDistinctBillsByMntkaDayMainAndFary(String mntka, String day, String main, String fary);
    Single<List<BillDataEntity>> getDistinctBillsByClientName(String clientName);


    //OfflineClient
    long addOfflineClient( OfflineClientEntity client);
    Single<Long> offlineClientsCount();


    //TransBills



    // TransData
    Single<TransDataWithTransBill> getTransByRefNo(int refNo);

}
