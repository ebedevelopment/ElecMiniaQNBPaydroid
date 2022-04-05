package com.ebe.miniaelec.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.AppDataBase;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.model.CollectedReport;
import com.ebe.miniaelec.model.TransData;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class TotalCollectedBillsFragment extends Fragment {

    ListView report_list;
    ArrayList<CollectedReport> reports;
    NavController navController;
    AppDataBase dataBase;
    CompositeDisposable disposable;


    public TotalCollectedBillsFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        reports = new ArrayList<>();
        navController = Navigation.findNavController(requireActivity(),R.id.content);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_total_collected_bills, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        report_list = view.findViewById(R.id.report_list);

        dataBase = AppDataBase.getInstance(this.requireActivity());
disposable = new CompositeDisposable();
        ArrayList<String> dates = new ArrayList<String>();
       disposable.add(dataBase.reportEntityDaoDao().getDistinctCollectedDates().subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<List<String>>() {
                   @Override
                   public void accept(List<String> strings) throws Throwable {
                       dates.addAll(strings);
                       for (String date :
                               dates) {
                           CollectedReport report = new CollectedReport(date);
                           report.setOnlineCashAmount(DBHelper.getInstance(requireActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.CASH.getValue()));
                           report.setOnlineCashCount(DBHelper.getInstance(requireActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.CASH.getValue()));
                           report.setOfflineCashAmount(DBHelper.getInstance(requireActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.OFFLINE_CASH.getValue()));
                           report.setOfflineCashCount(DBHelper.getInstance(requireActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.OFFLINE_CASH.getValue()));
                           report.setCardAmount(DBHelper.getInstance(requireActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.CARD.getValue()));
                           report.setCardCount(DBHelper.getInstance(requireActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.CARD.getValue()));
                           report.setWalletAmount(DBHelper.getInstance(requireActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.WALLET.getValue()));
                           report.setWalletCount(DBHelper.getInstance(requireActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.WALLET.getValue()));
                           reports.add(report);
                       }
                       AdapterTotalCollectedReport adapterBills = new AdapterTotalCollectedReport(requireActivity(), reports);
                       report_list.setAdapter(adapterBills);
                   }
               }));




    }

    @Override
    public void onStop() {
        super.onStop();
        disposable.dispose();
    }
}