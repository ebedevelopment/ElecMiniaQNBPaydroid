package com.ebe.miniaelec.domain;

import androidx.lifecycle.LiveData;

import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.RequestListener;

import java.util.List;

public interface LoginRepository {

    public void login(String userName, String userPassword, final RequestListener listener);

    LiveData<List<TransDataWithTransBill>> getAllTransBills();

    void clearClients();

    void clearBills();
}
