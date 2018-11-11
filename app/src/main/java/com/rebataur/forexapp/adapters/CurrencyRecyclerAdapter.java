package com.rebataur.forexapp.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.rebataur.forexapp.R;
import com.rebataur.forexapp.data.CurrencyData;

import java.util.ArrayList;

public class CurrencyRecyclerAdapter extends RecyclerView.Adapter<CurrencyRecyclerAdapter.CurrencyViewHolder>{

    Context context;
    ArrayList<CurrencyData> list;

    public CurrencyRecyclerAdapter(Context context, ArrayList<CurrencyData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CurrencyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_curr,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        CurrencyData td = list.get(position);

        holder.orig.setText(td.name);
        holder.rate.setText(td.rate);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CurrencyViewHolder extends RecyclerView.ViewHolder
    {
        TextView orig,rate;

        public CurrencyViewHolder(View itemView) {
            super(itemView);
            orig = itemView.findViewById(R.id.originalText);
            rate = itemView.findViewById(R.id.translatedText);
        }
    }
}
