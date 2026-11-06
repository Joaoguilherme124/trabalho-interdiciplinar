package com.example.trabalhointerdiciplinar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.trabalhointerdiciplinar.data.CarrinhoManager;
import com.example.trabalhointerdiciplinar.data.CheckoutService;
import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.data.simulacao.Armazenamento;
import com.example.trabalhointerdiciplinar.data.simulacao.AvisoPedido;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma.Previsao;
import com.example.trabalhointerdiciplinar.data.simulacao.Cronograma.TipoPrevisao;
import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador;
import com.example.trabalhointerdiciplinar.data.simulacao.RegistroSimulacao;
import com.example.trabalhointerdiciplinar.data.simulacao.SimulacaoConfig;
import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.model.TipoUsuario;
import com.example.trabalhointerdiciplinar.model.Usuario;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Fases A/C: cronograma, transições, persistência, isolamento e avisos sem duplicidade. */
public class SimuladorPedidoTest {

    // ---- dublês ----
    static final class Memoria implements Armazenamento {
        final Map<String, String> dados = new HashMap<>();
        @Override public String ler(String chave) { return dados.get(chave); }
        @Override public void gravar(String chave, String valor) { dados.put(chave, valor); }
    }

    static final class Relogio implements PedidoSimulador.Relogio {
        long agora = 1_000_000L;
        @Override public long agora() { return agora; }
        void avancar(long ms) { agora += ms; }
    }

    static final class Agenda implements PedidoSimulador.Agendador {
        final Map<String, Long> ativos = new HashMap<>();
        final List<String> cancelados = new ArrayList<>();
        @Override public void agendar(String id, long atraso) { ativos.put(id, atraso); }
        @Override public void cancelar(String id) { ativos.remove(id); cancelados.add(id); }
    }

    static final class Emissor implements PedidoSimulador.Emissor {
        final List<AvisoPedido> emitidos = new ArrayList<>();
        final List<String> cancelados = new ArrayList<>();
        @Override public void emitir(AvisoPedido a) { emitidos.add(a); }
        @Override public void cancelarDoPedido(String id) { cancelados.add(id); }
    }

    static final class Sessao implements PedidoSimulador.Sessao {
        String usuario;
        @Override public String usuarioAtualId() { return usuario; }
    }

    private final MockDataRepository repo = MockDataRepository.getInstance();
    private final CarrinhoManager carrinho = CarrinhoManager.getInstance();
    private Memoria memoria;
    private Relogio relogio;
    private Agenda agenda;
    private Emissor emissor;
    private Sessao sessao;
    private PedidoSimulador sim;
    private Usuario ana;
    private Usuario bia;

    @Before
    public void preparar() {
        carrinho.limpar();
        memoria = new Memoria();
        relogio = new Relogio();
        agenda = new Agenda();
        emissor = new Emissor();
        sessao = new Sessao();
        sim = new PedidoSimulador(memoria, repo, relogio, agenda, emissor, sessao);
        ana = novoUsuario("ana");
        bia = novoUsuario("bia");
        sessao.usuario = ana.getId();
    }

    private Usuario novoUsuario(String nome) {
        return new Usuario(UUID.randomUUID().toString(), nome, nome + "@t.com", "x",
                TipoUsuario.COMPRADOR, "Rua", "0");
    }

    private Pedido criarPedido(Usuario u) {
        carrinho.adicionar(repo.getProdutoById("p1"), 2);
        return CheckoutService.finalizar(u, carrinho, FormaPagamento.PIX, repo, "05/11/2026").get(0);
    }

    // ---- cronograma puro ----
    @Test
    public void statusPorTempoSegueOCronogramaDeDemonstracao() {
        assertEquals(StatusPedido.PENDENTE, Cronograma.statusPorTempo(0));
        assertEquals(StatusPedido.PENDENTE, Cronograma.statusPorTempo(SimulacaoConfig.INICIO_PREPARO_MS - 1));
        assertEquals(StatusPedido.SEPARACAO, Cronograma.statusPorTempo(SimulacaoConfig.INICIO_PREPARO_MS));
        assertEquals(StatusPedido.SEPARACAO, Cronograma.statusPorTempo(SimulacaoConfig.PRONTO_MS - 1));
        assertEquals(StatusPedido.ENVIADO, Cronograma.statusPorTempo(SimulacaoConfig.PRONTO_MS));
        // A retirada nunca é automática, por mais tempo que passe.
        assertEquals(StatusPedido.ENVIADO, Cronograma.statusPorTempo(10 * 3_600_000L));
        assertEquals(StatusPedido.PENDENTE, Cronograma.statusPorTempo(-5));
    }

    @Test
    public void previsaoAcompanhaAsEtapasENuncaFicaNegativa() {
        Previsao registrado = Cronograma.previsao(StatusPedido.PENDENTE, 0);
        assertEquals(TipoPrevisao.MINUTOS, registrado.tipo);
        assertEquals(SimulacaoConfig.PREVISAO_REGISTRADO_MIN, registrado.minutos);

        Previsao preparo = Cronograma.previsao(StatusPedido.SEPARACAO, SimulacaoConfig.INICIO_PREPARO_MS);
        assertEquals(TipoPrevisao.MINUTOS, preparo.tipo);
        assertEquals(SimulacaoConfig.PREVISAO_PREPARO_MIN, preparo.minutos);

        // Prazo da preparação esgotado, mas o estado ainda não avançou: "finalizando", nunca "pronto".
        Previsao finalizando = Cronograma.previsao(StatusPedido.SEPARACAO, SimulacaoConfig.ETAPA_FINAL_MS + 1);
        assertEquals(TipoPrevisao.FINALIZANDO, finalizando.tipo);
        assertEquals(0, finalizando.minutos);

        assertEquals(TipoPrevisao.PRONTO, Cronograma.previsao(StatusPedido.ENVIADO, 999_999).tipo);
        assertEquals(TipoPrevisao.FINALIZADO, Cronograma.previsao(StatusPedido.ENTREGUE, 0).tipo);
        for (long t = -1000; t < 500_000; t += 7_000) {
            for (StatusPedido s : StatusPedido.values()) {
                assertTrue(Cronograma.previsao(s, t).minutos >= 0);
            }
        }
    }

    @Test
    public void progressoCresceEsoChegaA100PorCentoEmProntoOuRetirado() {
        float anterior = -1;
        for (long t = 0; t < SimulacaoConfig.PRONTO_MS; t += 5_000) {
            float p = Cronograma.progresso(Cronograma.statusPorTempo(t), t);
            assertTrue(p >= anterior);
            assertTrue(p < 1f);
            anterior = p;
        }
        assertEquals(1f, Cronograma.progresso(StatusPedido.ENVIADO, 0), 0f);
        assertEquals(1f, Cronograma.progresso(StatusPedido.ENTREGUE, 0), 0f);
        // Estado adiantado não mostra progresso menor que o início da etapa.
        assertTrue(Cronograma.progresso(StatusPedido.SEPARACAO, 0)
                >= SimulacaoConfig.INICIO_PREPARO_MS / (float) SimulacaoConfig.PRONTO_MS);
    }

    @Test
    public void transicoesSoParaFrenteDentroDoFluxo() {
        assertTrue(Cronograma.transicaoPermitida(StatusPedido.PENDENTE, StatusPedido.SEPARACAO));
        assertTrue(Cronograma.transicaoPermitida(StatusPedido.SEPARACAO, StatusPedido.ENVIADO));
        assertTrue(Cronograma.transicaoPermitida(StatusPedido.ENVIADO, StatusPedido.ENTREGUE));
        assertTrue(Cronograma.transicaoPermitida(StatusPedido.PENDENTE, StatusPedido.ENVIADO));
        assertFalse(Cronograma.transicaoPermitida(StatusPedido.ENVIADO, StatusPedido.SEPARACAO));
        assertFalse(Cronograma.transicaoPermitida(StatusPedido.SEPARACAO, StatusPedido.SEPARACAO));
        assertFalse(Cronograma.transicaoPermitida(StatusPedido.ENTREGUE, StatusPedido.PENDENTE));
        assertFalse(Cronograma.transicaoPermitida(StatusPedido.CANCELADO, StatusPedido.ENVIADO));
        assertFalse(Cronograma.transicaoPermitida(StatusPedido.PENDENTE, StatusPedido.CANCELADO));
    }

    // ---- simulador ----
    @Test
    public void pedidoNovoNasceRegistradoComAvisoEAgendamento() {
        Pedido p = criarPedido(ana);
        assertEquals(StatusPedido.PENDENTE, p.getStatus());
        RegistroSimulacao r = sim.iniciar(p);
        assertNotNull(r);
        assertEquals(1, emissor.emitidos.size());
        assertEquals(StatusPedido.PENDENTE, emissor.emitidos.get(0).etapa);
        assertTrue(emissor.emitidos.get(0).mensagem.contains("25 minutos"));
        assertTrue(agenda.ativos.containsKey(p.getId()));
        assertEquals(1, sim.central().naoLidas(ana.getId()));
    }

    @Test
    public void iniciarDuasVezesNaoDuplicaRegistroNemAviso() {
        Pedido p = criarPedido(ana);
        RegistroSimulacao a = sim.iniciar(p);
        RegistroSimulacao b = sim.iniciar(p);
        assertEquals(a, b);
        sim.reconciliar();
        sim.reconciliar();
        assertEquals(1, emissor.emitidos.size());
        assertEquals(1, sim.central().listar(ana.getId()).size());
    }

    @Test
    public void statusAvancaComOTempoEcadaEtapaAvisaUmaUnicaVez() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);

        relogio.avancar(SimulacaoConfig.INICIO_PREPARO_MS);
        sim.reconciliar();
        sim.reconciliar();
        assertEquals(StatusPedido.SEPARACAO, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(2, emissor.emitidos.size());

        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        sim.reconciliar();
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(3, emissor.emitidos.size());
        assertEquals(StatusPedido.ENVIADO, emissor.emitidos.get(2).etapa);
        // Pronto: nada mais a agendar.
        assertFalse(agenda.ativos.containsKey(p.getId()));
    }

    @Test
    public void quemVoltaDepoisDeMuitoTempoRecebeSoOAvisoMaisRecenteNoSistema() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);                       // 1 aviso (registrado)
        relogio.avancar(10 * 60_000L);        // app ficou fechado: pulou preparo e chegou em pronto
        sim.reconciliar();
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
        // Sistema: registrado + apenas o "pronto" (sem emitir o "preparo" intermediário).
        assertEquals(2, emissor.emitidos.size());
        assertEquals(StatusPedido.ENVIADO, emissor.emitidos.get(1).etapa);
        // Central: histórico completo, um por etapa.
        assertEquals(3, sim.central().listar(ana.getId()).size());
    }

    @Test
    public void retiradaFinalizaCancelaAgendaENaoGeraNotificacaoDoSistema() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        int antes = emissor.emitidos.size();

        assertTrue(sim.avancarPara(p.getId(), ana.getId(), StatusPedido.ENTREGUE));
        assertEquals(StatusPedido.ENTREGUE, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(antes, emissor.emitidos.size());               // retirada não notifica o sistema
        assertTrue(emissor.cancelados.contains(p.getId()));         // limpa o que estiver na bandeja
        assertFalse(agenda.ativos.containsKey(p.getId()));
        assertTrue(agenda.cancelados.contains(p.getId()));
        // Já retirado: o tempo não reabre o pedido nem repete avisos.
        relogio.avancar(60 * 60_000L);
        sim.reconciliar();
        assertEquals(StatusPedido.ENTREGUE, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(antes, emissor.emitidos.size());
    }

    @Test
    public void transicoesInvalidasSaoBloqueadas() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        assertFalse(sim.avancarPara(p.getId(), ana.getId(), StatusPedido.SEPARACAO));  // para trás
        assertFalse(sim.avancarPara(p.getId(), ana.getId(), StatusPedido.ENVIADO));    // mesmo estado
        assertFalse(sim.avancarPara(p.getId(), ana.getId(), StatusPedido.CANCELADO));  // fora do fluxo
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
    }

    @Test
    public void painelAdiantaEtapasMantendoOCronogramaCoerente() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        assertTrue(sim.avancarPara(p.getId(), ana.getId(), StatusPedido.SEPARACAO));
        assertEquals(StatusPedido.SEPARACAO, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(SimulacaoConfig.INICIO_PREPARO_MS, sim.decorridoMs(p.getId()));
        assertTrue(sim.avancarPara(p.getId(), ana.getId(), StatusPedido.ENVIADO));
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
    }

    @Test
    public void reiniciarVoltaAoRegistradoEDescartaAvisosAntigos() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        assertEquals(3, sim.central().listar(ana.getId()).size());
        int emitidosAntes = emissor.emitidos.size();

        assertTrue(sim.reiniciar(p.getId(), ana.getId()));
        assertEquals(StatusPedido.PENDENTE, repo.getPedidoById(p.getId()).getStatus());
        assertTrue(emissor.cancelados.contains(p.getId()));
        // Só o aviso novo de "registrado"; os antigos não voltam.
        assertEquals(1, sim.central().listar(ana.getId()).size());
        assertEquals(emitidosAntes + 1, emissor.emitidos.size());
        assertEquals(StatusPedido.PENDENTE, emissor.emitidos.get(emissor.emitidos.size() - 1).etapa);
    }

    // ---- isolamento e múltiplos pedidos ----
    @Test
    public void outroUsuarioNaoControlaNemRecebeAvisosDoPedido() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        assertFalse(sim.avancarPara(p.getId(), bia.getId(), StatusPedido.SEPARACAO));
        assertFalse(sim.reiniciar(p.getId(), bia.getId()));
        assertFalse(sim.pertenceA(p.getId(), bia.getId()));
        assertEquals(0, sim.central().listar(bia.getId()).size());
        assertEquals(0, sim.central().naoLidas(bia.getId()));
        assertEquals(StatusPedido.PENDENTE, repo.getPedidoById(p.getId()).getStatus());
    }

    @Test
    public void naoNotificaOSistemaQuandoOUsuarioLogadoEOutro() {
        sessao.usuario = bia.getId();           // quem está logado não é a dona do pedido
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        assertTrue(emissor.emitidos.isEmpty());
        // O aviso fica guardado para a dona, que o verá ao entrar.
        assertEquals(3, sim.central().listar(ana.getId()).size());
        sessao.usuario = null;                  // ninguém logado
        sim.reconciliar();
        assertTrue(emissor.emitidos.isEmpty());
    }

    @Test
    public void multiplosPedidosTemEstadoECronogramaProprios() {
        Pedido a = criarPedido(ana);
        sim.iniciar(a);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);   // a chega em "pronto"
        Pedido b = criarPedido(ana);
        sim.iniciar(b);                               // b acaba de nascer
        sim.reconciliar();

        assertNotEquals(a.getId(), b.getId());
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(a.getId()).getStatus());
        assertEquals(StatusPedido.PENDENTE, repo.getPedidoById(b.getId()).getStatus());
        assertTrue(sim.decorridoMs(a.getId()) > sim.decorridoMs(b.getId()));
        assertTrue(agenda.ativos.containsKey(b.getId()));
        assertFalse(agenda.ativos.containsKey(a.getId()));
    }

    @Test
    public void avisosSaoMarcadosComoLidosSoPeloDono() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        AvisoPedido aviso = sim.central().listar(ana.getId()).get(0);
        sim.central().marcarLida(aviso.id, bia.getId());
        assertEquals(1, sim.central().naoLidas(ana.getId()));
        sim.central().marcarLida(aviso.id, ana.getId());
        assertEquals(0, sim.central().naoLidas(ana.getId()));
    }

    // ---- persistência ----
    @Test
    public void andamentoSobreviveAoFechamentoDoApp() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        relogio.avancar(SimulacaoConfig.INICIO_PREPARO_MS);
        sim.reconciliar();
        sim.definirModoDiaDoEvento(true);

        // "Reabre o app": novo simulador sobre o mesmo armazenamento e o relógio seguindo.
        relogio.avancar(5_000L);
        PedidoSimulador reaberto = new PedidoSimulador(memoria, repo, relogio, new Agenda(),
                new Emissor(), sessao);
        reaberto.carregar();

        assertNotNull(reaberto.registro(p.getId()));
        assertEquals(ana.getId(), reaberto.registro(p.getId()).usuarioId);
        assertEquals(SimulacaoConfig.INICIO_PREPARO_MS + 5_000L, reaberto.decorridoMs(p.getId()));
        assertEquals(StatusPedido.SEPARACAO, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(2, reaberto.central().listar(ana.getId()).size());   // histórico preservado
        assertTrue(reaberto.modoDiaDoEvento());
    }

    @Test
    public void pedidoAusenteDoRepositorioEReconstruidoAPartirDoRegistro() {
        // Simula um pedido criado em uma execução anterior: só existe no armazenamento.
        Pedido original = criarPedido(ana);
        sim.iniciar(original);
        String persistido = memoria.dados.get("registros");
        assertNotNull(persistido);
        String idFantasma = "ped-" + UUID.randomUUID().toString().substring(0, 6);
        Memoria outra = new Memoria();
        outra.gravar("registros", persistido.replace(original.getId(), idFantasma));

        PedidoSimulador novo = new PedidoSimulador(outra, repo, relogio, new Agenda(), new Emissor(), sessao);
        assertNull(repo.getPedidoById(idFantasma));
        novo.carregar();

        Pedido recriado = repo.getPedidoById(idFantasma);
        assertNotNull(recriado);
        assertEquals(ana.getId(), recriado.getIdComprador());
        assertEquals(original.getValorTotal(), recriado.getValorTotal(), 0.001);
        assertEquals(original.getListaItens().size(), recriado.getListaItens().size());
        assertEquals(original.getListaItens().get(0).getTitulo(), recriado.getListaItens().get(0).getTitulo());
        assertEquals(FormaPagamento.PIX, recriado.getFormaPagamento());
    }

    @Test
    public void registroCorrompidoNaoDerrubaOCarregamento() {
        Memoria suja = new Memoria();
        suja.gravar("registros", "lixo\u001Cmais-lixo\u001F1\u001F2");
        suja.gravar("avisos", "x\u001Fy");
        PedidoSimulador s = new PedidoSimulador(suja, repo, relogio, new Agenda(), new Emissor(), sessao);
        s.carregar();
        assertEquals(0, s.central().listar(ana.getId()).size());
    }

    @Test
    public void modoDiaDoEventoAlternaEPersiste() {
        assertFalse(sim.modoDiaDoEvento());
        sim.definirModoDiaDoEvento(true);
        assertTrue(sim.modoDiaDoEvento());
        sim.definirModoDiaDoEvento(false);
        assertFalse(sim.modoDiaDoEvento());
    }

    // ---- Home contextual, QR e destino dos avisos ----

    @Test
    public void homeContextualAcompanhaAEtapaEAPrevisaoDoPedidoAtivo() {
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        List<Pedido> meus = repo.getPedidosDoComprador(ana.getId());
        Pedido ativo = com.example.trabalhointerdiciplinar.util.PedidoApresentacao.primeiroAtivo(meus);
        assertNotNull(ativo);
        assertEquals(Cronograma.TipoPrevisao.MINUTOS,
                Cronograma.previsao(ativo.getStatus(), sim.decorridoMs(ativo.getId())).tipo);

        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        assertEquals(StatusPedido.ENVIADO, repo.getPedidoById(p.getId()).getStatus());
        assertEquals(Cronograma.TipoPrevisao.PRONTO,
                Cronograma.previsao(StatusPedido.ENVIADO, sim.decorridoMs(p.getId())).tipo);

        sim.avancarPara(p.getId(), ana.getId(), StatusPedido.ENTREGUE);
        Pedido depois = repo.getPedidoById(p.getId());
        assertFalse(com.example.trabalhointerdiciplinar.util.PedidoApresentacao.estaAtivo(depois.getStatus()));
    }

    @Test
    public void qrDoIngressoFicaVinculadoAoPedidoCorreto() {
        Pedido a = criarPedido(ana);
        Pedido b = criarPedido(ana);
        sim.iniciar(a);
        sim.iniciar(b);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        String qrA = com.example.trabalhointerdiciplinar.util.PedidoApresentacao.conteudoQrDemo(
                repo.getPedidoById(a.getId()));
        String qrB = com.example.trabalhointerdiciplinar.util.PedidoApresentacao.conteudoQrDemo(
                repo.getPedidoById(b.getId()));
        assertNotEquals(qrA, qrB);
        assertTrue(qrA.contains(a.getId()) || qrA.contains(com.example.trabalhointerdiciplinar.util
                .PedidoApresentacao.numero(a).replace("#", "")));
        assertTrue(com.example.trabalhointerdiciplinar.util.PedidoApresentacao.exibeQrDemo(StatusPedido.ENVIADO));
    }

    @Test
    public void cadaAvisoApontaParaOPedidoQueOGerou() {
        Pedido a = criarPedido(ana);
        Pedido b = criarPedido(ana);
        sim.iniciar(a);
        sim.iniciar(b);
        relogio.avancar(SimulacaoConfig.PRONTO_MS);
        sim.reconciliar();
        for (AvisoPedido aviso : sim.central().listar(ana.getId())) {
            assertTrue(aviso.pedidoId.equals(a.getId()) || aviso.pedidoId.equals(b.getId()));
        }
        for (AvisoPedido emitido : emitidos()) {
            assertTrue(emitido.pedidoId.equals(a.getId()) || emitido.pedidoId.equals(b.getId()));
        }
        // Um aviso por etapa e por pedido: 2 pedidos x 3 etapas.
        assertEquals(6, sim.central().listar(ana.getId()).size());
    }

    @Test
    public void indicadorDoSinoReflitaApenasAvisosNaoLidosDoUsuario() {
        assertEquals(0, sim.central().naoLidas(ana.getId()));
        Pedido p = criarPedido(ana);
        sim.iniciar(p);
        assertTrue(sim.central().naoLidas(ana.getId()) > 0);
        assertEquals(0, sim.central().naoLidas(bia.getId()));
        for (AvisoPedido a : sim.central().listar(ana.getId())) {
            sim.central().marcarLida(a.id, ana.getId());
        }
        assertEquals(0, sim.central().naoLidas(ana.getId()));
    }

    private List<AvisoPedido> emitidos() {
        return emissor.emitidos;
    }
}
