package com.ebe.miniaelec.ui.reprint;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.data.print.PrintListener;
import com.ebe.miniaelec.data.print.PrintReceipt;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class ReprintFragment extends Fragment implements View.OnClickListener {

    EditText et_clientID;
    NavController navController;
    AppDataBase dataBase;
    CompositeDisposable compositeDisposable;

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

        navController = Navigation.findNavController(requireActivity(), R.id.content);
        dataBase = AppDataBase.getInstance(requireContext());
        compositeDisposable = new CompositeDisposable();

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

        ArrayList<TransBillEntity> transBillEntities= new ArrayList<>();
        compositeDisposable.add(dataBase.transDataDao().getTransByClientId(et_clientID.getText().toString().trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> {

                    transBillEntities.addAll(response.getTransBills());
                    Log.e("transBills", "reprint: "+transBillEntities.size() );
                    return response.getTransData();
                })
                .subscribe(new Consumer<TransDataEntity>() {
                               @Override
                               public void accept(TransDataEntity transDataEntity) throws Throwable {


                                   if (transDataEntity != null && transDataEntity.getPaymentType() == TransDataEntity.PaymentType.OFFLINE_CASH.getValue()) {
                                       if (transDataEntity.getPrintCount() == 2) {
                                           Toast.makeText(requireActivity(), "تمت اعادة طباعة هذه الفاتورة من قبل!", Toast.LENGTH_SHORT).show();
                                           et_clientID.setText("");
                                           navController.popBackStack(R.id.mainFragment, false);
                                       } else {
                                           transDataEntity.setPrintCount(2);


                                           dataBase.transDataDao().updateTransData(transDataEntity);
                                           new PrintReceipt(requireActivity(), transBillEntities,transDataEntity, new PrintListener() {
                                               @Override
                                               public void onFinish() {
                                                   et_clientID.setText("");
                                                   navController.popBackStack(R.id.mainFragment, false);

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
                                                       TransDataEntity transData = new TransDataEntity();
                                                       JSONArray billsData = responseBody.optJSONArray("ModelPrintInquiryV");
                                                       transData.setReferenceNo(responseBody.getInt("BankReceiptNo"));
                                                       transData.setStan(String.valueOf(responseBody.getInt("BankReceiptNo")));
                                                       transData.setPaymentType(responseBody.getInt("PayType"));
                                                       transData.setTransDateTime(responseBody.getString("BankDateTime"));
                                                       transData.setClientID(et_clientID.getText().toString().trim());
                                                       transData.setStatus(TransDataEntity.STATUS.REPRINT.getValue());
                                                       ArrayList<TransBillEntity> billDetails = new ArrayList<>();
                                                       for (int i = 0; i < Objects.requireNonNull(billsData).length(); i++) {
                                                           TransBillEntity bill = new Gson().fromJson(billsData.getJSONObject(i).toString(), TransBillEntity.class);
                                                           billDetails.add(bill);
                                                           bill.setTransDataId(transData.getClientID());
                                                       }

                                                       int printCount = responseBody.getInt("PrintCount");
                                                       if (printCount > 2) {
                                                           Toast.makeText(requireContext(), "تمت اعادة طباعة هذه الفاتورة من قبل!", Toast.LENGTH_SHORT).show();
                                                           navController.popBackStack(R.id.mainFragment, false);
                                                       } else {
                                                           new PrintReceipt(requireContext(), transBillEntities,transDataEntity, new PrintListener() {
                                                               @Override
                                                               public void onFinish() {
                                                                   et_clientID.setText("");
                                                                   navController.popBackStack(R.id.mainFragment, false);

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
                           },throwable -> {Toast.makeText(ReprintFragment.this.requireContext(),throwable.getLocalizedMessage(),Toast.LENGTH_LONG).show();}

                )
);

    }

    @Override
    public void onStop() {
        super.onStop();

        compositeDisposable.dispose();
    }
}