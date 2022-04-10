package com.ebe.miniaelec.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewHomeFragment extends Fragment implements View.OnClickListener {

    FragmentManager fm;
    EditText et_clientID, et_clientName;
    ListView lv_clients;
    ArrayList<OfflineClient> offlineClients;
    ArrayList<BillData> offlineBills;
    AdapterOfflineClients offlineClientsAdapter;
    Spinner sp_mntka, sp_day, sp_main, sp_fary;
    ArrayList<String> mntakaList, dayList, mainList, faryList;
    int selectesMntka, selectedDay, selectedMain, selectedFary, selectedClient;
    private String clientId = "";
    private boolean emptyParams = false;
    TextView tv_search;
    LinearLayout ll_filters;
    static int FINISH_PENDING_TRANS_START = 888;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.setTitleText(getString(R.string.client_inquiry));
        MainActivity.setBackAction(0);
        fm = getFragmentManager();
        //offlineClients = new ArrayList<>();
        offlineBills = new ArrayList<>();
        mntakaList = new ArrayList<>();
        dayList = new ArrayList<>();
        mainList = new ArrayList<>();
        faryList = new ArrayList<>();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_home, container, false);
        et_clientID = view.findViewById(R.id.client_id);
        et_clientName = view.findViewById(R.id.client_name);
        lv_clients = view.findViewById(R.id.clients);
        tv_search = view.findViewById(R.id.tv_search);
        ll_filters = view.findViewById(R.id.ll_filters);
        et_clientID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() == 10) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        view.findViewById(R.id.start).setOnClickListener(this);
        //offlineClients = new ArrayList<>(DBHelper.getInstance(getActivity()).getAllClients());
        offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills());
        offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
        offlineClientsAdapter.notifyDataSetChanged();
        lv_clients.setAdapter(offlineClientsAdapter);
        lv_clients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedClient = position;
                clientId = offlineBills.get(position).getClientId();
                if (Utils.checkConnection(getActivity())) {
                    //inquiry();
                    startActivityForResult(new Intent(getActivity(), FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                } else {
                    Intent intent = new Intent(getActivity(), BillPaymentActivity.class);
                    Bundle bundle = new Bundle();
                    // bundle.putSerializable("response", offlineClients.get(position));
                    bundle.putString("clientID", clientId);
                    bundle.putBoolean("offline", true);
                    // fragment.setArguments(bundle);
                    intent.putExtra("params", bundle);
                    et_clientID.setText("");
                    getActivity().startActivity(intent);
                }
            }
        });
        sp_mntka = view.findViewById(R.id.mntka);
        sp_day = view.findViewById(R.id.day);
        sp_main = view.findViewById(R.id.main_code);
        sp_fary = view.findViewById(R.id.fary_code);
        mntakaList.add(getString(R.string.place));
        mntakaList.addAll(DBHelper.getInstance(getActivity()).getDistinctMntka());
        ArrayAdapter<String> mntkaAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, mntakaList);
        mntkaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_mntka.setAdapter(mntkaAdapter);
        dayList = new ArrayList<>();
        dayList.add(getString(R.string.daily));
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, dayList);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_day.setAdapter(daysAdapter);
        mainList = new ArrayList<>();
        mainList.add(getString(R.string.main_code));
        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, mainList);
        mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_main.setAdapter(mainAdapter);
        faryList = new ArrayList<>();
        faryList.add(getString(R.string.fary_code));
        ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, faryList);
        faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_fary.setAdapter(faryAdapter);
        sp_mntka.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectesMntka = position;
                if (position != 0) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBillsOfMntka(mntakaList.get(selectesMntka)));
                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    dayList = new ArrayList<>();
                    dayList.add(getString(R.string.daily));
                    dayList.addAll(DBHelper.getInstance(getActivity()).getDistinctDaysOfMntka(mntakaList.get(selectesMntka)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, dayList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_day.setAdapter(dataAdapter);
                    mainList = new ArrayList<>();
                    mainList.add(getString(R.string.main_code));
                    ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, mainList);
                    mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_main.setAdapter(mainAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, faryList);
                    faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_fary.setAdapter(faryAdapter);
                } else {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills());
                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    dayList = new ArrayList<>();
                    mainList = new ArrayList<>();
                    faryList = new ArrayList<>();
                    dayList = new ArrayList<>();
                    dayList.add(getString(R.string.daily));
                    ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, dayList);
                    daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_day.setAdapter(daysAdapter);
                    mainList = new ArrayList<>();
                    mainList.add(getString(R.string.main_code));
                    ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, mainList);
                    mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_main.setAdapter(mainAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, faryList);
                    faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_fary.setAdapter(faryAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = position;
                if (position != 0) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    mainList = new ArrayList<>();
                    mainList.add(getString(R.string.main_code));
                    mainList.addAll(DBHelper.getInstance(getActivity()).getDistinctMainsOfMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, mainList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_main.setAdapter(dataAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, faryList);
                    faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_fary.setAdapter(faryAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_main.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMain = position;
                if (position != 0) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    faryList.addAll(DBHelper.getInstance(getActivity()).getDistinctFaryOfMntkaAndDayAndMain(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, faryList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_fary.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_fary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFary = position;
                if (position != 0) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain), faryList.get(selectedFary)));
                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_filters.isShown()) {
                    Utils.slideUp(getActivity(), ll_filters);
                    ll_filters.setVisibility(View.GONE);
                } else {
                    ll_filters.setVisibility(View.VISIBLE);
                    Utils.slideDown(getActivity(), ll_filters);
                }
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            if (!et_clientID.getText().toString().trim().isEmpty() && et_clientID.getText().toString().trim().length() == 10) {
                // Toast.makeText(getActivity(), "أدخل رقم الاشتراك بشكل صحيح!", Toast.LENGTH_SHORT).show();
                clientId = et_clientID.getText().toString().trim();
                if (Utils.checkConnection(getActivity())) {
                    startActivityForResult(new Intent(getActivity(), FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                } else {
                    Intent intent = new Intent(getActivity(), BillPaymentActivity.class);
                    Bundle bundle = new Bundle();
                    // bundle.putSerializable("response", offlineClients.get(position));
                    bundle.putString("clientID", clientId);
                    bundle.putBoolean("offline", true);
                    // fragment.setArguments(bundle);
                    intent.putExtra("params", bundle);
                    et_clientID.setText("");
                    getActivity().startActivity(intent);
                }
            } else if (!et_clientName.getText().toString().trim().isEmpty()) {
                offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(et_clientName.getText().toString().trim()));
                offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                offlineClientsAdapter.notifyDataSetChanged();
                lv_clients.setAdapter(offlineClientsAdapter);
            } else {

                clientsFilter();
                if (emptyParams)
                {
                    Toast.makeText(getActivity(), "أدخل رقم الاشتراك او اسم العميل بشكل صحيح!", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

     void clientsFilter()
     {
         if (selectesMntka != 0 && selectedDay != 0 && selectedMain != 0 && selectedFary!=0) {
             offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain), faryList.get(selectedFary)));
             offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
             lv_clients.setAdapter(offlineClientsAdapter);
             emptyParams = false;
         } else if (selectesMntka != 0 && selectedDay != 0 && selectedMain != 0 ) {
             offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
             offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
             lv_clients.setAdapter(offlineClientsAdapter);
             faryList = new ArrayList<>();
             faryList.add(getString(R.string.fary_code));
             faryList.addAll(DBHelper.getInstance(getActivity()).getDistinctFaryOfMntkaAndDayAndMain(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
             ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                     android.R.layout.simple_spinner_dropdown_item, faryList);
             dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             sp_fary.setAdapter(dataAdapter);
             emptyParams = false;
         } else if (selectesMntka != 0 && selectedDay != 0 ) {
             offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
             offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
             lv_clients.setAdapter(offlineClientsAdapter);
             mainList = new ArrayList<>();
             mainList.add(getString(R.string.main_code));
             mainList.addAll(DBHelper.getInstance(getActivity()).getDistinctMainsOfMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
             ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                     android.R.layout.simple_spinner_dropdown_item, mainList);
             dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             sp_main.setAdapter(dataAdapter);
             faryList = new ArrayList<>();
             faryList.add(getString(R.string.fary_code));
             ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(getActivity(),
                     android.R.layout.simple_spinner_dropdown_item, faryList);
             faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             sp_fary.setAdapter(faryAdapter);
             emptyParams = false;
         } else if (selectesMntka != 0 ) {
             offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBillsOfMntka(mntakaList.get(selectesMntka)));
             offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
             lv_clients.setAdapter(offlineClientsAdapter);
             dayList = new ArrayList<>();
             dayList.add(getString(R.string.daily));
             dayList.addAll(DBHelper.getInstance(getActivity()).getDistinctDaysOfMntka(mntakaList.get(selectesMntka)));
             ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                     android.R.layout.simple_spinner_dropdown_item, dayList);
             dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
             sp_day.setAdapter(dataAdapter);
             emptyParams = false;
         }else
             emptyParams = true;

     }

    private void inquiry() {
        new ApiServices(getActivity(), false).billInquiry(clientId, new RequestListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String Error = responseBody.optString("Error").trim();
                    //Log.e("response", response);
                    if (!Error.isEmpty()) {
                        onFailure("فشل في الاستعلام!\n" + Error);
                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول")) {
                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();
                        }
                    } else {
                        //isFirst = true;
                        //Fragment fragment = new UnitBillsPaymentFragment();
                        Intent intent = new Intent(getActivity(), BillPaymentActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("response", response);
                        bundle.putString("clientID", clientId);
                        // fragment.setArguments(bundle);
                        bundle.putBoolean("offline", false);
                        intent.putExtra("params", bundle);
                        et_clientID.setText("");
                        getActivity().startActivity(intent);
                        // MainActivity.fragmentTransaction(fragment, "BillPayment");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onFailure(e.getMessage());
                }
            }

            @Override
            public void onFailure(String failureMsg) {
                Toast.makeText(getActivity(), failureMsg, Toast.LENGTH_SHORT).show();
                et_clientID.setText("");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        clientsFilter();
        offlineClientsAdapter.notifyDataSetChanged();
//        if (clientId != null && !clientId.isEmpty()) {
//            OfflineClient client = DBHelper.getInstance(getActivity()).getClientByClientId(clientId);
//            //clientId = null;
//            if (client == null || (client.getModelBillInquiryV() == null || client.getModelBillInquiryV().size() == 0)) {
//                offlineBills.remove(selectedClient);
//                offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
//                offlineClientsAdapter.notifyDataSetChanged();
//                lv_clients.setAdapter(offlineClientsAdapter);
//            } else {
//                //offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
//                offlineClientsAdapter.notifyDataSetChanged();
//                // lv_clients.setAdapter(offlineClientsAdapter);
//            }
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_PENDING_TRANS_START) {
            Log.e("onActivityResult", "NewHomeFragment");
            if (!MiniaElectricity.getPrefsManager().isLoggedIn()) {
                getActivity().finish();
            } else
            {
                if (MiniaElectricity.getPrefsManager().getOfflineBillStatus()==2)
                {
                    clientId = "";
                    selectedClient = 0;
                    offlineBills.clear();
                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);


                }


                inquiry();
            }

        }
    }
}
