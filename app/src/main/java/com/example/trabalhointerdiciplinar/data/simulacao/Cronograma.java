package com.example.trabalhointerdiciplinar.data.simulacao;

import com.example.trabalhointerdiciplinar.model.StatusPedido;

/**
 * Regras puras do andamento de um pedido: qual etapa o tempo decorrido indica, previsão
 * demonstrativa, progresso e quais transições são válidas. Sem Android, testável na JVM.
 *
 * Etapas (reaproveitando {@link StatusPedido}): PENDENTE = registrado, SEPARACAO = em
 * preparação, ENVIADO = pronto para retirar, ENTREGUE = retirado.
 */
public final class Cronograma {
    private Cronograma() {
    }

    public static final StatusPedido[] ETAPAS = {
            StatusPedido.PENDENTE, StatusPedido.SEPARACAO, StatusPedido.ENVIADO, StatusPedido.ENTREGUE
    };

    public enum TipoPrevisao { MINUTOS, FINALIZANDO, PRONTO, FINALIZADO }

    public static final class Previsao {
        public final TipoPrevisao tipo;
        public final int minutos;

        Previsao(TipoPrevisao tipo, int minutos) {
            this.tipo = tipo;
            this.minutos = Math.max(0, minutos);
        }
    }

    /** Posição da etapa (0..3) ou -1 quando fora do fluxo (cancelado). */
    public static int indice(StatusPedido status) {
        for (int i = 0; i < ETAPAS.length; i++) {
            if (ETAPAS[i] == status) return i;
        }
        return -1;
    }

    /** Etapa indicada só pelo tempo; a retirada nunca é automática. */
    public static StatusPedido statusPorTempo(long decorridoMs) {
        long t = Math.max(0, decorridoMs);
        if (t >= SimulacaoConfig.PRONTO_MS) return StatusPedido.ENVIADO;
        if (t >= SimulacaoConfig.INICIO_PREPARO_MS) return StatusPedido.SEPARACAO;
        return StatusPedido.PENDENTE;
    }

    /** Instante (desde o início do cronograma) em que a etapa começa. */
    public static long inicioDaEtapa(StatusPedido status) {
        switch (status) {
            case SEPARACAO:
                return SimulacaoConfig.INICIO_PREPARO_MS;
            case ENVIADO:
            case ENTREGUE:
                return SimulacaoConfig.PRONTO_MS;
            default:
                return 0L;
        }
    }

    /** Transição válida: sempre para frente dentro do fluxo (pular etapas é permitido). */
    public static boolean transicaoPermitida(StatusPedido de, StatusPedido para) {
        int a = indice(de);
        int b = indice(para);
        return a >= 0 && b > a;
    }

    /** O mais avançado dos dois; estados fora do fluxo (cancelado) vencem sempre. */
    public static StatusPedido maisAvancado(StatusPedido a, StatusPedido b) {
        if (indice(a) < 0) return a;
        if (indice(b) < 0) return b;
        return indice(b) > indice(a) ? b : a;
    }

    /**
     * Previsão demonstrativa para a etapa atual. Nunca negativa. Se o prazo da preparação
     * acabar antes de o estado avançar, mostra "finalizando", e não "pronto".
     */
    public static Previsao previsao(StatusPedido status, long decorridoMs) {
        long t = Math.max(0, decorridoMs);
        switch (status) {
            case PENDENTE:
                return new Previsao(TipoPrevisao.MINUTOS, SimulacaoConfig.PREVISAO_REGISTRADO_MIN);
            case SEPARACAO:
                return t >= SimulacaoConfig.ETAPA_FINAL_MS
                        ? new Previsao(TipoPrevisao.FINALIZANDO, 0)
                        : new Previsao(TipoPrevisao.MINUTOS, SimulacaoConfig.PREVISAO_PREPARO_MIN);
            case ENVIADO:
                return new Previsao(TipoPrevisao.PRONTO, 0);
            case ENTREGUE:
                return new Previsao(TipoPrevisao.FINALIZADO, 0);
            default:
                return new Previsao(TipoPrevisao.FINALIZADO, 0);
        }
    }

    /**
     * Progresso 0..1 da barra de preparo. Acompanha o tempo, mas nunca fica abaixo do início da
     * etapa atual (útil quando o estado foi adiantado) e só chega a 1 em "pronto"/"retirado".
     */
    public static float progresso(StatusPedido status, long decorridoMs) {
        if (status == StatusPedido.ENVIADO || status == StatusPedido.ENTREGUE) return 1f;
        long total = SimulacaoConfig.PRONTO_MS;
        float porTempo = Math.min(Math.max(0, decorridoMs), total - 1) / (float) total;
        float minimo = inicioDaEtapa(status) / (float) total;
        return Math.min(0.99f, Math.max(porTempo, minimo));
    }

    /**
     * Quanto falta até a próxima virada automática, ou -1 quando não há mais nenhuma
     * (pronto ou retirado).
     */
    public static long atrasoAteProximaEtapa(StatusPedido statusPorTempo, long decorridoMs) {
        long t = Math.max(0, decorridoMs);
        if (statusPorTempo == StatusPedido.PENDENTE) {
            return SimulacaoConfig.INICIO_PREPARO_MS - t;
        }
        if (statusPorTempo == StatusPedido.SEPARACAO) {
            return SimulacaoConfig.PRONTO_MS - t;
        }
        return -1L;
    }
}
