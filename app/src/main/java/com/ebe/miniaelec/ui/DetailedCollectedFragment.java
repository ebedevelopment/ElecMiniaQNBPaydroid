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
import com.ebe.miniaelec.model.DetailedReport;
import com.ebe.miniaelec.model.Report;

import java.util.ArrayList;

public class DetailedCollectedFragment extends Fragment {

    FragmentManager fm;
    ListView report_list;
    ArrayList<DetailedReport> reports;


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
        MainActivity.setTitleText(getString(R.string.collected_detailed));
        MainActivity.setBackAction(1);

        report_list = view.findViewById(R.id.report_list);

        ArrayList<String> dates = new ArrayList<String>();
        dates.addAll(DBHelper.getInstance(getActivity()).getDistinctCollectedDates());
        for (String date :
                dates) {
            ArrayList<Report> reportsList = new ArrayList<Report>();
            reportsList.addAll(DBHelper.getInstance(getActivity()).getReports(date));
            for (Report r :
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
        AdapterDetailedCollectedReport adapterBills = new AdapterDetailedCollectedReport(getActivity(), reports);
        report_list.setAdapter(adapterBills);

        return view;
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
