package com.ebe.miniaelec.domain;

import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.ReportEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface TotalFetchedRepository {

    Single<List<ReportEntity>> getReports();

    Single<List<BillDataEntity>> getAllBills();
}
