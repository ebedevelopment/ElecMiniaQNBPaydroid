package com.ebe.miniaelec.data.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TransDataWithTransBill {

    @Embedded
    TransDataEntity transData;

    @Relation(
            parentColumn = "client_id",
            entityColumn = "transDataId"
    )
    List<TransBillEntity> transBills;

    public TransDataEntity getTransData() {
        return transData;
    }

    public void setTransData(TransDataEntity transData) {
        this.transData = transData;
    }

    public List<TransBillEntity> getTransBills() {
        return transBills;
    }

    public void setTransBills(List<TransBillEntity> transBills) {
        this.transBills = transBills;
    }
}
