package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.model.CollectedReport;
import com.ebe.miniaelec.model.Report;
import com.ebe.miniaelec.model.TransData;

import java.util.ArrayList;

public class TotalCollectedFragment extends Fragment {

    FragmentManager fm;
    ListView report_list;
    ArrayList<CollectedReport> reports;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reports = new ArrayList<>();
        fm = getFragmentManager();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        MainActivity.setToolbarVisibility(View.VISIBLE);
        MainActivity.setTitleText(getString(R.string.total_collected));
        MainActivity.setBackAction(1);

        report_list = view.findViewById(R.id.report_list);

        ArrayList<String> dates = new ArrayList<String>();
        dates.addAll(DBHelper.getInstance(getActivity()).getDistinctCollectedDates());
        for (String date :
                dates) {
            CollectedReport report = new CollectedReport(date);
            report.setOnlineCashAmount(DBHelper.getInstance(getActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.CASH.getValue()));
            report.setOnlineCashCount(DBHelper.getInstance(getActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.CASH.getValue()));
            report.setOfflineCashAmount(DBHelper.getInstance(getActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.OFFLINE_CASH.getValue()));
            report.setOfflineCashCount(DBHelper.getInstance(getActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.OFFLINE_CASH.getValue()));
            report.setCardAmount(DBHelper.getInstance(getActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.CARD.getValue()));
            report.setCardCount(DBHelper.getInstance(getActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.CARD.getValue()));
            report.setWalletAmount(DBHelper.getInstance(getActivity()).getTotalAmountOfPaymentTypeAndDate(date, TransData.PaymentType.WALLET.getValue()));
            report.setWalletCount(DBHelper.getInstance(getActivity()).getTotalCountOfPaymentTypeAndDate(date, TransData.PaymentType.WALLET.getValue()));
            reports.add(report);
        }
        AdapterTotalCollectedReport adapterBills = new AdapterTotalCollectedReport(getActivity(), reports);
        report_list.setAdapter(adapterBills);

        return view;
    }

}
