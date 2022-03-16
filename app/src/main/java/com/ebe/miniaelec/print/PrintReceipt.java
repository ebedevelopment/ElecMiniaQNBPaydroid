package com.ebe.miniaelec.print;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import com.ebe.miniaelec.MiniaElectricity;
import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.ebe.miniaelec.utils.Utils;
import com.pax.gl.page.IPage;
import com.pax.gl.page.PaxGLPage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class PrintReceipt {
    private String data;
    private Handler handler = new Handler();
    private Context context;
    private int index;
    private TransData transData;
    private ArrayList<TransBill> transBills = new ArrayList<>();
    private PrintListener listener;
    private boolean isReport;

    public PrintReceipt(Context context) {
        this.context = context;
    }

    public PrintReceipt(Context context, TransData transData, PrintListener listener) {
        this.context = context;
        //this.transData = transData;
        this.listener = listener;
        printReceipt();
    }

    public PrintReceipt(Context context, ArrayList<TransBill> transBills, PrintListener listener) {
        this.context = context;
        this.transBills = transBills;
        this.transData = transBills.get(0).getTransData();
        this.listener = listener;
        printReceipt();
    }

    public void printReceipt() {
        if (index >= transBills.size()) {
            if (listener != null) listener.onFinish();
            return;
        }
        PaxGLPage paxGLPage = PaxGLPage.getInstance(context);
        IPage page = paxGLPage.createPage();
        page.adjustLineSpace(-7);

/*
        page.addLine().addUnit(BitmapFactory.decodeResource(context.getResources(), R.drawable.minia_elec_logo), IPage.EAlign.CENTER);
        page.addLine().addUnit("\n", 7);
        page.addLine().addUnit(BitmapFactory.decodeResource(context.getResources(), R.drawable.minia_elec_title), IPage.EAlign.CENTER);
        page.addLine().addUnit("\n", 7);
*/
        page.addLine().addUnit(BitmapFactory.decodeResource(context.getResources(), R.drawable.untitled), IPage.EAlign.CENTER);
        page.addLine().addUnit("\n", 7);

        if (transData.getPrintCount() > 1 || transData.getStatus() == TransData.STATUS.REPRINT.getValue()) {
            page.addLine().addUnit("إعادة طباعة", 30, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
            page.addLine().addUnit("\n", 5);
        }

        if (transData.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue())
            page.addLine().addUnit("رقم الفاتورة: f-" + transData.getStan(), 28);
        else page.addLine().addUnit("رقم الفاتورة: " + transData.getStan(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("رقم الاشتراك: " + transData.getClientID(), 28);
        page.addLine().addUnit("\n", 5);


        page.addLine().addUnit("تاريخ العمليه: " + transData.getTransDateTime(), 26);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("قطاع: " + transBills.get(index).getSectorName(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("فرع إيرادات: " + transBills.get(index).getBranchName(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("اسم المشترك: " + transBills.get(index).getClientName(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("نوع النشاط: " + transBills.get(index).getClientActivity(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("شهر الإصدار: " + transBills.get(index).getBillDate(), 28);
        page.addLine().addUnit("\n", 5);


        page.addLine().addUnit("العنوان: " + transBills.get(index).getClientAddress(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("وصف المكان: " + transBills.get(index).getClientPlace(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("القراءة الحالية: " + transBills.get(index).getCurrentRead(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("القراءة السابقة: " + transBills.get(index).getPreviousRead(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("كمية الاستهلاك: " + transBills.get(index).getConsumption(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("أقساط وتسويات :" + transBills.get(index).getInstallments(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("رسوم ودمغات: " + transBills.get(index).getFees(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("دفعات تخصم: " + transBills.get(index).getPayments(), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("قيمه الفاتوره: " + transBills.get(index).getBillValue() + context.getString(R.string.egp), 28);
        page.addLine().addUnit("\n", 5);

        double commission = 0;
        if (transData.getPaymentType() == TransData.PaymentType.CASH.getValue() || transData.getPaymentType() == TransData.PaymentType.OFFLINE_CASH.getValue()) {
            commission = (transBills.get(index).getCommissionValue());
        } else if (transData.getPaymentType() == TransData.PaymentType.CARD.getValue()) {
            commission = (transBills.get(index).getCommissionValue())
                    + (transBills.get(index).getBillValue()) * MiniaElectricity.getPrefsManager().getPercentFees();
        }

        page.addLine().addUnit("خدمات الكترونية: " + Utils.decimalFormat(commission) + context.getString(R.string.egp), 28);
        page.addLine().addUnit("\n", 5);
        //float total = Float.parseFloat(transData.getDetails().get(index).getCommission()) + Float.parseFloat(transData.getDetails().get(index).getBillValue());
        page.addLine().addUnit("المطلوب سداده: " + Utils.decimalFormat((transBills.get(index).getBillValue()) + commission) + context.getString(R.string.egp), 28, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 10);

        page.addLine().addUnit("POWERED BY QNB", 25, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);
        page.addLine()
                .addUnit(BitmapFactory.decodeResource(context.getResources(), R.drawable.qnb_print_logo), IPage.EAlign.CENTER);
        page.addLine().addUnit("\n\n\n", 30);
        print(paxGLPage.pageToBitmap(page, 384));
    }

    public void printTotalCollected(PrintListener listener) {
        isReport = true;
        this.listener = listener;
        PaxGLPage paxGLPage = PaxGLPage.getInstance(context);
        IPage page = paxGLPage.createPage();
        page.adjustLineSpace(-7);

        page.addLine().addUnit(BitmapFactory.decodeResource(context.getResources(), R.drawable.untitled), IPage.EAlign.CENTER);
        page.addLine().addUnit("\n", 7);

        page.addLine().addUnit("كود المحصل: " + MiniaElectricity.getPrefsManager().getCollectorCode(), 28, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);


        page.addLine().addUnit("تاريخ الطباعة: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                .format(new Date(System.currentTimeMillis())), 28);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("عدد الفواتير اوف لاين: " + MiniaElectricity.getPrefsManager().getPaidOfflineBillsCount(), 28);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit("قيم الفواتير اوف لاين: " + MiniaElectricity.getPrefsManager().getPaidOfflineBillsValue() / 100, 28);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit("---------------------------------------------------------", 22, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit("عدد الفواتير اون لاين: " + MiniaElectricity.getPrefsManager().getPaidOnlineBillsCount(), 28);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit("قيم الفواتير اون لاين: " + MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() / 100, 28);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit("---------------------------------------------------------", 22, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit(" إجمالي قيم الفواتير: " + (MiniaElectricity.getPrefsManager().getPaidOnlineBillsValue() + MiniaElectricity.getPrefsManager().getPaidOfflineBillsValue()) / 100, 28, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);
        page.addLine().addUnit("---------------------------------------------------------", 22, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);

        page.addLine().addUnit("POWERED BY QNB", 25, IPage.EAlign.CENTER, IPage.ILine.IUnit.TEXT_STYLE_BOLD);
        page.addLine().addUnit("\n", 5);
        page.addLine()
                .addUnit(BitmapFactory.decodeResource(context.getResources(), R.drawable.qnb_print_logo), IPage.EAlign.CENTER);
        page.addLine().addUnit("\n\n\n", 30);
        print(paxGLPage.pageToBitmap(page, 384));
    }

    public void print(final Bitmap bitmap) {
        if ("PAX".equals(Build.MANUFACTURER.toUpperCase()) || "PAX".equals(Build.BRAND.toUpperCase())) { //case of pax device
            new Thread(new Runnable() {
                public void run() {
                    Log.e("PrinterStatus", String.valueOf(PaxPrinter.getInstance().getStatus()));

                    PaxPrinter.getInstance().init();
                    PaxPrinter.getInstance().printBitmap(bitmap);

                    onShowMessage(PaxPrinter.getInstance().start());

                }
            }).start();

        } else { //for other devices use bluetooth print
            Log.i("printReceipt", "not pax");
        }
    }

    private void onShowMessage(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!message.equals("Success")) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(MiniaElectricity.getInstance().getString(R.string.oops));
                    alertDialog.setMessage(message);
                    alertDialog.setPositiveButton(MiniaElectricity.getInstance().getResources().getString(R.string.try_again),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!isReport)
                                        printReceipt();
                                    else printTotalCollected(listener);
                                    dialog.cancel();
                                }
                            });
                    alertDialog.setNegativeButton(MiniaElectricity.getInstance().getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    if (!isReport) {
                                        index++;
                                        printReceipt();
                                    } else
                                        listener.onCancel();
                                }
                            });
                    alertDialog.show();
                } else {
                    if (!isReport) {
                        index++;
                        printReceipt();
                    } else
                        listener.onFinish();
                }

            }
        });

    }
}
