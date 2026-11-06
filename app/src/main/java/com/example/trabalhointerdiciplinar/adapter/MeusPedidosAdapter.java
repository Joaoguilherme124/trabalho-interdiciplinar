package com.example.trabalhointerdiciplinar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;
import com.example.trabalhointerdiciplinar.util.SeloStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista "Meus pedidos" do comprador. É separado de {@link PedidoAdapter}, que continua
 * atendendo o modo lojista sem alterações.
 */
public class MeusPedidosAdapter extends RecyclerView.Adapter<MeusPedidosAdapter.VH> {

    public interface OnClick {
        void onPedidoClick(Pedido pedido);
    }

    private final List<Pedido> itens = new ArrayList<>();
    private final OnClick listener;

    public MeusPedidosAdapter(OnClick listener) {
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
                .inflate(R.layout.item_pedido_comprador, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Pedido p = itens.get(position);
        holder.numero.setText(PedidoApresentacao.numero(p));
        holder.data.setText(p.getData());
        holder.resumo.setText(PedidoApresentacao.resumoItens(p));
        holder.total.setText(MoneyUtils.format(p.getValorTotal()));
        SeloStatus.aplicar(holder.status, p.getStatus());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPedidoClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView numero, status, data, resumo, total;

        VH(@NonNull View itemView) {
            super(itemView);
            numero = itemView.findViewById(R.id.txt_numero);
            status = itemView.findViewById(R.id.txt_status);
            data = itemView.findViewById(R.id.txt_data);
            resumo = itemView.findViewById(R.id.txt_resumo);
            total = itemView.findViewById(R.id.txt_total);
        }
    }
}
