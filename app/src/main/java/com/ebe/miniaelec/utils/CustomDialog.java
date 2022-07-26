package com.ebe.miniaelec.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;

public class CustomDialog {


    public static final int INT_DISMISS = 1;
    /**
     * System default toast show duration time
     */
    private static final int TOAST_DURATION = 8000;
    static CountDownTimer countDownTimer = null;
    /**
     * Toast object
     */
    private static Toast toast = null;
    /**
     * Current show toast request time
     */
    private static long currentRequestTime = 0;
    private static AlertDialog alert;

    private CustomDialog() {
        //do nothing
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


        MiniaElectricity.getInstance().runOnUiThread(() -> {
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
           // handler.sendMessageDelayed(dismissMsg, 2000);
        });
    }


   public static void dismissCustomDialog()
    {
        if (alert !=null)
        alert.dismiss();
    }


}
