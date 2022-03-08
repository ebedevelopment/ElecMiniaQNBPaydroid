package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ebe.miniaelec.R;

public class CRNCollect extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    EditText subsidiary, apartment, building, block, section, daily, area, governorate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setTitleText(getString(R.string.crn_collect));
        MainActivity.setBackAction(0);
        fm = getFragmentManager();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crn_collect, container, false);
        subsidiary = view.findViewById(R.id.subsidiary);
        apartment = view.findViewById(R.id.apartment);
        building = view.findViewById(R.id.building);
        block = view.findViewById(R.id.block);
        section = view.findViewById(R.id.section);
        daily = view.findViewById(R.id.daily);
        area = view.findViewById(R.id.area);
        governorate = view.findViewById(R.id.governorate);
        governorate.requestFocus();
        governorate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    area.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        area.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    daily.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        daily.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    section.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        section.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    block.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        block.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    building.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        building.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    apartment.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        apartment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    subsidiary.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        subsidiary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 2) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.start).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Fragment fragment;
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.start:
                //MainActivity.fragmentTransaction(new UnitBillsPaymentFragment(), null);
                break;
        }
    }
}
