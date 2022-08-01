package com.ebe.miniaelec.data.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface TransDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long addTransData( TransDataEntity transData);

    @Update(onConflict = OnConflictStrategy.REPLACE )
    void updateTransData(TransDataEntity transDataEntity);

    @Delete
    void deleteTransData(TransDataEntity transData);


    @Query("Select * From TransData")
    @Transaction
    Single<List<TransDataWithTransBill>> getAllTrans();

    @Query("Select * From TransData")
    @Transaction
    LiveData<List<TransDataWithTransBill>> getAllTransBills();


    @Query("Select * From TransData Where reference_no = :refNo")
    @Transaction
    Single<TransDataWithTransBill> getTransByRefNo(int refNo);


    @Query("Select * From TransData Where client_id = :clientId")
    @Transaction
    Single<TransDataWithTransBill> getTransByClientId(String clientId);


}
