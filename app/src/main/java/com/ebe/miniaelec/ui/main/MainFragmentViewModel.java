package com.ebe.miniaelec.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ebe.miniaelec.data.database.AppDataBase;
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


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.dispose();
    }
}
