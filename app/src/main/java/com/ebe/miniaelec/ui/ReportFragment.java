package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.Report;

import java.util.ArrayList;

public class ReportFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    EditText current_meter;
    ListView report_list;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fm = getFragmentManager();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        MainActivity.setToolbarVisibility(View.VISIBLE);
        MainActivity.setTitleText(getString(R.string.today_report));
        MainActivity.setBackAction(1);

        report_list = view.findViewById(R.id.report_list);

        ArrayList<Report> report = new ArrayList<>();
       /* report.add(new Report("1234567890123456", "100", "كاش", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كارت", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كاش", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كارت", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كاش", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كارت", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كاش", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كارت", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كاش", "من شهر 06/2019 لشهر 09/2019"));
        report.add(new Report("1234567890123456", "100", "كارت", "من شهر 06/2019 لشهر 09/2019"));
*/
        AdapterReport adapterBills = new AdapterReport(getActivity(), report);
        report_list.setAdapter(adapterBills);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


        }
    }

}
