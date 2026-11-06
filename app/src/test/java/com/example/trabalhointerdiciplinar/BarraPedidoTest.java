package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.ItemCarrinho;
import com.example.trabalhointerdiciplinar.util.BarraPedido;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/** A barra flutuante deve sempre refletir o CarrinhoManager (única fonte de verdade). */
public class BarraPedidoTest {

    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();

    @Before
    public void limpar() {
        carrinho.limpar();
    }

    @Test
    public void carrinhoVazioNaoTemUnidadesNemTotalNemMiniaturas() {
        List<ItemCarrinho> itens = carrinho.getItens();
        assertEquals(0, BarraPedido.totalUnidades(itens));
        assertEquals(0.0, BarraPedido.totalValor(itens), 0.001);
        assertTrue(BarraPedido.miniaturas(itens, 2).isEmpty());
        assertEquals(0, BarraPedido.restantes(itens, 2));
        assertEquals(0, BarraPedido.totalUnidades(null));
        assertTrue(BarraPedido.miniaturas(null, 2).isEmpty());
    }

    @Test
    public void totalDeUnidadesSomaQuantidadesEIgualAoDoCarrinho() {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);
        carrinho.adicionar(repo.getProdutoById("p6"), 3);
        assertEquals(5, BarraPedido.totalUnidades(carrinho.getItens()));
        assertEquals(carrinho.getTotalItens(), BarraPedido.totalUnidades(carrinho.getItens()));
    }

    @Test
    public void totalDeValorBateComOCarrinho() {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);
        carrinho.adicionar(repo.getProdutoById("p7"), 1);
        assertEquals(carrinho.getTotal(), BarraPedido.totalValor(carrinho.getItens()), 0.001);
        assertEquals(2 * 42.00 + 5.00, BarraPedido.totalValor(carrinho.getItens()), 0.001);
    }

    @Test
    public void miniaturasLimitadasEComRestantesParaOIndicadorMaisN() {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        carrinho.adicionar(repo.getProdutoById("p2"), 1);
        carrinho.adicionar(repo.getProdutoById("p3"), 1);
        carrinho.adicionar(repo.getProdutoById("p6"), 1);

        List<ItemCarrinho> dois = BarraPedido.miniaturas(carrinho.getItens(), 2);
        assertEquals(2, dois.size());
        assertEquals("p1", dois.get(0).getProduto().getId());   // ordem de adição
        assertEquals("p2", dois.get(1).getProduto().getId());
        assertEquals(2, BarraPedido.restantes(carrinho.getItens(), 2));
        assertEquals(3, BarraPedido.restantes(carrinho.getItens(), 1));
        assertEquals(0, BarraPedido.restantes(carrinho.getItens(), 4));
    }

    @Test
    public void quantidadeNoMesmoProdutoNaoCriaMiniaturaExtra() {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        carrinho.adicionar(repo.getProdutoById("p1"), 4);
        assertEquals(1, BarraPedido.miniaturas(carrinho.getItens(), 2).size());
        assertEquals(5, BarraPedido.miniaturas(carrinho.getItens(), 2).get(0).getQuantidade());
        assertEquals(0, BarraPedido.restantes(carrinho.getItens(), 2));
    }

    @Test
    public void zerarQuantidadeRemoveDaBarra() {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);
        carrinho.atualizarQuantidade("p1", 0);
        assertEquals(0, BarraPedido.totalUnidades(carrinho.getItens()));
        assertEquals(Collections.emptyList(), BarraPedido.miniaturas(carrinho.getItens(), 2));
    }

    @Test
    public void maximoZeroOuNegativoNaoGeraMiniaturas() {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        assertTrue(BarraPedido.miniaturas(carrinho.getItens(), 0).isEmpty());
        assertTrue(BarraPedido.miniaturas(carrinho.getItens(), -1).isEmpty());
        assertEquals(1, BarraPedido.restantes(carrinho.getItens(), 0));
    }
}
