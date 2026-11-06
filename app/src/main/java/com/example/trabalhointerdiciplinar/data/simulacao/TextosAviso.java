package com.example.trabalhointerdiciplinar.data.simulacao;

import com.example.trabalhointerdiciplinar.model.StatusPedido;

/** Títulos e mensagens dos avisos de cada etapa (marcados como demonstração). */
public final class TextosAviso {
    private TextosAviso() {
    }

    public static String titulo(StatusPedido etapa) {
        switch (etapa) {
            case SEPARACAO:
                return "Sua paella está sendo preparada!";
            case ENVIADO:
                return "Sua paella está pronta!";
            case ENTREGUE:
                return "Pedido finalizado!";
            default:
                return "Pedido registrado!";
        }
    }

    /** @param numero identificador exibido, ex.: "#PED-3B3801" */
    public static String mensagem(StatusPedido etapa, String numero) {
        switch (etapa) {
            case SEPARACAO:
                return "O pedido " + numero + " entrou em preparação. Acompanhe sua jornada no aplicativo.";
            case ENVIADO:
                return "Pedido " + numero + " disponível para retirada demonstrativa. "
                        + "Abra seu QR Code no aplicativo.";
            case ENTREGUE:
                return "Obrigado por participar da Paella Beneficente ABADEUS!";
            default:
                return "Seu pedido " + numero + " foi recebido. Previsão demonstrativa: aproximadamente "
                        + SimulacaoConfig.PREVISAO_REGISTRADO_MIN + " minutos.";
        }
    }
}
