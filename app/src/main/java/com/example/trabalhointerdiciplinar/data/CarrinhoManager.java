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
    private Alteracao ultima;
    private long contador;

    /** Última alteração feita no carrinho, guardada para permitir "Desfazer". */
    private static final class Alteracao {
        final Produto produto;
        final int quantidadeAntes;
        final long id;

        Alteracao(Produto produto, int quantidadeAntes, long id) {
            this.produto = produto;
            this.quantidadeAntes = quantidadeAntes;
            this.id = id;
        }
    }

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
        ultima = new Alteracao(produto, getQuantidade(produto.getId()), ++contador);
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
                ultima = new Alteracao(itens.get(i).getProduto(), itens.get(i).getQuantidade(), ++contador);
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
        ultima = null;
    }

    /** Identificador da última alteração desfazível (0 = nenhuma). */
    public synchronized long ultimaAlteracaoId() {
        return ultima == null ? 0 : ultima.id;
    }

    /**
     * Desfaz a alteração {@code id}, só se ela ainda for a mais recente: uma ação antiga nunca
     * sobrescreve mudanças posteriores. Devolve se algo foi desfeito.
     */
    public synchronized boolean desfazer(long id) {
        if (ultima == null || ultima.id != id) return false;
        Alteracao a = ultima;
        ultima = null;
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getProduto().getId().equals(a.produto.getId())) {
                if (a.quantidadeAntes <= 0) itens.remove(i);
                else itens.get(i).setQuantidade(a.quantidadeAntes);
                return true;
            }
        }
        if (a.quantidadeAntes > 0) itens.add(new ItemCarrinho(a.produto, a.quantidadeAntes));
        return true;
    }

    public int getQuantidade(String produtoId) {
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId().equals(produtoId)) return item.getQuantidade();
        }
        return 0;
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
