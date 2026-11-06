package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.CheckoutService;
import com.example.trabalhointerdiciplinar.data.CheckoutTentativa;
import com.example.trabalhointerdiciplinar.data.CheckoutTentativa.Estado;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/** Pagamento simulado: seleção única, bloqueio de toques repetidos e ausência de duplicidade. */
public class CheckoutTentativaTest {

    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();
    private Usuario usuario;

    @Before
    public void setUp() {
        carrinho.limpar();
        usuario = new Usuario(UUID.randomUUID().toString(), "Teste", "t@t.com", "x",
                TipoUsuario.COMPRADOR, "Rua", "0");
    }

    // ---- Estado da tentativa ----

    @Test
    public void semFormaEscolhidaNaoInicia() {
        CheckoutTentativa t = new CheckoutTentativa();
        assertFalse(t.iniciar());
        assertEquals(Estado.OCIOSA, t.getEstado());
    }

    @Test
    public void somenteUmaFormaFicaSelecionada() {
        CheckoutTentativa t = new CheckoutTentativa();
        assertTrue(t.escolher(FormaPagamento.PIX));
        assertTrue(t.escolher(FormaPagamento.CARTAO));
        assertEquals(FormaPagamento.CARTAO, t.getForma());
    }

    @Test
    public void tocesRepetidosIniciamApenasUmaVez() {
        CheckoutTentativa t = new CheckoutTentativa();
        t.escolher(FormaPagamento.PIX);
        assertTrue(t.iniciar());
        assertFalse(t.iniciar());
        assertFalse(t.iniciar());
        assertEquals(Estado.PROCESSANDO, t.getEstado());
    }

    @Test
    public void naoTrocaDeFormaEnquantoProcessaNemDepoisDeConcluir() {
        CheckoutTentativa t = new CheckoutTentativa();
        t.escolher(FormaPagamento.PIX);
        t.iniciar();
        assertFalse(t.escolher(FormaPagamento.CARTAO));
        assertEquals(FormaPagamento.PIX, t.getForma());

        t.concluir();
        assertEquals(Estado.CONCLUIDA, t.getEstado());
        assertFalse(t.escolher(FormaPagamento.CARTAO));
        assertFalse(t.iniciar());
    }

    @Test
    public void cancelarPermiteNovaTentativa() {
        CheckoutTentativa t = new CheckoutTentativa();
        t.escolher(FormaPagamento.CARTAO);
        t.iniciar();
        t.cancelar();
        assertEquals(Estado.OCIOSA, t.getEstado());
        assertTrue(t.iniciar());
    }

    @Test
    public void concluirSemIniciarNaoMudaOEstado() {
        CheckoutTentativa t = new CheckoutTentativa();
        t.concluir();
        assertEquals(Estado.OCIOSA, t.getEstado());
    }

    // ---- Pedido criado uma única vez, com a forma escolhida ----

    @Test
    public void segundoCheckoutComOMesmoCarrinhoNaoGeraPedidoDuplicado() {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);

        List<Pedido> primeiro = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX,
                repo, "05/11/2026");
        List<Pedido> segundo = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX,
                repo, "05/11/2026");

        assertEquals(1, primeiro.size());
        assertTrue(segundo.isEmpty());
        assertEquals(1, repo.getPedidosDoComprador(usuario.getId()).size());
    }

    @Test
    public void pedidoRegistraAFormaDePagamentoEscolhida() {
        carrinho.adicionar(repo.getProdutoById("p6"), 1);
        Pedido pix = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d").get(0);
        carrinho.adicionar(repo.getProdutoById("p7"), 1);
        Pedido cartao = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.CARTAO, repo, "d").get(0);

        assertEquals(FormaPagamento.PIX, pix.getFormaPagamento());
        assertEquals(FormaPagamento.CARTAO, cartao.getFormaPagamento());
    }

    @Test
    public void totalDoPedidoIgualAoDoCarrinhoAntesDoCheckout() {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);
        carrinho.adicionar(repo.getProdutoById("p6"), 3);
        double totalDoCarrinho = carrinho.getTotal();

        Pedido pedido = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.CARTAO, repo,
                "d").get(0);

        assertEquals(totalDoCarrinho, pedido.getValorTotal(), 0.001);
        assertEquals(0.0, carrinho.getTotal(), 0.001);
    }

    @Test
    public void rotulosDePagamentoSaoLegiveis() {
        assertEquals("Pix", PedidoApresentacao.rotuloPagamento(FormaPagamento.PIX));
        assertEquals("Cartão", PedidoApresentacao.rotuloPagamento(FormaPagamento.CARTAO));
        assertEquals("—", PedidoApresentacao.rotuloPagamento(null));
    }
}
