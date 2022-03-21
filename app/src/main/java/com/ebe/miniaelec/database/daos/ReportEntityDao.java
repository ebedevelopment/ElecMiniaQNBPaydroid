package com.ebe.miniaelec.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ebe.miniaelec.database.entities.ReportEntity;
import com.ebe.miniaelec.model.Report;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface ReportEntityDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addReport(ReportEntity report);

    @Query("Select DISTINCT transDate From Reports")
    Flowable<List<String>> getDistinctCollectedDates();

    @Query("Select SUM(totalAmount)/100 From Reports Where transDate = :date and paymentType = :pType")
    double getTotalAmountOfPaymentTypeAndDate(String date, int pType);

    @Query("Select SUM(billsCount) From Reports Where transDate = :date and paymentType = :pType")
    int getTotalCountOfPaymentTypeAndDate(String date, int pType);

    @Query("Select * From Reports Where transDate =:date")
    Flowable<List<Report>> getReportsByDate(String date);

    @Query("Select * From Reports")
    Flowable<List<Report>> getReports();

    @Query("Delete From REPORTS")
    void clearReports();


    //void clearReportsByDate(String date);
}
