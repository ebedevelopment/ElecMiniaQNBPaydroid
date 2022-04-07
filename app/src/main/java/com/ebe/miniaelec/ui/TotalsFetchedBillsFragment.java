package com.ebe.miniaelec.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.AppDataBase;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.database.entities.BillDataEntity;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.Report;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class TotalsFetchedBillsFragment extends Fragment {

    private NavController navController;
    private AppDataBase database;
    private CompositeDisposable disposable;

    public TotalsFetchedBillsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        navController = Navigation.findNavController(requireActivity(),R.id.content);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_totals_fetched_bills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database= AppDataBase.getInstance(this.requireActivity());

        ArrayList<Report> report = new ArrayList<Report>(DBHelper.getInstance(requireActivity()).getReports());
        disposable = new CompositeDisposable();

       disposable.add(database.reportEntityDaoDao().getReports()
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<List<Report>>() {
                   @Override
                   public void accept(List<Report> reports) throws Throwable {
                       double totalAmount = 0;
                       int totalCount = 0;
                       for (Report r :
                               report) {
                           totalAmount += r.getTotalAmount();
                           totalCount += r.getBillsCount();
                       }
                       TextView tv_collected_amount = view.findViewById(R.id.total_collected_amount);
                       tv_collected_amount.setText(totalAmount/100 + " ج.م");
                       TextView tv_collected_count = view.findViewById(R.id.total_collected_count);
                       tv_collected_count.setText(String.valueOf(totalCount));
                   }
               },throwable -> {
                   Log.e("total fetched", "onViewCreated: "+throwable.getLocalizedMessage() );
               }));


        disposable.add(database.billDataDaoDao().getAllBills().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BillDataEntity>>() {
                    @Override
                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                        double totalAmount = 0;
                        int totalCount = billDataEntities.size();

                        for (BillDataEntity b :
                                billDataEntities) {
                            totalAmount += b.getBillValue();
                        }
                        TextView tv_remain_amount = view.findViewById(R.id.remain_bills_amount);
                        tv_remain_amount.setText(totalAmount + " ج.م");
                        TextView tv_remain_count = view.findViewById(R.id.remain_bills_count);
                        tv_remain_count.setText(String.valueOf(totalCount));
                    }
                },throwable -> {
                    Log.e("total fetched", "onViewCreated: "+throwable.getLocalizedMessage() );
                }));


    }

    @Override
    public void onStop() {
        super.onStop();
        disposable.dispose();
    }
}