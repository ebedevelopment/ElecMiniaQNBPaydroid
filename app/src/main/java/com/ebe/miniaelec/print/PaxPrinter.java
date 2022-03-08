package com.ebe.miniaelec.print;

import android.graphics.Bitmap;
import android.util.Log;

import com.ebe.miniaelec.MiniaElectricity;
import com.pax.dal.IPrinter;
import com.pax.dal.exceptions.PrinterDevException;


public class PaxPrinter {

    private static PaxPrinter paxPrinter;
    private final IPrinter printer;
    private final String TAG = "PaxPrinter";

    private PaxPrinter() {
        printer = MiniaElectricity.getPrinter();
    }

    public static PaxPrinter getInstance() {
        if (paxPrinter == null) {
            paxPrinter = new PaxPrinter();
        }
        return paxPrinter;
    }

    void init() {
        try {
            printer.init();
            printer.setGray(4);
            Log.i(TAG, "init");
        } catch (PrinterDevException e) {
            e.printStackTrace();
            Log.i(TAG, "init: " + e.toString());
        }
    }

    public int getStatus() {
        try {
            int status = printer.getStatus();
            Log.i(TAG, "getStatus");
            return (status);
        } catch (PrinterDevException e) {
            e.printStackTrace();
            Log.i(TAG, "getStatus" + e.toString());
            return -1;
        }

    }


    void printBitmap(Bitmap bitmap) {
        try {
            printer.printBitmap(bitmap);
            Log.i(TAG, "printBitmap");
        } catch (PrinterDevException e) {
            e.printStackTrace();
            Log.i(TAG, "printBitmap" + e.toString());
        }
    }

    String start() {
        try {
            int res = printer.start();
            Log.i(TAG, "start");
            return statusCode2Str(res);
        } catch (PrinterDevException e) {
            e.printStackTrace();
            Log.i(TAG, "start" + e.toString());
            return "";
        }

    }

    private String statusCode2Str(int status) {
        String res = "";
        switch (status) {
            case 0:
                res = "Success";
                break;
            case 1:
                res = "Printer is busy ";
                break;
            case 2:
                res = "Out of paper ";
                break;
            case 3:
                res = "The format of print data packet error ";
                break;
            case 4:
                res = "Printer malfunctions ";
                break;
            case 8:
                res = "Printer over heats ";
                break;
            case 9:
                res = "Printer voltage is too low";
                break;
            case 240:
                res = "Printing is unfinished ";
                break;
            case 252:
                res = " The printer has not installed font library ";
                break;
            case 254:
                res = "Data package is too long ";
                break;
            default:
                break;
        }
        return res;
    }
}
