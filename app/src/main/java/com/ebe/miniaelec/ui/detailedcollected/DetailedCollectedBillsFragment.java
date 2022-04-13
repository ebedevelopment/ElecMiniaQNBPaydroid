package com.ebe.miniaelec.ui.detailedcollected;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.ReportEntity;
import com.ebe.miniaelec.domain.model.DetailedReport;
import com.ebe.miniaelec.ui.adapters.AdapterDetailedCollectedReport;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class DetailedCollectedBillsFragment extends Fragment {


    NavController navController;
    ListView report_list;
    ArrayList<DetailedReport> reports;
    private AppDataBase dataBase;
    private CompositeDisposable disposable;


    public DetailedCollectedBillsFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        reports = new ArrayList<>();
        navController = Navigation.findNavController(requireActivity(),R.id.content);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
               // navController.popBackStack(R.id.mainFragment,false);
                navController.popBackStack(R.id.mainFragment,false);
            }
        });
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detailed_collected_bills, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        report_list = view.findViewById(R.id.report_list);
        dataBase = AppDataBase.getInstance(this.requireActivity());
        disposable = new CompositeDisposable();

        ArrayList<String> dates = new ArrayList<String>();

        disposable.add(dataBase.reportEntityDaoDao().getDistinctCollectedDates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Throwable {

                        dates.addAll(strings);
                        for (String date :
                                dates) {
                            ArrayList<ReportEntity> reportsList = new ArrayList<ReportEntity>();
                            reportsList.addAll(dataBase.reportEntityDaoDao().getReportsByDate(date));
                            for (ReportEntity r :
                                    reportsList) {
                                DetailedReport report = new DetailedReport(date);
                                report.setAmount(r.getTotalAmount());
                                report.setBankTransactionID(r.getBankTransactionID());
                                report.setClientID(r.getClientID());
                                report.setCollectTime(r.getTransTime());
                                report.setPaymentType(getType(r.getPaymentType()));
                                report.setCount(r.getBillsCount());
                                reports.add(report);

                            }
                        }
                        AdapterDetailedCollectedReport adapterBills = new AdapterDetailedCollectedReport(requireActivity(), reports);
                        report_list.setAdapter(adapterBills);
                    }
                },throwable -> {
                    Log.e("DetailedCollected", "onViewCreated: "+throwable.getLocalizedMessage() );
                }));

    }

    private String getType(int type){
        switch (type){
            case 1:
                return  getString(R.string.online_cash);
            case 2:
                return  getString(R.string.card);
            case 3:
                return  getString(R.string.wallet);
            case 4:
                return getString(R.string.offline_cash);
        }
        return "";
    }
}