package com.ebe.miniaelec.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ebe.miniaelec.R;
import com.ebe.miniaelec.data.database.AppDataBase;
import com.ebe.miniaelec.data.database.entities.BillDataEntity;
import com.ebe.miniaelec.data.database.entities.ClientWithBillData;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PagingClientsAdapter extends PagingDataAdapter<BillDataEntity, PagingClientsAdapter.ViewHolder> {


    public static PagingClientsAdapter.BillClickListener billClickListener;
    private static AppDataBase dataBase;
    private static CompositeDisposable disposable;




    private PagingClientsAdapter(@NonNull DiffUtil.ItemCallback<BillDataEntity> diffCallback, Context context) {
        super(diffCallback);
        dataBase = AppDataBase.getInstance(context);
        disposable = new CompositeDisposable();

    }

    public static PagingClientsAdapter getInstance(Context context)
    {

        return new PagingClientsAdapter(new PagingClientsAdapter.diffUtil(),context);
    }

    @NonNull
    @Override
    public PagingClientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_item,parent,false);
        return new PagingClientsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagingClientsAdapter.ViewHolder holder, int position) {

        BillDataEntity bill = getItem(position);
        holder.bind(bill);
    }

    public void disposeAdapterDisposable()
    {
        disposable.clear();
    }


    public static interface BillClickListener
    {
        public abstract void onClick(String id);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        TextView main_code, fary_code, client_name, client_id, bills_count, bills_amount;
        LinearLayout container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            main_code = itemView.findViewById(R.id.main_code);
            fary_code = itemView.findViewById(R.id.fary_code);
            client_name = itemView.findViewById(R.id.client_name);
            client_id = itemView.findViewById(R.id.client_id);
            bills_count = itemView.findViewById(R.id.bills_amount);
            bills_amount = itemView.findViewById(R.id.bills_count);
            container = itemView.findViewById(R.id.item_container);

        }

        void bind(BillDataEntity billDataEntity)
        {
            main_code.setText(billDataEntity.getMainCode());
            fary_code.setText(billDataEntity.getFaryCode());
            client_name.setText(billDataEntity.getClientName());
            client_id.setText(billDataEntity.getClientId());
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    billClickListener.onClick(billDataEntity.getClientId());
                }
            });
            //mainThreadImpl
//            ClientWithBillData clientWithBillData =  dataBase.offlineClientsDao().getClientByClientIdForAdapter(billDataEntity.getClientId());
//            List<BillDataEntity> bills = clientWithBillData.getBills();
//            bills_count.setText("ع: " + bills.size());
//            double total = 0;
//            for (BillDataEntity b :
//                    bills) {
//                total += b.getBillValue();
//                total += b.getCommissionValue();
//            }
//            bills_amount.setText("ق: " + total);




            //background Thread impl
          disposable.add(dataBase.offlineClientsDao().getClientByClientId(billDataEntity.getClientId())
                  .subscribeOn(Schedulers.computation())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Consumer<ClientWithBillData>() {
                      @Override
                      public void accept(ClientWithBillData clientWithBillData) throws Throwable {
                          List<BillDataEntity> bills = clientWithBillData.getBills();
                          bills_count.setText("ع: " + bills.size());
                          double total = 0;
                          for (BillDataEntity b :
                                  bills) {
                              total += b.getBillValue();
                              total += b.getCommissionValue();
                          }
                          bills_amount.setText("ق: " + total);
                      }
                  },throwable -> {
                      Log.e("offlineClientsAdapter", "setRow: "+throwable.getLocalizedMessage() );
                  }));

        }
    }

    private static class diffUtil extends DiffUtil.ItemCallback<BillDataEntity>
    {


        @Override
        public boolean areItemsTheSame(@NonNull BillDataEntity oldItem, @NonNull BillDataEntity newItem) {
            return oldItem.getBillUnique() == newItem.getBillUnique();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull BillDataEntity oldItem, @NonNull BillDataEntity newItem) {
            return oldItem.equals(newItem);
        }
    }
}
