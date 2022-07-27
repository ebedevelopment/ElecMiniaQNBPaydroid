package com.ebe.miniaelec;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.ebe.miniaelec.data.database.PrefsManager;
import com.ebe.miniaelec.data.http.UnsafeOkHttpClient;
import com.ebe.miniaelec.data.http.api.API;
import com.pax.dal.IDAL;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MiniaElectricity extends Application {
    private static MiniaElectricity instance;
    private static Locale _local;
    public static API api;
    private static PrefsManager prefsManager;
    private static IDAL dal;
    private static IPrinter printer;
    public static int TERMINAL_TYPE_ANDROID_POS = 2;
    public static int TERMINAL_TYPE_OTHER = -1;
    private static Handler handler;
    private HttpLoggingInterceptor logging;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        handler = new Handler();
        /*if (Utils.getDefaultDataSubId(MiniaElectricity.getInstance()) != 1) {
            getDal().getSys().switchSimCard(2);

        }*/

       logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    public static MiniaElectricity getInstance() {
        return instance;
    }

    public static Locale getLocal() {
        if (_local == null) {
            _local = new Locale("ar");
        }
        return _local;
    }


    public static API getApi(String baseUrl, boolean needCert, Context context) {
        if (needCert) {

            OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder()
                    .connectTimeout(90, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .addInterceptor(getInstance().logging)
                    .build();
            api = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(API.class);
        } else {
            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(getInstance().logging)
                    .build();
            api = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build().create(API.class);
        }
        return api;
    }

    public static IPrinter getPrinter() {
        if (printer == null) {
            printer = getDal().getPrinter();
        }
        return printer;
    }

    public static IDAL getDal() {
        if (dal == null) {
            try {
                dal = NeptuneLiteUser.getInstance().getDal(getInstance());
                // dal.getSys().setSettingsNeedPassword(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dal;
    }

    public static PrefsManager getPrefsManager() {
        if (prefsManager == null) {
            prefsManager = new PrefsManager();
        }
        return prefsManager;
    }

    public int defineDeviceType() {
        if ("PAX".equalsIgnoreCase(Build.MANUFACTURER) || "PAX".equalsIgnoreCase(Build.BRAND)) {
            getPrefsManager().setTerminalType(TERMINAL_TYPE_ANDROID_POS);
            return TERMINAL_TYPE_ANDROID_POS;
        } else {
            getPrefsManager().setTerminalType(TERMINAL_TYPE_OTHER);
            return TERMINAL_TYPE_OTHER;
        }
    }

    public static String getSerial() {
        Map<ETermInfoKey, String> info = dal.getSys().getTermInfo();
        return info.get(ETermInfoKey.SN);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Build.getSerial();
        } else {
            return Build.SERIAL;
        }*/
    }


    public void runOnUiThread(final Runnable runnable) {
        handler.post(runnable);
    }

}
