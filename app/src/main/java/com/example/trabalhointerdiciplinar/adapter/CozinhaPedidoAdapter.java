package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Pedido;

import java.util.ArrayList;
import java.util.List;

/** Lista de pedidos da cozinha: o cartão é montado pela tela (que conhece o simulador). */
public class CozinhaPedidoAdapter extends RecyclerView.Adapter<CozinhaPedidoAdapter.VH> {

    public interface Binder {
        void preencher(View card, Pedido pedido);
    }

    private final List<Pedido> itens = new ArrayList<>();
    private final Binder binder;

    public CozinhaPedidoAdapter(Binder binder) {
        this.binder = binder;
    }

    public void submit(List<Pedido> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cozinha_pedido, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        binder.preencher(holder.itemView, itens.get(position));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) {
            super(itemView);
        }
    }
}
