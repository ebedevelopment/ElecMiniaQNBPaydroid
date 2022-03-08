package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.CollectedReport;

import java.util.ArrayList;


public class AdapterTotalCollectedReport extends BaseAdapter {

    public static ViewHolder holder;
    private Context context;
    private ArrayList<CollectedReport> rows;
    private int position;

    public AdapterTotalCollectedReport(Context c, ArrayList<CollectedReport> data) {
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
            convertView = inflater.inflate(R.layout.total_collected_item, parent, false);
            holder = new ViewHolder();

            holder.date = convertView.findViewById(R.id.date);
            holder.online_cash_amount = convertView.findViewById(R.id.online_cash_amount);
            holder.online_cash_count = convertView.findViewById(R.id.online_cash_bills_count);

            holder.offline_cash_amount = convertView.findViewById(R.id.offline_cash_amount);
            holder.offline_cash_count = convertView.findViewById(R.id.offline_cash_bills_count);

            holder.card_amount = convertView.findViewById(R.id.visa_amount);
            holder.card_count = convertView.findViewById(R.id.visa_bills_count);

            holder.wallet_amount = convertView.findViewById(R.id.wallet_amount);
            holder.wallet_count = convertView.findViewById(R.id.wallet_bills_count);

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
        CollectedReport report = rows.get(position);
        holder.date.setText(report.getCollectDate());
        holder.online_cash_count.setText("ع: " + report.getOnlineCashCount());
        holder.online_cash_amount.setText("ق: " + report.getOnlineCashAmount());
        holder.offline_cash_count.setText("ع: " + report.getOfflineCashCount());
        holder.offline_cash_amount.setText("ق: " + report.getOfflineCashAmount());
        holder.card_count.setText("ع: " + report.getCardCount());
        holder.card_amount.setText("ق: " + report.getCardAmount());
        holder.wallet_count.setText("ع: " + report.getWalletCount());
        holder.wallet_amount.setText("ق: " + report.getWalletAmount());
    }


    private static class ViewHolder {
        TextView date, online_cash_amount, online_cash_count, offline_cash_amount, offline_cash_count, card_amount, card_count, wallet_amount, wallet_count;
    }
}
