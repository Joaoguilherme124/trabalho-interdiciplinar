package com.example.trabalhointerdiciplinar.ui.buyer;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.trabalhointerdiciplinar.R;
import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.model.ItemPedido;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.util.MoneyUtils;

import java.util.List;

/** Pedaços de interface compartilhados pelas telas de confirmação, lista e detalhes de pedido. */
public final class PedidoViews {
    private PedidoViews() {
    }

    /** Itens do carrinho, com o valor unitário sob o nome (usado no checkout). */
    static void preencherItensCarrinho(LinearLayout container, List<ItemCarrinho> itens) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (ItemCarrinho item : itens) {
            View linha = inflater.inflate(R.layout.item_pedido_linha, container, false);
            ((TextView) linha.findViewById(R.id.txt_linha_qtd)).setText(item.getQuantidade() + "×");
            ((TextView) linha.findViewById(R.id.txt_linha_titulo)).setText(item.getProduto().getTitulo());
            TextView unitario = linha.findViewById(R.id.txt_linha_unitario);
            unitario.setText(MoneyUtils.format(item.getProduto().getPreco()) + " cada");
            unitario.setVisibility(View.VISIBLE);
            ((TextView) linha.findViewById(R.id.txt_linha_subtotal))
                    .setText(MoneyUtils.format(item.getSubtotal()));
            container.addView(linha);
        }
    }

    /** Preenche o container com uma linha por item (quantidade, nome e subtotal) dos pedidos. */
    public static void preencherItens(LinearLayout container, List<Pedido> pedidos) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (Pedido pedido : pedidos) {
            for (ItemPedido item : pedido.getListaItens()) {
                View linha = inflater.inflate(R.layout.item_pedido_linha, container, false);
                ((TextView) linha.findViewById(R.id.txt_linha_qtd))
                        .setText(item.getQuantidade() + "×");
                ((TextView) linha.findViewById(R.id.txt_linha_titulo)).setText(item.getTitulo());
                ((TextView) linha.findViewById(R.id.txt_linha_subtotal))
                        .setText(MoneyUtils.format(item.getSubtotal()));
                container.addView(linha);
            }
        }
    }
}
