package com.ebe.miniaelec.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "OfflineClient")
public class OfflineClient {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;



    @ColumnInfo(name = "client_id")
    private String SerialNo;

    private String ClientMobileNo;

    private ArrayList<BillData> ModelBillInquiryV;

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

    public List<BillData> getModelBillInquiryV() {
        return ModelBillInquiryV;
    }

    public void setModelBillInquiryV(ArrayList<BillData> modelBillInquiryV) {
        ModelBillInquiryV = modelBillInquiryV;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }




}
