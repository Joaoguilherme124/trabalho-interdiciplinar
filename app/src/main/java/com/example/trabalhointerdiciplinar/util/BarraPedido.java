package com.example.trabalhointerdiciplinar.util;

import com.example.trabalhointerdiciplinar.model.ItemCarrinho;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras da barra flutuante de pedido do Cardápio. Tudo é derivado dos itens do
 * CarrinhoManager (fonte única de verdade); aqui só se decide o que exibir.
 */
public final class BarraPedido {
    private BarraPedido() {
    }

    /** Total de unidades (soma das quantidades), igual ao exibido no Carrinho. */
    public static int totalUnidades(List<ItemCarrinho> itens) {
        int total = 0;
        if (itens == null) return 0;
        for (ItemCarrinho item : itens) total += item.getQuantidade();
        return total;
    }

    public static double totalValor(List<ItemCarrinho> itens) {
        double total = 0;
        if (itens == null) return 0;
        for (ItemCarrinho item : itens) total += item.getSubtotal();
        return total;
    }

    /** Os primeiros {@code max} produtos distintos, na ordem em que foram adicionados. */
    public static List<ItemCarrinho> miniaturas(List<ItemCarrinho> itens, int max) {
        List<ItemCarrinho> resultado = new ArrayList<>();
        if (itens == null || max <= 0) return resultado;
        for (ItemCarrinho item : itens) {
            if (resultado.size() == max) break;
            resultado.add(item);
        }
        return resultado;
    }

    /** Quantos produtos distintos não couberam nas miniaturas (para o indicador "+N"). */
    public static int restantes(List<ItemCarrinho> itens, int max) {
        if (itens == null) return 0;
        return Math.max(0, itens.size() - Math.max(0, max));
    }
}
