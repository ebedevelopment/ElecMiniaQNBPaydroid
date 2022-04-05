package com.ebe.miniaelec.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.AppDataBase;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.database.PrefsManager;
import com.ebe.miniaelec.database.entities.TransBillEntity;
import com.ebe.miniaelec.database.entities.TransDataEntity;
import com.ebe.miniaelec.database.entities.TransDataWithTransBill;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pax.dal.entity.ENavigationKey;
import com.pax.dal.exceptions.PhoneDevException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Utils {
    public static final String DATE_TIME_PATTERN = "ddMMyyyyHHmmss";
    public static final String TIME_PATTERN = "HHmmss";
    public static final String TIME_PATTERN2 = "HH:mm";
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN2 = "dd/MM/yyyy";

    private static CompositeDisposable compositeDisposable = new CompositeDisposable();


    public static void switchSimCard(int simNo) {
        if (Build.MODEL.equalsIgnoreCase("a920")) {
            MiniaElectricity.getDal().getSys().switchSimCard(simNo + 1);//index 1 refers to SIM1 & 2 refers to SIM2
        } else if (Build.MODEL.equalsIgnoreCase("a920pro")) {
            try {
                MiniaElectricity.getDal().getPhoneManager().setDefaultDataSubId(MiniaElectricity.getDal().getPhoneManager().getSubId(simNo)[0]);
            } catch (PhoneDevException e) {
                e.printStackTrace();
            }
        }
//            MiniaElectricity.getDal().getSys().switchSimCard(simNo);

    }

    public static Integer getDefaultDataSubId(Context context) {
        int id = -1;

        try {
            SubscriptionManager sm;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                sm = SubscriptionManager.from(context.getApplicationContext());
                Method getSubId = sm.getClass().getMethod("getDefaultDataPhoneId");
//            Method getSubId = Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("getSubId", new Class[]{Integer.TYPE});
                id = (int) getSubId.invoke(sm);
                Log.i("DEBUG", (int) ((Method) getSubId).invoke(sm) + "");
            }
        } catch (NoSuchMethodException e) {
            try {
                SubscriptionManager sm = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    sm = SubscriptionManager.from(context.getApplicationContext());
                    Method getSubId = sm.getClass().getMethod("getDefaultDataSubscrptionId");
                    id = (int) getSubId.invoke(sm);
                }
            } catch (NoSuchMethodException e1) {
//
                try {

                    SubscriptionManager sm = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        sm = SubscriptionManager.from(context.getApplicationContext());
                        Method getSubId = sm.getClass().getMethod("getDefaultDataSubId");
                        id = (int) getSubId.invoke(sm);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e2) {
                    e2.printStackTrace();
                }
            } catch (IllegalAccessException | InvocationTargetException e1) {
                e1.printStackTrace();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return id;

    }

    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            // Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

            // connected to the mobile provider's data plan
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    public static String decimalFormat(double d) {
        String s = new DecimalFormat("##.##").format(d);
        Locale arabic = new Locale("ar", "EG");
        NumberFormat numberFormat = NumberFormat.getInstance(arabic);
        try {
            numberFormat.setMinimumFractionDigits(0);
            numberFormat.setMaximumFractionDigits(2);
            Number parsed = numberFormat.parse(numberFormat.format(d));
            System.out.println(parsed);
            return String.valueOf(parsed);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static void enableStatusBar(boolean enable) {
        MiniaElectricity.getDal().getSys().enableStatusBar(enable);
    }

    public static void enableHomeRecentKey(boolean enable) {
        MiniaElectricity.getDal().getSys().enableNavigationKey(ENavigationKey.HOME, enable);
        MiniaElectricity.getDal().getSys().enableNavigationKey(ENavigationKey.RECENT, enable);
    }

    public static long calcDifferenceHours(Date endDate, Date startDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;
        return elapsedDays * 24 + elapsedHours;
    }

//    public static void callPermissions(@NonNull final Activity activity, String[] permission, @NonNull Action action, final String failedMsg) {
//        RxPermissions rxPermissions = new RxPermissions(activity);
//        List<Observable<Boolean>> permissionList = new ArrayList<>();
//        for (String s : permission) {
//            permissionList.add(rxPermissions.request(s));
//        }
//        Observable.concat(permissionList)
//                .subscribe(new Consumer<Boolean>() {
//                    @Override
//                    public void accept(Boolean granted) throws Exception {
//                        if (!granted) {
//                            Toast.makeText(activity, failedMsg, Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                    }
//                }, action);
//    }

    public static void slideDown(Context ctx, View v) {

        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    public static void slideUp(Context ctx, View v) {

        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_up);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    public static void copyBillsFromDB(Context context, AppDataBase dataBase) {
        String name = "Bills_" + new SimpleDateFormat("dd-MM-yy", Locale.US)
                .format(new Date(System.currentTimeMillis())) + ".txt";
        File outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);

        try {

            compositeDisposable.add(dataBase.transDataDao().getAllTrans().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<TransDataWithTransBill>>() {
                        @Override
                        public void accept(List<TransDataWithTransBill> transDataWithTransBills) throws Throwable {
                            FileWriter writer = null;
                            writer = new FileWriter(outFile);

                            JsonArray jBills = new JsonArray();
                            for (TransDataWithTransBill b :
                                    transDataWithTransBills) {
                                if (b.getTransBills() != null) {
                                    for (TransBillEntity bill :
                                            b.getTransBills()) {
                                        JsonObject jBill = new JsonObject();
                                        jBill.addProperty("clientId", b.getTransData().getClientID());
                                        jBill.addProperty("RowNum", bill.getRawNum());
                                        jBill.addProperty("SectorName", bill.getSectorName());
                                        jBill.addProperty("BranchName", bill.getBranchName());
                                        jBill.addProperty("ClientAddress", bill.getClientAddress());
                                        jBill.addProperty("ClientActivity", bill.getClientActivity());
                                        jBill.addProperty("ClientPlace", bill.getClientPlace());
                                        jBill.addProperty("CurrentRead", bill.getCurrentRead());
                                        jBill.addProperty("PreviousRead", bill.getPreviousRead());
                                        jBill.addProperty("Consumption", bill.getConsumption());
                                        jBill.addProperty("Installment", bill.getInstallments());
                                        jBill.addProperty("Fees", bill.getFees());
                                        jBill.addProperty("Payments", bill.getPayments());
                                        jBill.addProperty("CommissionValue", bill.getCommissionValue());
                                        jBill.addProperty("ClientName", bill.getClientName());
                                        jBill.addProperty("BillDate", bill.getBillDate());
                                        jBill.addProperty("BillValue", bill.getBillValue());
                                        jBill.addProperty("MntkaCode", bill.getMntkaCode());
                                        jBill.addProperty("DayCode", bill.getDayCode());
                                        jBill.addProperty("MainCode", bill.getMainCode());
                                        jBill.addProperty("FaryCode", bill.getFaryCode());
                                        jBill.addProperty("PrintCount", b.getTransData().getPrintCount());
                                        jBill.addProperty("ReceiptNo", b.getTransData().getStan());
                                        jBill.addProperty("TransDateTime", b.getTransData().getTransDateTime());
                                        jBill.addProperty("ClientMobileNo", b.getTransData().getClientMobileNo());
                                        jBill.addProperty("BankTransactionID", b.getTransData().getBankTransactionID());
                                        jBill.addProperty("DrmData", b.getTransData().getDrmData());
                                        jBill.addProperty("InquiryID", b.getTransData().getInquiryID());
                                        jBill.addProperty("Status", b.getTransData().getStatus());
                                        jBill.addProperty("PaymentType", b.getTransData().getPaymentType());
                                        jBills.add(jBill);

                                    }
                                } else {
                                    JsonObject jBill = new JsonObject();
                                    jBill.addProperty("clientId", b.getTransData().getClientID());
                                    jBill.addProperty("PrintCount", b.getTransData().getPrintCount());
                                    jBill.addProperty("ReceiptNo", b.getTransData().getStan());
                                    jBill.addProperty("TransDateTime", b.getTransData().getTransDateTime());
                                    jBill.addProperty("ClientMobileNo", b.getTransData().getClientMobileNo());
                                    jBill.addProperty("BankTransactionID", b.getTransData().getBankTransactionID());
                                    jBill.addProperty("DrmData", b.getTransData().getDrmData());
                                    jBill.addProperty("InquiryID", b.getTransData().getInquiryID());
                                    jBill.addProperty("Status", b.getTransData().getStatus());
                                    jBill.addProperty("PaymentType", b.getTransData().getPaymentType());
                                    jBills.add(jBill);
                                }
                            }
                            writer.append(jBills.toString());
                            writer.flush();
                            writer.close();
                        }
                    }, throwable -> {
                        Log.e(null, "copyBillsFromDB: " + throwable.getLocalizedMessage());
                    }, () -> {
                        compositeDisposable.dispose();
                    }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNeedDeleteLogs() {
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int lastDeleteLog = new PrefsManager().getLastResetLogsMonth();
        return thisMonth != lastDeleteLog;
    }

    public static String convert(String formattedTime, final String oldPattern, final String newPattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(oldPattern, Locale.US);
        java.util.Date date;
        try {
            date = sdf.parse(formattedTime);
        } catch (ParseException e) {
            return formattedTime;
        }
        sdf = new SimpleDateFormat(newPattern, Locale.US);
        return sdf.format(date);
    }

    public void disposeUtilObservable()
    {
       compositeDisposable.dispose();
    }
}
