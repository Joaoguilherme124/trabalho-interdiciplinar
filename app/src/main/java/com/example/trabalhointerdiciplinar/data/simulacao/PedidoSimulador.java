package com.example.trabalhointerdiciplinar.data.simulacao;

import com.example.trabalhointerdiciplinar.data.MockDataRepository;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;
import com.example.trabalhointerdiciplinar.util.PedidoApresentacao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Única camada que evolui o andamento demonstrativo dos pedidos. O estado do pedido continua
 * sendo o {@link StatusPedido} do repositório; aqui só se decide, a partir do cronograma
 * persistido, quando ele avança, se há aviso a emitir e quando agendar a próxima checagem.
 *
 * A fonte de verdade é sempre o estado persistido + o relógio: {@link #reconciliar()} pode ser
 * chamado a qualquer momento (tela aberta, trabalho em segundo plano, reabertura do app) e
 * produz o mesmo resultado, sem repetir avisos.
 */
public final class PedidoSimulador {

    public interface Relogio {
        long agora();
    }

    /** Agenda uma checagem futura (WorkManager no app). Um agendamento por pedido. */
    public interface Agendador {
        void agendar(String pedidoId, long atrasoMs);

        void cancelar(String pedidoId);
    }

    /** Emite a notificação do sistema (não afeta a central interna). */
    public interface Emissor {
        void emitir(AvisoPedido aviso);

        void cancelarDoPedido(String pedidoId);
    }

    public interface Sessao {
        /** Id do usuário logado, ou null. */
        String usuarioAtualId();
    }

    private static final String CHAVE_REGISTROS = "registros";
    private static final String CHAVE_MODO_EVENTO = "modo_dia_do_evento";
    private static final String CHAVE_COZINHA = "cozinha_manual";
    private static final String SEP = "\u001C";

    private final Armazenamento armazenamento;
    private final MockDataRepository repo;
    private final Relogio relogio;
    private final Agendador agendador;
    private final Emissor emissor;
    private final Sessao sessao;
    private final CentralAvisos central;
    private final Map<String, RegistroSimulacao> registros = new LinkedHashMap<>();

    public PedidoSimulador(Armazenamento armazenamento, MockDataRepository repo, Relogio relogio,
                           Agendador agendador, Emissor emissor, Sessao sessao) {
        this.armazenamento = armazenamento;
        this.repo = repo;
        this.relogio = relogio;
        this.agendador = agendador;
        this.emissor = emissor;
        this.sessao = sessao;
        this.central = new CentralAvisos(armazenamento);
    }

    public CentralAvisos central() {
        return central;
    }

    // ---------------------------------------------------------------- ciclo de vida

    /**
     * Lê o que foi persistido e recria no repositório os pedidos que ainda não existem nele
     * (reabertura do app). Em seguida reconcilia com o relógio.
     */
    public synchronized void carregar() {
        registros.clear();
        String bruto = armazenamento.ler(CHAVE_REGISTROS);
        if (bruto != null && !bruto.isEmpty()) {
            for (String linha : bruto.split(SEP)) {
                RegistroSimulacao r = RegistroSimulacao.decodificar(linha);
                if (r != null) registros.put(r.pedidoId, r);
            }
        }
        for (RegistroSimulacao r : registros.values()) {
            if (repo.getPedidoById(r.pedidoId) == null) repo.addPedido(r.paraPedido());
        }
        reconciliar();
    }

    /** Começa a simular um pedido recém-criado. Idempotente: não duplica nem reinicia. */
    public synchronized RegistroSimulacao iniciar(Pedido pedido) {
        RegistroSimulacao existente = registros.get(pedido.getId());
        if (existente != null) return existente;
        RegistroSimulacao r = new RegistroSimulacao(pedido, relogio.agora());
        r.manual = cozinhaManual();
        registros.put(pedido.getId(), r);
        salvar();
        reconciliar();
        return r;
    }

    /**
     * Adianta o pedido para uma etapa à frente (painel de demonstração / retirada simulada).
     * Só o dono pode fazê-lo, e só para frente. O cronograma é "rebaseado" para que o relógio
     * passe a indicar a nova etapa, mantendo o restante automático.
     */
    public synchronized boolean avancarPara(String pedidoId, String usuarioId, StatusPedido alvo) {
        RegistroSimulacao r = registros.get(pedidoId);
        Pedido p = repo.getPedidoById(pedidoId);
        if (r == null || p == null || !r.usuarioId.equals(usuarioId)) return false;
        if (!Cronograma.transicaoPermitida(p.getStatus(), alvo)) return false;
        if (alvo == StatusPedido.ENTREGUE) {
            r.retirado = true;
        } else {
            r.inicioMs = relogio.agora() - Cronograma.inicioDaEtapa(alvo);
            if (r.manual) repo.atualizarStatusPedido(pedidoId, alvo);   // cozinha manual: não há relógio
        }
        salvar();
        reconciliar();
        return true;
    }

    /** Reinicia a demonstração do pedido: volta a "registrado" e descarta avisos antigos. */
    public synchronized boolean reiniciar(String pedidoId, String usuarioId) {
        RegistroSimulacao r = registros.get(pedidoId);
        Pedido p = repo.getPedidoById(pedidoId);
        if (r == null || p == null || !r.usuarioId.equals(usuarioId)) return false;
        agendador.cancelar(pedidoId);
        emissor.cancelarDoPedido(pedidoId);
        central.removerDoPedido(pedidoId);
        r.inicioMs = relogio.agora();
        r.retirado = false;
        r.notificadas = 0;
        r.emitidas = 0;
        r.manual = cozinhaManual();
        r.recebido = false;
        r.previsaoMin = -1;
        r.statusSalvo = StatusPedido.PENDENTE;
        repo.atualizarStatusPedido(pedidoId, StatusPedido.PENDENTE);
        salvar();
        reconciliar();
        return true;
    }

    // ---------------------------------------------------------------- reconciliação

    /**
     * Alinha todos os pedidos com o cronograma e o relógio: atualiza o status (só para frente),
     * registra os avisos das etapas alcançadas (uma única vez cada) e reagenda a próxima checagem.
     */
    public synchronized void reconciliar() {
        boolean mudou = false;
        String atual = sessao.usuarioAtualId();
        for (RegistroSimulacao r : new ArrayList<>(registros.values())) {
            Pedido p = repo.getPedidoById(r.pedidoId);
            if (p == null || p.getStatus() == StatusPedido.CANCELADO) {
                agendador.cancelar(r.pedidoId);
                continue;
            }
            long decorrido = relogio.agora() - r.inicioMs;
            StatusPedido porTempo = r.retirado ? StatusPedido.ENTREGUE
                    : r.manual ? p.getStatus() : Cronograma.statusPorTempo(decorrido);
            StatusPedido novo = Cronograma.maisAvancado(p.getStatus(), porTempo);
            if (novo != p.getStatus()) {
                repo.atualizarStatusPedido(p.getId(), novo);
            }
            if (novo != r.statusSalvo) {
                r.statusSalvo = novo;
                mudou = true;
            }
            mudou |= registrarAvisos(r, p, novo, atual);

            if (novo == StatusPedido.ENTREGUE) {
                agendador.cancelar(r.pedidoId);
                emissor.cancelarDoPedido(r.pedidoId);
            } else {
                long atraso = r.manual ? -1 : Cronograma.atrasoAteProximaEtapa(porTempo, decorrido);
                if (atraso >= 0) {
                    agendador.agendar(r.pedidoId, atraso + SimulacaoConfig.FOLGA_AGENDAMENTO_MS);
                } else {
                    agendador.cancelar(r.pedidoId);
                }
            }
        }
        if (mudou) salvar();
    }

    /**
     * Registra na central o aviso de cada etapa alcançada e ainda não registrada. Só a etapa mais
     * avançada gera notificação do sistema, e só para o dono logado (sem avisos redundantes).
     */
    private boolean registrarAvisos(RegistroSimulacao r, Pedido p, StatusPedido novo, String atual) {
        int ate = Cronograma.indice(novo);
        boolean registrou = false;
        for (int i = 0; i <= ate; i++) {
            int bit = 1 << i;
            if ((r.notificadas & bit) != 0) continue;
            r.notificadas |= bit;
            central.adicionar(avisoDaEtapa(r, p, i));
            registrou = true;
        }
        // Notificação do sistema: só ao dono, e só a etapa mais recente ainda não emitida. Se a
        // cozinha mudou o pedido com outra conta aberta, a notificação sai quando o dono voltar.
        if (atual != null && atual.equals(r.usuarioId)) {
            AvisoPedido maisRecente = null;
            for (int i = 0; i <= ate; i++) {
                int bit = 1 << i;
                if ((r.emitidas & bit) != 0) continue;
                r.emitidas |= bit;
                registrou = true;
                if (Cronograma.ETAPAS[i] != StatusPedido.ENTREGUE) maisRecente = avisoDaEtapa(r, p, i);
            }
            if (maisRecente != null) emissor.emitir(maisRecente);
        }
        return registrou;
    }

    private AvisoPedido avisoDaEtapa(RegistroSimulacao r, Pedido p, int i) {
        StatusPedido etapa = Cronograma.ETAPAS[i];
        String numero = PedidoApresentacao.numero(p);
        return new AvisoPedido(r.pedidoId + "-" + i, r.pedidoId, r.usuarioId, etapa,
                TextosAviso.titulo(etapa), TextosAviso.mensagem(etapa, numero), relogio.agora(), false);
    }

    // ---------------------------------------------------------------- consultas

    public synchronized RegistroSimulacao registro(String pedidoId) {
        return registros.get(pedidoId);
    }

    /** Tempo decorrido do cronograma do pedido (0 quando não há simulação). */
    public synchronized long decorridoMs(String pedidoId) {
        RegistroSimulacao r = registros.get(pedidoId);
        if (r == null) return 0L;
        if (r.manual) {                                   // sem relógio: o ponto da etapa atual
            Pedido p = repo.getPedidoById(pedidoId);
            return p == null ? 0L : Cronograma.inicioDaEtapa(p.getStatus());
        }
        return Math.max(0, relogio.agora() - r.inicioMs);
    }

    /** O pedido pertence ao usuário e está em simulação. */
    public synchronized boolean pertenceA(String pedidoId, String usuarioId) {
        RegistroSimulacao r = registros.get(pedidoId);
        return r != null && r.usuarioId.equals(usuarioId);
    }

    // ---------------------------------------------------------------- cozinha (lojista)

    /** Ações do lojista sobre um pedido da sua loja. */
    public enum AcaoCozinha { RECEBER, INICIAR_PREPARO, MARCAR_PRONTO, CONFIRMAR_RETIRADA }

    /**
     * Executa uma ação da cozinha. Só vale para pedidos da loja informada e para o próximo passo
     * do fluxo. O pedido passa a ser controlado pela cozinha (o cronograma automático não o
     * sobrescreve) e o status muda na mesma fonte usada por Minha Jornada e pelas notificações.
     */
    public synchronized boolean executarAcao(String pedidoId, String lojaId, AcaoCozinha acao) {
        Pedido p = repo.getPedidoById(pedidoId);
        if (p == null || lojaId == null || !lojaId.equals(p.getIdLoja())) return false;
        StatusPedido s = p.getStatus();
        RegistroSimulacao r = registros.get(pedidoId);
        StatusPedido alvo;
        switch (acao) {
            case RECEBER:
                if (s != StatusPedido.PENDENTE) return false;
                if (r != null) {
                    if (r.recebido) return false;
                    r.recebido = true;
                    r.manual = true;
                    salvar();
                }
                return true;
            case INICIAR_PREPARO:
                if (s != StatusPedido.PENDENTE) return false;
                alvo = StatusPedido.SEPARACAO;
                break;
            case MARCAR_PRONTO:
                if (s != StatusPedido.SEPARACAO) return false;
                alvo = StatusPedido.ENVIADO;
                break;
            case CONFIRMAR_RETIRADA:
                if (s != StatusPedido.ENVIADO) return false;
                alvo = StatusPedido.ENTREGUE;
                break;
            default:
                return false;
        }
        if (r != null) {
            r.manual = true;
            r.recebido = true;
            if (alvo == StatusPedido.ENTREGUE) r.retirado = true;
        }
        repo.atualizarStatusPedido(pedidoId, alvo);
        if (r != null) {
            salvar();
            reconciliar();
        }
        return true;
    }

    /** Ajusta a previsão (minutos) de um pedido ainda não pronto da loja. */
    public synchronized boolean ajustarPrevisao(String pedidoId, String lojaId, int minutos) {
        Pedido p = repo.getPedidoById(pedidoId);
        RegistroSimulacao r = registros.get(pedidoId);
        if (p == null || r == null || lojaId == null || !lojaId.equals(p.getIdLoja())) return false;
        if (p.getStatus() != StatusPedido.PENDENTE && p.getStatus() != StatusPedido.SEPARACAO) return false;
        r.previsaoMin = Math.max(1, Math.min(120, minutos));
        r.manual = true;
        salvar();
        reconciliar();
        return true;
    }

    /** Previsão exibida: a ajustada pela cozinha, quando houver; senão a demonstrativa. */
    public synchronized Cronograma.Previsao previsao(String pedidoId) {
        Pedido p = repo.getPedidoById(pedidoId);
        StatusPedido status = p == null ? StatusPedido.PENDENTE : p.getStatus();
        RegistroSimulacao r = registros.get(pedidoId);
        if (r != null && r.previsaoMin > 0
                && (status == StatusPedido.PENDENTE || status == StatusPedido.SEPARACAO)) {
            return new Cronograma.Previsao(Cronograma.TipoPrevisao.MINUTOS, r.previsaoMin);
        }
        return Cronograma.previsao(status, decorridoMs(pedidoId));
    }

    /** Instante de criação do pedido simulado, ou -1 quando ele não está em simulação. */
    public synchronized long criadoEmMs(String pedidoId) {
        RegistroSimulacao r = registros.get(pedidoId);
        return r == null ? -1L : r.criadoMs;
    }

    public synchronized boolean recebido(String pedidoId) {
        RegistroSimulacao r = registros.get(pedidoId);
        return r != null && r.recebido;
    }

    public synchronized boolean controladoPelaCozinha(String pedidoId) {
        RegistroSimulacao r = registros.get(pedidoId);
        return r != null && r.manual;
    }

    /** Com a cozinha manual ligada, pedidos novos esperam o lojista (sem avanço automático). */
    public synchronized boolean cozinhaManual() {
        return "1".equals(armazenamento.ler(CHAVE_COZINHA));
    }

    public synchronized void definirCozinhaManual(boolean ativo) {
        armazenamento.gravar(CHAVE_COZINHA, ativo ? "1" : "0");
    }

    // ---------------------------------------------------------------- modo dia do evento

    public synchronized boolean modoDiaDoEvento() {
        return "1".equals(armazenamento.ler(CHAVE_MODO_EVENTO));
    }

    public synchronized void definirModoDiaDoEvento(boolean ativo) {
        armazenamento.gravar(CHAVE_MODO_EVENTO, ativo ? "1" : "0");
    }

    private void salvar() {
        StringBuilder sb = new StringBuilder();
        for (RegistroSimulacao r : registros.values()) {
            if (sb.length() > 0) sb.append(SEP);
            sb.append(r.codificar());
        }
        armazenamento.gravar(CHAVE_REGISTROS, sb.toString());
    }
}
