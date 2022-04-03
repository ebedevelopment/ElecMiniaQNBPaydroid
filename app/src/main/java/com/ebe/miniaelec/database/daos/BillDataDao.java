package com.ebe.miniaelec.database.daos;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.ebe.miniaelec.database.entities.BillDataEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;


@Dao
public interface BillDataDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void newOfflineBillAppend(BillDataEntity bill);

    @Update
    void updateOfflineBill(BillDataEntity bill);


    @Query("Delete FROM BillDataEntity WHERE billUnique = :billUnique")
    void deleteClientBill(String billUnique);

    @Query("Delete From BillDataEntity Where clientId = :Id")
    void deleteBillByClientId(String Id);

    @Query("Select * From BillDataEntity Where clientId = :Id")
    Single<List<BillDataEntity>> getBillsByClientId(String Id);


    @Query("Select * From BillDataEntity")
    Single<List<BillDataEntity>> getAllBills();


    @Query("SElECT DISTINCT mntkaCode From BillDataEntity")
    Single<List<String>> getDistinctMntka();

    @Query("Select DISTINCT dayCode From BillDataEntity Where mntkaCode = :mntka ")
    Single<List<String>> getDistinctDaysOfMntka(String mntka);

    @Query("Select DISTINCT mainCode From BillDataEntity Where mntkaCode = :mntka and dayCode = :day")
    Single<List<String>> getDistinctMainsOfMntkaAndDay(String mntka, String day);

    @Query("Select DISTINCT faryCode From BillDataEntity Where mntkaCode = :mntka and dayCode = :day and mainCode = :main")
   Single<List<String>> getDistinctFaryOfMntkaAndDayAndMain(String mntka, String day, String main);

    @Query("Select * From BillDataEntity Group by faryCode ,clientName, mainCode,clientId Order by mainCode Asc  ")
    Single<List<BillDataEntity>> getDistinctBills();


    @Query("Select * From BillDataEntity Where mntkaCode = :mntka Group by faryCode ,clientName, mainCode,clientId")
    Single<List<BillDataEntity>> getDistinctBillsOfMntka(String mntka);


    @Query("Select * From BillDataEntity Where mntkaCode =:mntka and dayCode =:day Group by faryCode ,clientName, mainCode,clientId Order by mainCode Asc")
    Single<List<BillDataEntity>> getDistinctBillsByMntkaAndDay(String mntka, String day);

    @Query("Select * From BillDataEntity Where mntkaCode =:mntka and dayCode =:day and mainCode =:main Group by faryCode ,clientName, mainCode,clientId Order by mainCode Asc")
    Single<List<BillDataEntity>> getDistinctBillsByMntkaDayAndMain(String mntka, String day, String main);

    @Query("Select * From BillDataEntity Where mntkaCode =:mntka and dayCode =:day and mainCode =:main and faryCode = :fary Group by faryCode ,clientName, mainCode,clientId Order by mainCode Asc")
    Single<List<BillDataEntity>> getDistinctBillsByMntkaDayMainAndFary(String mntka, String day, String main, String fary);


    @Query("Select * From BillDataEntity Where clientName = :clientName Group by faryCode ,clientName, mainCode,clientId Order by mainCode Asc")
    Single<List<BillDataEntity>> getDistinctBillsByClientName(String clientName);

    @Query("Delete From BillDataEntity")
    void clearBills();


}
