package com.ebe.miniaelec.domain;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

public interface TotalCollectedRepository {


    Single<List<String>> getDistinctCollectedDates();

    Double getTotalAmountOfPaymentTypeAndDate(String date, int pType);

    Integer getTotalCountOfPaymentTypeAndDate(String date, int pType);
}
