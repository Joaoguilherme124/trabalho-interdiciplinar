package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Agenda;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Emissor;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Memoria;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Relogio;
import com.example.trabalhointerdiciplinar.SimuladorPedidoTest.Sessao;
import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.CheckoutService;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.data.simulacao.SimulacaoConfig;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;
import com.example.trabalhointerdiciplinar.util.AcompanhamentoPedido;
import com.example.trabalhointerdiciplinar.util.AcompanhamentoPedido.Estado;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Etapa 16: acompanhamento do pedido no topo da Início. */
public class Etapa16Test {
    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();
    private Relogio relogio;
    private Emissor emissor;
    private Sessao sessao;
    private PedidoSimulador sim;
    private Usuario ana;
    private Usuario bia;
    private String lojaId;

    @Before
    public void preparar() {
        carrinho.limpar();
        relogio = new Relogio();
        emissor = new Emissor();
        sessao = new Sessao();
        sim = new PedidoSimulador(new Memoria(), repo, relogio, new Agenda(), emissor, sessao);
        ana = novo("ana");
        bia = novo("bia");
        lojaId = repo.getProdutoById("p1").getIdLoja();
        sessao.usuario = ana.getId();
    }

    private Usuario novo(String nome) {
        return new Usuario(UUID.randomUUID().toString(), nome, nome + "@t.com", "x", TipoUsuario.COMPRADOR, "R", "0");
    }

    private Pedido pedido(Usuario u) {
        carrinho.adicionar(repo.getProdutoById("p1"), 1);
        Pedido p = CheckoutService.finalizar(u, carrinho, FormaPagamento.PIX, repo, "05/11/2026").get(0);
        sim.iniciar(p);
        return p;
    }

    private AcompanhamentoPedido acompanhamento(Usuario u) {
        List<Pedido> meus = repo.getPedidosDoComprador(u.getId());
        List<Pedido> grupo = AcompanhamentoPedido.escolherGrupo(meus, p -> sim.criadoEmMs(p.getId()));
        return AcompanhamentoPedido.calcular(grupo, p -> sim.previsao(p.getId()));
    }

    // ---- 1, 2, 3, 4, 5 : estados ----

    @Test
    public void semPedidoNaoHaAcompanhamento() {
        assertNull(acompanhamento(ana));
        assertTrue(AcompanhamentoPedido.escolherGrupo(null, p -> 0L).isEmpty());
        assertNull(AcompanhamentoPedido.calcular(Collections.<Pedido>emptyList(), p -> null));
    }

    @Test
    public void pedidoRecebidoAtivaAPrimeiraEtapaComPrevisao() {
        Pedido p = pedido(ana);
        AcompanhamentoPedido a = acompanhamento(ana);
        assertNotNull(a);
        assertEquals(Estado.REGISTRADO, a.estado);
        assertEquals(0, a.estado.etapa);
        assertEquals(25, a.minutos);
        assertEquals(p.getId(), a.pedidoId);
        assertTrue(a.numero.startsWith("#PED-"));
    }

    @Test
    public void cozinhaIniciandoPreparoMoveParaEmPreparoComPrevisaoAjustada() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido(ana);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.ajustarPrevisao(p.getId(), lojaId, 40);
        AcompanhamentoPedido a = acompanhamento(ana);
        assertEquals(Estado.EM_PREPARO, a.estado);
        assertEquals(1, a.estado.etapa);
        assertEquals(40, a.minutos);
    }

    @Test
    public void quaseProntoSoApareceNaFaseFinalDoCronogramaNuncaNaCozinhaManual() {
        sim.definirCozinhaManual(true);
        Pedido manual = pedido(ana);
        sim.executarAcao(manual.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        relogio.avancar(SimulacaoConfig.PRONTO_MS - 1);          // o relógio não move pedido manual
        sim.reconciliar();
        assertEquals(Estado.EM_PREPARO, acompanhamento(ana).estado);

        sim.definirCozinhaManual(false);
        Usuario outra = novo("outra");
        sessao.usuario = outra.getId();
        pedido(outra);
        relogio.avancar(SimulacaoConfig.ETAPA_FINAL_MS);          // fase final do cronograma automático
        sim.reconciliar();
        assertEquals(Estado.QUASE_PRONTO, acompanhamento(outra).estado);
        assertEquals(2, acompanhamento(outra).estado.etapa);
    }

    @Test
    public void pedidoProntoMostraUltimaEtapaESemPrevisao() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido(ana);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        AcompanhamentoPedido a = acompanhamento(ana);
        assertEquals(Estado.PRONTO, a.estado);
        assertTrue(a.pronto());
        assertEquals(3, a.estado.etapa);
        assertEquals(-1, a.minutos);
    }

    @Test
    public void pedidoRetiradoSaiDoAcompanhamentoAtivo() {
        sim.definirCozinhaManual(true);
        Pedido p = pedido(ana);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        sim.executarAcao(p.getId(), lojaId, AcaoCozinha.CONFIRMAR_RETIRADA);
        assertEquals(StatusPedido.ENTREGUE, repo.getPedidoById(p.getId()).getStatus());
        assertNull(acompanhamento(ana));
    }

    // ---- 6, 7 : vários pedidos ----

    @Test
    public void escolheOPedidoAtivoMaisRecenteSemDependerDaOrdemDaLista() {
        sim.definirCozinhaManual(true);
        Pedido antigo = pedido(ana);
        relogio.avancar(60_000L);
        Pedido recente = pedido(ana);
        List<Pedido> embaralhada = new ArrayList<>(repo.getPedidosDoComprador(ana.getId()));
        Collections.reverse(embaralhada);
        List<Pedido> g1 = AcompanhamentoPedido.escolherGrupo(repo.getPedidosDoComprador(ana.getId()),
                p -> sim.criadoEmMs(p.getId()));
        List<Pedido> g2 = AcompanhamentoPedido.escolherGrupo(embaralhada, p -> sim.criadoEmMs(p.getId()));
        assertEquals(recente.getId(), g1.get(0).getId());
        assertEquals(recente.getId(), g2.get(0).getId());
        assertEquals(1, g1.size());                  // o antigo não entra no mesmo acompanhamento
        assertFalse(g1.contains(repo.getPedidoById(antigo.getId())));
    }

    @Test
    public void ajustarPrevisaoNaoMudaQualPedidoEOMaisRecente() {
        sim.definirCozinhaManual(true);
        Pedido antigo = pedido(ana);
        relogio.avancar(30_000L);
        Pedido recente = pedido(ana);
        sim.executarAcao(antigo.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);   // rebase não afeta criação
        sim.executarAcao(antigo.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        assertEquals(recente.getId(), acompanhamento(ana).pedidoId);
        assertEquals(Estado.REGISTRADO, acompanhamento(ana).estado);
    }

    @Test
    public void comprasJuntasFormamGrupoENaoViramProntasAntesDeTodas() {
        sim.definirCozinhaManual(true);
        Pedido a = pedido(ana);
        Pedido b = pedido(ana);                       // mesma "compra": criados no mesmo instante
        List<Pedido> grupo = AcompanhamentoPedido.escolherGrupo(repo.getPedidosDoComprador(ana.getId()),
                p -> sim.criadoEmMs(p.getId()));
        assertEquals(2, grupo.size());
        sim.executarAcao(a.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(a.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        AcompanhamentoPedido ac = acompanhamento(ana);
        assertEquals("só um pedido pronto: o grupo ainda está no começo", Estado.REGISTRADO, ac.estado);
        assertEquals(1, ac.extras);
        sim.executarAcao(b.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        sim.executarAcao(b.getId(), lojaId, AcaoCozinha.MARCAR_PRONTO);
        assertEquals(Estado.PRONTO, acompanhamento(ana).estado);
    }

    // ---- 8, 9 : estimativa e transições ----

    @Test
    public void estimativaVemDaMesmaFonteDaJornada() {
        Pedido p = pedido(ana);
        assertEquals(sim.previsao(p.getId()).minutos, acompanhamento(ana).minutos);
        relogio.avancar(SimulacaoConfig.INICIO_PREPARO_MS);
        sim.reconciliar();
        assertEquals(sim.previsao(p.getId()).minutos, acompanhamento(ana).minutos);
        assertEquals(Estado.EM_PREPARO, acompanhamento(ana).estado);
    }

    @Test
    public void transicoesSoAvancamEmDirecaoAoProntoPronto() {
        Pedido p = pedido(ana);
        int anterior = -1;
        long[] marcos = {0, SimulacaoConfig.INICIO_PREPARO_MS, SimulacaoConfig.ETAPA_FINAL_MS,
                SimulacaoConfig.PRONTO_MS};
        long agora = 0;
        for (long marco : marcos) {
            relogio.avancar(marco - agora);
            agora = marco;
            sim.reconciliar();
            AcompanhamentoPedido a = acompanhamento(ana);
            assertTrue("a etapa nunca recua", a.estado.etapa >= anterior);
            anterior = a.estado.etapa;
        }
        assertEquals(Estado.PRONTO, acompanhamento(ana).estado);
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
    }

    @Test
    public void mesmoAcompanhamentoNaoPedeRedesenho() {
        pedido(ana);
        AcompanhamentoPedido a = acompanhamento(ana);
        AcompanhamentoPedido b = acompanhamento(ana);
        assertTrue(a.mesmoQue(b));
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        assertFalse(a.mesmoQue(acompanhamento(ana)));
    }

    // ---- 10, 11 : notificações e isolamento ----

    @Test
    public void calcularOAcompanhamentoNuncaGeraNotificacao() {
        pedido(ana);
        int avisos = sim.central().listar(ana.getId()).size();
        int emitidas = emissor.emitidos.size();
        for (int i = 0; i < 5; i++) acompanhamento(ana);          // abrir a Início várias vezes
        assertEquals(avisos, sim.central().listar(ana.getId()).size());
        assertEquals(emitidas, emissor.emitidos.size());
    }

    @Test
    public void acompanhamentoEIsoladoEntreUsuarios() {
        sim.definirCozinhaManual(true);
        Pedido deAna = pedido(ana);
        assertNull(acompanhamento(bia));
        sessao.usuario = bia.getId();
        Pedido deBia = pedido(bia);
        sim.executarAcao(deBia.getId(), lojaId, AcaoCozinha.INICIAR_PREPARO);
        assertEquals(deAna.getId(), acompanhamento(ana).pedidoId);
        assertEquals(Estado.REGISTRADO, acompanhamento(ana).estado);
        assertEquals(deBia.getId(), acompanhamento(bia).pedidoId);
        assertEquals(Estado.EM_PREPARO, acompanhamento(bia).estado);
    }

    // ---- 12 : destino do toque ----

    @Test
    public void toqueNoCartaoAbreAJornadaDoPedidoDestacado() {
        sim.definirCozinhaManual(true);
        pedido(ana);
        relogio.avancar(5_000L);
        Pedido recente = pedido(ana);
        assertEquals("o id carregado pelo cartão é o que a Jornada recebe",
                recente.getId(), acompanhamento(ana).pedidoId);
    }
}
