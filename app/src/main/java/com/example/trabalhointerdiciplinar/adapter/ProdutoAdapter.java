package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.VH> {

    public interface OnClick {
        void onProdutoClick(Produto produto);
    }

    private final List<Produto> itens = new ArrayList<>();
    private final OnClick listener;

    public ProdutoAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void submit(List<Produto> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produto, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Produto p = itens.get(position);
        holder.titulo.setText(p.getTitulo());
        holder.loja.setText(p.getNomeLoja());
        holder.preco.setText(MoneyUtils.format(p.getPreco()));
        holder.avaliacao.setText(String.format(Locale.getDefault(), "★ %.1f", p.getAvaliacao()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProdutoClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titulo, loja, preco, avaliacao;

        VH(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txt_titulo);
            loja = itemView.findViewById(R.id.txt_loja);
            preco = itemView.findViewById(R.id.txt_preco);
            avaliacao = itemView.findViewById(R.id.txt_avaliacao);
        }
    }
}
