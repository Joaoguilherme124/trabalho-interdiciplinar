package com.example.trabalhointerdiciplinar.data.simulacao;

/**
 * Configuração ÚNICA da simulação demonstrativa de preparo. Os tempos abaixo são uma simulação
 * acelerada para apresentação: não representam o tempo real de uma cozinha.
 */
public final class SimulacaoConfig {
    private SimulacaoConfig() {
    }

    /** Pedido registrado → em preparação. */
    public static final long INICIO_PREPARO_MS = 20_000L;
    /** A partir daqui o preparo está na etapa final ("Finalizando preparo"). */
    public static final long ETAPA_FINAL_MS = 80_000L;
    /** Em preparação → pronto para retirar. */
    public static final long PRONTO_MS = 120_000L;

    /** Previsão exibida (em minutos) enquanto o pedido está registrado. */
    public static final int PREVISAO_REGISTRADO_MIN = 25;
    /** Previsão exibida (em minutos) durante a preparação. */
    public static final int PREVISAO_PREPARO_MIN = 15;

    /** Folga somada ao agendamento para o trabalho disparar já depois da virada de etapa. */
    public static final long FOLGA_AGENDAMENTO_MS = 500L;
}
