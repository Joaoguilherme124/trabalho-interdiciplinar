package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.VH> {

    public interface Listener {
        void onAvancarStatus(Pedido pedido);
    }

    private final List<Pedido> itens = new ArrayList<>();
    private final boolean modoLojista;
    private final Listener listener;

    public PedidoAdapter(boolean modoLojista, Listener listener) {
        this.modoLojista = modoLojista;
        this.listener = listener;
    }

    public void submit(List<Pedido> data) {
        itens.clear();
        if (data != null) itens.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Pedido p = itens.get(position);
        holder.id.setText("Pedido #" + p.getId());
        holder.loja.setText(p.getNomeLoja());
        holder.data.setText(p.getData());
        holder.status.setText(p.getStatus().name());
        holder.total.setText(MoneyUtils.format(p.getValorTotal()));
        holder.pagamento.setText(p.getFormaPagamento().name());

        if (modoLojista && p.getStatus() != StatusPedido.ENTREGUE
                && p.getStatus() != StatusPedido.CANCELADO) {
            holder.btnAcao.setVisibility(View.VISIBLE);
            holder.btnAcao.setText(labelAcao(p.getStatus()));
            holder.btnAcao.setOnClickListener(v -> {
                if (listener != null) listener.onAvancarStatus(p);
            });
        } else {
            holder.btnAcao.setVisibility(View.GONE);
        }
    }

    private String labelAcao(StatusPedido status) {
        switch (status) {
            case PENDENTE:
                return "Aceitar / Separação";
            case SEPARACAO:
                return "Marcar Enviado";
            case ENVIADO:
                return "Marcar Entregue";
            default:
                return "Atualizar";
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView id, loja, data, status, total, pagamento;
        final Button btnAcao;

        VH(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.txt_id);
            loja = itemView.findViewById(R.id.txt_loja);
            data = itemView.findViewById(R.id.txt_data);
            status = itemView.findViewById(R.id.txt_status);
            total = itemView.findViewById(R.id.txt_total);
            pagamento = itemView.findViewById(R.id.txt_pagamento);
            btnAcao = itemView.findViewById(R.id.btn_acao);
        }
    }
}
