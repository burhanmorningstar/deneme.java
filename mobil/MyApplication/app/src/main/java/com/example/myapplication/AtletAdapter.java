package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.List;

public class AtletAdapter extends RecyclerView.Adapter<AtletAdapter.ViewHolder> {

    private Context context;
    private List<Atlet> atletList;

    public AtletAdapter(Context context, List<Atlet> atletList) {
        this.context = context;
        this.atletList = atletList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_atlet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Atlet atlet = atletList.get(position);
        holder.textViewName.setText(atlet.getName());
        holder.textViewCountry.setText(atlet.getCountry());
        holder.textViewAge.setText(atlet.getAge());
        holder.textViewDisciplinesName.setText(atlet.getDisciplines());
    }

    @Override
    public int getItemCount() {
        return atletList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAge;
        TextView textViewDisciplinesName;
        TextView textViewName;
        TextView textViewCountry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.atletName);
            textViewCountry = itemView.findViewById(R.id.atletUlke);
            textViewAge = itemView.findViewById(R.id.age); // Yeni eklediğiniz öğe
            textViewDisciplinesName = itemView.findViewById(R.id.disciplinesName); // Yeni eklediğiniz öğe
        }
    }
}
