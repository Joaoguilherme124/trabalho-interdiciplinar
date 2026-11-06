package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Agenda;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Emissor;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Memoria;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Relogio;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Sessao;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.CheckoutService;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.cozinha.IndicadoresCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.AvisoPedido;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.SimulacaoConfig;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Loja;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.Produto;
import com.example.trabalhointerdiciplinar.model.StatusLoja;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Etapa 15A: operação do lojista (fila, ações, produtos publicados) integrada ao participante. */
public class CozinhaTest {
    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();
    private Memoria memoria;
    private Relogio relogio;
    private Emissor emissor;
    private Sessao sessao;
    private PedidoSimulador sim;
    private Usuario ana;
    private Usuario lojista;
    private String lojaId;

    @Before
    public void preparar() {
        carrinho.limpar();
        memoria = new Memoria();
        relogio = new Relogio();
        emissor = new Emissor();
        sessao = new Sessao();
        sim = new PedidoSimulador(memoria, repo, relogio, new Agenda(), emissor, sessao);
        ana = new Usuario(UUID.randomUUID().toString(), "ana", "ana@t.com", "x", TipoUsuario.COMPRADOR, "R", "0");
        lojista = new Usuario("u2", "Maria Lojista", "maria@loja.com", "123", TipoUsuario.LOJISTA, "R", "0");
        Produto p = repo.getProdutoById("p1");
        lojaId = p.getIdLoja();
        sessao.usuario = ana.getId();
    }

    private Pedido pedido() {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        Pedido p = CheckoutService.finalizar(ana, carrinho, FormaPagamento.PIX, repo, "05/11/2026").get(0);
        sim.iniciar(p);
        return p;
    }

    // ---- fluxo da cozinha ----

    @Test
    public void lojistaIniciaPreparoEMarcaProntoNaMesmaFonteDoParticipante() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido();
        assertEquals(StatusPedido.PENDENTE, repo.getPedidoById(p.getId()).getStatus());

        // Sem a cozinha, nada avança: o cronograma automático não sobrescreve o lojista.
        relogio.avancar(SimulacaoConfig.PRONTO_MS * 3);
        sim.reconciliar();
        assertEquals(StatusPedido.PENDENTE, repo.getPedidoById(p.getId()).getStatus());

        assertTrue(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.RECEBER));
        assertTrue(sim.recebido(p.getId()));
        assertTrue(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO));
        assertEquals(StatusPedido.SEPARACAO, repo.getPedidoById(p.getId()).getStatus());
        relogio.avancar(SimulacaoConfig.PRONTO_MS * 3);
        sim.reconciliar();
        assertEquals(StatusPedido.SEPARACAO, repo.getPedidoById(p.getId()).getStatus());

        assertTrue(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO));
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
        assertTrue(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.CONFIRMAR_RETIRADA));
        assertEquals(StatusPedido.ENTREGUE, repo.getPedidoById(p.getId()).getStatus());
    }

    @Test
    public void acoesForaDeOrdemOuDeOutraLojaSaoRecusadas() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido();
        assertFalse(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO));      // pula etapa
        assertFalse(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.CONFIRMAR_RETIRADA));
        assertFalse(sim.executarAcao(p.getId(), "outra-loja", AcaoCozinha.INICIAR_PREPARO));
        assertFalse(sim.executarAcao(p.getId(), null, AcaoCozinha.INICIAR_PREPARO));
        assertEquals(StatusPedido.PENDENTE, repo.getPedidoById(p.getId()).getStatus());
        assertTrue(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO));
        assertFalse(sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO));    // repetida
    }

    @Test
    public void pedidoDeOutraLojaNaoApareceNaFilaNemNosIndicadores() {
        Pedido p = pedido();
        assertTrue(repo.getPedidosDaLoja(lojaId).contains(repo.getPedidoById(p.getId())));
        assertTrue(repo.getPedidosDaLoja("loja-inexistente").isEmpty());
        assertEquals(0, IndicadoresCozinha.calcular(repo.getPedidosDaLoja("loja-inexistente")).emAndamento());
    }

    @Test
    public void indicadoresSaoDerivadosDosPedidosReais() {
        sim.definirCozinhaManual(true);
        List<Pedido> lista = new ArrayList<>();
        Pedido a = pedido();
        Pedido b = pedido();
        Pedido c = pedido();
        sim.executarAcao(b.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(c.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(c.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        lista.add(repo.getPedidoById(a.getId()));
        lista.add(repo.getPedidoById(b.getId()));
        lista.add(repo.getPedidoById(c.getId()));
        IndicadoresCozinha ind = IndicadoresCozinha.calcular(lista);
        assertEquals(1, ind.novos);
        assertEquals(1, ind.emPreparo);
        assertEquals(1, ind.prontos);
        assertEquals(0, ind.finalizados);
        assertEquals(3, ind.emAndamento());
        assertEquals(0, IndicadoresCozinha.calcular(null).emAndamento());
    }

    // ---- previsão ----

    @Test
    public void previsaoAjustadaPeloLojistaApareceParaOParticipante() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido();
        assertEquals(25, sim.previsao(p.getId()).minutos);
        assertTrue(sim.ajustarPrevisao(p.getId(), lojaId, 40));
        assertEquals(40, sim.previsao(p.getId()).minutos);
        assertFalse(sim.ajustarPrevisao(p.getId(), "outra-loja", 5));
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        assertFalse(sim.ajustarPrevisao(p.getId(), lojaId, 10));                          // já pronto
        assertEquals(Cronograma.TipoPrevisao.PRONTO, sim.previsao(p.getId()).tipo);
    }

    @Test
    public void previsaoAjustadaTemLimites() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido();
        sim.ajustarPrevisao(p.getId(), lojaId, 0);
        assertEquals(1, sim.previsao(p.getId()).minutos);
        sim.ajustarPrevisao(p.getId(), lojaId, 9999);
        assertEquals(120, sim.previsao(p.getId()).minutos);
    }

    // ---- notificações ----

    @Test
    public void notificacaoDoPedidoProntoSaiQuandoOParticipanteVolta() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido();                       // ana logada: "registrado" já foi notificado
        emissor.emitidos.clear();
        sessao.usuario = lojista.getId();          // o lojista opera com a sessão dele
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        assertTrue("nada é emitido para quem não é o dono", emissor.emitidos.isEmpty());
        assertEquals(3, sim.central().listar(ana.getId()).size());

        sessao.usuario = ana.getId();              // o participante abre o app
        sim.reconciliar();
        assertEquals(1, emissor.emitidos.size());
        AvisoPedido aviso = emissor.emitidos.get(0);
        assertEquals(StatusPedido.ENVIADO, aviso.etapa);
        assertEquals(p.getId(), aviso.pedidoId);
        sim.reconciliar();
        assertEquals("sem duplicidade", 1, emissor.emitidos.size());
    }

    @Test
    public void registrosPersistemOEstadoManualAoReabrir() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido();
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.RECEBER);
        sim.ajustarPrevisao(p.getId(), lojaId, 33);
        PedidoSimulador reaberto = new PedidoSimulador(memoria, repo, relogio, new Agenda(), new Emissor(), sessao);
        reaberto.carregar();
        assertTrue(reaberto.controladoPelaCozinha(p.getId()));
        assertTrue(reaberto.recebido(p.getId()));
        assertEquals(33, reaberto.previsao(p.getId()).minutos);
        assertTrue(reaberto.cozinhaManual());
    }

    @Test
    public void modoAutomaticoContinuaFuncionandoQuandoACozinhaNaoAssume() {
        Pedido p = pedido();
        assertFalse(sim.controladoPelaCozinha(p.getId()));
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
    }

    // ---- produtos e publicação ----

    @Test
    public void produtoNovoAparecePublicadoEDesativadoSai() {
        Loja loja = repo.getLojaById(lojaId);
        assertEquals(StatusLoja.APROVADA, loja.getStatus());
        Produto novo = new Produto(UUID.randomUUID().toString(), lojaId, loja.getNome(), "Pudim Teste",
                "Sobremesa de teste", 12.5, 10, "", 0f, "Paellas");
        novo.setIngredientes("leite, ovos");
        novo.setPorcao("1 fatia");
        novo.setTempoEstimadoMin(5);
        repo.addProduto(novo);
        assertEquals(1, repo.buscarProdutos("pudim", "Todos").size());
        assertEquals(1, repo.buscarProdutos("ovos", "Todos").size());      // busca por ingrediente

        novo.setDisponivel(false);
        repo.updateProduto(novo);
        assertTrue(repo.buscarProdutos("pudim", "Todos").isEmpty());
        assertFalse(repo.getProdutosPublicadosDaLoja(lojaId).contains(novo));
        assertTrue(repo.getProdutosPorLoja(lojaId).contains(novo));        // a loja ainda o gerencia
        repo.removeProduto(novo.getId());
    }

    @Test
    public void produtoDeLojaNaoAprovadaNaoEPublicado() {
        Loja pendente = new Loja(UUID.randomUUID().toString(), "u2", "Loja Nova", "00", "Paellas", 0f,
                "", StatusLoja.PENDENTE, "", "", "");
        repo.addLoja(pendente);
        Produto p = new Produto(UUID.randomUUID().toString(), pendente.getId(), pendente.getNome(),
                "Item Oculto Teste", "x", 1.0, 1, "", 0f, "Paellas");
        repo.addProduto(p);
        assertTrue(repo.buscarProdutos("oculto", "Todos").isEmpty());
        repo.atualizarStatusLoja(pendente.getId(), StatusLoja.APROVADA);
        assertEquals(1, repo.buscarProdutos("oculto", "Todos").size());
        repo.removeProduto(p.getId());
    }

    @Test
    public void editarLojaAtualizaONomeNosProdutos() {
        Loja loja = repo.getLojaById(lojaId);
        String nomeOriginal = loja.getNome();
        loja.setNome("Nome Editado Teste");
        repo.atualizarLoja(loja);
        assertNotNull(repo.getProdutoById("p1"));
        assertEquals("Nome Editado Teste", repo.getProdutoById("p1").getNomeLoja());
        loja.setNome(nomeOriginal);
        repo.atualizarLoja(loja);
        assertEquals(nomeOriginal, repo.getProdutoById("p1").getNomeLoja());
    }
}
