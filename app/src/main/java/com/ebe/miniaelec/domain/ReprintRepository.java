package com.ebe.miniaelec.domain;

import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.RequestListener;

import io.reactivex.rxjava3.core.Single;

public interface ReprintRepository {

    Single<TransDataWithTransBill> getTransByClientId(String clientId);

    void updateTransData(TransDataEntity transDataEntity);

    public void rePrint(String ClientID, final RequestListener listener);
}
