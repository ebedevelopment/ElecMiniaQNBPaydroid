package com.ebe.miniaelec.ui;

import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
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
import com.ebe.miniaelec.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class RePrintActivity extends AppCompatActivity implements View.OnClickListener {

    FragmentManager fm;
    EditText et_clientID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_reprint);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = findViewById(R.id.title);
        setSupportActionBar(toolbar);
        setStatusBarColor();
        fm = getFragmentManager();
        et_clientID = findViewById(R.id.client_id);
        et_clientID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 10) {
                    InputMethodManager inputManager = (InputMethodManager) RePrintActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(RePrintActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        findViewById(R.id.reprint).setOnClickListener(this);
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reprint) {
            if (et_clientID.getText().toString().trim().isEmpty()) {
                Toast.makeText(RePrintActivity.this, "أدخل رقم الاشتراك!", Toast.LENGTH_SHORT).show();
            } else {
                reprint();
            }
        }
    }

    private void reprint() {
        final TransData transData = DBHelper.getInstance(this).getTransByClientId(et_clientID.getText().toString().trim());
        if (transData != null && transData.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue()) {
            if (transData.getPrintCount() == 2) {
                Toast.makeText(RePrintActivity.this, "تمت اعادة طباعة هذه الفاتورة من قبل!", Toast.LENGTH_SHORT).show();
                et_clientID.setText("");
                finish();
            } else {
                transData.setPrintCount(2);
                DBHelper.getInstance(RePrintActivity.this).updateTransData(transData);
                new PrintReceipt(RePrintActivity.this, transData.getTransBills(), new PrintListener() {
                    @Override
                    public void onFinish() {
                        et_clientID.setText("");
                       finish();

                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        } else
            new ApiServices(RePrintActivity.this, false).rePrint(et_clientID.getText().toString().trim(), new RequestListener() {
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
                                Toast.makeText(RePrintActivity.this, "تمت اعادة طباعة هذه الفاتورة من قبل!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                new PrintReceipt(RePrintActivity.this, billDetails, new PrintListener() {
                                    @Override
                                    public void onFinish() {
                                        et_clientID.setText("");
                                        finish();

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
                    Toast.makeText(RePrintActivity.this, failureMsg, Toast.LENGTH_SHORT).show();
                    et_clientID.setText("");

                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
    }
}
