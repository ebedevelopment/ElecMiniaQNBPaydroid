package com.ebe.miniaelec.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;

@DatabaseTable(tableName = "offlineclients")
public class OfflineClient implements Serializable {
    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id = 0;
    @DatabaseField(unique = true, columnName = "client_id")
    private String SerialNo;
    @DatabaseField
    private String ClientMobileNo;
    //@DatabaseField(dataType = DataType.SERIALIZABLE)
    @ForeignCollectionField
    private ForeignCollection<BillData> ModelBillInquiryV;


    public OfflineClient() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public ArrayList<BillData> getModelBillInquiryV() {
        return new ArrayList<>(ModelBillInquiryV);
    }

    public void setModelBillInquiryV(ForeignCollection<BillData> modelBillInquiryV) {
        ModelBillInquiryV = modelBillInquiryV;
    }
}
