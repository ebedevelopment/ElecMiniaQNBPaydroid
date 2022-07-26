package com.ebe.miniaelec.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionInflater;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.message.VoidMsg;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.ui.adapters.AdapterOfflineClients;
import com.ebe.miniaelec.ui.adapters.PagingClientsAdapter;
import com.ebe.miniaelec.ui.billpayment.PaymentActivity;
import com.ebe.miniaelec.ui.login.LoginActivity;
import com.ebe.miniaelec.ui.services.FinishPendingTransService;
import com.ebe.miniaelec.utils.CustomDialog;
import com.ebe.miniaelec.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainFragment extends Fragment implements View.OnClickListener,AdapterOfflineClients.BillClickListener,PagingClientsAdapter.BillClickListener {


    EditText et_clientID, et_clientName;
    RecyclerView lv_clients;
    private AppDataBase dataBase;
    private CompositeDisposable compositeDisposable;
    ArrayList<OfflineClientEntity> offlineClients;
    ArrayList<BillDataEntity> offlineBills;
    AdapterOfflineClients offlineClientsAdapter;
    Spinner sp_mntka, sp_day, sp_main, sp_fary;
    ArrayList<String> mntakaList, dayList, mainList, faryList;
    Integer selectesMntka, selectedDay, selectedMain, selectedFary, selectedClient = 0;
    private String clientId = "";
    TextView tv_search;
    LinearLayout ll_filters;
    static int FINISH_PENDING_TRANS_START = 888;
    NavController navController;
    private SpotsDialog progressDialog;
    public ITransAPI transAPI;
    ArrayAdapter<String> daysAdapter;
    PagingClientsAdapter pagingAdapter;
    String error_message ="";

    ArrayList<TransDataEntity> pendingTransData;
    boolean dataState = false;
    boolean lateDataState = false;

    int index = 0;

    MainViewModel viewModel;


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

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);


        offlineClients = new ArrayList<>();
        offlineBills = new ArrayList<>();
        mntakaList = new ArrayList<>();
        dayList = new ArrayList<>();
        mainList = new ArrayList<>();
        faryList = new ArrayList<>();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        viewModel = new ViewModelProvider(requireActivity(), new MainViewModelFactory(requireActivity().getApplication())).get(MainViewModel.class);


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        dataBase = AppDataBase.getInstance(requireContext());
        et_clientID = view.findViewById(R.id.client_id);
        et_clientName = view.findViewById(R.id.client_name);
        lv_clients = view.findViewById(R.id.clients);
        tv_search = view.findViewById(R.id.tv_search);
        ll_filters = view.findViewById(R.id.ll_filters);
        navController = Navigation.findNavController(requireActivity(), R.id.content);
        NavOptions.Builder navBuilder = new NavOptions.Builder();
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        compositeDisposable = new CompositeDisposable();
        offlineBills = new ArrayList<>();
        offlineClientsAdapter = AdapterOfflineClients.getInstance(this.requireActivity());
        AdapterOfflineClients.billClickListener = this;
        offlineClientsAdapter.submitList(offlineBills);
        pagingAdapter = PagingClientsAdapter.getInstance(this.requireActivity());
        PagingClientsAdapter.billClickListener = this;

        transAPI = TransAPIFactory.createTransAPI();
        progressDialog = new SpotsDialog(requireContext(), R.style.ProcessingProgress);
        progressDialog.setCancelable(false);
        progressDialog.create();
        progressDialog.setMessage(requireActivity().getString(R.string.switching));
        addObservers();
        Log.d("MaxValue", "onCreateView: " + MiniaElectricity.getPrefsManager().getOfflineBillValue());
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
       // setClientList();

        sp_mntka = view.findViewById(R.id.mntka);
        sp_day = view.findViewById(R.id.day);
        sp_main = view.findViewById(R.id.main_code);
        sp_fary = view.findViewById(R.id.fary_code);


        viewModel.getMntkaList();


        dayList = new ArrayList<>();
        dayList.add(getString(R.string.daily));
        daysAdapter = new ArrayAdapter<>(getActivity(),
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

                if (selectesMntka > 0) {

                    //viewModel.filterByMantka(mntakaList.get(selectesMntka));
                    filterByMntka();
                    getDistinctDaysOfMntka();

                } else {

                    if (lateDataState)
                    filterByMntkaIfPosZero();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });
        sp_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (position != 0) {
                    selectedDay = position;
                    if (dayList.size() > 1)
                    {
                        filterByDay();
                        getDistinctMains();
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_main.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (position != 0) {
                    selectedMain = position;
                    if (mainList.size() > 1)
                    {
                        filterByMain();
                        getDistictFaryList();
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });
        sp_fary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position != 0) {
                    selectedFary = position;
                    if (faryList.size() >1)
                    {
                        filterByFary();
                    }


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
                    requireActivity().startService(new Intent(requireContext(), FinishPendingTransService.class));


                } else {

                    Bundle bundle = new Bundle();
                    // bundle.putSerializable("response", offlineClients.get(position));
                    bundle.putString("clientID", clientId);
                    bundle.putBoolean("offline", true);
                    et_clientID.setText("");
                    navController.navigate(R.id.billPaymentFragment, bundle);

                }
            } else if (!et_clientName.getText().toString().trim().isEmpty()) {
                getBillsByClientName();

            } else {
                if (selectesMntka != 0 && selectedDay != 0 && selectedMain != 0 && selectedFary != 0) {
                    filterByFary();
                } else if (selectesMntka != 0 && selectedDay != 0 && selectedMain != 0) {
                    filterByMain();
                } else if (selectesMntka != 0 && selectedDay != 0) {
                    filterByDay();
                } else if (selectesMntka != 0) {
                    filterByMntka();
                } else
                    CustomDialog.showMessage(getActivity(), "أدخل رقم الاشتراك او اسم العميل بشكل صحيح!");
            }
        }
    }

    private void inquiry() {
        if (Utils.checkConnection(this.requireActivity())) {
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
                            Intent intent = new Intent(requireActivity(), PaymentActivity.class);
                            et_clientID.setText("");
                            clientId = "";
                            intent.putExtra("data",bundle);
                           // navController.navigate(R.id.paymentActivity, bundle);
                            requireActivity().startActivity(intent);

                            // MainActivity.fragmentTransaction(fragment, "BillPayment");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        onFailure(e.getMessage());
                    }
                }

                @Override
                public void onFailure(String failureMsg) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (!requireActivity().isDestroyed())
                            {
                                CustomDialog.showMessage(getActivity(), failureMsg);
                                et_clientID.setText("");
                            }

                        }
                    });

                }
            });
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (clientId != null && !clientId.isEmpty()) {

        }
    }


    private void aVoidReq(TransDataEntity transData) {
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
        Intent serviceIntent = new Intent(requireContext(), FinishPendingTransService.class);
        serviceIntent.putExtra("pending", true);
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

                    int refNumber = pendingTransData.get(index - 1).getReferenceNo();
                    compositeDisposable.add(dataBase.transDataDao().getTransByRefNo(refNumber)
                            .subscribeOn(Schedulers.io())
                            .onErrorReturn(throwable -> {
                                Log.d(null, "onActivityResult: " + throwable.getMessage());
                                return null;
                            })
                            .subscribe(new Consumer<TransDataWithTransBill>() {
                                @Override
                                public void accept(TransDataWithTransBill transDataWithTransBill) throws Throwable {
                                    TransDataEntity transData = transDataWithTransBill.getTransData();
                                    if (transData != null) {

                                        transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                                        for (TransBillEntity bill :
                                                transDataWithTransBill.getTransBills()) {
                                            dataBase.transBillDao().deleteTransBill(bill.getBillUnique());
                                        }
                                        dataBase.transDataDao().deleteTransData(transData);
                                    }

                                }
                            }, throwable -> {
                                Log.e("MainFragment", "onActivityResult: " + throwable.getLocalizedMessage());
                            }));


                } else {

                    if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15
                            || baseResponse.getRspCode() == -16 || baseResponse.getRspCode() == -17 || baseResponse.getRspCode() == -18) {
                        compositeDisposable.add(dataBase.transDataDao().getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo())
                                .subscribeOn(Schedulers.io())
                                .onErrorReturn(throwable -> {
                                    Log.d(null, "onActivityResult: " + throwable.getMessage());
                                    return null;
                                }).subscribe(new Consumer<TransDataWithTransBill>() {
                                    @Override
                                    public void accept(TransDataWithTransBill transDataWithTransBill) throws Throwable {

                                        TransDataEntity transData = transDataWithTransBill.getTransData();
                                        if (transData != null) {

                                            transData.setStatus(TransDataEntity.STATUS.COMPLETED.getValue());
                                            for (TransBillEntity bill :
                                                    transDataWithTransBill.getTransBills()) {
                                                dataBase.transBillDao().deleteTransBill(bill.getBillUnique());
                                            }
                                            dataBase.transDataDao().deleteTransData(transData);
                                        }
                                    }
                                }, throwable -> {
                                    Log.e("MainFragment", "onActivityResult: " + throwable);
                                }));


                    }
                }
                requireActivity().startService(serviceIntent);
            }


        }
    }

    void addObservers() {
        FinishPendingTransService.aVoid.observe(getViewLifecycleOwner(), new Observer<TransDataEntity>() {
                    @Override
                    public void onChanged(TransDataEntity transDataEntity) {
                        if (transDataEntity != null)
                            aVoidReq(transDataEntity);
                    }
                }

        );

        FinishPendingTransService.errorMsg.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (!s.isEmpty())
                    error_message = s;

            }
        });

        FinishPendingTransService.goToLogin.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    if (!error_message.isEmpty())
                    {
                        CustomDialog.showMessage(requireActivity(), error_message);
                    }
                    requireActivity().startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                }

            }
        });

        FinishPendingTransService.goToPayment.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {
                    if (clientId != null && !clientId.isEmpty()) {
                        inquiry();
                        progressDialog.dismiss();

                        FinishPendingTransService.goToPayment.setValue(false);


                    }

                }

            }
        });

        FinishPendingTransService.pendingData.observe(getViewLifecycleOwner(), new Observer<ArrayList<TransDataEntity>>() {
                    @Override
                    public void onChanged(ArrayList<TransDataEntity> transDataEntities) {
                        if (transDataEntities != null && !transDataEntities.isEmpty()) {
                            pendingTransData = transDataEntities;
                        }
                    }
                }
        );

        FinishPendingTransService.indexState.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                index = integer;
            }
        });


        viewModel.offlineBills.observe(getViewLifecycleOwner(), new Observer<List<BillDataEntity>>() {
            @Override
            public void onChanged(List<BillDataEntity> billDataEntities) {
                offlineBills = new ArrayList<>();
                offlineBills.addAll(billDataEntities);
                offlineClientsAdapter = AdapterOfflineClients.getInstance(MainFragment.this.requireActivity());
                offlineClientsAdapter.submitList(offlineBills);
                lv_clients.setAdapter(offlineClientsAdapter);
            }
        });

        viewModel.mntkas.observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                if (!strings.isEmpty())
                    mntakaList.clear();
                mntakaList.add(getString(R.string.place));
                mntakaList.addAll(strings);
                ArrayAdapter<String> mntkaAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, mntakaList);
                mntkaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp_mntka.setAdapter(mntkaAdapter);
                if (selectesMntka != null) {
                    sp_mntka.setSelection(selectesMntka);
                }
            }
        });

        viewModel.insertionState.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                dataState = aBoolean;
                if (dataState)
                {
                    setClientList();
                    viewModel.PostInsertionState.setValue(true);
                    viewModel.insertionState.setValue(false);
                }



            }
        });

        viewModel.PostInsertionState.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                lateDataState = aBoolean;
                if (lateDataState)
                {
                    setClientList();
                    viewModel.PostInsertionState.removeObservers(getViewLifecycleOwner());
                }

            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent(requireContext(), FinishPendingTransService.class);

        requireActivity().stopService(intent);
        viewModel.saveFilterParams(selectesMntka, selectedDay, selectedMain, selectedFary);
      offlineClientsAdapter.disposeAdapterDisposable();

    }

    @Override
    public void onStart() {
        super.onStart();

        selectesMntka = viewModel.mntka;
        selectedDay = viewModel.day;
        selectedMain = viewModel.main;
        selectedFary = viewModel.fary;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }


    void setClientList() {

        offlineBills = new ArrayList<>();


//        dataBase.billDataDaoDao().getDistinctBills().observe(getViewLifecycleOwner(), new Observer<List<BillDataEntity>>() {
//            @Override
//            public void onChanged(List<BillDataEntity> billDataEntities) {
//                offlineBills.clear();
//                offlineBills.addAll(billDataEntities);
//                offlineClientsAdapter = AdapterOfflineClients.getInstance(MainFragment.this.requireActivity());
//                offlineClientsAdapter.submitList(offlineBills);
//                lv_clients.setAdapter(offlineClientsAdapter);
//                progressDialog.dismiss();
//            }
//        });

//        viewModel.getPagedBillsData().observe(getViewLifecycleOwner(), new Observer<PagingData<BillDataEntity>>() {
//            @Override
//            public void onChanged(PagingData<BillDataEntity> billDataEntityPagingData) {
//                pagingAdapter = PagingClientsAdapter.getInstance(MainFragment.this.requireActivity());
//                pagingAdapter.submitData(MainFragment.this.getLifecycle(),billDataEntityPagingData);
//                    progressDialog.dismiss();
//                lv_clients.setAdapter(pagingAdapter);
//
//
//            }
//        });

        progressDialog.show();
        compositeDisposable.add(viewModel.getPagedBillsData().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PagingData<BillDataEntity>>() {
                    @Override
                    public void accept(PagingData<BillDataEntity> billDataEntityPagingData) throws Throwable {
                        offlineBills.clear();
                        pagingAdapter = PagingClientsAdapter.getInstance(MainFragment.this.requireActivity());
                        pagingAdapter.submitData(MainFragment.this.getLifecycle(),billDataEntityPagingData );


                        lv_clients.setAdapter(pagingAdapter);
                        progressDialog.dismiss();

                    }
                }));

    }


    void filterByMntka() {

        offlineBills = new ArrayList<>();
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsOfMntka(mntakaList.get(selectesMntka))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BillDataEntity>>() {
                    @Override
                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                        offlineBills.addAll(billDataEntities);
                        offlineClientsAdapter = AdapterOfflineClients.getInstance(MainFragment.this.requireActivity());
                        offlineClientsAdapter.submitList(offlineBills);
                        lv_clients.setAdapter(offlineClientsAdapter);

                    }
                }, throwable -> {
                    Log.e("filterByMntka", "filterByMntka: " + throwable.getLocalizedMessage());
                }));


    }

    void getDistinctDaysOfMntka()
    {
        dayList = new ArrayList<>();
        dayList.add(getString(R.string.daily));
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctDaysOfMntka(mntakaList.get(selectesMntka))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Throwable {
                        if (strings != null && !strings.isEmpty())
                            dayList.addAll(strings);
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_dropdown_item, dayList);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_day.setAdapter(dataAdapter);


                        if (selectedDay != null) {
                            sp_day.setSelection(selectedDay);

                        }

                    }
                }, throwable -> {
                    Log.e("filterByMntka", "filterByMntka: " + throwable.getLocalizedMessage());
                }));
    }

    private void filterByMntkaIfPosZero() {


//        dataBase.billDataDaoDao().getDistinctBills().observe(getViewLifecycleOwner(), new Observer<List<BillDataEntity>>() {
//            @Override
//            public void onChanged(List<BillDataEntity> billDataEntities) {
//                offlineBills.clear();
//                offlineBills.addAll(billDataEntities);
//                offlineClientsAdapter = AdapterOfflineClients.getInstance(MainFragment.this.requireActivity());
//                offlineClientsAdapter.submitList(offlineBills);
//                lv_clients.setAdapter(offlineClientsAdapter);
//            }
//        });

        progressDialog.show();
        compositeDisposable.add(viewModel.getPagedBillsData().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PagingData<BillDataEntity>>() {
                    @Override
                    public void accept(PagingData<BillDataEntity> billDataEntityPagingData) throws Throwable {
                        offlineBills.clear();
                        pagingAdapter = PagingClientsAdapter.getInstance(MainFragment.this.requireActivity());
                        pagingAdapter.submitData(MainFragment.this.getLifecycle(),billDataEntityPagingData );


                        lv_clients.setAdapter(pagingAdapter);
                        progressDialog.dismiss();

                    }
                }));

        selectesMntka = 0;

        resetDaysList();
        resetMainList();
        resetFary();



    }

    void resetDaysList()
    {
        selectedDay = 0;
        dayList = new ArrayList<>();
        dayList.add(getString(R.string.daily));
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, dayList);
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_day.setAdapter(daysAdapter);
    }

    void resetMainList()
    {
        selectedMain = 0;
        mainList = new ArrayList<>();
        mainList.add(getString(R.string.main_code));
        ArrayAdapter<String> mainAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, mainList);
        mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_main.setAdapter(mainAdapter);
    }

    void resetFary()
    {
        selectedFary = 0;
        faryList = new ArrayList<>();
        faryList.add(getString(R.string.fary_code));
        ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, faryList);
        faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_fary.setAdapter(faryAdapter);
    }

    void filterByDay() {


        offlineBills = new ArrayList<>();
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsByMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BillDataEntity>>() {
                    @Override
                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                        if (billDataEntities != null)
                            offlineBills.addAll(billDataEntities);
                        offlineClientsAdapter = AdapterOfflineClients.getInstance(MainFragment.this.requireActivity());
                        offlineClientsAdapter.submitList(offlineBills);
                        lv_clients.setAdapter(offlineClientsAdapter);
                    }
                }, throwable -> {
                    Log.e(null, "filterByDay: " + throwable.getLocalizedMessage());
                }));


    }

    void getDistinctMains()
    {
        mainList = new ArrayList<>();
        mainList.add(getString(R.string.main_code));
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctMainsOfMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Throwable {
                        if (strings != null)
                            mainList.addAll(strings);
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireActivity(),
                                android.R.layout.simple_spinner_dropdown_item, mainList);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_main.setAdapter(dataAdapter);
                        if (selectedMain != null)
                            sp_main.setSelection(selectedMain);
                    }
                }, throwable -> {
                    Log.e("filterByDay", "filterByDay: " + throwable.getLocalizedMessage());
                }));
    }

    void filterByMain() {


        offlineBills = new ArrayList<>();
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsByMntkaDayAndMain(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BillDataEntity>>() {
                    @Override
                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                        if (billDataEntities != null)
                            offlineBills.addAll(billDataEntities);
                        offlineClientsAdapter = AdapterOfflineClients.getInstance(MainFragment.this.requireActivity());
                        offlineClientsAdapter.submitList(offlineBills);
                        lv_clients.setAdapter(offlineClientsAdapter);
                    }
                }, throwable -> {
                    Log.e(null, "filterByMain: " + throwable.getLocalizedMessage());
                }));


    }

    void getDistictFaryList()
    {
        faryList = new ArrayList<>();
        faryList.add(getString(R.string.fary_code));

        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctFaryOfMntkaAndDayAndMain(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Throwable {
                        faryList.addAll(strings);
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_dropdown_item, faryList);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_fary.setAdapter(dataAdapter);
                        if (selectedFary != null)
                            sp_fary.setSelection(selectedFary);

                    }
                }, throwable -> {
                    Log.e("filterByMain", "filterByMain: " + throwable.getLocalizedMessage());
                }));
    }

    void filterByFary() {

        offlineBills = new ArrayList<>();
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsByMntkaDayMainAndFary(mntakaList.get(selectesMntka), dayList.get(selectedDay), mainList.get(selectedMain), faryList.get(selectedFary))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BillDataEntity>>() {
                    @Override
                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                        offlineBills.addAll(billDataEntities);
                        offlineClientsAdapter = AdapterOfflineClients.getInstance(requireActivity());
                        offlineClientsAdapter.submitList(offlineBills);
                        lv_clients.setAdapter(offlineClientsAdapter);
                    }
                }, throwable -> {
                    Log.e(null, "filterByFary: " + throwable.getLocalizedMessage());
                }));


    }

    void getBillsByClientName() {

        offlineBills = new ArrayList<>();
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsByClientName(et_clientName.getText().toString().trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BillDataEntity>>() {
                    @Override
                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                        offlineClientsAdapter = AdapterOfflineClients.getInstance(requireActivity());
                        offlineClientsAdapter.submitList(offlineBills);
                        lv_clients.setAdapter(offlineClientsAdapter);

                    }
                }, throwable -> {
                    Log.e("FilterByname", "getBillsByClientName: " + throwable.getLocalizedMessage());
                }));

    }


    @Override
    public void onClick(String id) {
        clientId = id;

        if (Utils.checkConnection(requireActivity())) {

            if (!MiniaElectricity.getPrefsManager().isLoggedIn()) {
                requireActivity().finish();
            }


            if (progressDialog != null) {

                progressDialog.show();
            }

            Log.d("Pressed", "onItemClick: clicked ");

            requireActivity().startService(new Intent(requireContext(), FinishPendingTransService.class));
        } else {
//            Bundle bundle = new Bundle();
//            bundle.putString("clientID", clientId);
//            bundle.putBoolean("offline", true);
//            et_clientID.setText("");
//            navController.navigate(R.id.billPaymentFragment, bundle);

            Bundle bundle = new Bundle();
            bundle.putString("clientID", clientId);
            // fragment.setArguments(bundle);
            bundle.putBoolean("offline", false);
            Intent intent = new Intent(requireActivity(), PaymentActivity.class);
            et_clientID.setText("");
            clientId = "";
            intent.putExtra("data",bundle);
            // navController.navigate(R.id.paymentActivity, bundle);
            requireActivity().startActivity(intent);
        }
    }
}