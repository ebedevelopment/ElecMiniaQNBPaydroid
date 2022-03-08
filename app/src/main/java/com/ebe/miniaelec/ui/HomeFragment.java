package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.transactions.InquiryFragment;

public class HomeFragment extends Fragment implements View.OnClickListener  {

    FragmentManager fm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setBackAction(0);
        MainActivity.setTitleText(getString(R.string.get_building_data));
        fm = getFragmentManager();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        LinearLayout payBills = view.findViewById(R.id.btn_pay_bills);
        payBills.setOnClickListener(this);
        LinearLayout payBuildingBills = view.findViewById(R.id.btn_pay_building_bills);
        payBuildingBills.setOnClickListener(this);
        LinearLayout readCounter = view.findViewById(R.id.btn_read);
        readCounter.setOnClickListener(this);
        LinearLayout reports = view.findViewById(R.id.btn_reports);
        reports.setOnClickListener(this);
        LinearLayout otherServices = view.findViewById(R.id.btn_other);
        otherServices.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Fragment fragment;
        Bundle bundle = new Bundle();
        switch (v.getId()){
            case R.id.btn_pay_bills:
                fragment = new InquiryFragment();
                bundle.putInt("action", 1);
                fragment.setArguments(bundle);
                MainActivity.fragmentTransaction(fragment, null);
                break;
            case R.id.btn_pay_building_bills:
                fragment = new InquiryFragment();
                bundle.putInt("action", 3);
                fragment.setArguments(bundle);
                MainActivity.fragmentTransaction(fragment, null);
                break;
            case R.id.btn_read:
                fragment = new InquiryFragment();
                bundle.putInt("action", 2);
                fragment.setArguments(bundle);
                MainActivity.fragmentTransaction(fragment, null);
                break;
            case R.id.btn_reports:
               // MainActivity.fragmentTransaction(new ReportFragment(), null);
                break;
            case R.id.btn_other:
                Toast.makeText(getActivity(), getString(R.string.soon), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
