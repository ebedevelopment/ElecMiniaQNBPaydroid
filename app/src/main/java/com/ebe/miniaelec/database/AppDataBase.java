package com.ebe.miniaelec.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ebe.miniaelec.database.daos.BillDataDao;
import com.ebe.miniaelec.database.daos.OfflineClientsDao;
import com.ebe.miniaelec.database.daos.TransBillDao;
import com.ebe.miniaelec.database.daos.TransDataDao;
import com.ebe.miniaelec.database.entities.BillData;
import com.ebe.miniaelec.database.entities.OfflineClient;
import com.ebe.miniaelec.database.entities.Report;
import com.ebe.miniaelec.database.entities.TransBill;
import com.ebe.miniaelec.database.entities.TransData;

@Database(entities = {BillData.class, OfflineClient.class, Report.class, TransBill.class, TransData.class}, version = 0)
public abstract class AppDataBase extends RoomDatabase {


    private static AppDataBase dataBase;

    public abstract BillDataDao billDataDaoDao();
    public abstract OfflineClientsDao offlineClientsDao();
    abstract TransBillDao transBillDao();
    abstract TransDataDao transDataDao();

    private AppDataBase(){

    };

    public static AppDataBase getInstance(Context cntxt)
    {
        if (dataBase==null)
        {
            synchronized (AppDataBase.class){
                if (dataBase==null)
                {
                    dataBase = Room.databaseBuilder(cntxt,
                            AppDataBase.class, "AppDataBase").fallbackToDestructiveMigration().
                            build();
                }
            }
        }

        return dataBase;
    }
}
