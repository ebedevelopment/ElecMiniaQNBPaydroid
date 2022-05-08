package com.ebe.miniaelec.domain;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;

import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.RequestListener;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface MainRepository {

    //remote
    public void billInquiry(String ClientID, final RequestListener listener);
   // public void offlineBillPayment(final JsonArray ModelClintPaymentV, final RequestListener listener);
   // public void getClients(final RequestListener listener);


    ///local

    //BillData
   // void newOfflineBillAppend(BillDataEntity bill);
    Flowable<List<String>> getDistinctMntka();
    Flowable<List<String>> getDistinctDaysOfMntka(String mntka);
    Flowable<List<String>> getDistinctMainsOfMntkaAndDay(String mntka, String day);
    Flowable<List<String>> getDistinctFaryOfMntkaAndDayAndMain(String mntka, String day, String main);
    LiveData<List<BillDataEntity>> getDistinctBills();
    PagingSource<Integer,BillDataEntity> getPagedBills();
    Single<List<BillDataEntity>> getDistinctBillsOfMntka(String mntka);
    Single<List<BillDataEntity>> getDistinctBillsByMntkaAndDay(String mntka, String day);
    Single<List<BillDataEntity>> getDistinctBillsByMntkaDayAndMain(String mntka, String day, String main);
    Single<List<BillDataEntity>> getDistinctBillsByMntkaDayMainAndFary(String mntka, String day, String main, String fary);
    Single<List<BillDataEntity>> getDistinctBillsByClientName(String clientName);


    //OfflineClient
    // long addOfflineClient( OfflineClientEntity client);
   // Single<Long> offlineClientsCount();


    //TransBills
    public void deleteTransBill(long billUnique);
    public void deleteTransData(TransDataEntity transData);


    // TransData
    Single<TransDataWithTransBill> getTransByRefNo(int refNo);

}
