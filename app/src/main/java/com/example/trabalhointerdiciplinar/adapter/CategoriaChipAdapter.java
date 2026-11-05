package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;

import java.util.Arrays;
import java.util.List;

public class CategoriaChipAdapter extends RecyclerView.Adapter<CategoriaChipAdapter.VH> {

    public interface OnSelect {
        void onCategoria(String categoria);
    }

    private final List<String> categorias;
    private final OnSelect listener;
    private int selected = 0;

    public CategoriaChipAdapter(String[] categorias, OnSelect listener) {
        this.categorias = Arrays.asList(categorias);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chip_categoria, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String cat = categorias.get(position);
        holder.chip.setText(cat);
        holder.chip.setBackgroundResource(position == selected
                ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        holder.chip.setOnClickListener(v -> {
            int old = selected;
            selected = holder.getBindingAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selected);
            if (listener != null) listener.onCategoria(cat);
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView chip;

        VH(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.txt_chip);
        }
    }
}
