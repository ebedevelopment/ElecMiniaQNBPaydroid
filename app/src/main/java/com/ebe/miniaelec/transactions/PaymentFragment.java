package com.ebe.miniaelec.transactions;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.Bill;
import com.ebe.miniaelec.ui.HomeFragment;
import com.ebe.miniaelec.ui.MainActivity;

import java.util.ArrayList;

public class PaymentFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    static EditText subscriber_number, customer_name;
    static ListView bills_list;
    static Context cntxt;
    static LinearLayout bill;
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
        View view = inflater.inflate(R.layout.fragment_pay_bills, container, false);

        MainActivity.setToolbarVisibility(View.VISIBLE);
        MainActivity.setTitleText(getString(R.string.pay_bills));
        MainActivity.setBackAction(1);

        bills_list = view.findViewById(R.id.bills);
        subscriber_number = view.findViewById(R.id.subscriber_number);
        customer_name = view.findViewById(R.id.customer_name);
        bill = view.findViewById(R.id.bill);
        bill.setVisibility(View.GONE);
        bills_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                billDetails(billsData.get(position).toString());
            }
        });

        Button cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        Button pay = view.findViewById(R.id.pay);
        pay.setOnClickListener(this);
        return view;
    }

    public static void setBillData(String customerNo, String customerName, String areaName, ArrayList<Bill> bills) {
        billsData = bills;
        bill.setVisibility(View.VISIBLE);
        customer_name.setText(customerName);
        subscriber_number.setText(customerNo);
        //AdapterBills adapterBills = new AdapterBills(cntxt, bills);
        //bills_list.setAdapter(adapterBills);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cancel:
                // Toast.makeText(getActivity(), "تم تحديث القراءة!", Toast.LENGTH_LONG).show();
                MainActivity.fragmentTransaction(new HomeFragment(), null);
                break;
            case R.id.pay:
                AmountDialog();
                //Toast.makeText(getActivity(), getString(R.string.soon), Toast.LENGTH_LONG).show();
                //MainActivity.fragmentTransaction(new HomeFragment());
                break;
        }
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
                        if (input.getText().toString().trim().isEmpty()) {
                            input.setError(getResources().getString(R.string.required_field));

                        } else {
                            int totalPaymentAmount = 0, billsCount = 0;
                            for (int i = 0; i < billsData.size(); i++) {
                                if (totalPaymentAmount + Integer.parseInt(billsData.get(i).getAmount()) <= Integer.parseInt(input.getText().toString())) {
                                    totalPaymentAmount += Integer.parseInt(billsData.get(i).getAmount());
                                    billsCount += 1;
                                } else break;
                            }
                            paymentDialog(totalPaymentAmount, billsCount);
                            dialog.cancel();
                        }

                    }
                });
        alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void paymentDialog(int totalPaymentAmount, int billsCount) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage("المبلغ الصحيح: "+ totalPaymentAmount + "\n الفواتير: من شهر "+ billsData.get(0).getMonth() + " الى شهر "
                + billsData.get(billsCount-1).getMonth() );
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
        alertDialog.setPositiveButton(getString(R.string.done),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }
}
