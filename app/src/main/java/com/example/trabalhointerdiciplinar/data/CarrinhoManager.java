package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.model.Produto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CarrinhoManager {
    private static CarrinhoManager instance;
    private final List<ItemCarrinho> itens = new ArrayList<>();

    private CarrinhoManager() {
    }

    public static synchronized CarrinhoManager getInstance() {
        if (instance == null) {
            instance = new CarrinhoManager();
        }
        return instance;
    }

    public List<ItemCarrinho> getItens() {
        return itens;
    }

    public void adicionar(Produto produto, int quantidade) {
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId().equals(produto.getId())) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        itens.add(new ItemCarrinho(produto, quantidade));
    }

    public void atualizarQuantidade(String produtoId, int quantidade) {
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getProduto().getId().equals(produtoId)) {
                if (quantidade <= 0) {
                    itens.remove(i);
                } else {
                    itens.get(i).setQuantidade(quantidade);
                }
                return;
            }
        }
    }

    public void remover(String produtoId) {
        atualizarQuantidade(produtoId, 0);
    }

    public void limpar() {
        itens.clear();
    }

    public int getTotalItens() {
        int total = 0;
        for (ItemCarrinho item : itens) {
            total += item.getQuantidade();
        }
        return total;
    }

    public double getTotal() {
        double total = 0;
        for (ItemCarrinho item : itens) {
            total += item.getSubtotal();
        }
        return total;
    }

    /** Agrupa itens por id da loja. */
    public Map<String, List<ItemCarrinho>> agruparPorLoja() {
        Map<String, List<ItemCarrinho>> mapa = new LinkedHashMap<>();
        for (ItemCarrinho item : itens) {
            String lojaId = item.getProduto().getIdLoja();
            if (!mapa.containsKey(lojaId)) {
                mapa.put(lojaId, new ArrayList<>());
            }
            mapa.get(lojaId).add(item);
        }
        return mapa;
    }
}
