package com.ebe.miniaelec.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//@Database()
public abstract class AppDataBase extends RoomDatabase {


    private static AppDataBase dataBase;

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
                            AppDataBase.class, "AppDataBase").build();
                }
            }
        }

        return dataBase;
    }
}
