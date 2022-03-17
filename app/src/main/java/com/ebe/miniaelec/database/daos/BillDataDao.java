package com.ebe.miniaelec.database.daos;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ebe.miniaelec.database.entities.BillDataEntity;
import com.ebe.miniaelec.model.BillData;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;


@Dao
public interface BillDataDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long newOfflineBillAppend(BillDataEntity bill);

    @Update
    void updateOfflineBill(BillDataEntity bill);


    @Query("Delete FROM BillDataEntity WHERE billUnique = :billUnique")
    void deleteClientBill(String billUnique);


    @Query("Select * From BillDataEntity")
    Flowable<List<BillDataEntity>> getAllBills();


    @Query("SElECT DISTINCT mntkaCode From BillDataEntity")
    Flowable<List<String>> getDistinctMntka();

    @Query("Select DISTINCT dayCode From BillDataEntity Where mntkaCode = :mntka ")
    Flowable<List<String>> getDistinctDaysOfMntka(String mntka);

    @Query("Select DISTINCT mainCode From BillDataEntity Where mntkaCode = :mntka and dayCode = :day")
    Flowable<List<String>> getDistinctMainsOfMntkaAndDay(String mntka, String day);

    @Query("Select DISTINCT faryCode From BillDataEntity Where mntkaCode = :mntka and dayCode = :day and mainCode = :main")
    Flowable<List<String>> getDistinctFaryOfMntkaAndDayAndMain(String mntka, String day, String main);

    //@Query()
    Flowable<List<BillData>> getDistinctBills();
}
