package com.ebe.miniaelec.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.model.TransData;

import java.util.ArrayList;


public class AdapterBills extends BaseAdapter {

    public static ViewHolder holder;
    private Context context;
    private ArrayList<TransData> rows;
    private int position;

    public AdapterBills(Context c, ArrayList<TransData> data) {
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
            convertView = inflater.inflate(R.layout.customer_bill_item, parent, false);
            holder = new ViewHolder();

            holder.invoise_number = (TextView) convertView.findViewById(R.id.invoice_number);
            holder.month = (TextView) convertView.findViewById(R.id.month);
            holder.amount = (TextView) convertView.findViewById(R.id.amount);

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
        holder.invoise_number.setText(rows.get(position).getClientID());
       // holder.month.setText(rows.get(position).get);
        holder.month.setText(rows.get(position).getTransDateTime());
    }


    private static class ViewHolder {
        TextView invoise_number, month, amount;
    }
}
