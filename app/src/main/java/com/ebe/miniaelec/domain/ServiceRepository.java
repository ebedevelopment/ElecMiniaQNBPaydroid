package com.ebe.miniaelec.domain;

import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.RequestListener;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface ServiceRepository {

    Flowable<List<TransDataWithTransBill>> getAllTrans();

    void deleteTransBill(long BillUnique);

    void deleteTransData(TransDataEntity transData);

    Single<List<TransBillEntity>> getTransBillsByTransData(int transId);

    void updateTransData(TransDataEntity transDataEntity);

    public void cancelBillPayment(String BankTransactionID, final RequestListener listener);

    public void sendDrm();

    void clearBills();

    void clearClients();
}
