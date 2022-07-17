package com.ebe.miniaelec.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;


/**
 * The type Toast utils.
 */
@UiThread
public class ToastUtils {

    /**
     * Toast object
     */
    private static Toast toast = null;
    /**
     * Current show toast request time
     */
    private static long currentRequestTime = 0;
    /**
     * System default toast show duration time
     */
    private static final int TOAST_DURATION = 8000;
    static CountDownTimer countDownTimer = null;
    private static AlertDialog alert;
    public static final int INT_DISMISS = 1;

    private ToastUtils() {
        //do nothing
    }

    /**
     * Show message.
     *
     * @param strId the str id
     */
    public static void showMessage(@StringRes int strId) {
        showMessage(MiniaElectricity.getInstance(), MiniaElectricity.getInstance().getString(strId));
    }

    /**
     * Show message.
     *
     * @param message the message
     */
    public static void showMessage(String message) {
        showMessage(MiniaElectricity.getInstance(), message);
    }

    public static void showMessage(final Activity context, final String msg) {
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == INT_DISMISS) {
                    if (alert != null) {
                        alert.dismiss();
                    }
                }
            }
        };
        MiniaElectricity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                LayoutInflater inflater = context.getLayoutInflater();
                alert = alertDialog.create();
                alert.setCancelable(false);
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                View dialogView = inflater.inflate(R.layout.toast_layout, null);
                alert.setView(dialogView);
                TextView textView = (TextView) dialogView.findViewById(R.id.message);
                textView.setText(msg);
                alert.show();
                Message dismissMsg = handler.obtainMessage();
                dismissMsg.what = INT_DISMISS;
                handler.sendMessageDelayed(dismissMsg, 5000);
            }
        });
    }

    /**
     * Show message.
     *
     * @param context the context
     * @param message the message
     */
    @UiThread
    public static void showMessage(final Context context, final String message) {
        MiniaElectricity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                countDownTimer = new CountDownTimer(TOAST_DURATION, 3500) {
                    public void onTick(long millisUntilFinished) {
                        if (toast != null && context != null) {
//                            toast.cancel();
                            toast.show();
                            Log.e("onTick", "" + millisUntilFinished);

                        }
                        /*if (millisUntilFinished < 3500) {
                            toast = null;
                            countDownTimer.cancel();
                        }*/

                        // Log.e("onTick", "" + (toast == null));
                    }

                    public void onFinish() {
                        Log.e("onFinish", "" + TOAST_DURATION);
                        if (toast != null && context != null && countDownTimer != null) {
                            toast.cancel();
                            toast = null;
                            countDownTimer.cancel();
                        }
                    }
                };

                //if (toast == null) {
                LayoutInflater inflate = LayoutInflater.from(context);
                View view = inflate.inflate(R.layout.toast_layout, null);
                TextView textView = (TextView) view.findViewById(R.id.message);
                // Create new toast
                toast = new Toast(context);
                toast.setView(view);
                textView.setText(message);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);// set gravity center
                //toast.show();
                countDownTimer.start();
                // }
            }
        });
    }
}
