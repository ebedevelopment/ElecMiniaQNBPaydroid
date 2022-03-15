package com.ebe.miniaelec.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.ebe.miniaelec.model.BillData;

import java.util.ArrayList;
import java.util.List;

@Entity
public class OfflineClient {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    @ColumnInfo(name = "client_id")
    private String SerialNo;

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

    private String ClientMobileNo;

    private ArrayList<BillData> ModelBillInquiryV;
}
