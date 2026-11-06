package com.example.trabalhointerdiciplinar.data;

import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.model.ItemPedido;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Transforma o carrinho em pedidos (um por loja) e os registra no repositório.
 * Não processa pagamento: no ambiente de demonstração o pagamento é apenas simulado.
 */
public final class CheckoutService {

    private CheckoutService() {
    }

    /**
     * Cria um pedido {@link StatusPedido#PENDENTE} por loja presente no carrinho, grava no
     * repositório e esvazia o carrinho. Carrinho vazio não gera pedidos.
     *
     * @return os pedidos criados, na ordem das lojas no carrinho
     */
    public static List<Pedido> finalizar(Usuario usuario, CarrinhoManager carrinho,
                                         FormaPagamento pagamento, MockDataRepository repo,
                                         String data) {
        List<Pedido> criados = new ArrayList<>();
        Map<String, List<ItemCarrinho>> porLoja = carrinho.agruparPorLoja();

        for (Map.Entry<String, List<ItemCarrinho>> entry : porLoja.entrySet()) {
            List<ItemPedido> itens = new ArrayList<>();
            double total = 0;
            String nomeLoja = entry.getValue().get(0).getProduto().getNomeLoja();
            for (ItemCarrinho item : entry.getValue()) {
                itens.add(new ItemPedido(
                        item.getProduto().getId(),
                        item.getProduto().getTitulo(),
                        item.getQuantidade(),
                        item.getProduto().getPreco()));
                total += item.getSubtotal();
            }
            Pedido pedido = new Pedido(novoId(repo), usuario.getId(), entry.getKey(), nomeLoja,
                    itens, total, pagamento, StatusPedido.PENDENTE, data, usuario.getEndereco());
            repo.addPedido(pedido);
            criados.add(pedido);
        }

        if (!criados.isEmpty()) {
            carrinho.limpar();
        }
        return criados;
    }

    private static String novoId(MockDataRepository repo) {
        String id;
        do {
            id = "ped-" + UUID.randomUUID().toString().substring(0, 6);
        } while (repo.getPedidoById(id) != null);
        return id;
    }
}
