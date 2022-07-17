package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.SettleMsg;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.BuildConfig;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.PrefsManager;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.OfflineClientEntity;
import com.ebe.miniaelec.data.http.ApiServices;
import com.ebe.miniaelec.data.http.RequestListener;
import com.ebe.miniaelec.data.print.PrintListener;
import com.ebe.miniaelec.data.print.PrintReceipt;
import com.ebe.miniaelec.ui.login.LoginActivity;
import com.ebe.miniaelec.ui.main.MainViewModel;
import com.ebe.miniaelec.ui.main.MainViewModelFactory;
import com.ebe.miniaelec.ui.services.FinishPendingTransService;
import com.ebe.miniaelec.utils.CustomDialog;
import com.ebe.miniaelec.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static Toolbar toolbar;
    private static TextView title;
    private static Activity cntxt;
    private static int BACK_ACTION;
    DrawerLayout dlDrawer;
    NavigationView nvNavigation;
    public ITransAPI transAPI;
    private ApiServices services;
    static int FINISH_PENDING_TRANS_START = 999;
    private boolean isAfterLogin;
    public static NavController navController;
    private AppDataBase dataBase;
    CompositeDisposable disposable;
    DisposableCompletableObserver observer;
    MainViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        disposable = new CompositeDisposable();
        viewModel = new ViewModelProvider(this, new MainViewModelFactory(this.getApplication())).get(MainViewModel.class);



        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_main);
dataBase= AppDataBase.getInstance(this);
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.content);
        assert navHostFragment != null;
         navController = navHostFragment.getNavController();
         services = new ApiServices(this, false);


        cntxt = this;
        toolbar = findViewById(R.id.toolbar);
       // title = findViewById(R.id.title);
        setSupportActionBar(toolbar);

        setStatusBarColor();
        hideToolbar();

//        MiniaElectricity.getPrefsManager().setMaxOfflineHours(48);

        dlDrawer = findViewById(R.id.drawer_layout);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.mainFragment,R.id.totalCollectedBillsFragment,R.id.detailedCollectedBillsFragment,R.id.totalsFetchedBillsFragment)
                        .setDrawerLayout(dlDrawer)
                        .build();
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, dlDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        dlDrawer.addDrawerListener(toggle);
//        toggle.syncState();

        nvNavigation = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(nvNavigation, navController);
        NavigationUI.setupWithNavController(
                toolbar, navController, appBarConfiguration);
        //nvNavigation.getMenu().getItem(0).setChecked(true);
       // nvNavigation.setNavigationItemSelectedListener(this);

        if (navController.getCurrentDestination()== navController.getGraph().findNode(R.id.mainFragment))
        {
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.client_inquiry);
        }

        addDrawerActionListener(nvNavigation);
        transAPI = TransAPIFactory.createTransAPI();
        Bundle bundle = getIntent().getBundleExtra("params");
        if (bundle != null) {
            isAfterLogin = bundle.getBoolean("after_login");
        }

        startService(new Intent(this, FinishPendingTransService.class));

        FinishPendingTransService.loadingState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                {
                    services.showDialog();
                }else
                {
                    services.hideDialog();
                }
            }
        });

        FinishPendingTransService.drmLoadingState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                {
                    services.showDialog();
                }else
                {
                    services.hideDialog();
                }
            }
        });

        FinishPendingTransService.serviceState.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                {
                    if (!MiniaElectricity.getPrefsManager().isLoggedIn()) {
                        finish();
                    } else {
                        onNavigationItemSelected(nvNavigation.getMenu().getItem(0));
                       disposable.add(dataBase.offlineClientsDao().offlineClientsCount()
                               .subscribeOn(Schedulers.io())
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribe(new Consumer<Long>() {
                                   @Override
                                   public void accept(Long aLong) throws Throwable {
                                       if ((MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1 ||

                                               (isAfterLogin && aLong == 0 && Utils.checkConnection(MainActivity.this)))) {
                                           getClientsData();
                                       }else
                                       {
                                           viewModel.PostInsertionState.setValue(true);
                                       }
                                   }
                               },throwable -> {
                                   Log.e("serviceState", "onChanged: "+throwable.getLocalizedMessage() );
                               }));


                    }
                }

            }
        });

    }

    void addDrawerActionListener(NavigationView nv)
    {

        nv.getMenu().findItem(R.id.app_version).setTitle(getString(R.string.app_version) + " " + BuildConfig.VERSION_NAME);

        nv.getMenu().findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            }
        });

        nv.getMenu().findItem(R.id.load_clients_data).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                disposable.add(dataBase.offlineClientsDao().offlineClientsCount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Throwable {
                                if (MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1 || aLong == 0)
                                    startService(new Intent(MainActivity.this,FinishPendingTransService.class));
                                    //getClientsData();
                                else CustomDialog.showMessage(cntxt, "لا يوجد فواتير جديدة");
                            }
                        },throwable -> {
                            Log.e("MainActivity", "onMenuItemClick: "+throwable.getLocalizedMessage() );
                        }));

                return true;
            }
        });

        nv.getMenu().findItem(R.id.nav_settle).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SettleMsg.Request request = new SettleMsg.Request();
                request.setCategory(SdkConstants.CATEGORY_SETTLE);
                request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
                Bundle bundle = new Bundle();
                request.setExtraBundle(bundle);
                transAPI.startTrans(MainActivity.this, request);
                return true;
            }
        });

        nv.getMenu().findItem(R.id.exit).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(cntxt);
                alertDialog.setTitle(cntxt.getString(R.string.exit_password));
                final EditText input = new EditText(cntxt);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(params);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alertDialog.setView(input);
                alertDialog.setPositiveButton(cntxt.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String text = input.getText().toString().trim();
                                if (text.isEmpty()) {
                                    dialog.cancel();
                                } else {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd", Locale.ENGLISH);
                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));
                                    String[] res = simpleDateFormat.format(new Date()).split("/");
                                    int sum = 0;
                                    for (String str : res) {
                                        sum += Integer.parseInt(str);
                                    }
                                    sum = (sum + 55) * 128;
                                    if (String.valueOf(sum).equals(text)) {
                                        Utils.enableStatusBar(true);
                                        Utils.enableHomeRecentKey(true);
                                        dialog.cancel();
                                        finish();
                                    } else
                                        CustomDialog.showMessage(MainActivity.this, "كلمة المرور التي أدخلتها غير صحيحة");
                                }
                            }
                        });
                alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
                return true;
            }
        });

        nv.getMenu().findItem(R.id.nav_check_collected).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(cntxt);
                alertDialog.setTitle(cntxt.getString(R.string.exit_password));
                final EditText pwd_input = new EditText(cntxt);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                pwd_input.setLayoutParams(params);
                pwd_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alertDialog.setView(pwd_input);
                alertDialog.setPositiveButton(cntxt.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String text = pwd_input.getText().toString().trim();
                                if (text.isEmpty()) {
                                    dialog.cancel();
                                } else {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd", Locale.ENGLISH);
                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));
                                    String[] res = simpleDateFormat.format(new Date()).split("/");
                                    int sum = 0;
                                    for (String str : res) {
                                        sum += Integer.parseInt(str);
                                    }
                                    sum = (sum + 55) * 128;
                                    if (String.valueOf(sum).equals(text)) {
                                        new PrintReceipt(cntxt).printTotalCollected(new PrintListener() {
                                            @Override
                                            public void onFinish() {
                                                MiniaElectricity.getPrefsManager().setPaidOnlineBillsCount(0);
                                                MiniaElectricity.getPrefsManager().setPaidOfflineBillsCount(0);
                                                MiniaElectricity.getPrefsManager().setPaidOnlineBillsValue(0);
                                                MiniaElectricity.getPrefsManager().setPaidOfflineBillsValue(0);
                                            }

                                            @Override
                                            public void onCancel() {
                                                //Do nothing
                                            }
                                        });
                                    } else
                                        CustomDialog.showMessage(MainActivity.this, "كلمة المرور التي أدخلتها غير صحيحة");
                                }
                            }
                        });
                alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
                return true;
            }
        });

        nv.getMenu().findItem(R.id.extract_data).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(cntxt);
                alertDialog.setTitle(cntxt.getString(R.string.enter_password));
                final EditText et_input = new EditText(cntxt);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                et_input.setLayoutParams(params);
                et_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                alertDialog.setView(et_input);
                alertDialog.setPositiveButton(cntxt.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String text = et_input.getText().toString().trim();
                                if (text.isEmpty()) {
                                    dialog.cancel();
                                } else {
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd", Locale.ENGLISH);
                                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+2:00"));
                                    String[] res = simpleDateFormat.format(new Date()).split("/");
                                    int sum = 0;
                                    for (String str : res) {
                                        sum += Integer.parseInt(str);
                                    }
                                    sum = (sum + 55) * 128;
                                    if (String.valueOf(sum).equals(text)) {
                                        Utils.copyBillsFromDB(cntxt,dataBase);
                                    } else
                                       CustomDialog.showMessage(MainActivity.this, "كلمة المرور التي أدخلتها غير صحيحة");
                                }
                            }
                        });
                alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
                return true;
            }
        });
    }





    public static void setToolbarVisibility(int visibility) {
        toolbar.setVisibility(visibility);
    }

    public static void setTitleText(String _title) {

        //title.setText(_title);
    }

    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }

    public static void setBackAction(int backAction) {
        BACK_ACTION = backAction;
    }

    @Override
    public void onBackPressed() {

            navController.popBackStack(R.id.mainFragment,false);


    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController.getCurrentDestination()!= navController.getGraph().findNode(R.id.mainFragment))
        {
            navController.popBackStack(R.id.mainFragment,false);
        }
        return super.onSupportNavigateUp();
    }

    public  void getClientsData() {
        services.getClients(new RequestListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    Log.e("getClients", response);
                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String Error = responseBody.optString("Error").trim();
                    String InquiryID = responseBody.optString("InquiryID").trim();
                    JSONArray ModelSerialNoV = responseBody.optJSONArray("ModelSerialNoV");
                    int totalBills = 0;
                    if (ModelSerialNoV != null)
                    {
                        for (int i = 0; i < ModelSerialNoV.length(); i++) {
                            totalBills += Objects.requireNonNull(ModelSerialNoV.getJSONObject(i).optJSONArray("ModelBillInquiryV")).length();
                        }
                    }

                    if (!Error.isEmpty()) {
                        onFailure("فشل في تحميل بيانات المشتركين!\n" + Error);
                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول") || Error.contains("ليس لديك صلاحيات الوصول للهندسه")) {
                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                            cntxt.startActivity(new Intent(cntxt, LoginActivity.class));
                            cntxt.finish();
                        }
                    } else if (!InquiryID.isEmpty()) {
                        MiniaElectricity.getPrefsManager().setInquiryID(InquiryID);
                        if (responseBody.getInt("BillCount") == totalBills)
                        {
                            insertInDB(responseBody);
                        }else
                        {

                            onFailure("فشل في تحميل بيانات المشتركين!");
                        }

                    } else onFailure("فشل في تحميل بيانات المشتركين!");
                } catch (JSONException e) {
                    e.printStackTrace();
                    onFailure(e.getMessage());
                }
            }

            @Override
            public void onFailure(String failureMsg) {
                CustomDialog.showMessage(cntxt, failureMsg);
                Log.e("getClients", failureMsg);

                startService(new Intent(MainActivity.this, FinishPendingTransService.class));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        menuItem.setChecked(true);
        //dlDrawer.closeDrawers();
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_PENDING_TRANS_START) {
            Log.e("onActivityResult", "MainActivity");

        } else {
            BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);

            //when you didn't chose any one
            if (baseResponse == null) {
               CustomDialog.showMessage(cntxt, "حدث خطأ أثناء التسوية!");
                return;
            }
            boolean isTransResponse = baseResponse instanceof TransResponse;
            if (!isTransResponse) {
                if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15) {
                    CustomDialog.showMessage(cntxt, "تمت التسوية بنجاح");
                    startService(new Intent(MainActivity.this,FinishPendingTransService.class));
                } else CustomDialog.showMessage(cntxt, "حدث خطأ أثناء التسوية!");

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
        if (Utils.isNeedDeleteLogs()) {

           disposable.add(dataBase.reportEntityDaoDao().clearReports().subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Consumer<Integer>() {
                       @Override
                       public void accept(Integer integer) throws Throwable {
                           if (integer>0) {
                               new PrefsManager().setLastResetLogsMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
                           }
                       }
                   },throwable -> {
                      Log.e("MainActivity", "onResume: "+throwable.getLocalizedMessage() );
                  }));

        }
        if (!isAfterLogin && MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1) {

            startService(new Intent(this, FinishPendingTransService.class));

        }
    }


    void hideToolbar()
    {
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {

                    if (getSupportActionBar() != null)
                    {
                        if (navDestination.getId() == R.id.onlineReportsFragment)
                        getSupportActionBar().hide();
                        else
                            getSupportActionBar().show();
                    }


            }
        });

    }

    void addMainFragmentTransition()
    {
        if (navController.getCurrentDestination()== navController.getGraph().findNode(R.id.mainFragment))
        getSupportFragmentManager().addFragmentOnAttachListener(new FragmentOnAttachListener() {
            @Override
            public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
                fragmentManager.beginTransaction().setCustomAnimations(R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out).commit();
            }
        });
    }


    void insertInDB(JSONObject responseBody)
    {
        SpotsDialog progressDialog;
        progressDialog = new SpotsDialog(cntxt, R.style.SwitchingProgress);
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONArray ModelSerialNoV = responseBody.optJSONArray("ModelSerialNoV");
      observer = Completable.fromRunnable(new Runnable() {
           @Override
           public void run() {
              // dataBase.offlineClientsDao().clearClients();
              // dataBase.billDataDaoDao().clearBills();

               for (int i = 0; i < ModelSerialNoV.length(); i++) {
                   try {
                       OfflineClientEntity client = new OfflineClientEntity();
                       client.setClientMobileNo(ModelSerialNoV.getJSONObject(i).optString("ClientMobileNo"));
                       client.setSerialNo(ModelSerialNoV.getJSONObject(i).optString("SerialNo"));
                       if (client.getSerialNo() == null || client.getSerialNo().equalsIgnoreCase("null")) {
                           continue;
                       }
                       int finalI = i;
                       long added = dataBase.offlineClientsDao().addOfflineClient(client);
                       JSONArray billsData = ModelSerialNoV.getJSONObject(finalI).optJSONArray("ModelBillInquiryV");
                       for (int j = 0; j < billsData.length(); j++) {
                           BillDataEntity bill = new Gson().fromJson(billsData.getJSONObject(j).toString(), BillDataEntity.class);
                           bill.setClient(client.getId());
                           bill.setClientId(client.getSerialNo());
                           if (added >= 0 && (bill.getMainCode() != null && bill.getMntkaCode() != null && bill.getDayCode() != null && bill.getFaryCode() != null)) {

                               dataBase.billDataDaoDao().newOfflineBillAppend(bill);


                           }

                       }


                   } catch (JSONException e) {
                       e.printStackTrace();
                   }

               }


           }
       }).subscribeOn(Schedulers.computation())
              .observeOn(AndroidSchedulers.mainThread())
               .subscribeWith(new DisposableCompletableObserver() {
                   @Override
                   public void onComplete() {
                       postDataInsertion(responseBody,progressDialog);
                       viewModel.insertionState.setValue(true);

                   }

                   @Override
                   public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                       Log.d(null, "insertInDB: "+e.getMessage());
                   }
               });


    }

    void postDataInsertion(JSONObject responseBody,SpotsDialog progressDialog)
    {
        try {
            MiniaElectricity.getPrefsManager().setMaxOfflineHours(responseBody.getInt("MaxOfflineHoure"));

            MiniaElectricity.getPrefsManager().setMaxOfflineBillCount(responseBody.getInt("MaxOfflineBillCount"));

            MiniaElectricity.getPrefsManager().setMaxOfflineBillValue(responseBody.getInt("MaxOfflineBillValue"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        MiniaElectricity.getPrefsManager().setOfflineStartingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                .format(new Date(System.currentTimeMillis())));
        MiniaElectricity.getPrefsManager().setOfflineBillValue(0);
        MiniaElectricity.getPrefsManager().setOfflineBillCount(0);
        MiniaElectricity.getPrefsManager().setOfflineBillsStatus(0);
        progressDialog.dismiss();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        if (Utils.compositeDisposable!=null)
            Utils.compositeDisposable.dispose();
        if (observer != null)
            observer.dispose();
    }

    private void stoppingDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(cntxt);
        alertDialog.setMessage(cntxt.getString(R.string.err_get_bills));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(cntxt.getResources().getString(R.string.try_again),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        getClientsData();
                    }
                });
        alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                cntxt.startActivity(new Intent(cntxt, LoginActivity.class));
                cntxt.finish();
            }
        });

        alertDialog.show();
    }
}