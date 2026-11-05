package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Loja;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LojaAdapter extends RecyclerView.Adapter<LojaAdapter.VH> {

    public interface OnClick {
        void onLojaClick(Loja loja);
    }

    private final List<Loja> itens = new ArrayList<>();
    private final OnClick listener;

    public LojaAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void submit(List<Loja> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loja, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Loja l = itens.get(position);
        String initial = l.getNome().isEmpty() ? "?" : l.getNome().substring(0, 1).toUpperCase(Locale.getDefault());
        holder.avatar.setText(initial);
        holder.nome.setText(l.getNome());
        holder.categoria.setText(l.getCategoria());
        holder.avaliacao.setText(String.format(Locale.getDefault(), "★ %.1f", l.getAvaliacao()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLojaClick(l);
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView avatar, nome, categoria, avaliacao;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.txt_avatar);
            nome = itemView.findViewById(R.id.txt_nome);
            categoria = itemView.findViewById(R.id.txt_categoria);
            avaliacao = itemView.findViewById(R.id.txt_avaliacao);
        }
    }
}
