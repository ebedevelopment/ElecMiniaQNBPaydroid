package com.ebe.miniaelec.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.print.PrintListener;
import com.ebe.miniaelec.print.PrintReceipt;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class ReprintFragment extends Fragment implements View.OnClickListener {

    EditText et_clientID;
    NavController navController;

    public ReprintFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        requireActivity().getResources().updateConfiguration(config,
                requireActivity().getResources().getDisplayMetrics());

        navController = Navigation.findNavController(requireActivity(),R.id.content);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // navController.popBackStack(R.id.mainFragment,false);
                navController.navigate(R.id.mainFragment);
            }
        });
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reprint, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        et_clientID = view.findViewById(R.id.client_id);
        et_clientID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 10) {
                    InputMethodManager inputManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.reprint).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reprint) {
            if (et_clientID.getText().toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "أدخل رقم الاشتراك!", Toast.LENGTH_SHORT).show();
            } else {
                reprint();
            }
        }
    }


        private void reprint() {
            final TransData transData = DBHelper.getInstance(requireContext()).getTransByClientId(et_clientID.getText().toString().trim());
            if (transData != null && transData.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue()) {
                if (transData.getPrintCount() == 2) {
                    Toast.makeText(requireActivity(), "تمت اعادة طباعة هذه الفاتورة من قبل!", Toast.LENGTH_SHORT).show();
                    et_clientID.setText("");
                    navController.popBackStack(R.id.mainFragment,false);
                } else {
                    transData.setPrintCount(2);
                    DBHelper.getInstance(requireActivity()).updateTransData(transData);
                    new PrintReceipt(requireActivity(), transData.getTransBills(), new PrintListener() {
                        @Override
                        public void onFinish() {
                            et_clientID.setText("");
                            navController.popBackStack(R.id.mainFragment,false);

                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }
            } else
                new ApiServices(requireContext(), false).rePrint(et_clientID.getText().toString().trim(), new RequestListener() {
                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                            String Error = responseBody.optString("Error").trim();
                            Log.e("response", response);
                            if (Error != null && !Error.isEmpty()) {
                                onFailure("فشل في اعادة الطباعة!\n" + Error);
                            } else {
                                TransData transData = new TransData();
                                JSONArray billsData = responseBody.optJSONArray("ModelPrintInquiryV");
                                transData.setReferenceNo(responseBody.getInt("BankReceiptNo"));
                                transData.setStan(String.valueOf(responseBody.getInt("BankReceiptNo")));
                                transData.setPaymentType(responseBody.getInt("PayType"));
                                transData.setTransDateTime(responseBody.getString("BankDateTime"));
                                transData.setClientID(et_clientID.getText().toString().trim());
                                transData.setStatus(TransData.STATUS.REPRINT.getValue());
                                ArrayList<TransBill> billDetails = new ArrayList<>();
                                for (int i = 0; i < billsData.length(); i++) {
                                    TransBill bill = new Gson().fromJson(billsData.getJSONObject(i).toString(), TransBill.class);
                                    billDetails.add(bill);
                                    bill.setTransData(transData);
                                }
//                            transData.setTransBills((ForeignCollection<TransBill>) billDetails);

                                int printCount = responseBody.getInt("PrintCount");
                                if (printCount > 2) {
                                    Toast.makeText(requireContext(), "تمت اعادة طباعة هذه الفاتورة من قبل!", Toast.LENGTH_SHORT).show();
                                    navController.popBackStack(R.id.mainFragment,false);
                                } else {
                                    new PrintReceipt(requireContext(), billDetails, new PrintListener() {
                                        @Override
                                        public void onFinish() {
                                            et_clientID.setText("");
                                            navController.popBackStack(R.id.mainFragment,false);

                                        }

                                        @Override
                                        public void onCancel() {

                                        }
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            onFailure(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(String failureMsg) {
                        Toast.makeText(requireContext(), failureMsg, Toast.LENGTH_SHORT).show();
                        et_clientID.setText("");

                    }
                });
        }

}