package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.model.DetailedReport;
import com.ebe.miniaelec.model.Report;

import java.util.ArrayList;

public class DetailedCollectedFragment extends Fragment {

    FragmentManager fm;
    ListView report_list;
    ArrayList<DetailedReport> reports;
    Spinner fromSpinneer;
    Spinner toSpinner;
    int startDay=1,endDay=1;
    Button showReportsButton;


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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fromSpinneer = (Spinner)view.findViewById(R.id.from_spinner);
        toSpinner = (Spinner)view.findViewById(R.id.to_Spinner);
        showReportsButton = view.findViewById(R.id.report_filter_button);
        spinnerSetup();
        addSpinnerClickListener();

        showReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getReportsByDateRange();
            }
        });


    }

    private void getReportsByDateRange() {
    }


    void spinnerSetup()
    {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(),
                R.array.days, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        fromSpinneer.setAdapter(adapter);
        toSpinner.setAdapter(adapter);
        fromSpinneer.setSelection(0);
        fromSpinneer.setSelection(0);

    }


    void addSpinnerClickListener()
    {
        fromSpinneer.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                startDay = (int) adapterView.getItemAtPosition(i);
            }
        }


        );


        toSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                endDay = (int) adapterView.getItemAtPosition(i);
            }
        });
    }


}
