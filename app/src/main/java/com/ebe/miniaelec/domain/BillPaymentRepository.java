package com.ebe.miniaelec.domain;

import com.ebe.miniaelec.data.database.entities.ClientWithBillData;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.ReportEntity;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.http.RequestListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.core.Single;

public interface BillPaymentRepository {

    Single<ClientWithBillData> getClientByClientId(String clientId);

    public void sendDRM(Boolean isView, final JsonObject paraObj, final RequestListener listener);

    Long addTransData( TransDataEntity transData);

    void addReport(ReportEntity report);

    void updateTransData(TransDataEntity transDataEntity);

    public void billPayment(String InquiryID, int PayType, String ClientMobileNo, String ClientID, final JsonArray ModelBillPaymentV,
                            String BankDateTime, String BankReceiptNo, String BankTransactionID,
                            String ClientCreditCard, String AcceptCode, final RequestListener listener);

    void deleteTransBill(long BillUnique);

    void deleteTransData(TransDataEntity transData);

    void deleteClientBill(long billUnique);

    void newTransBillAppend(TransBillEntity transBill);

    Single<ClientWithBillData> getClientByClientIdForAdapter(String clientId);

    void deleteOfflineClient( OfflineClientEntity client);

    public void cancelBillPayment(String BankTransactionID, final RequestListener listener);
}
