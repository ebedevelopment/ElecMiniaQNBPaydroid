package com.ebe.miniaelec.transactions;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.ui.BuildingBillsFragment;
import com.ebe.miniaelec.ui.MainActivity;

public class InquiryFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    EditText subscriber_number;
    TextView hint;
    int subNumCount = 0, action;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fm = getFragmentManager();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inquiry, container, false);

        MainActivity.setToolbarVisibility(View.VISIBLE);
        Bundle bundle = getArguments();
        action = bundle.getInt("action");
        MainActivity.setBackAction(1);
        hint = view.findViewById(R.id.hint);
        if (action == 1) {
            MainActivity.setTitleText(getString(R.string.inquiry_title));
            hint.setText(getString(R.string.enter_subscriber_num));
        } else if (action == 2) {MainActivity.setTitleText(getString(R.string.read_counter));}
        else if (action == 3) {
            MainActivity.setTitleText(getString(R.string.inquiry_building_number));
            hint.setText(getString(R.string.enter_building_num));
        }
        subscriber_number = view.findViewById(R.id.subscriber_number);
        subscriber_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length()>13) {
                    subscriber_number.setText(charSequence.toString().substring(0,13));
                    subscriber_number.setSelection(13);
                }
                /*int len = charSequence.toString().length();
                Log.i("length", "" + len + " ////// " + i + " ////// " + i1 + " ////// " + i2);
                if (i1 == 0 && (len == 2 || len == 5 || len == 8 || len == 11 || len == 14 || len == 17 || len == 20)) {
                    subscriber_number.setText(charSequence.toString().concat(" "));
                    subscriber_number.setSelection(len + 1);
                } else if (subNumCount < len) {
                    subscriber_number.setSelection(len);
                }else if (subNumCount > len && (i == 2 || i == 5 || i == 8 || i == 11 || i == 14 || i == 17 || i == 20)) {
                    subscriber_number.setText(charSequence.toString().substring(0,i).concat(" ").concat(charSequence.toString().substring(i)));
                    subscriber_number.setSelection(len+1);
                }
                subNumCount = subscriber_number.getText().toString().length();*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
              /*  String s = "";
                if (editable.toString().contains(" "))
                    s = editable.toString().replace(" ", "");

                if (s.toString().length() > 2 && s.toString().charAt(2) != ' ')
                    editable.insert(2, " ");

                if (s.toString().length() > 5 && s.toString().charAt(5) != ' ')
                    editable.insert(5, " ");

                if (s.toString().length() > 8 && s.toString().charAt(8) != ' ')
                    editable.insert(8, " ");

                if (s.toString().length() > 11 && s.toString().charAt(11) != ' ')
                    editable.insert(11, " ");

                if (s.toString().length() > 14 && s.toString().charAt(14) != ' ')
                    editable.insert(14, " ");

                if (s.toString().length() > 17 && s.toString().charAt(17) != ' ')
                    editable.insert(17, " ");

                if (s.toString().length() > 20 && s.toString().charAt(20) != ' ')
                    editable.insert(20, " ");
                subscriber_number.setText(editable.toString());
*/
           /*     if (numberCounter == 2) {
                    String str = editable.toString();
                    str = str + " ";
                    if (str.contains("  "))
                        str = str.replaceAll("  ", " ");

                    subscriber_number.setText(str);

                    // Required to move cursor position after space
                    if (str.length() < 24)
                        subscriber_number.setSelection(str.length());

                    numberCounter = 0;
                }*/
            }
        });
        Button inquiry = view.findViewById(R.id.inquiry);
        inquiry.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.inquiry:
                if (subscriber_number.getText().toString().isEmpty()) {
                    subscriber_number.setError(getString(R.string.required_field));
                } else {
                    if (action == 1) {
                        new ApiServices(getActivity(), false).getCustomerBillsData(subscriber_number.getText().toString());
                        MainActivity.fragmentTransaction(new PaymentFragment(), null);
                        // Toast.makeText(getActivity(), getString(R.string.soon), Toast.LENGTH_SHORT).show();
                    } else if (action == 2) {
                        new ApiServices(getActivity(), false).getMeterReadData(subscriber_number.getText().toString());
                        MainActivity.fragmentTransaction(new ReadCounterMeterFragment(), null);
                    }
                    else if (action == 3) {
                        new ApiServices(getActivity(), false).getBuildingBillsData(subscriber_number.getText().toString());
                        MainActivity.fragmentTransaction(new BuildingBillsFragment(), null);
                    }
                    break;
                }
        }
    }
}
