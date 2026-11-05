package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MerchantProdutoAdapter extends RecyclerView.Adapter<MerchantProdutoAdapter.VH> {

    public interface Listener {
        void onEdit(Produto produto);
        void onDelete(Produto produto);
    }

    private final List<Produto> itens = new ArrayList<>();
    private final Listener listener;

    public MerchantProdutoAdapter(Listener listener) {
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
                .inflate(R.layout.item_merchant_produto, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Produto p = itens.get(position);
        holder.titulo.setText(p.getTitulo());
        holder.preco.setText(MoneyUtils.format(p.getPreco()));
        holder.estoque.setText(String.format(Locale.getDefault(), "Estoque: %d", p.getEstoque()));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(p));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(p));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titulo, preco, estoque;
        final ImageButton btnEdit, btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txt_titulo);
            preco = itemView.findViewById(R.id.txt_preco);
            estoque = itemView.findViewById(R.id.txt_estoque);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
