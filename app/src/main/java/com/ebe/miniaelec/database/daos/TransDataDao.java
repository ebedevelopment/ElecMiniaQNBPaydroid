package com.ebe.miniaelec.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ebe.miniaelec.database.entities.TransDataEntity;
import com.ebe.miniaelec.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.model.TransData;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TransDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long addTransData( TransDataEntity transData);

    @Delete
    void deleteTransData(TransDataEntity transData);


    @Query("Select * From TransData")
    @Transaction
    List<TransDataWithTransBill>getAllTrans();


    @Query("Select * From TransData Where reference_no = :refNo")
    @Transaction
    TransDataWithTransBill getTransByRefNo(int refNo);


    @Query("Select * From TransData Where client_id = :clientId")
    @Transaction
    TransDataWithTransBill getTransByClientId(String clientId);


}
