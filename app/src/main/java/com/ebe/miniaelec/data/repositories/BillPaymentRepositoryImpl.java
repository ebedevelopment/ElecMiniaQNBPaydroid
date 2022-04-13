package com.ebe.miniaelec.data.repositories;

import com.ebe.miniaelec.data.database.entities.ClientWithBillData;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.ReportEntity;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.domain.BillPaymentRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.core.Single;

public class BillPaymentRepositoryImpl implements BillPaymentRepository {
    @Override
    public Single<ClientWithBillData> getClientByClientId(String clientId) {
        return null;
    }

    @Override
    public void sendDRM(Boolean isView, JsonObject paraObj, RequestListener listener) {

    }

    @Override
    public Long addTransData(TransDataEntity transData) {
        return null;
    }

    @Override
    public void addReport(ReportEntity report) {

    }

    @Override
    public void updateTransData(TransDataEntity transDataEntity) {

    }

    @Override
    public void billPayment(String InquiryID, int PayType, String ClientMobileNo, String ClientID, JsonArray ModelBillPaymentV, String BankDateTime, String BankReceiptNo, String BankTransactionID, String ClientCreditCard, String AcceptCode, RequestListener listener) {

    }

    @Override
    public void deleteTransBill(long BillUnique) {

    }

    @Override
    public void deleteTransData(TransDataEntity transData) {

    }

    @Override
    public void deleteClientBill(long billUnique) {

    }

    @Override
    public void newTransBillAppend(TransBillEntity transBill) {

    }

    @Override
    public Single<ClientWithBillData> getClientByClientIdForAdapter(String clientId) {
        return null;
    }

    @Override
    public void deleteOfflineClient(OfflineClientEntity client) {

    }

    @Override
    public void cancelBillPayment(String BankTransactionID, RequestListener listener) {

    }
}
