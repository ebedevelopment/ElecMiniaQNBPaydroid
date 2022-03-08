package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.Report;

import java.util.ArrayList;


public class AdapterReport extends BaseAdapter {

    public static ViewHolder holder;
    private Context context;
    private ArrayList<Report> rows;
    private int position;

    public AdapterReport(Context c, ArrayList<Report> data) {
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
            convertView = inflater.inflate(R.layout.report_item, parent, false);
            holder = new ViewHolder();

            holder.subscreiber_number = convertView.findViewById(R.id.subscriber_number);
            holder.payment_method = convertView.findViewById(R.id.payment_method);
            holder.amount = convertView.findViewById(R.id.amount);
            holder.duration = convertView.findViewById(R.id.duration);

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
     /*   String temp = context.getString(R.string.subscriber_number) + ": " + rows.get(position).getSubscriberNumber();
        holder.subscreiber_number.setText(temp);
        temp = context.getString(R.string.amount) + ": " + rows.get(position).getAmount();
        holder.amount.setText(temp);
        temp = context.getString(R.string.payment_method) + ": " + rows.get(position).getPaymentMethod();
        holder.payment_method.setText(temp);
        temp = context.getString(R.string.duration) + ": " + rows.get(position).getMonths();
        holder.duration.setText(temp);*/
    }


    private static class ViewHolder {
        TextView subscreiber_number, amount, payment_method, duration;
    }
}
