package com.ebe.miniaelec.ui.main;

import android.app.Application;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.lang.reflect.InvocationTargetException;

import io.reactivex.rxjava3.annotations.NonNull;

public class MainViewModelFactory implements ViewModelProvider.Factory {

    private final Application app;

    public MainViewModelFactory(Application app) {

        this.app = app;
    }

    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> mainViewModel)
    {

        try
          {
              return mainViewModel.getConstructor(Application.class).newInstance(app);

          }
        catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Can't create instance of MainViewModel" +e);
        }
    }
}
