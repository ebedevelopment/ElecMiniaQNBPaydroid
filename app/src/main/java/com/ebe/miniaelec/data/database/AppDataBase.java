package com.ebe.miniaelec.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ebe.miniaelec.data.database.daos.BillDataDao;
import com.ebe.miniaelec.data.database.daos.DeductsDao;
import com.ebe.miniaelec.data.database.daos.OfflineClientsDao;
import com.ebe.miniaelec.data.database.daos.ReportEntityDao;
import com.ebe.miniaelec.data.database.daos.TransBillDao;
import com.ebe.miniaelec.data.database.daos.TransDataDao;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.DeductType;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.ReportEntity;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;


@Database(entities = {BillDataEntity.class, OfflineClientEntity.class, ReportEntity.class, TransBillEntity.class, TransDataEntity.class, DeductType.class}, version = 2)
public abstract class AppDataBase extends RoomDatabase {



    private volatile static AppDataBase dataBase;

    public abstract BillDataDao billDataDaoDao();
    public abstract OfflineClientsDao offlineClientsDao();
   public abstract TransBillDao transBillDao();
   public abstract TransDataDao transDataDao();
    public abstract ReportEntityDao reportEntityDaoDao();
    public abstract DeductsDao deductsDao();



    public static AppDataBase getInstance(Context cntxt)
    {
        if (dataBase==null)
        {
            synchronized (AppDataBase.class){
                if (dataBase==null)
                {
                    dataBase = Room.databaseBuilder(cntxt,
                            AppDataBase.class, "AppDataBase").fallbackToDestructiveMigration().
                            allowMainThreadQueries().
                            build();
                }
            }
        }

        return dataBase;
    }
}
