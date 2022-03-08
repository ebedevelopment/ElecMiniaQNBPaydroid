package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.Bill;

import java.util.ArrayList;

public class BuildingBillsFragment extends Fragment {

    FragmentManager fm;
    ListView bills_list;
    Context cntxt;
    static ArrayList<Bill> billsData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cntxt = getActivity();
        fm = getFragmentManager();
        billsData = new ArrayList<>();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_building_bills, container, false);

        MainActivity.setTitleText(getString(R.string.building_data));
        MainActivity.setBackAction(1);

        bills_list = view.findViewById(R.id.bills);
        bills_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              /*  //billDetails(billsData.get(position).toString());
                new ApiServices(getActivity()).getCustomerBillsData(billsData.get(position).getCustomerNo());
                MainActivity.fragmentTransaction(new PaymentFragment());*/

            //    MainActivity.fragmentTransaction(new UnitBillsPaymentFragment(), null);
            }
        });


        billsData.add(new Bill("010203040506070", "المشترك الأول", 4));
        billsData.add(new Bill("010203040506071", "المشترك الثاني", 2));
        billsData.add(new Bill("010203040506072", "المشترك الثالث", 0));
        billsData.add(new Bill("010203040506073", "المشترك الرابع", 7));
        billsData.add(new Bill("010203040506074", "المشترك الخامس", 1));
        billsData.add(new Bill("010203040506075", "المشترك السادس", 2));
        billsData.add(new Bill("010203040506076", "المشترك السابع", 0));
        setBillData(billsData);
        return view;
    }

    public void setBillData(ArrayList<Bill> bills) {
        billsData = bills;
        AdapterBuildingBills adapterBills = new AdapterBuildingBills(cntxt, bills);
        bills_list.setAdapter(adapterBills);
    }


    private void AmountDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(getString(R.string.enter_amount));
        final EditText input = new EditText(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(params);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_CLASS_NUMBER);
        input.setHint(getString(R.string.invoice_amount));
        alertDialog.setView(input);
        alertDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       /* if (input.getText().toString().trim().isEmpty()) {
                            input.setError(context.getResources().getString(R.string.error_field_required));

                        } else */

                        paymentDialog();
                        dialog.cancel();

                    }
                });
        alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void paymentDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage("المبلغ الصحيح: 1400\n الفواتير: من شهر 05/19 الى شهر 07/19");
        alertDialog.setPositiveButton(getString(R.string.cash),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "تم الدفع نقداَ!", Toast.LENGTH_LONG).show();
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        dialog.cancel();
                    }
                });
        alertDialog.setNegativeButton(getString(R.string.card),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "تم الدفع بالكارت!", Toast.LENGTH_LONG).show();
                        MainActivity.fragmentTransaction(new HomeFragment(), null);
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void billDetails(String bill) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(bill);
        alertDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
