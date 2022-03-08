package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.CollectedReport;
import com.ebe.miniaelec.model.Report;

import java.util.ArrayList;

public class TotalBillsFragment extends Fragment {

    FragmentManager fm;
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
        View view = inflater.inflate(R.layout.fragment_total_bills, container, false);

        MainActivity.setToolbarVisibility(View.VISIBLE);
        MainActivity.setTitleText(getString(R.string.total_bills));
        MainActivity.setBackAction(1);


        ArrayList<Report> report = new ArrayList<Report>(DBHelper.getInstance(getActivity()).getReports());
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
        ArrayList<BillData> bills = new ArrayList<BillData>(DBHelper.getInstance(getActivity()).getAllOfflineBills());
        totalAmount = 0;
        totalCount = bills.size();
        for (BillData b :
                bills) {
            totalAmount += b.getBillValue();
        }
        TextView tv_remain_amount = view.findViewById(R.id.remain_bills_amount);
        tv_remain_amount.setText(totalAmount + " ج.م");
        TextView tv_remain_count = view.findViewById(R.id.remain_bills_count);
        tv_remain_count.setText(String.valueOf(totalCount));
        return view;
    }

}
