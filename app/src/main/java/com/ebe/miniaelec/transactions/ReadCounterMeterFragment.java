package com.ebe.miniaelec.transactions;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.ui.MainActivity;

import java.util.Calendar;

public class ReadCounterMeterFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    EditText current_meter;

    private static int closeDate;
    private static int lastMeterValue;
    private static String customerNo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fm = getFragmentManager();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read_counter_meter, container, false);

        MainActivity.setToolbarVisibility(View.VISIBLE);
        MainActivity.setTitleText(getString(R.string.read_counter));
        MainActivity.setBackAction(2);
        current_meter = view.findViewById(R.id.current_meter);
        Button inquiry = view.findViewById(R.id.send_current_meter);
        inquiry.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.send_current_meter:
                int DAY_OF_MONTH = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                Log.i("DAY_OF_MONTH", String.valueOf(DAY_OF_MONTH));
                /*SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = format.parse(dtStart);
                String day = (String) DateFormat.format("dd", date);*/
                if (current_meter.getText().toString().isEmpty()) {
                    current_meter.setError(getString(R.string.required_field));
                } /*else if (DAY_OF_MONTH > closeDate) {
                    Toast.makeText(getActivity(), "لقد انتهت فترة تسجيل قراءة العداد في منطقتك لهذا الشهر برجاء المحاولة لاحقاً", Toast.LENGTH_LONG).show();
                } else if (Integer.parseInt(current_meter.getText().toString()) <= lastMeterValue) {
                    Toast.makeText(getActivity(), "برجاء كتابة الرقم كما هو موضح على عداد الغاز والمحدد بالظل الأسود!", Toast.LENGTH_LONG).show();

                }*/ else {
                    Toast.makeText(getActivity(), "تم تسجيل القراءة بنجاح!", Toast.LENGTH_LONG).show();
                    getFragmentManager().popBackStack();
                   // MainActivity.fragmentAddTransaction(new NewHomeFragment());
                    //   new ApiServices(getActivity()).updateMeterReadData(customerNo, current_meter.getText().toString());
                }
                break;
        }
    }

    public static void setValidationData(int _closeDate, int _lastMeterValue, String _customerNo) {
        customerNo = _customerNo;
        closeDate = _closeDate;
        lastMeterValue = _lastMeterValue;
    }
}
