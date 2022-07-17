package com.ebe.miniaelec.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "deducts")
public class DeductType {
    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false)
    private int id = 0;
    @SerializedName("KTID")
    @DatabaseField(columnName = "deduct_id")
    private int deductId;
    @DatabaseField(columnName = "deduct_type")
    @SerializedName("KTName")
    private String deductType;

    public DeductType() {
    }

    public DeductType(int deductId, String deductType) {
        this.deductId = deductId;
        this.deductType = deductType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeductId() {
        return deductId;
    }

    public void setDeductId(int deductId) {
        this.deductId = deductId;
    }

    public String getDeductType() {
        return deductType;
    }

    public void setDeductType(String deductType) {
        this.deductType = deductType;
    }
}
