package com.ebe.miniaelec.data.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ebe.miniaelec.data.database.entities.DeductType;

@Dao
public interface DeductsDao {

    @Query("Delete From deducts")
    void clearDeducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addDeductType(DeductType deductType);
}
