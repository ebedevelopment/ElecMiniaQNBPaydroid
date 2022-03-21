package com.ebe.miniaelec.database.daos;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ebe.miniaelec.database.entities.ClientWithBillData;
import com.ebe.miniaelec.database.entities.OfflineClientEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface OfflineClientsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addOfflineClient( OfflineClientEntity client);

    @Query("Select Count(*) From OfflineClient")
    long offlineClientsCount();

    @Delete
    void deleteOfflineClient( OfflineClientEntity client);

    @Transaction
    @Query("Select * From OfflineClient")
    Flowable<List<ClientWithBillData>> getAllOfflineClients();


    @Transaction
    @Query("Select * From OfflineClient Where client_id = :clientId")
    Single<ClientWithBillData> getClientByClientId(String clientId);
}
