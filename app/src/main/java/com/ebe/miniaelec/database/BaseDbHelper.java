package com.ebe.miniaelec.database;/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-17
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.DeductType;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.model.Report;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * DB helper
 */
public class BaseDbHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DB";
    // DB Name
    private static final String DATABASE_NAME = "data.db";
    // DB version
    private static final int DATABASE_VERSION = 10;

    private static BaseDbHelper instance;


    private BaseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, TransData.class);
            TableUtils.createTableIfNotExists(connectionSource, TransBill.class);
            TableUtils.createTableIfNotExists(connectionSource, OfflineClient.class);
            TableUtils.createTableIfNotExists(connectionSource, BillData.class);
            TableUtils.createTableIfNotExists(connectionSource, Report.class);
            TableUtils.createTableIfNotExists(connectionSource, DeductType.class);
        } catch (SQLException e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVersion,
                          int newVersion) {
        try {
            for (int i = oldVersion; i < newVersion; ++i) {
//                 TableUtils.dropTable(connectionSource, Report.class, true);
               /* TableUtils.dropTable(connectionSource, TransData.class, true);
//                if (oldVersion > 6)
                TableUtils.dropTable(connectionSource, TransBill.class, true);
                TableUtils.dropTable(connectionSource, OfflineClient.class, true);
                TableUtils.dropTable(connectionSource, BillData.class, true);
                TableUtils.dropTable(connectionSource, Report.class, true);
                TableUtils.dropTable(connectionSource, DeductType.class, true);*/
                onCreate(sqliteDatabase, connectionSource);
            }
          /*  if (oldVersion < 3) {

                Dao<Product, Integer> dao = instance.getRuntimeExceptionDao (Product.class);
                // to add new column to existing table
                dao.executeRaw("ALTER TABLE `tableName` ADD COLUMN columnName dataType;");

//                dao.executeRaw("ALTER TABLE `tableName` ADD COLUMN columnName dataType DEFAULT 0;");
//                dao.updateRaw("UPDATE `tableName` SET columnName = 1 WHERE columnName2 > 0;");
            }*/
        } catch (/*SQLException*/Exception e) {
            Log.e(TAG, "Unable to upgrade database from version " + oldVersion + " to new "
                    + newVersion, e);
        }
    }

    public void clearOfflineData() {
        try {
            TableUtils.dropTable(connectionSource, OfflineClient.class, true);
            TableUtils.dropTable(connectionSource, BillData.class, true);
            TableUtils.createTableIfNotExists(connectionSource, OfflineClient.class);
            TableUtils.createTableIfNotExists(connectionSource, BillData.class);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean deleteReports() {
        try {
            TableUtils.dropTable(connectionSource, Report.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Report.class);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean clearDeducts() {
        try {
            TableUtils.dropTable(connectionSource, DeductType.class, true);
            TableUtils.createTableIfNotExists(connectionSource, DeductType.class);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    /**
     * get the Singleton of the DB Helper
     *
     * @return the Singleton of DB helper
     */
    public static synchronized BaseDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BaseDbHelper(context);
        }

        return instance;
    }
}
