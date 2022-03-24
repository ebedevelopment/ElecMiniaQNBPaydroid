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
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.model.CollectedReport;
import com.ebe.miniaelec.model.TransData;

import java.util.ArrayList;


public class TotalCollectedBillsFragment extends Fragment {

    ListView report_list;
    ArrayList<CollectedReport> reports;
    NavController navController;


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

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // navController.popBackStack(R.id.mainFragment,false);
                navController.popBackStack(R.id.mainFragment,false);
            }
        });
        report_list = view.findViewById(R.id.report_list);

        ArrayList<String> dates = new ArrayList<String>();
        dates.addAll(DBHelper.getInstance(requireActivity()).getDistinctCollectedDates());
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
}