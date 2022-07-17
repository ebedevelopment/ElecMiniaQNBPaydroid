package com.ebe.miniaelec.database;

import android.content.Context;
import android.util.Log;

import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.DeductType;
import com.ebe.miniaelec.model.OfflineClient;
import com.ebe.miniaelec.model.Report;
import com.ebe.miniaelec.model.TransBill;
import com.ebe.miniaelec.model.TransData;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DBHelper {
    private static final String TAG = "ServiceDb";

    private RuntimeExceptionDao<TransData, Integer> transDataDao = null;
    private RuntimeExceptionDao<OfflineClient, Integer> offlineClients = null;
    private RuntimeExceptionDao<BillData, Integer> offlineClientBills = null;
    private RuntimeExceptionDao<TransBill, Integer> transBillsDao = null;
    private RuntimeExceptionDao<Report, Integer> reportDao = null;
    private RuntimeExceptionDao<DeductType, Integer> deductsDao = null;


    private BaseDbHelper dbHelper;

    private static DBHelper instance;

    private DBHelper(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }

        return instance;
    }

    private RuntimeExceptionDao<TransData, Integer> getTransDataDao() {
        if (transDataDao == null) {
            transDataDao = dbHelper.getRuntimeExceptionDao(TransData.class);
        }
        return transDataDao;
    }
    private RuntimeExceptionDao<TransBill, Integer> getTransBillDao() {
        if (transBillsDao == null) {
            transBillsDao = dbHelper.getRuntimeExceptionDao(TransBill.class);
        }
        return transBillsDao;
    }

    private RuntimeExceptionDao<Report, Integer> getReportDao() {
        if (reportDao == null) {
            reportDao = dbHelper.getRuntimeExceptionDao(Report.class);
        }
        return reportDao;
    }
    private RuntimeExceptionDao<DeductType, Integer> getDeductsDao() {
        if (deductsDao == null) {
            deductsDao = dbHelper.getRuntimeExceptionDao(DeductType.class);
        }
        return deductsDao;
    }

    private RuntimeExceptionDao<OfflineClient, Integer> getClientsDataDao() {
        if (offlineClients == null) {
            offlineClients = dbHelper.getRuntimeExceptionDao(OfflineClient.class);
        }
        return offlineClients;
    }

    private RuntimeExceptionDao<BillData, Integer> getClientBillsDataDao() {
        if (offlineClientBills == null) {
            offlineClientBills = dbHelper.getRuntimeExceptionDao(BillData.class);
        }
        return offlineClientBills;
    }

    public boolean addOfflineClient(final OfflineClient client) {
        try {
            RuntimeExceptionDao<OfflineClient, Integer> dao = getClientsDataDao();
            //dao.create(billData); // ignore the return value from create
            //Log.e(TAG, "create " + create);
            return dao.create(client) == 1;
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }

    }

    public long offlineClientsCount() {
        try {
            RuntimeExceptionDao<OfflineClient, Integer> dao = getClientsDataDao();
            //dao.create(billData); // ignore the return value from create
            return dao.countOf();
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return 0;
        }

    }

    public boolean addTransData(final TransData transData) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
            return dao.create(transData) == 1;
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }

    }

    public boolean addReport(final Report report) {
        try {
            RuntimeExceptionDao<Report, Integer> dao = getReportDao();
            return dao.create(report) == 1;
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }

    }

    public boolean newOfflineBillAppend(BillData bill) {
        try {
            RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
            return dao.create(bill) == 1;
            //Log.e(TAG, "create " + dao.create(bill));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateOfflineBill(BillData bill) {
        try {
            RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
            dao.update(bill);
            //  Log.e(TAG, "create " + dao.update(bill));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    public void newTransBillAppend(TransBill bill) {
        try {
            RuntimeExceptionDao<TransBill, Integer> dao = getTransBillDao();
            dao.create(bill);
            //Log.e(TAG, "create " + dao.create(bill));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void updateTransBill(TransBill bill) {
        try {
            RuntimeExceptionDao<TransBill, Integer> dao = getTransBillDao();
            dao.update(bill);
            //  Log.e(TAG, "create " + dao.update(bill));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public boolean updateClientData(final OfflineClient client) {
        try {
            RuntimeExceptionDao<OfflineClient, Integer> dao = getClientsDataDao();
            Log.e(TAG, "update" + dao.update(client));
            //dao.update(billData); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }
        return true;
    }

    public boolean updateTransData(final TransData transData) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
            // Log.e(TAG, "update" + dao.update(transData));
            return dao.update(transData) == 1; // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public boolean deleteOfflineClient(final OfflineClient client) {
        try {
            RuntimeExceptionDao<OfflineClient, Integer> dao = getClientsDataDao();
            dao.delete(client); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }
        return true;
    }

    public void deleteOfflineClientBills(final ArrayList<BillData> bills) {
        try {
            RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
            dao.delete(bills); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        }
    }

    public void clearOfflineData() {
        try {
            RuntimeExceptionDao<BillData, Integer> billsDataDao = getClientBillsDataDao();
            List<BillData> bills = getAllOfflineBills();
            billsDataDao.delete(bills); // ignore the return value from create
            RuntimeExceptionDao<OfflineClient, Integer> clientsDataDao = getClientsDataDao();
            List<OfflineClient> clients = getAllClients();
            clientsDataDao.delete(clients); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        }
    }

    public void deleteClientBillByDate(String clientId, String billDate) {
        try {
            RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
            BillData bill = dao.queryBuilder().where().eq("clientId", clientId).and().eq("billDate", billDate).queryForFirst();
            dao.delete(bill); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public void deleteClientBill(String BillUnique) {
        try {
            RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
            BillData bill = dao.queryBuilder().where().eq("BillUnique", BillUnique).queryForFirst();
            dao.delete(bill); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public void deleteTransBill(String BillUnique) {
        try {
            RuntimeExceptionDao<TransBill, Integer> dao = getTransBillDao();
            TransBill bill = dao.queryBuilder().where().eq("BillUnique", BillUnique).queryForFirst();
            dao.delete(bill); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void deleteTransData(final TransData transData) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
            dao.delete(transData); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        }
    }

    public void deleteTransData(final ArrayList<TransData> transData) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
            dao.delete(transData); // ignore the return value from create
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
        }
    }

    public List<TransData> getAllTrans() {
        RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
        return dao.queryForAll();
    }

    public List<OfflineClient> getAllClients() {
        RuntimeExceptionDao<OfflineClient, Integer> dao = getClientsDataDao();
        return dao.queryForAll();
    }

    public List<BillData> getAllOfflineBills() {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        return dao.queryForAll();
    }

    public List<String> getDistinctMntka() {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        ArrayList<String> mntka = new ArrayList<>();
        try {
            List<BillData> billDetails = dao.queryBuilder()
                    .distinct().selectColumns("mntkaCode")
                    .query();
            for (BillData b :
                    billDetails) {
                mntka.add(b.getMntkaCode());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return mntka;
    }

    public List<String> getDistinctCollectedDates() {
        RuntimeExceptionDao<Report, Integer> dao = getReportDao();
        ArrayList<String> dates = new ArrayList<>();
        try {
            List<Report> Reports = dao.queryBuilder()
                    .distinct().selectColumns("transDate")
                    .query();
            for (Report b :
                    Reports) {
                dates.add(b.getTransDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return dates;
    }

    public double getTotalAmountOfPaymentTypeAndDate(String date, int pType) {
        RuntimeExceptionDao<Report, Integer> dao = getReportDao();
        try {
            List<Report> reports = dao.queryBuilder()
                    .selectColumns("totalAmount")
                    .where().eq("transDate", date)
                    .and().eq("paymentType", pType)
                    .query();
            double amount = 0;
            for (Report b :
                    reports) {
                amount += b.getTotalAmount();
            }
            return amount / 100;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return 0;
        }
    }

    public int getTotalCountOfPaymentTypeAndDate(String date, int pType) {
        RuntimeExceptionDao<Report, Integer> dao = getReportDao();
        try {
            List<Report> reports = dao.queryBuilder()
                    .selectColumns("billsCount")
                    .where().eq("transDate", date)
                    .and().eq("paymentType", pType)
                    .query();
            int count = 0;
            for (Report b :
                    reports) {
                count += b.getBillsCount();
            }
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return 0;
        }
    }

    public List<Report> getReports(String date) {
        RuntimeExceptionDao<Report, Integer> dao = getReportDao();
        try {
            return dao.queryBuilder()
                    .where().eq("transDate", date)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }
    public List<Report> getReports() {
        RuntimeExceptionDao<Report, Integer> dao = getReportDao();
        try {
            return dao.queryBuilder()
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }

    public List<String> getDistinctDaysOfMntka(String mntka) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        ArrayList<String> days = new ArrayList<>();
        try {
            List<BillData> billDetails = dao.queryBuilder()
                    .distinct().selectColumns("dayCode")
                    .where().eq("mntkaCode", mntka)
                    .query();
            for (BillData b :
                    billDetails) {
                days.add(b.getDayCode());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return days;
    }

    public List<String> getDistinctMainsOfMntkaAndDay(String mntka, String day) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        ArrayList<String> days = new ArrayList<>();
        try {
            List<BillData> billDetails = dao.queryBuilder()
                    .distinct().selectColumns("mainCode")
                    .where().eq("mntkaCode", mntka)
                    .and().eq("dayCode", day)
                    .query();
            for (BillData b :
                    billDetails) {
                days.add(b.getMainCode());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return days;
    }

    public List<String> getDistinctFaryOfMntkaAndDayAndMain(String mntka, String day, String main) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        ArrayList<String> days = new ArrayList<>();
        try {
            List<BillData> billDetails = dao.queryBuilder()
                    .distinct().selectColumns("faryCode")
                    .where().eq("mntkaCode", mntka)
                    .and().eq("dayCode", day)
                    .and().eq("mainCode", main)
                    .query();
            for (BillData b :
                    billDetails) {
                days.add(b.getFaryCode());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
        }
        return days;
    }

    public List<BillData> getDistinctBills() {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        try {
            List<BillData> items = dao.queryBuilder()
                    .distinct().selectColumns("clientName", /*"mntkaCode", "dayCode",*/ "faryCode", "mainCode", "clientId")
                    .query();
            if (items != null)
                Collections.sort(items, new Comparator<BillData>() {
                    public int compare(BillData o1, BillData o2) {
                        if (o1.getMainCode().equals(o2.getMainCode()))
                            return 0;
                        return Long.parseLong(o1.getMainCode()) < Long.parseLong(o2.getMainCode()) ? -1 : 1;
                    }
                });
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }
    public List<BillData> getDistinctBillsOfMntka(String mntka) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        try {
            List<BillData> items = dao.queryBuilder()
                    .distinct().selectColumns("clientName", /*"mntkaCode", "dayCode",*/ "faryCode", "mainCode", "clientId")
                    .where().eq("mntkaCode", mntka)
                    .query();
            if (items != null)
                Collections.sort(items, new Comparator<BillData>() {
                    public int compare(BillData o1, BillData o2) {
                        if (o1.getMainCode().equals(o2.getMainCode()))
                            return 0;
                        return Long.parseLong(o1.getMainCode()) < Long.parseLong(o2.getMainCode()) ? -1 : 1;
                    }
                });
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }
    public List<BillData> getDistinctBills(String mntka, String day) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        try {
            List<BillData> items = dao.queryBuilder()
                    .distinct().selectColumns("clientName", /*"mntkaCode", "dayCode",*/ "faryCode", "mainCode", "clientId")
                    .where().eq("mntkaCode", mntka).and().eq("dayCode", day)
                    .query();
            if (items != null)
                Collections.sort(items, new Comparator<BillData>() {
                    public int compare(BillData o1, BillData o2) {
                        if (o1.getMainCode().equals(o2.getMainCode()))
                            return 0;
                        return Long.parseLong(o1.getMainCode()) < Long.parseLong(o2.getMainCode()) ? -1 : 1;
                    }
                });
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }

    public List<BillData> getDistinctBills(String mntka, String day, String main) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        try {
            List<BillData> items = dao.queryBuilder()
                    .distinct().selectColumns("clientName", /*"mntkaCode", "dayCode",*/ "faryCode", "mainCode", "clientId")
                    .where().eq("mntkaCode", mntka)
                    .and().eq("dayCode", day)
                    .and().eq("mainCode", main)
                    .query();
            if (items != null)
                Collections.sort(items, new Comparator<BillData>() {
                    public int compare(BillData o1, BillData o2) {
                        if (o1.getMainCode().equals(o2.getMainCode()))
                            return 0;
                        return Long.parseLong(o1.getMainCode()) < Long.parseLong(o2.getMainCode()) ? -1 : 1;
                    }
                });
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }

    public List<BillData> getDistinctBills(String mntka, String day, String main, String fary) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        try {
            List<BillData> items = dao.queryBuilder()
                    .distinct().selectColumns("clientName", /*"mntkaCode", "dayCode",*/ "faryCode", "mainCode", "clientId")
                    .where().eq("mntkaCode", mntka)
                    .and().eq("dayCode", day)
                    .and().eq("mainCode", main)
                    .and().eq("faryCode", fary)
                    .query();
            if (items != null)
                Collections.sort(items, new Comparator<BillData>() {
                    public int compare(BillData o1, BillData o2) {
                        if (o1.getMainCode().equals(o2.getMainCode()))
                            return 0;
                        return Long.parseLong(o1.getMainCode()) < Long.parseLong(o2.getMainCode()) ? -1 : 1;
                    }
                });
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }

    public List<BillData> getDistinctBills(String clientName) {
        RuntimeExceptionDao<BillData, Integer> dao = getClientBillsDataDao();
        try {
            List<BillData> items = dao.queryBuilder()
                    .distinct().selectColumns("clientName", /*"mntkaCode", "dayCode",*/ "faryCode", "mainCode", "clientId")
                    .where().like("clientName", "%" + clientName + "%")
                    .query();
            if (items != null)
                Collections.sort(items, new Comparator<BillData>() {
                    public int compare(BillData o1, BillData o2) {
                        if (o1.getMainCode().equals(o2.getMainCode()))
                            return 0;
                        return Long.parseLong(o1.getMainCode()) < Long.parseLong(o2.getMainCode()) ? -1 : 1;
                    }
                });
            return items;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }

    public TransData getTransByRefNo(int refNo) {
        RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
        if (dao.queryForEq("reference_no", refNo).size() > 0) {
            return dao.queryForEq("reference_no", refNo).get(0);
        }
        return null;
    }

    public TransData getTransByClientId(String clientId) {
        RuntimeExceptionDao<TransData, Integer> dao = getTransDataDao();
        if (dao.queryForEq("client_id", clientId).size() > 0) {
            return dao.queryForEq("client_id", clientId).get(0);
        }
        return null;
    }

    public OfflineClient getClientByClientId(String clientId) {
        RuntimeExceptionDao<OfflineClient, Integer> dao = getClientsDataDao();
        if (dao.queryForEq("client_id", clientId).size() > 0) {
            return dao.queryForEq("client_id", clientId).get(0);
        }
        return null;
    }
    public boolean addDeductType(final DeductType deductType) {
        try {
            RuntimeExceptionDao<DeductType, Integer> dao = getDeductsDao();
            return dao.create(deductType) == 1;
        } catch (RuntimeException e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
    public List<DeductType> getDeductTypes() {
        RuntimeExceptionDao<DeductType, Integer> dao = getDeductsDao();
        try {
            return dao.queryBuilder()
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "", e);
            return null;
        }
    }
}
