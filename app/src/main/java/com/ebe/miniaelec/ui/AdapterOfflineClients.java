package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.database.DBHelper;
import com.ebe.miniaelec.model.BillData;
import com.ebe.miniaelec.model.OfflineClient;

import java.util.ArrayList;


public class AdapterOfflineClients extends BaseAdapter {

    public static ViewHolder holder;
    private final Context context;
    private final ArrayList<BillData> rows;
    private int position;

    public AdapterOfflineClients(Context c, ArrayList<BillData> data) {
        rows = data;
        context = c;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        holder = null;
        // view = convertView;
        this.position = position;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.client_item, parent, false);
            holder = new ViewHolder();

            holder.main_code = (TextView) convertView.findViewById(R.id.main_code);
            holder.fary_code = (TextView) convertView.findViewById(R.id.fary_code);
            holder.client_name = (TextView) convertView.findViewById(R.id.client_name);
            holder.client_id = (TextView) convertView.findViewById(R.id.client_id);
            holder.bills_amount = (TextView) convertView.findViewById(R.id.bills_amount);
            holder.bills_count = (TextView) convertView.findViewById(R.id.bills_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //  final int temp = (int) getItem(position);
    //    this.position = (int) getItem(position);
        setRow();
        return convertView;

    }

    private void setRow() {
        holder.main_code.setText(rows.get(position).getMainCode());
        holder.fary_code.setText(rows.get(position).getFaryCode());
        holder.client_name.setText(rows.get(position).getClientName());
        holder.client_id.setText(rows.get(position).getClientId());
        OfflineClient client = DBHelper.getInstance(context).getClientByClientId(rows.get(position).getClientId());
        if (client != null) {
            ArrayList<BillData> bills = client.getModelBillInquiryV();
            holder.bills_count.setText("??: " + bills.size());
            double total = 0;
            for (BillData b :
                    bills) {
                total += b.getBillValue();
                total += b.getCommissionValue();
            }
            holder.bills_amount.setText("??: " + total);
        }
    }


    private static class ViewHolder {
        TextView main_code, fary_code, client_name, client_id, bills_count, bills_amount;
    }
}
