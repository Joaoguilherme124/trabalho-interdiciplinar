package com.example.trabalhointerdiciplinar.util;

import com.example.trabalhointerdiciplinar.model.FormaPagamento;
import com.example.trabalhointerdiciplinar.model.ItemPedido;
import com.example.trabalhointerdiciplinar.model.Pedido;
import com.example.trabalhointerdiciplinar.model.StatusPedido;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Regras de apresentação de pedidos. Só traduz/formata para exibição: os valores internos
 * ({@link StatusPedido}) e a lógica de negócio não mudam.
 */
public final class PedidoApresentacao {

    private PedidoApresentacao() {
    }

    public static String rotuloStatus(StatusPedido status) {
        if (status == null) return "—";
        switch (status) {
            case PENDENTE:
                return "Registrado";
            case SEPARACAO:
                return "Em preparação";
            case ENVIADO:
                return "Pronto para retirar";
            case ENTREGUE:
                return "Retirado";
            case CANCELADO:
                return "Cancelado";
            default:
                return status.name();
        }
    }

    public static String rotuloPagamento(FormaPagamento forma) {
        if (forma == null) return "—";
        switch (forma) {
            case PIX:
                return "Pix";
            case CARTAO:
                return "Cartão";
            case BOLETO:
                return "Boleto";
            default:
                return forma.name();
        }
    }

    /** Número exibido ao usuário, ex.: "#PED-3B3801". */
    public static String numero(Pedido pedido) {
        return "#" + pedido.getId().toUpperCase(Locale.ROOT);
    }

    /** QR de demonstração: pedidos ativos (inclusive pronto para retirar); retirado não reutiliza. */
    public static boolean exibeQrDemo(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.SEPARACAO
                || status == StatusPedido.ENVIADO;
    }

    /**
     * Conteúdo do QR Code de demonstração: único por pedido e marcado explicitamente como
     * demonstração. Não é comprovante de pagamento nem código de retirada válido.
     */
    public static String conteudoQrDemo(Pedido pedido) {
        return "PAELLA-DEMO|PEDIDO=" + pedido.getId() + "|SEM-VALOR-DE-RETIRADA";
    }

    /** Pedido ainda em andamento (aparece como status contextual na Início). */
    public static boolean estaAtivo(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.SEPARACAO
                || status == StatusPedido.ENVIADO;
    }

    /**
     * Pedido ativo mais recente, ou null. O critério é a data do pedido ("dd/MM/yyyy"); em
     * empate vale a ordem da lista, que o repositório entrega do mais recente ao mais antigo.
     * Datas ilegíveis contam como as mais antigas, então nunca vencem um pedido datado.
     */
    public static Pedido primeiroAtivo(List<Pedido> pedidos) {
        if (pedidos == null) return null;
        Pedido melhor = null;
        long melhorData = Long.MIN_VALUE;
        for (Pedido p : pedidos) {
            if (!estaAtivo(p.getStatus())) continue;
            long data = dataEmMillis(p.getData());
            if (melhor == null || data > melhorData) {
                melhor = p;
                melhorData = data;
            }
        }
        return melhor;
    }

    /** Instante (início do dia) da data do pedido; datas ilegíveis contam como as mais antigas. */
    public static long instanteDaData(Pedido pedido) {
        return dataEmMillis(pedido.getData());
    }

    private static long dataEmMillis(String data) {
        if (data == null) return Long.MIN_VALUE + 1;
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
            formato.setLenient(false);
            return formato.parse(data.trim()).getTime();
        } catch (ParseException e) {
            return Long.MIN_VALUE + 1;
        }
    }

    /** Primeiro pedido para o qual o QR Code de demonstração é exibido, ou null. */
    public static Pedido primeiroComQr(List<Pedido> pedidos) {
        if (pedidos == null) return null;
        for (Pedido p : pedidos) {
            if (exibeQrDemo(p.getStatus())) return p;
        }
        return null;
    }

    public static int totalDeUnidades(Pedido pedido) {
        int total = 0;
        for (ItemPedido item : pedido.getListaItens()) total += item.getQuantidade();
        return total;
    }

    /** Resumo curto dos itens, ex.: "2× Paella Valenciana, 2× Água com Gás". */
    public static String resumoItens(Pedido pedido) {
        List<ItemPedido> itens = pedido.getListaItens();
        StringBuilder sb = new StringBuilder();
        for (ItemPedido item : itens) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(item.getQuantidade()).append("× ").append(item.getTitulo());
        }
        return sb.toString();
    }
}
