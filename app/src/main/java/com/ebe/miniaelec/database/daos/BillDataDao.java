package com.ebe.miniaelec.database.daos;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ebe.miniaelec.database.entities.BillData;

@Dao
public interface BillDataDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long newOfflineBillAppend(BillData bill);

    @Update
    void updateOfflineBill(BillData bill);


    @Query("Delete From BillData Where billUnique = :BillUnique")
    void deleteClientBill(String BillUnique);
}
