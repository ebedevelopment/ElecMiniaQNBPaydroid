package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ebe.ebeunifiedlibrary.factory.ITransAPI;
import com.ebe.ebeunifiedlibrary.factory.TransAPIFactory;
import com.ebe.ebeunifiedlibrary.message.BaseResponse;
import com.ebe.ebeunifiedlibrary.message.SettleMsg;
import com.ebe.ebeunifiedlibrary.message.TransResponse;
import com.ebe.ebeunifiedlibrary.sdkconstants.SdkConstants;
import com.ebe.miniaelec.BuildConfig;
import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.BaseDbHelper;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.database.PrefsManager;
import com.ebe.miniaelec.http.ApiServices;
import com.ebe.miniaelec.http.RequestListener;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.print.PrintListener;
import com.ebe.miniaelec.print.PrintReceipt;
import com.ebe.miniaelec.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static Toolbar toolbar;
    private static TextView title;
    private static Activity cntxt;
    private static int BACK_ACTION;
    DrawerLayout dlDrawer;
    NavigationView nvNavigation;
    public ITransAPI transAPI;
    static int FINISH_PENDING_TRANS_START = 999;
    private boolean isAfterLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale loc = MiniaElectricity.getLocal();
        Configuration config = new Configuration();
        config.locale = loc;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_main);

        cntxt = this;
        toolbar = findViewById(R.id.toolbar);
        title = findViewById(R.id.title);
        setSupportActionBar(toolbar);
        setStatusBarColor();
//        MiniaElectricity.getPrefsManager().setMaxOfflineHours(48);

        dlDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, dlDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        dlDrawer.addDrawerListener(toggle);
        toggle.syncState();

        nvNavigation = findViewById(R.id.nav_view);
        nvNavigation.getMenu().getItem(0).setChecked(true);
        nvNavigation.setNavigationItemSelectedListener(this);
        nvNavigation.getMenu().findItem(R.id.app_version).setTitle(getString(R.string.app_version) + " " + BuildConfig.VERSION_NAME);
        transAPI = TransAPIFactory.createTransAPI();
        Bundle bundle = getIntent().getBundleExtra("params");
        if (bundle != null) {
            isAfterLogin = bundle.getBoolean("after_login");
        }
        startActivityForResult(new Intent(this, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
 /*if (MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1 || (isAfterLogin && DBHelper.getInstance(cntxt).offlineClientsCount() == 0)) {
            getClientsData();
        } else {
            startActivityForResult(new Intent(this, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
            //onNavigationItemSelected(nvNavigation.getMenu().getItem(0));
        }*/

    }

    public static void fragmentTransaction(Fragment fragment, String tag) {
        cntxt.getFragmentManager().beginTransaction().setCustomAnimations(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out,
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out).replace(R.id.content, fragment, tag).commit();
    }

    public static void fragmentAddTransaction(Fragment fragment) {
        cntxt.getFragmentManager().beginTransaction().setCustomAnimations(
                R.animator.card_flip_right_in,
                R.animator.card_flip_right_out,
                R.animator.card_flip_left_in,
                R.animator.card_flip_left_out).replace(R.id.content, fragment).addToBackStack("").commit();
    }

    public static void setToolbarVisibility(int visibility) {
        toolbar.setVisibility(visibility);
    }

    public static void setTitleText(String _title) {
        title.setText(_title);
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
        switch (BACK_ACTION) {
            case 1:
                fragmentTransaction(new NewHomeFragment(), null);
                break;
            case 2:
                getFragmentManager().popBackStack();
                break;
            default:
                //super.onBackPressed();

        }
    }

    public static void getClientsData() {
        new ApiServices(cntxt, false).getClients(new RequestListener() {
            @Override
            public void onSuccess(String response) {
                try {
                    Log.e("getClients", response);
                    JSONObject responseBody = new JSONObject(response.subSequence(response.indexOf("{"), response.length()).toString());
                    String Error = responseBody.optString("Error").trim();
                    String InquiryID = responseBody.optString("InquiryID").trim();
                    if (!Error.isEmpty()) {
                        onFailure("فشل في تحميل بيانات المشتركين!\n" + Error);
                        if (Error.contains("تم انتهاء صلاحية الجلسه") || Error.contains("لم يتم تسجيل الدخول") || Error.contains("ليس لديك صلاحيات الوصول للهندسه")) {
                            MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                            cntxt.startActivity(new Intent(cntxt, LoginActivity.class));
                            cntxt.finish();
                        }
                    } else if (!InquiryID.isEmpty()) {
                        MiniaElectricity.getPrefsManager().setInquiryID(InquiryID);
                        new InsertInDB(responseBody).execute();
                    } else onFailure("فشل في تحميل بيانات المشتركين!");
                } catch (JSONException e) {
                    e.printStackTrace();
                    onFailure(e.getMessage());
                }
            }

            @Override
            public void onFailure(String failureMsg) {
                Toast.makeText(cntxt, failureMsg, Toast.LENGTH_LONG).show();
                Log.e("getClients", failureMsg);
                cntxt.startActivityForResult(new Intent(cntxt, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.logout:
                MiniaElectricity.getPrefsManager().setLoggedStatus(false);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.load_clients_data:
                if (MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1 || DBHelper.getInstance(cntxt).offlineClientsCount() == 0)
                    startActivityForResult(new Intent(this, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                    //getClientsData();
                else Toast.makeText(cntxt, "لا يوجد فواتير جديدة", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_reports:
                Intent intent = new Intent(getBaseContext(), WebViewActivity.class);
                intent.putExtra("url", "http://10.224.246.181:3000/");
                startActivity(intent);
                break;
            case R.id.nav_total_collected:
                fragmentTransaction(new TotalCollectedFragment(), null);
                break;
            case R.id.nav_collected_detailed:
                fragmentTransaction(new DetailedCollectedFragment(), null);
                break;
            case R.id.nav_total_bills:
                fragmentTransaction(new TotalBillsFragment(), null);
                break;
            case R.id.nav_re_print:
                startActivity(new Intent(MainActivity.this, RePrintActivity.class));
                break;
            case R.id.nav_settle:
                SettleMsg.Request request = new SettleMsg.Request();
                request.setCategory(SdkConstants.CATEGORY_SETTLE);
                request.setPackageName(MiniaElectricity.getPrefsManager().getPackageName());
                Bundle bundle = new Bundle();
                request.setExtraBundle(bundle);
                transAPI.startTrans(this, request);
                break;
            case R.id.exit:
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
                                        Toast.makeText(MainActivity.this, "كلمة المرور التي أدخلتها غير صحيحة", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
                break;
            case R.id.nav_check_collected:
                alertDialog = new AlertDialog.Builder(cntxt);
                alertDialog.setTitle(cntxt.getString(R.string.exit_password));
                final EditText pwd_input = new EditText(cntxt);
                params = new LinearLayout.LayoutParams(
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
                                        Toast.makeText(MainActivity.this, "كلمة المرور التي أدخلتها غير صحيحة", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
                break;
            case R.id.extract_data:
                alertDialog = new AlertDialog.Builder(cntxt);
                alertDialog.setTitle(cntxt.getString(R.string.enter_password));
                final EditText et_input = new EditText(cntxt);
                params = new LinearLayout.LayoutParams(
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
                                        Utils.copyBillsFromDB(cntxt);
                                    } else
                                        Toast.makeText(MainActivity.this, "كلمة المرور التي أدخلتها غير صحيحة", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                alertDialog.setNegativeButton(cntxt.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                alertDialog.show();
                break;
            default:
                fragmentTransaction(new NewHomeFragment(), null);
                break;
        }

        menuItem.setChecked(true);
        dlDrawer.closeDrawers();
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINISH_PENDING_TRANS_START) {
            Log.e("onActivityResult", "MainActivity");
            if (!MiniaElectricity.getPrefsManager().isLoggedIn()) {
                finish();
            } else {
                onNavigationItemSelected(nvNavigation.getMenu().getItem(0));
                int flag = MiniaElectricity.getPrefsManager().getOfflineBillStatus();
                Log.d("Flag", "onActivityResult:  " +flag);
                if ((!isAfterLogin && MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1) ||
                        (MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1 || (DBHelper.getInstance(cntxt).offlineClientsCount() == 0 && MiniaElectricity.getPrefsManager().getOfflineBillStatus() != 2 ))) {
                    getClientsData();
                } if (flag ==2)
                {
                    BaseDbHelper.getInstance(cntxt).dropTables();
                }
            }
        } else {
            BaseResponse baseResponse = transAPI.onResult(requestCode, resultCode, data);

            //when you didn't chose any one
            if (baseResponse == null) {
                Toast.makeText(cntxt, "حدث خطأ أثناء التسوية!", Toast.LENGTH_LONG).show();
                return;
            }
            boolean isTransResponse = baseResponse instanceof TransResponse;
            if (!isTransResponse) {
                if (baseResponse.getRspCode() == 0 || baseResponse.getRspCode() == -15) {
                    Toast.makeText(cntxt, "تمت التسوية بنجاح", Toast.LENGTH_LONG).show();
                    startActivityForResult(new Intent(this, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);
                } else Toast.makeText(cntxt, "حدث خطأ أثناء التسوية!", Toast.LENGTH_LONG).show();

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.enableHomeRecentKey(false);
        Utils.enableStatusBar(false);
        if (Utils.isNeedDeleteLogs()) {
            if (BaseDbHelper.getInstance(cntxt).deleteReports()) {
                new PrefsManager().setLastResetLogsMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
            }
        }
        if (!isAfterLogin && MiniaElectricity.getPrefsManager().getOfflineBillStatus() == 1) {
            // getClientsData();
            startActivityForResult(new Intent(this, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);

        }
    }

    public static class InsertInDB extends AsyncTask<Void, Integer, Void> {

        private SpotsDialog progressDialog;
        private final JSONObject responseBody;

        InsertInDB(JSONObject responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new SpotsDialog(cntxt, R.style.SwitchingProgress);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... arg0) {
            JSONArray ModelSerialNoV = responseBody.optJSONArray("ModelSerialNoV");
            //DBHelper.getInstance(cntxt).clearOfflineData();
            BaseDbHelper.getInstance(cntxt).dropTables();
            for (int i = 0; i < ModelSerialNoV.length(); i++) {
                try {
                    OfflineClient client = new OfflineClient();
                    client.setClientMobileNo(ModelSerialNoV.getJSONObject(i).optString("ClientMobileNo"));
                    client.setSerialNo(ModelSerialNoV.getJSONObject(i).optString("SerialNo"));
                    if (client.getSerialNo() == null || client.getSerialNo().equalsIgnoreCase("null")) {
                        continue;
                    }
                    boolean added = DBHelper.getInstance(cntxt).addOfflineClient(client);
                    JSONArray billsData = ModelSerialNoV.getJSONObject(i).optJSONArray("ModelBillInquiryV");
                    // ArrayList<BillDetails> billDetails = new ArrayList<>();
                    for (int j = 0; j < billsData.length(); j++) {
                        BillData bill = new Gson().fromJson(billsData.getJSONObject(j).toString(), BillData.class);
                        bill.setClient(client);
                        bill.setClientId(client.getSerialNo());
                        if (added && (bill.getMainCode() != null && bill.getMntkaCode() != null && bill.getDayCode() != null && bill.getFaryCode() != null)) {
                            DBHelper.getInstance(cntxt).newOfflineBillAppend(bill);
                            DBHelper.getInstance(cntxt).updateOfflineBill(bill);
                        }
                        //billDetails.add(bill);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //client.setModelBillInquiryV(billDetails);
                //offlineClients.add(client);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
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
            //ArrayList<OfflineClient> offlineClients = new ArrayList<>();
            progressDialog.dismiss();
            fragmentTransaction(new NewHomeFragment(), null);
            // cntxt.startActivityForResult(new Intent(cntxt, FinishPendingTransActivity.class), FINISH_PENDING_TRANS_START);

        }

    }
}