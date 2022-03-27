package com.ebe.miniaelec.ui;

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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.transition.TransitionInflater;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.message.VoidMsg;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.services.FinishPendingTransService;
import com.ebe.miniaelec.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import dmax.dialog.SpotsDialog;


public class MainFragment extends Fragment implements View.OnClickListener {


    EditText et_clientID, et_clientName;
    ListView lv_clients;
    ArrayList<OfflineClient> offlineClients;
    ArrayList<BillData> offlineBills;
    AdapterOfflineClients offlineClientsAdapter;
    Spinner sp_mntka, sp_day, sp_main, sp_fary;
    ArrayList<String> mntakaList, dayList, mainList, faryList;
    int selectesMntka, selectedDay, selectedMain, selectedFary, selectedClient;
    private String clientId = "";
    TextView tv_search;
    LinearLayout ll_filters;
    static int FINISH_PENDING_TRANS_START = 888;
    NavController navController;
    private SpotsDialog progressDialog;
    public ITransAPI transAPI;

    ArrayList<TransData> pendingTransData;

    int index = 0;



    public MainFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this,callback);


        //offlineClients = new ArrayList<>();
        offlineBills = new ArrayList<>();
        mntakaList = new ArrayList<>();
        dayList = new ArrayList<>();
        mainList = new ArrayList<>();
        faryList = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        et_clientID = view.findViewById(R.id.client_id);
        et_clientName = view.findViewById(R.id.client_name);
        lv_clients = view.findViewById(R.id.clients);
        tv_search = view.findViewById(R.id.tv_search);
        ll_filters = view.findViewById(R.id.ll_filters);
        navController = Navigation.findNavController(requireActivity(),R.id.content);
        NavOptions.Builder navBuilder =  new NavOptions.Builder();
        TransitionInflater inflater = TransitionInflater.from(requireContext());

        transAPI = TransAPIFactory.createTransAPI();
        progressDialog = new SpotsDialog(requireContext(), R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        progressDialog.create();
        progressDialog.setMessage(requireActivity().getString(R.string.switching));
        addObservers();
        Log.d("MaxValue", "onCreateView: "+ MiniaElectricity.getPrefsManager().getOfflineBillValue());
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
        view.findViewById(R.id.start).setOnClickListener(this);
        //offlineClients = new ArrayList<>(DBHelper.getInstance(getActivity()).getAllClients());
        offlineBills = new ArrayList<>(DBHelper.getInstance(getActivity()).getDistinctBills());
        offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
        lv_clients.setAdapter(offlineClientsAdapter);

        lv_clients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedClient = position;
                clientId = offlineBills.get(position).getClientId();

                if (Utils.checkConnection(requireActivity())) {
                    //inquiry();
                    if (!MiniaElectricity.getPrefsManager().isLoggedIn()) {
                        requireActivity().finish();
                    }


                    if (progressDialog != null)
                    {

                        progressDialog.show();
                    }

                    Log.d("Pressed", "onItemClick: clicked ");
                    //startActivityForResult(new Intent(getActivity(), FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                    requireActivity().startService(new Intent(requireContext(), FinishPendingTransService.class));
                } else {
                    Bundle bundle = new Bundle();
                    // bundle.putSerializable("response", offlineClients.get(position));
                    bundle.putString("clientID", clientId);
                    bundle.putBoolean("offline", true);
                    et_clientID.setText("");
                    navController.navigate(R.id.billPaymentFragment,bundle);

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
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBillsOfMntka(mntakaList.get(selectesMntka)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    dayList = new ArrayList<>();
                    dayList.add(getString(R.string.daily));
                    dayList.addAll(DBHelper.getInstance(requireActivity()).getDistinctDaysOfMntka(mntakaList.get(selectesMntka)));
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
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills());
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
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
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    mainList = new ArrayList<>();
                    mainList.add(getString(R.string.main_code));
                    mainList.addAll(DBHelper.getInstance(requireActivity()).getDistinctMainsOfMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireActivity(),
                            android.R.layout.simple_spinner_dropdown_item, mainList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_main.setAdapter(dataAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(requireActivity(),
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
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    faryList.addAll(DBHelper.getInstance(requireActivity()).getDistinctFaryOfMntkaAndDayAndMain(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
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
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain), faryList.get(selectedFary)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
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
                    Utils.slideUp(requireActivity(), ll_filters);
                    ll_filters.setVisibility(View.GONE);
                } else {
                    ll_filters.setVisibility(View.VISIBLE);
                    Utils.slideDown(requireActivity(), ll_filters);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start) {
            if (!et_clientID.getText().toString().trim().isEmpty() && et_clientID.getText().toString().trim().length() == 10) {
                // Toast.makeText(getActivity(), "أدخل رقم الاشتراك بشكل صحيح!", Toast.LENGTH_SHORT).show();
                clientId = et_clientID.getText().toString().trim();
                if (Utils.checkConnection(requireActivity())) {
                   // startActivityForResult(new Intent(getActivity(), FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                    requireActivity().startService(new Intent(requireContext(),FinishPendingTransService.class));

                } else {

                    Bundle bundle = new Bundle();
                    // bundle.putSerializable("response", offlineClients.get(position));
                    bundle.putString("clientID", clientId);
                    bundle.putBoolean("offline", true);
                    et_clientID.setText("");
                    navController.navigate(R.id.billPaymentFragment,bundle);

                }
            } else if (!et_clientName.getText().toString().trim().isEmpty()) {
                offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(et_clientName.getText().toString().trim()));
                offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                offlineClientsAdapter.notifyDataSetChanged();
                lv_clients.setAdapter(offlineClientsAdapter);
            } else {
                if (selectesMntka != 0 && selectedDay != 0 && selectedMain != 0 && selectedFary!=0) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain), faryList.get(selectedFary)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                } else if (selectesMntka != 0 && selectedDay != 0 && selectedMain != 0 ) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    faryList.addAll(DBHelper.getInstance(requireActivity()).getDistinctFaryOfMntkaAndDayAndMain(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireActivity(),
                            android.R.layout.simple_spinner_dropdown_item, faryList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_fary.setAdapter(dataAdapter);
                } else if (selectesMntka != 0 && selectedDay != 0 ) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBills(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    mainList = new ArrayList<>();
                    mainList.add(getString(R.string.main_code));
                    mainList.addAll(DBHelper.getInstance(requireActivity()).getDistinctMainsOfMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireActivity(),
                            android.R.layout.simple_spinner_dropdown_item, mainList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_main.setAdapter(dataAdapter);
                    faryList = new ArrayList<>();
                    faryList.add(getString(R.string.fary_code));
                    ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(requireActivity(),
                            android.R.layout.simple_spinner_dropdown_item, faryList);
                    faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_fary.setAdapter(faryAdapter);
                } else if (selectesMntka != 0 ) {
                    offlineBills = new ArrayList<>(DBHelper.getInstance(requireActivity()).getDistinctBillsOfMntka(mntakaList.get(selectesMntka)));
                    offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                    lv_clients.setAdapter(offlineClientsAdapter);
                    dayList = new ArrayList<>();
                    dayList.add(getString(R.string.daily));
                    dayList.addAll(DBHelper.getInstance(requireActivity()).getDistinctDaysOfMntka(mntakaList.get(selectesMntka)));
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireActivity(),
                            android.R.layout.simple_spinner_dropdown_item, dayList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_day.setAdapter(dataAdapter);
                } else
                    Toast.makeText(getActivity(), "أدخل رقم الاشتراك او اسم العميل بشكل صحيح!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inquiry() {
        new ApiServices(requireActivity(), false).billInquiry(clientId, new RequestListener() {
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
                            startActivity(new Intent(requireActivity(), LoginActivity.class));
                            requireActivity().finish();
                        }
                    } else {

                        Bundle bundle = new Bundle();
                        bundle.putString("response", response);
                        bundle.putString("clientID", clientId);
                        // fragment.setArguments(bundle);
                        bundle.putBoolean("offline", false);
                        et_clientID.setText("");
                        navController.navigate(R.id.billPaymentFragment,bundle);

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
        if (clientId != null && !clientId.isEmpty()) {
            OfflineClient client = DBHelper.getInstance(requireActivity()).getClientByClientId(clientId);
            //clientId = null;
            if (client == null || (client.getModelBillInquiryV() == null || client.getModelBillInquiryV().size() == 0)) {
                offlineBills.remove(selectedClient);
                offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                offlineClientsAdapter.notifyDataSetChanged();
                lv_clients.setAdapter(offlineClientsAdapter);
            } else {
                //offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
                offlineClientsAdapter.notifyDataSetChanged();
                // lv_clients.setAdapter(offlineClientsAdapter);
            }
        }
    }

    private void aVoidReq(TransData transData) {
        VoidMsg.Request request = new VoidMsg.Request();

        request.setEcrRef(transData.getReferenceNo());
        request.setNeedReceipt(true);
        request.setNeedToConfirm(false);
        transAPI = TransAPIFactory.createTransAPI();
        request.setCategory(SdkConstants.CATEGORY_VOID);
        request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
        transAPI.startTrans(requireContext(), request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);
        Intent serviceIntent = new Intent(requireContext(),FinishPendingTransService.class);
        serviceIntent.putExtra("pending",true);
        if (baseResponse == null) {
            //Log.e("onActivityResult", "null");

            requireActivity().startService(serviceIntent);
        } else {
            boolean isTransResponse = baseResponse instanceof TransResponse;
            if (isTransResponse) {
                final TransResponse transResponse = (TransResponse) baseResponse;
                //Log.e("response", "//" + transResponse.toString());
                if (transResponse.getRspCode() == 0 || transResponse.getRspCode() == -15
                        || transResponse.getRspCode() == -16 || transResponse.getRspCode() == -17 || transResponse.getRspCode() == -18) {
                    TransData transData = DBHelper.getInstance(requireActivity()).getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo());
                    if (transData != null) {

                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        for (TransBill bill :
                                transData.getTransBills()) {
                            DBHelper.getInstance(requireActivity()).deleteTransBill(bill.getBillUnique());
                        }
                        DBHelper.getInstance(requireActivity()).deleteTransData(transData);
                    }
                    //DBHelper.getInstance(cntxt).updateTransData(transData);
                }
            } else {
                //Log.e("onActivityResult", "BaseResponse");
                if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15
                        || baseResponse.getRspCode() == -16 || baseResponse.getRspCode() == -17 || baseResponse.getRspCode() == -18) {
                    TransData transData = DBHelper.getInstance(requireActivity()).getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo());
                    if (transData != null) {

                        transData.setStatus(TransData.STATUS.COMPLETED.getValue());
                        for (TransBill bill :
                                transData.getTransBills()) {
                            DBHelper.getInstance(requireActivity()).deleteTransBill(bill.getBillUnique());
                        }
                        DBHelper.getInstance(requireActivity()).deleteTransData(transData);
                    }
                    //DBHelper.getInstance(cntxt).updateTransData(transData);
                }
            }
            requireActivity().startService(serviceIntent);
        }


    }

    void addObservers()
    {
        FinishPendingTransService.aVoid.observe(getViewLifecycleOwner(), new Observer<TransData>() {
            @Override
            public void onChanged(TransData transData) {
                if (transData != null)
                aVoidReq(transData);
            }
        });

        FinishPendingTransService.errorMsg.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (!s.isEmpty())
                Toast.makeText(requireContext(),s,Toast.LENGTH_LONG).show();
            }
        });

        FinishPendingTransService.goToLogin.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                {
                    requireActivity().startActivity(new Intent(requireContext(),LoginActivity.class));
                    requireActivity().finish();
                }

            }
        });

        FinishPendingTransService.goToPayment.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean)
                {
                    if (clientId !=null && !clientId.isEmpty())
                    {
                        inquiry();
                        progressDialog.dismiss();

                        FinishPendingTransService.goToPayment.setValue(false);

                    }

                }

            }
        });

        FinishPendingTransService.pendingData.observe(getViewLifecycleOwner(), new Observer<ArrayList<TransData>>() {
            @Override
            public void onChanged(ArrayList<TransData> transData) {
                if (!transData.isEmpty())
                {
                    pendingTransData = transData;
                }
            }
        });

        FinishPendingTransService.indexState.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                index = integer;
            }
        });


    }


    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent(requireContext(),FinishPendingTransService.class);

        requireActivity().stopService(intent);
    }
}