package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.CheckoutService;
import com.example.trabalhointerdiciplinar.data.DemoCatalog;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/** Cobre carrinho → checkout demonstrativo → pedido, e a apresentação dos status/QR. */
public class PedidoFluxoTest {

    private static final double DELTA = 0.001;

    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();
    private Usuario usuario;
    private Produto paellaMista;
    private Produto suco;

    @Before
    public void setUp() {
        carrinho.limpar();
        usuario = new Usuario(UUID.randomUUID().toString(), "Teste", "t@t.com", "x",
                TipoUsuario.COMPRADOR, "Rua Teste, 1", "0");
        paellaMista = repo.getProdutoById("p1");
        suco = repo.getProdutoById("p6");
        assertNotNull(paellaMista);
        assertNotNull(suco);
    }

    // ---- Carrinho: total e quantidades ----

    @Test
    public void totalDoCarrinhoSomaPrecoVezesQuantidade() {
        carrinho.adicionar(paellaMista, 2);
        carrinho.adicionar(suco, 3);
        assertEquals(2 * paellaMista.getPreco() + 3 * suco.getPreco(), carrinho.getTotal(), DELTA);
    }

    @Test
    public void adicionarMesmoProdutoSomaQuantidadeEmUmaUnicaLinha() {
        carrinho.adicionar(paellaMista, 1);
        carrinho.adicionar(paellaMista, 2);
        assertEquals(1, carrinho.getItens().size());
        assertEquals(3, carrinho.getQuantidade(paellaMista.getId()));
        assertEquals(3, carrinho.getTotalItens());
    }

    @Test
    public void quantidadeZeroRemoveOItem() {
        carrinho.adicionar(paellaMista, 2);
        carrinho.atualizarQuantidade(paellaMista.getId(), 0);
        assertEquals(0, carrinho.getQuantidade(paellaMista.getId()));
        assertTrue(carrinho.getItens().isEmpty());
        assertEquals(0.0, carrinho.getTotal(), DELTA);
    }

    // ---- Checkout: criação do pedido e transição de estado ----

    @Test
    public void checkoutCriaPedidoPendenteComItensETotalCorretos() {
        carrinho.adicionar(paellaMista, 2);
        carrinho.adicionar(suco, 1);
        double totalEsperado = carrinho.getTotal();

        List<Pedido> criados = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX,
                repo, "05/11/2026");

        assertEquals(1, criados.size());
        Pedido pedido = criados.get(0);
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());
        assertEquals(usuario.getId(), pedido.getIdComprador());
        assertEquals(DemoCatalog.LOJA_ID, pedido.getIdLoja());
        assertEquals(2, pedido.getListaItens().size());
        assertEquals(totalEsperado, pedido.getValorTotal(), DELTA);
        assertEquals(3, PedidoApresentacao.totalDeUnidades(pedido));
        assertEquals("05/11/2026", pedido.getData());
        assertEquals(FormaPagamento.PIX, pedido.getFormaPagamento());
    }

    @Test
    public void checkoutEsvaziaCarrinhoERegistraPedidoNoRepositorio() {
        carrinho.adicionar(paellaMista, 1);

        Pedido pedido = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.CARTAO,
                repo, "05/11/2026").get(0);

        assertTrue(carrinho.getItens().isEmpty());
        assertEquals(pedido, repo.getPedidoById(pedido.getId()));
        assertTrue(repo.getPedidosDoComprador(usuario.getId()).contains(pedido));
    }

    @Test
    public void checkoutComCarrinhoVazioNaoCriaPedido() {
        List<Pedido> criados = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX,
                repo, "05/11/2026");
        assertTrue(criados.isEmpty());
        assertTrue(repo.getPedidosDoComprador(usuario.getId()).isEmpty());
    }

    @Test
    public void idsDePedidosSaoUnicos() {
        carrinho.adicionar(paellaMista, 1);
        Pedido a = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d").get(0);
        carrinho.adicionar(paellaMista, 1);
        Pedido b = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d").get(0);
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    public void statusDoPedidoPodeAvancarDepoisDoCheckout() {
        carrinho.adicionar(suco, 1);
        Pedido pedido = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d")
                .get(0);
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());

        repo.atualizarStatusPedido(pedido.getId(), StatusPedido.SEPARACAO);

        assertEquals(StatusPedido.SEPARACAO, repo.getPedidoById(pedido.getId()).getStatus());
    }

    // ---- Apresentação: status em português, QR de demonstração ----

    @Test
    public void statusSaoTraduzidosParaExibicaoSemAlterarOEnum() {
        assertEquals("Registrado", PedidoApresentacao.rotuloStatus(StatusPedido.PENDENTE));
        assertEquals("Em preparação", PedidoApresentacao.rotuloStatus(StatusPedido.SEPARACAO));
        assertEquals("Pronto para retirar", PedidoApresentacao.rotuloStatus(StatusPedido.ENVIADO));
        assertEquals("Retirado", PedidoApresentacao.rotuloStatus(StatusPedido.ENTREGUE));
        assertEquals("Cancelado", PedidoApresentacao.rotuloStatus(StatusPedido.CANCELADO));
        // valores internos preservados
        assertEquals("SEPARACAO", StatusPedido.SEPARACAO.name());
    }

    @Test
    public void todoStatusPossuiRotulo() {
        for (StatusPedido s : StatusPedido.values()) {
            assertNotEquals("Sem rótulo para " + s, s.name(), PedidoApresentacao.rotuloStatus(s));
        }
    }

    @Test
    public void qrDemoApareceSomenteEmPedidosAtivos() {
        assertTrue(PedidoApresentacao.exibeQrDemo(StatusPedido.PENDENTE));
        assertTrue(PedidoApresentacao.exibeQrDemo(StatusPedido.SEPARACAO));
        assertTrue(PedidoApresentacao.exibeQrDemo(StatusPedido.ENVIADO));
        assertFalse(PedidoApresentacao.exibeQrDemo(StatusPedido.ENTREGUE));
        assertFalse(PedidoApresentacao.exibeQrDemo(StatusPedido.CANCELADO));
    }

    @Test
    public void conteudoDoQrDemoEDistintoPorPedidoEMarcadoComoDemonstracao() {
        carrinho.adicionar(suco, 1);
        Pedido a = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d").get(0);
        carrinho.adicionar(suco, 1);
        Pedido b = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d").get(0);

        String qrA = PedidoApresentacao.conteudoQrDemo(a);
        String qrB = PedidoApresentacao.conteudoQrDemo(b);

        assertNotEquals(qrA, qrB);
        assertTrue(qrA.contains(a.getId()));
        assertTrue(qrA.startsWith("PAELLA-DEMO"));
    }

    @Test
    public void numeroDoPedidoEExibidoEmMaiusculas() {
        carrinho.adicionar(suco, 1);
        Pedido pedido = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d")
                .get(0);
        assertEquals("#" + pedido.getId().toUpperCase(java.util.Locale.ROOT),
                PedidoApresentacao.numero(pedido));
    }

    @Test
    public void resumoDeItensListaQuantidadeETitulo() {
        carrinho.adicionar(paellaMista, 2);
        carrinho.adicionar(suco, 1);
        Pedido pedido = CheckoutService.finalizar(usuario, carrinho, FormaPagamento.PIX, repo, "d")
                .get(0);
        assertEquals("2× " + paellaMista.getTitulo() + ", 1× " + suco.getTitulo(),
                PedidoApresentacao.resumoItens(pedido));
    }
}
