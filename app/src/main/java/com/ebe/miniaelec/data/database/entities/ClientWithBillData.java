package com.ebe.miniaelec.data.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ClientWithBillData {

    @Embedded
    OfflineClientEntity client;

    @Relation(
            parentColumn = "client_id",
            entityColumn = "clientId"
    )
    List<BillDataEntity> Bills;

    public OfflineClientEntity getClient() {
        return client;
    }

    public void setClient(OfflineClientEntity client) {
        this.client = client;
    }

    public List<BillDataEntity> getBills() {
        return Bills;
    }

    public void setBills(List<BillDataEntity> bills) {
        Bills = bills;
    }
}
