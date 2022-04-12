package com.ebe.miniaelec.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "OfflineClient")
public class OfflineClientEntity {

    @PrimaryKey(autoGenerate = true)
    private long id = 0;



    @ColumnInfo(name = "client_id")
    private String SerialNo;

    private String ClientMobileNo;


    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public String getClientMobileNo() {
        return ClientMobileNo;
    }

    public void setClientMobileNo(String clientMobileNo) {
        ClientMobileNo = clientMobileNo;
    }


    public long getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }




}
