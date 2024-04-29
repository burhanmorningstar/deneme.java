package com.example.myapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RedbullIcecekAdapter extends RecyclerView.Adapter<RedbullIcecekAdapter.RedbullIcecekViewHolder> {

    private Context context;
    private List<DrinkActivity.RedbullIcecek> icecekler;
    private OnSepeteEkleClickListener sepeteEkleClickListener;

    public RedbullIcecekAdapter(Context context, OnSepeteEkleClickListener sepeteEkleClickListener) {
        this.context = context;
        this.icecekler = new ArrayList<>();
        this.sepeteEkleClickListener = sepeteEkleClickListener;
    }

    public void ekle(DrinkActivity.RedbullIcecek icecek) {
        icecekler.add(icecek);
        notifyItemInserted(icecekler.size() - 1);
    }

    @NonNull
    @Override
    public RedbullIcecekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.redbull_icecek_item, parent, false);
        return new RedbullIcecekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RedbullIcecekViewHolder holder, int position) {
        DrinkActivity.RedbullIcecek icecek = icecekler.get(position);

        holder.tvIsim.setText(icecek.getName());
        holder.tvAciklama.setText(icecek.getDescription());
        holder.tvHacim.setText(String.valueOf(icecek.getVolume()));

        // Görsel yüklendiyse göster
        if (icecek.getResim() != null) {
            holder.ivResim.setImageBitmap(icecek.getResim());
        }

        // Sepete ekle butonu
        holder.btnSepeteEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && sepeteEkleClickListener != null) {
                    sepeteEkleClickListener.onSepeteEkleClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return icecekler.size();
    }

    // RedbullIcecekViewHolder class (create this class)
    public static class RedbullIcecekViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivResim;
        private TextView tvIsim, tvAciklama, tvHacim;
        private Button btnSepeteEkle; // Sepete ekle butonu

        public RedbullIcecekViewHolder(@NonNull View itemView) {
            super(itemView);

            ivResim = itemView.findViewById(R.id.iv_redbull_resmi);
            tvIsim = itemView.findViewById(R.id.tv_redbull_ismi);
            tvAciklama = itemView.findViewById(R.id.tv_redbull_aciklama);
            tvHacim = itemView.findViewById(R.id.tv_redbull_hacmi);
            btnSepeteEkle = itemView.findViewById(R.id.btnSepeteEkle); // Sepete ekle butonu ekle
        }
    }

    public interface OnSepeteEkleClickListener {
        void onSepeteEkleClick(int position);
    }
}