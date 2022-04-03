package com.ebe.miniaelec.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ebe.miniaelec.database.entities.ReportEntity;
import com.ebe.miniaelec.model.Report;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface ReportEntityDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addReport(ReportEntity report);

    @Query("Select DISTINCT transDate From Reports")
    Single<List<String>> getDistinctCollectedDates();

    @Query("Select SUM(totalAmount)/100 From Reports Where transDate = :date and paymentType = :pType")
    Single<Double> getTotalAmountOfPaymentTypeAndDate(String date, int pType);

    @Query("Select SUM(billsCount) From Reports Where transDate = :date and paymentType = :pType")
    Single<Integer> getTotalCountOfPaymentTypeAndDate(String date, int pType);

    @Query("Select * From Reports Where transDate =:date")
    Single<List<Report>> getReportsByDate(String date);

    @Query("Select * From Reports")
    Single<List<Report>> getReports();

    @Query("Delete From REPORTS")
   Single<Integer> clearReports();


    //void clearReportsByDate(String date);
}
