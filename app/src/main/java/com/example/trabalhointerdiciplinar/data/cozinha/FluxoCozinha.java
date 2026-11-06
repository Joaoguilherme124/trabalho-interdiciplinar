package com.example.trabalhointerdiciplinar.data.cozinha;

import com.example.trabalhointerdiciplinar.data.simulacao.PedidoSimulador.AcaoCozinha;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

/** Qual é a próxima ação da cozinha para um pedido (regra pura, sem Android). */
public final class FluxoCozinha {
    private FluxoCozinha() {
    }

    /**
     * @param podeReceber pedido simulado ainda não recebido pela cozinha
     * @return a próxima ação, ou null quando o pedido já terminou
     */
    public static AcaoCozinha proxima(StatusPedido status, boolean podeReceber) {
        if (status == null) return null;
        switch (status) {
            case PENDENTE:
                return podeReceber ? AcaoCozinha.RECEBER : AcaoCozinha.INICIAR_PREPARO;
            case SEPARACAO:
                return AcaoCozinha.MARCAR_PRONTO;
            case ENVIADO:
                return AcaoCozinha.CONFIRMAR_RETIRADA;
            default:
                return null;
        }
    }
}
