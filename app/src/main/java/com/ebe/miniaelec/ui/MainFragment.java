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
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.database.entities.TransBillEntity;
import com.ebe.miniaelec.data.database.entities.TransDataEntity;
import com.ebe.miniaelec.data.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.ui.adapters.AdapterOfflineClients;
import com.ebe.miniaelec.ui.services.FinishPendingTransService;
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


public class MainFragment extends Fragment implements View.OnClickListener {


    EditText et_clientID, et_clientName;
    ListView lv_clients;
    private AppDataBase dataBase;
    private CompositeDisposable compositeDisposable;
    ArrayList<OfflineClientEntity> offlineClients;
    ArrayList<BillDataEntity> offlineBills;
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

    ArrayList<TransDataEntity> pendingTransData;

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
        offlineBills = new  ArrayList<>();
        offlineClientsAdapter = new AdapterOfflineClients(this.requireActivity(), offlineBills);

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
        setClientList();
        offlineClientsAdapter.notifyDataSetChanged();

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


                    if (progressDialog != null) {

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
                    navController.navigate(R.id.billPaymentFragment, bundle);

                }
            }
        });
        sp_mntka = view.findViewById(R.id.mntka);
        sp_day = view.findViewById(R.id.day);
        sp_main = view.findViewById(R.id.main_code);
        sp_fary = view.findViewById(R.id.fary_code);



       compositeDisposable.add(dataBase.billDataDaoDao().getDistinctMntka()
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<List<String>>() {
                   @Override
                   public void accept(List<String> strings) throws Throwable {
                       if (strings != null)
                           mntakaList.clear();
                           mntakaList.add(getString(R.string.place));
                       mntakaList.addAll(strings);
                       ArrayAdapter<String> mntkaAdapter = new ArrayAdapter<>(getActivity(),
                               android.R.layout.simple_spinner_dropdown_item, mntakaList);
                       mntkaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                       sp_mntka.setAdapter(mntkaAdapter);
                   }
               },throwable -> {
                   Log.e("getDistinctMntka", "onViewCreated: "+throwable.getLocalizedMessage() );
               }));





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
                    filterByMntka();
                } else {
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
                selectedDay = position;
                if (position != 0) {
                    filterByDay();
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

                    filterByMain();
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
                    filterByFary();

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
                        navController.navigate(R.id.billPaymentFragment, bundle);

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

        offlineClientsAdapter.notifyDataSetChanged();
        if (clientId != null && !clientId.isEmpty()) {



            //clientId = null;
//           compositeDisposable.add(dataBase.offlineClientsDao().getClientByClientId(clientId).subscribeOn(Schedulers.io())
//                   .observeOn(AndroidSchedulers.mainThread())
//                   .subscribe(new Consumer<ClientWithBillData>() {
//                       @Override
//                       public void accept(ClientWithBillData clientWithBillData) throws Throwable {
//                           OfflineClientEntity client = clientWithBillData.getClient();
//                           if (client == null || (clientWithBillData.getBills() == null || clientWithBillData.getBills().size() == 0)) {
//                               offlineBills.remove(selectedClient);
//                               offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
//                               offlineClientsAdapter.notifyDataSetChanged();
//                               lv_clients.setAdapter(offlineClientsAdapter);
//                           } else {
//                               //offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
//                               offlineClientsAdapter.notifyDataSetChanged();
//                               // lv_clients.setAdapter(offlineClientsAdapter);
//                           }
//                       }
//                   },throwable ->{
//                       Log.e("Main Fragment", "onResume: "+throwable.getLocalizedMessage() );
//                   } ));

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

                    compositeDisposable.add(dataBase.transDataDao().getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo())
                            .subscribeOn(Schedulers.io())
                            .onErrorReturn(throwable->{
                                Log.d(null, "onActivityResult: "+throwable.getMessage());
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
                            },throwable -> {
                                Log.e("MainFragment", "onActivityResult: "+throwable.getLocalizedMessage() );
                            }));



                } else {

                    if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15
                            || baseResponse.getRspCode() == -16 || baseResponse.getRspCode() == -17 || baseResponse.getRspCode() == -18) {
                        compositeDisposable.add(dataBase.transDataDao().getTransByRefNo(pendingTransData.get(index - 1).getReferenceNo())
                                .subscribeOn(Schedulers.io())
                                .onErrorReturn(throwable -> {
                                    Log.d(null, "onActivityResult: "+throwable.getMessage());
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
                                },throwable -> {
                                    Log.e("MainFragment", "onActivityResult: "+throwable );
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
                    Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show();
            }
        });

        FinishPendingTransService.goToLogin.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
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


    }


    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent(requireContext(), FinishPendingTransService.class);

        requireActivity().stopService(intent);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    void setClientList() {

offlineBills = new ArrayList<>();
progressDialog.show();

      dataBase.billDataDaoDao().getDistinctBills().observe(getViewLifecycleOwner(), new Observer<List<BillDataEntity>>() {
          @Override
          public void onChanged(List<BillDataEntity> billDataEntities) {
              offlineBills.clear();
              offlineBills.addAll(billDataEntities);
              if (!billDataEntities.isEmpty())
              {
                  long unique =  billDataEntities.get(0).getBillUnique();
              }

              offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
              lv_clients.setAdapter(offlineClientsAdapter);
              offlineClientsAdapter.notifyDataSetChanged();
              progressDialog.dismiss();
          }
      });
//        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBills()
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        billDataEntities -> {
//                    offlineBills = new ArrayList<>();
//                    offlineBills.addAll(billDataEntities);
//                    offlineClientsAdapter = new AdapterOfflineClients(getActivity(), offlineBills);
//                    lv_clients.setAdapter(offlineClientsAdapter);
//                    progressDialog.dismiss();
//                }, throwable -> {
//                    Log.e("setClients", "setClientList: "+throwable.getLocalizedMessage() );
//                }));






    }


    void filterByMntka() {

        offlineBills = new ArrayList<>();
       compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsOfMntka(mntakaList.get(selectesMntka))
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<List<BillDataEntity>>() {
                   @Override
                   public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                       offlineBills.addAll( billDataEntities);
                       offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                       lv_clients.setAdapter(offlineClientsAdapter);

                   }
               },throwable -> {
                   Log.e("filterByMntka", "filterByMntka: "+throwable.getLocalizedMessage() );
               }));

        dayList = new ArrayList<>();
        dayList.add(getString(R.string.daily));
       compositeDisposable.add(dataBase.billDataDaoDao().getDistinctDaysOfMntka(mntakaList.get(selectesMntka))
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<List<String>>() {
                   @Override
                   public void accept(List<String> strings) throws Throwable {
                       if (strings!=null && !strings.isEmpty())
                       dayList.addAll(strings);
                       ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                               android.R.layout.simple_spinner_dropdown_item, dayList);
                       dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                       sp_day.setAdapter(dataAdapter);
                   }
               },throwable -> {
                   Log.e("filterByMntka", "filterByMntka: "+throwable.getLocalizedMessage() );
               }));

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

    private void filterByMntkaIfPosZero() {




        dataBase.billDataDaoDao().getDistinctBills().observe(getViewLifecycleOwner(), new Observer<List<BillDataEntity>>() {
            @Override
            public void onChanged(List<BillDataEntity> billDataEntities) {
                offlineBills.clear();
                offlineBills.addAll(billDataEntities);
                offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                lv_clients.setAdapter(offlineClientsAdapter);
            }
        });

//        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBills()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<List<BillDataEntity>>() {
//                    @Override
//                    public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
//                        offlineBills.addAll(billDataEntities);
//                        offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
//                        lv_clients.setAdapter(offlineClientsAdapter);
//                    }
//                },throwable -> {
//                    Log.e("filterByMntkaZero", "filterByMntkaIfPosZero: "+throwable.getLocalizedMessage() );
//                }));

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
                        offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                        lv_clients.setAdapter(offlineClientsAdapter);
                    }
                },throwable -> {
                    Log.e(null, "filterByDay: "+throwable.getLocalizedMessage() );
                }));





        mainList = new ArrayList<>();
        mainList.add(getString(R.string.main_code));
        compositeDisposable.add(dataBase.billDataDaoDao().getDistinctMainsOfMntkaAndDay(mntakaList.get(selectesMntka), dayList.get(selectedDay))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Throwable {
                        if (strings!=null)
                        mainList.addAll(strings);
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(requireActivity(),
                                android.R.layout.simple_spinner_dropdown_item, mainList);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        sp_main.setAdapter(dataAdapter);
                    }
                },throwable -> {
                    Log.e("filterByDay", "filterByDay: "+throwable.getLocalizedMessage() );
                }));


        faryList = new ArrayList<>();
        faryList.add(getString(R.string.fary_code));
        ArrayAdapter<String> faryAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_dropdown_item, faryList);
        faryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_fary.setAdapter(faryAdapter);


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
                       offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                       lv_clients.setAdapter(offlineClientsAdapter);
                   }
               },throwable -> {
                   Log.e(null, "filterByMain: "+throwable.getLocalizedMessage() );
               }));


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

                   }
               },throwable -> {
                   Log.e("filterByMain", "filterByMain: "+throwable.getLocalizedMessage() );
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
                       offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                       lv_clients.setAdapter(offlineClientsAdapter);
                   }
               },throwable -> {
                   Log.e(null, "filterByFary: "+ throwable.getLocalizedMessage() );
               })) ;


    }

   void getBillsByClientName()
   {

       offlineBills = new ArrayList<>();
       compositeDisposable.add(dataBase.billDataDaoDao().getDistinctBillsByClientName(et_clientName.getText().toString().trim())
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<List<BillDataEntity>>() {
                   @Override
                   public void accept(List<BillDataEntity> billDataEntities) throws Throwable {
                       offlineClientsAdapter = new AdapterOfflineClients(requireActivity(), offlineBills);
                       offlineClientsAdapter.notifyDataSetChanged();
                       lv_clients.setAdapter(offlineClientsAdapter);

                   }
               },throwable -> {
                   Log.e("FilterByname", "getBillsByClientName: "+throwable.getLocalizedMessage() );
               }));

   }


}