package com.ebe.miniaelec.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ebe.miniaelec.database.daos.BillDataDao;
import com.ebe.miniaelec.database.daos.OfflineClientsDao;
import com.ebe.miniaelec.database.daos.ReportEntityDao;
import com.ebe.miniaelec.database.daos.TransBillDao;
import com.ebe.miniaelec.database.daos.TransDataDao;
import com.ebe.miniaelec.database.entities.BillDataEntity;
import com.ebe.miniaelec.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.database.entities.ReportEntity;
import com.ebe.miniaelec.database.entities.TransBillEntity;
import com.ebe.miniaelec.database.entities.TransDataEntity;


@Database(entities = {BillDataEntity.class, OfflineClientEntity.class, ReportEntity.class, TransBillEntity.class, TransDataEntity.class}, version = 0)
public abstract class AppDataBase extends RoomDatabase {



    private volatile static AppDataBase dataBase;

    public abstract BillDataDao billDataDaoDao();
    public abstract OfflineClientsDao offlineClientsDao();
   public abstract TransBillDao transBillDao();
   public abstract TransDataDao transDataDao();
    public abstract ReportEntityDao reportEntityDaoDao();



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
