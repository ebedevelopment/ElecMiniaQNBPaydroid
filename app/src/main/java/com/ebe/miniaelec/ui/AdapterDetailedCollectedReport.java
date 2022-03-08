package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.DetailedReport;

import java.util.ArrayList;


public class AdapterDetailedCollectedReport extends BaseAdapter {

    public static ViewHolder holder;
    private Context context;
    private ArrayList<DetailedReport> rows;
    private int position;

    public AdapterDetailedCollectedReport(Context c, ArrayList<DetailedReport> data) {
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
            convertView = inflater.inflate(R.layout.detailed_collected_item, parent, false);
            holder = new ViewHolder();

            holder.date = convertView.findViewById(R.id.date);
            holder.client_id = convertView.findViewById(R.id.client_id);
            holder.time = convertView.findViewById(R.id.trans_time);

            holder.bank_transaction_id = convertView.findViewById(R.id.bank_transaction_id);
            holder.total_amount = convertView.findViewById(R.id.total_amount);

            holder.count = convertView.findViewById(R.id.bills_count);
            holder.payment_type = convertView.findViewById(R.id.payment_type);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //  final int temp = (int) getItem(position);
        this.position = (int) getItem(position);
        setRow();
        return convertView;

    }

    private void setRow() {
        DetailedReport report = rows.get(position);
        holder.date.setText(report.getCollectDate());
        holder.payment_type.setText(report.getPaymentType());
        holder.count.setText("ع: "+String.valueOf(report.getCount()));
        holder.total_amount.setText("ق: "+String.valueOf(report.getAmount()/100));
        holder.client_id.setText(report.getClientID());
        holder.time.setText(report.getCollectTime());
        holder.bank_transaction_id.setText(report.getBankTransactionID());

    }


    private static class ViewHolder {
        TextView date, client_id, total_amount, payment_type, time, bank_transaction_id, count;
    }
}
