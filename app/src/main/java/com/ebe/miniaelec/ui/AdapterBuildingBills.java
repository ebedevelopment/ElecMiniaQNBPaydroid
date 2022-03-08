package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.Bill;

import java.util.ArrayList;


public class AdapterBuildingBills extends BaseAdapter {

    public static ViewHolder holder;
    private Context context;
    private ArrayList<Bill> rows;
    private int position;

    public AdapterBuildingBills(Context c, ArrayList<Bill> data) {
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
            convertView = inflater.inflate(R.layout.building_bill_item, parent, false);
            holder = new ViewHolder();

            holder.subscriber_number = convertView.findViewById(R.id.subscriber_number);
            holder.subscriber_name = convertView.findViewById(R.id.subscriber_name);
            holder.bills_count = convertView.findViewById(R.id.bills_count);

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

        holder.subscriber_number.setText(rows.get(position).getCustomerNo());
        holder.subscriber_name.setText(rows.get(position).getCustomerName());
        holder.bills_count.setText(String.valueOf(rows.get(position).getCount()));
    }


    private static class ViewHolder {
        TextView subscriber_number, subscriber_name, bills_count;
    }
}
