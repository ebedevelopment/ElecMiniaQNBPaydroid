package com.ebe.miniaelec.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.repositories.MainRepositoryImpl;
import com.ebe.miniaelec.domain.MainRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainFragmentViewModel extends AndroidViewModel {

   public Integer mntka,day,main,fary = 0;
   MutableLiveData<List<String>> mntkas = new MutableLiveData<>(new ArrayList<>());
   MutableLiveData<List<BillDataEntity>> offlineBills = new MutableLiveData<>(new ArrayList<>());
    private AppDataBase dataBase;
    private ApiServices services;
    private MainRepository repository;
   private CompositeDisposable disposable;



    public MainFragmentViewModel(@NonNull Application application) {
        super(application);
        this.dataBase = AppDataBase.getInstance(application);
        this.services = new ApiServices(application,false);
        this.repository = new MainRepositoryImpl(dataBase,services);
        disposable = new CompositeDisposable();
    }

    public void saveFilterParams(Integer mntka,Integer day,Integer main,Integer fary)
    {
        this.mntka = mntka;
        this.main = main;
        this.day = day;
        this.fary = fary;

    }

    public void getMntkaList()
    {
       disposable.add(repository.getDistinctMntka()
               .subscribe(new Consumer<List<String>>() {
                   @Override
                   public void accept(List<String> strings) throws Throwable {
                       if (strings != null)

                           mntkas.setValue(strings);

                   }
               },throwable -> {
                   Log.e("getDistinctMntka", "onViewCreated: "+throwable.getLocalizedMessage() );
               }));
    }


    public void filterByMantka(String mntka)
    {
       disposable.add(repository.getDistinctBillsOfMntka(mntka).subscribe(new Consumer<List<BillDataEntity>>() {
           @Override
           public void accept(List<BillDataEntity> billDataEntities) throws Throwable {

               offlineBills.setValue(billDataEntities);
           }
       }));
    }

    public LiveData<PagingData<BillDataEntity>> getPagedBillsData()
    {
        Pager<Integer,BillDataEntity> pager = new Pager<>(new PagingConfig(50, 20, false, 80, PagingConfig.MAX_SIZE_UNBOUNDED), () -> repository.getPagedBills());
        return PagingLiveData.getLiveData(pager);



    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }
}
