package com.ebe.miniaelec.database.daos;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ebe.miniaelec.database.entities.OfflineClientEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface OfflineClientsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    int addOfflineClient( OfflineClientEntity client);

    @Query("Select Count(*) From OfflineClient")
    long offlineClientsCount();

    @Delete
    void deleteOfflineClient( OfflineClientEntity client);

    @Query("Select * From OfflineClient")
    Flowable<List<OfflineClientEntity>> getAllOfflineClients();
}
