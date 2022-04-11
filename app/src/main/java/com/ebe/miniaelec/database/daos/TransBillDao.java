package com.ebe.miniaelec.database.daos;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ebe.miniaelec.database.entities.TransBillEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TransBillDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void newTransBillAppend(TransBillEntity transBill);

    @Query("Delete From TransBillEntity Where BillUnique = :BillUnique")
    void deleteTransBill(long BillUnique);

    @Query("Delete From TransBillEntity Where transDataId = :transId")
    void deleteTransBillsByTransData(int transId);

    @Query("Select * From TransBillEntity Where transDataId = :transId")
    Single<List<TransBillEntity>> getTransBillsByTransData(int transId);
}
