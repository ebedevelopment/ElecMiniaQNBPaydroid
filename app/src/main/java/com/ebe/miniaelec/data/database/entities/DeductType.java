package com.ebe.miniaelec.data.database.entities;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "deducts")
public class DeductType {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    @SerializedName("KTID")
    @ColumnInfo(name = "deduct_id")
    private int deductId;
    @ColumnInfo(name = "deduct_type")
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
