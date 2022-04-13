package com.ebe.miniaelec.domain;

import com.ebe.miniaelec.data.database.entities.ReportEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface DetailedColletedRepository {

    Single<List<String>> getDistinctCollectedDates();
    List<ReportEntity> getReportsByDate(String date);
}
